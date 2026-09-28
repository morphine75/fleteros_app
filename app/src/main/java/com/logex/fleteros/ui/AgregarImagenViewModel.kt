package com.logex.fleteros.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logex.fleteros.data.ImagenPlanilla
import com.logex.fleteros.data.SeguimientoRepository
import com.logex.fleteros.data.TicketsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AgregarImagenUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val imagenesExistentes: List<ImagenPlanilla> = emptyList(),
    val imagenesSeleccionadas: List<Uri> = emptyList(),
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class AgregarImagenViewModel(
    private val repository: SeguimientoRepository,
    private val numplanilla: Int,
    private val idPlanillac: Int,
    private val idCliente: Int,
    private val nombreCliente: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgregarImagenUiState())
    val uiState: StateFlow<AgregarImagenUiState> = _uiState

    fun cargarImagenes() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            // Reutilizamos el mismo endpoint de detalle: solo nos interesan
            // las imagenes que ya trae, ignoramos facturas/motivos/registro.
            when (val resultado = repository.obtenerDetalle(numplanilla, idCliente, idPlanillac)) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        imagenesExistentes = resultado.datos.imagenes ?: emptyList()
                    )
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun onImagenesCambiadas(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(imagenesSeleccionadas = uris)
    }

    fun guardar(context: Context) {
        val estado = _uiState.value
        if (estado.imagenesSeleccionadas.isEmpty()) {
            _uiState.value = estado.copy(error = "Elegi al menos una foto")
            return
        }

        _uiState.value = estado.copy(guardando = true, error = null)

        viewModelScope.launch {
            val resultado = repository.agregarImagenes(context, idPlanillac, idCliente, nombreCliente, estado.imagenesSeleccionadas)
            when (resultado) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(guardando = false, guardadoExitoso = true)
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(guardando = false, error = resultado.mensaje)
                }
            }
        }
    }
}