package com.logex.fleteros.data

import android.content.Context
import android.net.Uri
import com.logex.fleteros.network.ApiClient
import com.logex.fleteros.util.comprimirImagenComoParte
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class SeguimientoRepository(private val tokenManager: TokenManager) {

    private fun tokenHeader(): String = "Bearer ${tokenManager.token()}"

    suspend fun obtenerDetalle(numplanilla: Int, idcliente: Int, idPlanillac: Int): TicketsResult<DetalleSeguimientoResponse> {
        return try {
            val respuesta = ApiClient.seguimientoApi.detalle(tokenHeader(), numplanilla, idcliente, idPlanillac)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo cargar el detalle")
            }
        } catch (e: java.net.SocketTimeoutException) {
            TicketsResult.Error("La consulta tardo demasiado (señal lenta). Probá de nuevo")
        } catch (e: java.io.IOException) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        } catch (e: Exception) {
            TicketsResult.Error("No se pudo procesar la respuesta: ${e.message}")
        }
    }

    suspend fun eliminarImagen(idDetalleImg: Int): TicketsResult<Unit> {
        return try {
            val respuesta = ApiClient.seguimientoApi.eliminarImagen(tokenHeader(), idDetalleImg)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(Unit)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo eliminar la imagen")
            }
        } catch (e: Exception) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        }
    }

    /**
     * Para clientes que ya estan en una liquidacion cerrada: SOLO permite
     * sumar fotos, no toca el registro de pago/rechazo para nada.
     */
    suspend fun agregarImagenes(
        context: Context,
        idPlanillac: Int,
        idCliente: Int,
        nombreCliente: String,
        imagenes: List<Uri>
    ): TicketsResult<Int> {
        return try {
            val partesImagenes = imagenes.mapIndexedNotNull { indice, uri ->
                comprimirImagenComoParte(context, uri, "imagen[]", indice)
            }
            val respuesta = ApiClient.seguimientoApi.agregarImagen(
                token = tokenHeader(),
                idPlanillac = idPlanillac.toString().toRequestBody(),
                idCliente = idCliente.toString().toRequestBody(),
                nomCliente = nombreCliente.toRequestBody(),
                imagenes = partesImagenes
            )
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body.imagenes_guardadas ?: 0)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudieron guardar las fotos")
            }
        } catch (e: java.net.SocketTimeoutException) {
            TicketsResult.Error("La subida tardo demasiado (señal lenta). Probá de nuevo")
        } catch (e: java.io.IOException) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        } catch (e: Exception) {
            TicketsResult.Error("No se pudo procesar: ${e.message}")
        }
    }

    suspend fun guardar(
        context: Context,
        campos: Map<String, String>,
        imagenes: List<Uri>
    ): TicketsResult<Int> {
        return try {
            val partesTexto: Map<String, RequestBody> = campos.mapValues { (_, valor) ->
                valor.toRequestBody()
            }
            val partesImagenes = imagenes.mapIndexedNotNull { indice, uri ->
                comprimirImagenComoParte(context, uri, "imagen[]", indice)
            }

            val respuesta = ApiClient.seguimientoApi.guardar(tokenHeader(), partesTexto, partesImagenes)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body.id_detalle_cli ?: 0)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo guardar")
            }
        } catch (e: java.net.SocketTimeoutException) {
            TicketsResult.Error("La subida tardo demasiado (señal lenta). Probá de nuevo")
        } catch (e: java.io.IOException) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        } catch (e: Exception) {
            TicketsResult.Error("No se pudo procesar: ${e.message}")
        }
    }
}