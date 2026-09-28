package com.logex.fleteros.network

import com.logex.fleteros.data.DetalleSeguimientoResponse
import com.logex.fleteros.data.GuardarSeguimientoResponse
import com.logex.fleteros.data.SimpleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface SeguimientoApi {

    @FormUrlEncoded
    @POST("detalle_seguimiento.php")
    suspend fun detalle(
        @Header("Authorization") token: String,
        @Field("numplanilla") numplanilla: Int,
        @Field("idcliente") idcliente: Int,
        @Field("id_planillac") idPlanillac: Int
    ): Response<DetalleSeguimientoResponse>

    @Multipart
    @POST("guardar_seguimiento.php")
    suspend fun guardar(
        @Header("Authorization") token: String,
        @PartMap datos: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part imagenes: List<MultipartBody.Part>
    ): Response<GuardarSeguimientoResponse>

    @FormUrlEncoded
    @POST("eliminar_imagen_planilla.php")
    suspend fun eliminarImagen(
        @Header("Authorization") token: String,
        @Field("id_detalle_img") idDetalleImg: Int
    ): Response<SimpleResponse>

    @Multipart
    @POST("agregar_imagen_planilla.php")
    suspend fun agregarImagen(
        @Header("Authorization") token: String,
        @Part("id_planillac") idPlanillac: RequestBody,
        @Part("idcliente") idCliente: RequestBody,
        @Part("nomcli") nomCliente: RequestBody,
        @Part imagenes: List<MultipartBody.Part>
    ): Response<GuardarSeguimientoResponse>
}