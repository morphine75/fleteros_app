package com.logex.fleteros.network

import com.logex.fleteros.data.GuardarTicketResponse
import com.logex.fleteros.data.ListarTicketsResponse
import com.logex.fleteros.data.SimpleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TicketsGastosApi {

    @GET("listar_tickets_gastos.php")
    suspend fun listar(
        @Header("Authorization") token: String
    ): Response<ListarTicketsResponse>

    @Multipart
    @POST("guardar_ticket_gasto.php")
    suspend fun guardar(
        @Header("Authorization") token: String,
        @Part("fecha") fecha: RequestBody,
        @Part("importe") importe: RequestBody,
        @Part("observaciones") observaciones: RequestBody,
        @Part imagenes: List<MultipartBody.Part>
    ): Response<GuardarTicketResponse>

    @FormUrlEncoded
    @POST("eliminar_ticket_gasto.php")
    suspend fun eliminar(
        @Header("Authorization") token: String,
        @Field("id") id: Int
    ): Response<SimpleResponse>
}