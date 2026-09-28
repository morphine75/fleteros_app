package com.logex.fleteros.data

import android.content.Context
import android.net.Uri
import com.logex.fleteros.network.ApiClient
import com.logex.fleteros.util.comprimirImagenComoParte
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class TicketsResult<out T> {
    data class Exito<T>(val datos: T) : TicketsResult<T>()
    data class Error(val mensaje: String) : TicketsResult<Nothing>()
}

class TicketsGastosRepository(private val tokenManager: TokenManager) {

    private fun tokenHeader(): String = "Bearer ${tokenManager.token()}"

    suspend fun listar(): TicketsResult<List<TicketGasto>> {
        return try {
            val respuesta = ApiClient.ticketsGastosApi.listar(tokenHeader())
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body.tickets ?: emptyList())
            } else {
                TicketsResult.Error(body?.error ?: "No se pudieron cargar los tickets")
            }
        } catch (e: Exception) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        }
    }

    suspend fun guardar(
        context: Context,
        fecha: String,
        importe: String,
        observaciones: String,
        imagenes: List<Uri>
    ): TicketsResult<Int> {
        return try {
            val partes: List<MultipartBody.Part> = imagenes.mapIndexedNotNull { indice, uri ->
                comprimirImagenComoParte(context, uri, "imagen[]", indice)
            }

            val respuesta = ApiClient.ticketsGastosApi.guardar(
                token = tokenHeader(),
                fecha = fecha.toRequestBody(),
                importe = importe.toRequestBody(),
                observaciones = observaciones.toRequestBody(),
                imagenes = partes
            )
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body.id_ticket ?: 0)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo guardar el ticket")
            }
        } catch (e: java.net.SocketTimeoutException) {
            TicketsResult.Error("La subida tardo demasiado (señal lenta). Probá de nuevo, o con menos fotos")
        } catch (e: java.io.IOException) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        } catch (e: Exception) {
            TicketsResult.Error("No se pudo procesar la imagen: ${e.message}")
        }
    }

    suspend fun eliminar(idTicket: Int): TicketsResult<Unit> {
        return try {
            val respuesta = ApiClient.ticketsGastosApi.eliminar(tokenHeader(), idTicket)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(Unit)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo eliminar el ticket")
            }
        } catch (e: Exception) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        }
    }
}