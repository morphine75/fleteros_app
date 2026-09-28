package com.logex.fleteros.network

import com.logex.fleteros.data.SimpleResponse
import com.logex.fleteros.data.TieneRutaResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface UbicacionApi {

    @FormUrlEncoded
    @POST("reportar_ubicacion.php")
    suspend fun reportar(
        @Header("Authorization") token: String,
        @Field("latitud") latitud: Double,
        @Field("longitud") longitud: Double,
        @Field("fecha_hora") fechaHora: String
    ): Response<SimpleResponse>

    @FormUrlEncoded
    @POST("tiene_ruta_hoy.php")
    suspend fun tieneRutaHoy(
        @Header("Authorization") token: String,
        @Field("fecha") fecha: String
    ): Response<TieneRutaResponse>
}