package com.logex.fleteros.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logex.fleteros.data.TicketGasto
import com.logex.fleteros.data.TicketsGastosRepository
import com.logex.fleteros.data.TicketsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TicketsGastosUiState(
    val tickets: List<TicketGasto> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val mostrarDialogoNuevo: Boolean = false,
    val fecha: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val importe: String = "",
    val observaciones: String = "",
    val imagenesSeleccionadas: List<Uri> = emptyList(),
    val guardando: Boolean = false
)

class TicketsGastosViewModel(private val repository: TicketsGastosRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsGastosUiState())
    val uiState: StateFlow<TicketsGastosUiState> = _uiState

    fun cargarTickets() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val resultado = repository.listar()) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(cargando = false, tickets = resultado.datos)
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun abrirDialogoNuevo() {
        _uiState.value = _uiState.value.copy(mostrarDialogoNuevo = true)
    }

    fun cerrarDialogoNuevo() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoNuevo = false,
            importe = "",
            observaciones = "",
            imagenesSeleccionadas = emptyList()
        )
    }

    fun onFechaChange(valor: String) {
        _uiState.value = _uiState.value.copy(fecha = valor)
    }

    fun onImporteChange(valor: String) {
        _uiState.value = _uiState.value.copy(importe = valor)
    }

    fun onObservacionesChange(valor: String) {
        _uiState.value = _uiState.value.copy(observaciones = valor)
    }

    fun onImagenesSeleccionadas(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(imagenesSeleccionadas = uris)
    }

    fun guardarTicket(context: Context) {
        val estado = _uiState.value
        if (estado.importe.isBlank()) {
            _uiState.value = estado.copy(error = "Ingresa el importe")
            return
        }

        _uiState.value = estado.copy(guardando = true, error = null)

        viewModelScope.launch {
            val resultado = repository.guardar(
                context = context,
                fecha = estado.fecha,
                importe = estado.importe,
                observaciones = estado.observaciones,
                imagenes = estado.imagenesSeleccionadas
            )
            when (resultado) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(guardando = false)
                    cerrarDialogoNuevo()
                    cargarTickets()
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(guardando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun eliminarTicket(idTicket: Int) {
        viewModelScope.launch {
            when (val resultado = repository.eliminar(idTicket)) {
                is TicketsResult.Exito -> cargarTickets()
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = resultado.mensaje)
                }
            }
        }
    }
}