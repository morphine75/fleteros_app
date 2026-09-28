package com.logex.fleteros.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logex.fleteros.data.ClienteCobranza
import com.logex.fleteros.data.PlanillasRepository
import com.logex.fleteros.data.TicketsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CobranzaPorClienteUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val clientes: List<ClienteCobranza> = emptyList()
) {
    val totalFacturado: Double get() = clientes.sumOf { it.total }
    val totalCobrado: Double get() = clientes.sumOf { it.total_pagado }
    val totalRechazo: Double get() = clientes.sumOf { it.rechazo }
    val diferencia: Double get() = totalFacturado - (totalCobrado + totalRechazo)
    val porcentajeCobrado: Double get() = if (totalFacturado > 0) (totalCobrado / totalFacturado) * 100 else 0.0
    val cantEntregados: Int get() = clientes.count { it.estado == "ENTREGADO" }
    val cantRechazados: Int get() = clientes.count { it.estado == "RECHAZADO" }
    val cantParciales: Int get() = clientes.count { it.estado == "RECHAZADO_PARCIAL" }
    val cantSinInformar: Int get() = clientes.count { it.estado == "SIN_INFORMAR" }
}

class CobranzaPorClienteViewModel(
    private val repository: PlanillasRepository,
    private val numplanilla: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CobranzaPorClienteUiState())
    val uiState: StateFlow<CobranzaPorClienteUiState> = _uiState

    fun cargar() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val resultado = repository.obtenerCobranzaPorCliente(numplanilla)) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(cargando = false, clientes = resultado.datos)
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }
}