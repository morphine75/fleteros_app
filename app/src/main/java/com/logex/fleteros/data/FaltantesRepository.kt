package com.logex.fleteros.data

import com.logex.fleteros.network.ApiClient

class FaltantesRepository(private val tokenManager: TokenManager) {

    private fun tokenHeader(): String = "Bearer ${tokenManager.token()}"

    suspend fun obtenerDetalle(numplanilla: Int, idcliente: Int): TicketsResult<List<FacturaConArticulos>> {
        return try {
            val respuesta = ApiClient.faltantesApi.detalle(tokenHeader(), numplanilla, idcliente)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body.facturas ?: emptyList())
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

    suspend fun guardarFaltante(
        idPlanillac: Int,
        idCliente: Int,
        nomCliente: String,
        fletero: String,
        nrodoc: Int,
        letra: String,
        codart: Int,
        descripcion: String,
        bultosFalta: String,
        restoFalta: String
    ): TicketsResult<Unit> {
        return try {
            val respuesta = ApiClient.faltantesApi.guardar(
                tokenHeader(), idPlanillac, idCliente, nomCliente, fletero,
                nrodoc, letra, codart, descripcion, bultosFalta, restoFalta
            )
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(Unit)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo guardar el faltante")
            }
        } catch (e: Exception) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        }
    }
}