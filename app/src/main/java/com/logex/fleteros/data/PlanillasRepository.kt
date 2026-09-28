package com.logex.fleteros.data

import com.logex.fleteros.network.ApiClient

class PlanillasRepository(private val tokenManager: TokenManager) {

    private fun tokenHeader(): String = "Bearer ${tokenManager.token()}"

    suspend fun listar(fecha: String): TicketsResult<List<PlanillaCarga>> {
        return try {
            val respuesta = ApiClient.planillasApi.listar(tokenHeader(), fecha)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body.planillas ?: emptyList())
            } else {
                TicketsResult.Error(body?.error ?: "No se pudieron cargar las planillas")
            }
        } catch (e: java.net.SocketTimeoutException) {
            TicketsResult.Error("La consulta tardo demasiado (señal lenta). Probá de nuevo")
        } catch (e: java.io.IOException) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        } catch (e: Exception) {
            TicketsResult.Error("No se pudo procesar la respuesta: ${e.message}")
        }
    }

    suspend fun obtenerResumen(idPlanillac: Int, totalPlanilla: Double): TicketsResult<ResumenCobranza> {
        return try {
            val respuesta = ApiClient.planillasApi.resumen(tokenHeader(), idPlanillac, totalPlanilla)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true && body.resumen != null) {
                TicketsResult.Exito(body.resumen)
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo cargar el resumen")
            }
        } catch (e: Exception) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        }
    }

    suspend fun obtenerCobranzaPorCliente(numplanilla: Int): TicketsResult<List<ClienteCobranza>> {
        return try {
            val respuesta = ApiClient.planillasApi.cobranzaPorCliente(tokenHeader(), numplanilla)
            val body = respuesta.body()
            if (respuesta.isSuccessful && body?.ok == true) {
                TicketsResult.Exito(body.clientes ?: emptyList())
            } else {
                TicketsResult.Error(body?.error ?: "No se pudo cargar la cobranza")
            }
        } catch (e: Exception) {
            TicketsResult.Error("Sin conexion. Verifica tu senal e intenta de nuevo")
        }
    }
}