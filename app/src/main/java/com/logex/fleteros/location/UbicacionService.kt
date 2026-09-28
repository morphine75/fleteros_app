package com.logex.fleteros.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.logex.fleteros.data.TokenManager
import com.logex.fleteros.data.UbicacionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Servicio de seguimiento GPS.
 *
 * GPS -> filtro de precision -> se movio lo suficiente? -> cola local (Room)
 *                                                                  |
 *                                                    intenta mandar ya mismo
 *                                                                  |
 *                                                  si no hay señal, queda
 *                                                  pendiente y se reintenta
 *                                                  cada REINTENTO_ENVIO_MS
 *
 * Guardar primero en la cola y recien despues intentar mandar (en vez de
 * mandar directo) es lo que evita perder puntos cuando el camion entra en
 * una zona sin señal: el punto no se pierde, solo espera su turno.
 *
 * A proposito NO tiene heartbeat: solo reporta cuando hay movimiento real,
 * para no saturar con puntos mientras el camion esta parado entregando.
 */
class UbicacionService : Service() {

    companion object {
        private const val TAG = "UbicacionService"
        private const val CHANNEL_ID = "ubicacion_channel"
        private const val NOTIFICATION_ID = 1001

        private const val INTERVALO_GPS_MS = 30_000L
        private const val INTERVALO_GPS_MIN_MS = 10_000L
        private const val DISTANCIA_MINIMA_METROS = 30f

        // Una posicion con error de mas de esto se descarta directamente
        // (evita los "saltos" raros en el mapa por GPS de mala calidad).
        private const val ACCURACY_MAXIMA_METROS = 50f

        private const val HORA_INICIO_JORNADA = 6
        private const val HORA_FIN_JORNADA = 20

        private const val REINTENTO_ENVIO_MS = 60_000L

        private const val MOSTRAR_DEBUG = false

        fun iniciar(context: Context) {
            val intent = Intent(context, UbicacionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun detener(context: Context) {
            context.stopService(Intent(context, UbicacionService::class.java))
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: UbicacionRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Ultimo punto que efectivamente se guardo/reporto (no el ultimo que
    // llego del GPS, que puede ser descartado por precision o distancia).
    private var ultimaLatitudReportada: Double? = null
    private var ultimaLongitudReportada: Double? = null
    private var procesandoUbicacion = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val ubicacion = result.lastLocation ?: run {
                log("GPS no devolvio ninguna ubicacion")
                return
            }
            procesarUbicacion(ubicacion)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        repository = UbicacionRepository(applicationContext, TokenManager(applicationContext))
        crearCanalNotificacion()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, construirNotificacion())
        log("Servicio iniciado")

        serviceScope.launch {
            if (!verificarRuta()) {
                log("No hay ruta asignada hoy")
                detenerServicio()
                return@launch
            }
            if (!dentroDeHorario()) {
                log("Fuera de horario")
                detenerServicio()
                return@launch
            }
            iniciarActualizacionesUbicacion()
            iniciarProcesoReintentos()
        }

        return START_STICKY
    }

    private suspend fun verificarRuta(): Boolean {
        return try {
            repository.tieneRutaHoy(fechaHoy())
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando ruta", e)
            false
        }
    }

    private fun iniciarActualizacionesUbicacion() {
        val tienePermiso = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermiso) {
            log("Falta el permiso de ubicacion")
            detenerServicio()
            return
        }

        val solicitud = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVALO_GPS_MS)
            .setMinUpdateIntervalMillis(INTERVALO_GPS_MIN_MS)
            .setMinUpdateDistanceMeters(10f)
            .build()

        fusedLocationClient.requestLocationUpdates(solicitud, locationCallback, Looper.getMainLooper())
        log("Actualizaciones GPS iniciadas")
    }

    private fun procesarUbicacion(ubicacion: Location) {
        if (procesandoUbicacion) {
            log("Ubicacion anterior todavia procesandose")
            return
        }
        procesandoUbicacion = true

        serviceScope.launch {
            try {
                if (!dentroDeHorario()) {
                    log("Fin de jornada, se detiene el servicio")
                    detenerServicio()
                    return@launch
                }

                if (!ubicacionValida(ubicacion)) {
                    log("Ubicacion descartada por baja precision: ${ubicacion.accuracy}m")
                    return@launch
                }

                val distancia = calcularDistanciaDesdeUltimoReporte(ubicacion)
                val debeReportar = ultimaLatitudReportada == null || distancia >= DISTANCIA_MINIMA_METROS

                log("lat=${ubicacion.latitude} lon=${ubicacion.longitude} accuracy=${ubicacion.accuracy} distancia=$distancia debeReportar=$debeReportar")

                if (!debeReportar) {
                    return@launch
                }

                val punto = crearPuntoUbicacion(ubicacion)

                // Guarda PRIMERO en la cola local. Aunque no haya señal en
                // este momento, el punto ya quedo a salvo.
                repository.guardarUbicacionPendiente(punto)

                ultimaLatitudReportada = ubicacion.latitude
                ultimaLongitudReportada = ubicacion.longitude

                // Intenta vaciar la cola ya mismo (si hay señal, este punto
                // y cualquier otro pendiente se mandan al toque).
                enviarUbicacionesPendientes()
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando ubicacion", e)
            } finally {
                procesandoUbicacion = false
            }
        }
    }

    private fun ubicacionValida(ubicacion: Location): Boolean {
        if (ubicacion.latitude == 0.0 && ubicacion.longitude == 0.0) return false
        if (ubicacion.accuracy <= 0) return false
        if (ubicacion.accuracy > ACCURACY_MAXIMA_METROS) return false
        return true
    }

    private fun calcularDistanciaDesdeUltimoReporte(ubicacion: Location): Float {
        val lat = ultimaLatitudReportada ?: return Float.MAX_VALUE
        val lon = ultimaLongitudReportada ?: return Float.MAX_VALUE
        val resultado = FloatArray(1)
        Location.distanceBetween(lat, lon, ubicacion.latitude, ubicacion.longitude, resultado)
        return resultado[0]
    }

    private fun crearPuntoUbicacion(ubicacion: Location): UbicacionPendiente {
        val ahora = System.currentTimeMillis()
        val fechaHoraTexto = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ahora))
        return UbicacionPendiente(
            latitud = ubicacion.latitude,
            longitud = ubicacion.longitude,
            precision = ubicacion.accuracy.toDouble(),
            velocidad = if (ubicacion.hasSpeed()) (ubicacion.speed * 3.6).toDouble() else 0.0,
            rumbo = if (ubicacion.hasBearing()) ubicacion.bearing.toDouble() else null,
            altitud = if (ubicacion.hasAltitude()) ubicacion.altitude else null,
            timestamp = ahora,
            fechaHoraTexto = fechaHoraTexto
        )
    }

    /**
     * Intenta vaciar la cola de puntos pendientes, en orden cronologico.
     * Si uno falla, corta ahi (no sigue con los siguientes) para no
     * desordenar el recorrido -- en el proximo intento se retoma desde
     * el mismo punto.
     */
    private suspend fun enviarUbicacionesPendientes() {
        try {
            val pendientes = repository.obtenerUbicacionesPendientes()
            if (pendientes.isEmpty()) return

            log("${pendientes.size} ubicaciones pendientes en la cola")

            for (punto in pendientes) {
                val enviado = try {
                    repository.reportarPendiente(punto)
                } catch (e: Exception) {
                    Log.e(TAG, "Error enviando punto ${punto.id}", e)
                    false
                }

                if (enviado) {
                    repository.eliminarUbicacionPendiente(punto)
                    log("Punto ${punto.id} enviado y quitado de la cola")
                } else {
                    log("No se pudo enviar el punto ${punto.id}, se reintenta despues")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando la cola offline", e)
        }
    }

    private fun iniciarProcesoReintentos() {
        serviceScope.launch {
            while (isActive) {
                try {
                    enviarUbicacionesPendientes()
                } catch (e: Exception) {
                    Log.e(TAG, "Error en el reintento periodico", e)
                }
                delay(REINTENTO_ENVIO_MS)
            }
        }
    }

    private fun dentroDeHorario(): Boolean {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hora in HORA_INICIO_JORNADA until HORA_FIN_JORNADA
    }

    private fun fechaHoy(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun detenerServicio() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error deteniendo GPS", e)
        }
        stopSelf()
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CHANNEL_ID, "Seguimiento de ubicacion", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(canal)
        }
    }

    private fun construirNotificacion(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fleteros")
            .setContentText("Registrando tu ubicacion en ruta")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun log(mensaje: String) {
        if (MOSTRAR_DEBUG) Log.d(TAG, mensaje)
    }

    override fun onDestroy() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando actualizaciones GPS", e)
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}