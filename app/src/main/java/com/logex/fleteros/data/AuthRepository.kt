package com.logex.fleteros.data

import com.logex.fleteros.network.ApiClient
import java.io.IOException

sealed class LoginResult {
    data class Exito(val usuario: Usuario) : LoginResult()
    data class Error(val mensaje: String) : LoginResult()
}

class AuthRepository(private val tokenManager: TokenManager) {

    suspend fun login(usuario: String, clave: String): LoginResult {
        return try {
            val respuesta = ApiClient.authApi.login(usuario, clave)
            val body = respuesta.body()

            if (respuesta.isSuccessful && body?.ok == true && body.token != null && body.usuario != null) {
                tokenManager.guardarSesion(body.token, body.usuario)
                LoginResult.Exito(body.usuario)
            } else {
                LoginResult.Error(body?.error ?: "No se pudo iniciar sesion")
            }
        } catch (e: IOException) {
            LoginResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        } catch (e: Exception) {
            LoginResult.Error("Ocurrio un error inesperado")
        }
    }
}