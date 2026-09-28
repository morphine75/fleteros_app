package com.logex.fleteros.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Sin HTTPS: requiere habilitar cleartext para este dominio en
    // network_security_config.xml (ver carpeta res_xml)
    private const val BASE_URL = "http://logex.dyndns.org:83/Logex/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val ticketsGastosApi: TicketsGastosApi = retrofit.create(TicketsGastosApi::class.java)
    val ticketsDepositoApi: TicketsDepositoApi = retrofit.create(TicketsDepositoApi::class.java)
    val planillasApi: PlanillasApi = retrofit.create(PlanillasApi::class.java)
    val seguimientoApi: SeguimientoApi = retrofit.create(SeguimientoApi::class.java)
    val faltantesApi: FaltantesApi = retrofit.create(FaltantesApi::class.java)
    val ubicacionApi: UbicacionApi = retrofit.create(UbicacionApi::class.java)

    /**
     * Arma la URL completa de una imagen a partir de la ruta relativa
     * que devuelve el servidor (ej: "imagenes/12_1699999_0_foto.jpg"),
     * ya que las imagenes se guardan en la misma carpeta que la API.
     */
    fun urlImagen(rutaRelativa: String): String = BASE_URL + rutaRelativa

    /**
     * Las fotos de seguimiento de planilla viven en una carpeta compartida
     * con el sistema de escritorio viejo (no en imagenes/ local), asi que
     * se sirven a traves de imagen_planilla.php en vez de una ruta directa.
     */
    fun urlImagenPlanilla(nombreArchivo: String): String =
        BASE_URL + "imagen_planilla.php?nombre=" + java.net.URLEncoder.encode(nombreArchivo, "UTF-8")
}