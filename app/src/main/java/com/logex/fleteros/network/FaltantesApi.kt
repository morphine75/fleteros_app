package com.logex.fleteros.network

import com.logex.fleteros.data.DetalleFaltantesResponse
import com.logex.fleteros.data.SimpleResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface FaltantesApi {

    @FormUrlEncoded
    @POST("detalle_faltantes.php")
    suspend fun detalle(
        @Header("Authorization") token: String,
        @Field("numplanilla") numplanilla: Int,
        @Field("idcliente") idcliente: Int
    ): Response<DetalleFaltantesResponse>

    @FormUrlEncoded
    @POST("guardar_faltante.php")
    suspend fun guardar(
        @Header("Authorization") token: String,
        @Field("id_planillac") idPlanillac: Int,
        @Field("idcliente") idCliente: Int,
        @Field("nomcli") nomCliente: String,
        @Field("fletero") fletero: String,
        @Field("nrodoc") nrodoc: Int,
        @Field("letra") letra: String,
        @Field("codart") codart: Int,
        @Field("descripcion") descripcion: String,
        @Field("bultos_falta") bultosFalta: String,
        @Field("resto_falta") restoFalta: String
    ): Response<SimpleResponse>
}