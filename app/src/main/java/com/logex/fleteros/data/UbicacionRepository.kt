package com.logex.fleteros.data

import android.content.Context
import com.logex.fleteros.location.UbicacionDatabase
import com.logex.fleteros.location.UbicacionPendiente
import com.logex.fleteros.network.ApiClient

class UbicacionRepository(context: Context, private val tokenManager: TokenManager) {

    private val dao = UbicacionDatabase.obtener(context).ubicacionPendienteDao()

    private fun tokenHeader(): String = "Bearer ${tokenManager.token()}"

    /**
     * Manda un punto directo al servidor (sin pasar por la cola). Devuelve
     * false ante cualquier problema -- quien llama decide si lo guarda en
     * la cola offline para reintentar despues.
     */
    suspend fun reportar(latitud: Double, longitud: Double, fechaHoraTexto: String): Boolean {
        return try {
            val token = tokenManager.token() ?: return false
            val respuesta = ApiClient.ubicacionApi.reportar("Bearer $token", latitud, longitud, fechaHoraTexto)
            respuesta.isSuccessful && respuesta.body()?.ok == true
        } catch (e: Exception) {
            android.util.Log.w("UbicacionRepository", "reportar fallo: ${e.message}")
            false
        }
    }

    suspend fun tieneRutaHoy(fecha: String): Boolean {
        return try {
            val token = tokenManager.token() ?: return false
            val respuesta = ApiClient.ubicacionApi.tieneRutaHoy("Bearer $token", fecha)
            if (!respuesta.isSuccessful) {
                android.util.Log.w("UbicacionRepository", "tieneRutaHoy: HTTP ${respuesta.code()}")
            }
            respuesta.isSuccessful && respuesta.body()?.tiene_ruta == true
        } catch (e: Exception) {
            android.util.Log.w("UbicacionRepository", "tieneRutaHoy fallo: ${e.message}")
            false
        }
    }

    // --- Cola offline (Room) ---

    suspend fun guardarUbicacionPendiente(punto: UbicacionPendiente) {
        dao.insertar(punto)
        // Limite de seguridad: si por lo que sea se acumulan mas de 500
        // puntos sin poder mandar (varios dias sin señal), se descartan
        // los mas viejos para no llenar el almacenamiento del celular.
        if (dao.contar() > 500) {
            dao.eliminarMasViejas(dao.contar() - 500)
        }
    }

    suspend fun obtenerUbicacionesPendientes(): List<UbicacionPendiente> {
        return dao.obtenerTodas()
    }

    suspend fun eliminarUbicacionPendiente(punto: UbicacionPendiente) {
        dao.eliminar(punto)
    }

    suspend fun reportarPendiente(punto: UbicacionPendiente): Boolean {
        return reportar(punto.latitud, punto.longitud, punto.fechaHoraTexto)
    }
}