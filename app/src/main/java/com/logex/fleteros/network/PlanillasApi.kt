package com.logex.fleteros.network

import com.logex.fleteros.data.CobranzaPorClienteResponse
import com.logex.fleteros.data.ListarPlanillasResponse
import com.logex.fleteros.data.ResumenCobranzaResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface PlanillasApi {

    @FormUrlEncoded
    @POST("listar_planillas.php")
    suspend fun listar(
        @Header("Authorization") token: String,
        @Field("desde") desde: String
    ): Response<ListarPlanillasResponse>

    @FormUrlEncoded
    @POST("resumen_cobranza.php")
    suspend fun resumen(
        @Header("Authorization") token: String,
        @Field("id_planillac") idPlanillac: Int,
        @Field("total_planilla") totalPlanilla: Double
    ): Response<ResumenCobranzaResponse>

    @FormUrlEncoded
    @POST("cobranza_por_cliente.php")
    suspend fun cobranzaPorCliente(
        @Header("Authorization") token: String,
        @Field("numplanilla") numplanilla: Int
    ): Response<CobranzaPorClienteResponse>
}