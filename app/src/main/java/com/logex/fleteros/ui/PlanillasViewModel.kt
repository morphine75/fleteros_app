package com.logex.fleteros.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logex.fleteros.data.PlanillaCarga
import com.logex.fleteros.data.PlanillasRepository
import com.logex.fleteros.data.TicketsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FiltroEstado { TODOS, INFORMADOS, NO_INFORMADOS }

data class PlanillasUiState(
    val fecha: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val planillas: List<PlanillaCarga> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val filtro: String = "",
    val filtroEstado: FiltroEstado = FiltroEstado.TODOS
)

class PlanillasViewModel(private val repository: PlanillasRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanillasUiState())
    val uiState: StateFlow<PlanillasUiState> = _uiState

    fun onFechaChange(valor: String) {
        _uiState.value = _uiState.value.copy(fecha = valor)
    }

    fun onFiltroChange(valor: String) {
        _uiState.value = _uiState.value.copy(filtro = valor)
    }

    fun onFiltroEstadoChange(valor: FiltroEstado) {
        _uiState.value = _uiState.value.copy(filtroEstado = valor)
    }

    fun cargarPlanillas() {
        val fecha = _uiState.value.fecha
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val resultado = repository.listar(fecha)) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(cargando = false, planillas = resultado.datos)
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    suspend fun obtenerResumen(idPlanillac: Int, totalPlanilla: Double) =
        repository.obtenerResumen(idPlanillac, totalPlanilla)
}