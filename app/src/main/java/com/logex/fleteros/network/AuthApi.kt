package com.logex.fleteros.network

import com.logex.fleteros.data.LoginResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    @FormUrlEncoded
    @POST("login.php")
    suspend fun login(
        @Field("usuario") usuario: String,
        @Field("clave") clave: String
    ): Response<LoginResponse>
}