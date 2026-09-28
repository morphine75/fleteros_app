package com.logex.fleteros.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logex.fleteros.data.ArticuloFactura
import com.logex.fleteros.data.FacturaConArticulos
import com.logex.fleteros.data.FaltantesRepository
import com.logex.fleteros.data.TicketsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FaltantesUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val mensaje: String? = null,
    val facturas: List<FacturaConArticulos> = emptyList(),
    val marcados: Set<Int> = emptySet(),
    val bultosIngresados: Map<Int, String> = emptyMap(),
    val unidadesIngresadas: Map<Int, String> = emptyMap(),
    val guardandoCodart: Int? = null
)

class FaltantesViewModel(
    private val repository: FaltantesRepository,
    private val numplanilla: Int,
    private val idPlanillac: Int,
    private val idCliente: Int,
    private val nombreCliente: String,
    private val fletero: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(FaltantesUiState())
    val uiState: StateFlow<FaltantesUiState> = _uiState

    fun cargarDetalle() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val resultado = repository.obtenerDetalle(numplanilla, idCliente)) {
                is TicketsResult.Exito -> {
                    val facturas = resultado.datos
                    val marcados = mutableSetOf<Int>()
                    val bultos = mutableMapOf<Int, String>()
                    val unidades = mutableMapOf<Int, String>()

                    facturas.forEach { factura ->
                        factura.articulos.forEach { art ->
                            if (art.faltante != null) {
                                marcados.add(art.codart)
                                bultos[art.codart] = art.faltante.bultos_falta.toString()
                                unidades[art.codart] = art.faltante.resto_falta.toString()
                            } else {
                                bultos[art.codart] = art.cant.toString()
                                unidades[art.codart] = art.resto.toString()
                            }
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        facturas = facturas,
                        marcados = marcados,
                        bultosIngresados = bultos,
                        unidadesIngresadas = unidades
                    )
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun toggleMarcado(codart: Int) {
        val actuales = _uiState.value.marcados.toMutableSet()
        if (actuales.contains(codart)) actuales.remove(codart) else actuales.add(codart)
        _uiState.value = _uiState.value.copy(marcados = actuales, error = null, mensaje = null)
    }

    fun onBultosChange(codart: Int, valor: String) {
        val mapa = _uiState.value.bultosIngresados.toMutableMap()
        mapa[codart] = valor
        _uiState.value = _uiState.value.copy(bultosIngresados = mapa)
    }

    fun onUnidadesChange(codart: Int, valor: String) {
        val mapa = _uiState.value.unidadesIngresadas.toMutableMap()
        mapa[codart] = valor
        _uiState.value = _uiState.value.copy(unidadesIngresadas = mapa)
    }

    fun guardarFaltante(factura: FacturaConArticulos, articulo: ArticuloFactura) {
        val estado = _uiState.value
        val bultosTexto = estado.bultosIngresados[articulo.codart] ?: "0"
        val unidadesTexto = estado.unidadesIngresadas[articulo.codart] ?: "0"

        val bultosNum = bultosTexto.toDoubleOrNull() ?: 0.0
        val unidadesNum = unidadesTexto.toDoubleOrNull() ?: 0.0

        // Chequeo rapido en la app (el servidor tambien lo valida contra
        // Progress, esto es solo para avisar sin esperar la respuesta)
        if (bultosNum > articulo.cant || unidadesNum > articulo.resto || bultosNum < 0 || unidadesNum < 0) {
            _uiState.value = estado.copy(error = "La cantidad faltante no puede superar lo facturado")
            return
        }

        _uiState.value = estado.copy(guardandoCodart = articulo.codart, error = null, mensaje = null)

        viewModelScope.launch {
            val resultado = repository.guardarFaltante(
                idPlanillac = idPlanillac,
                idCliente = idCliente,
                nomCliente = nombreCliente,
                fletero = fletero,
                nrodoc = factura.nrodoc,
                letra = factura.letra,
                codart = articulo.codart,
                descripcion = articulo.descripcion,
                bultosFalta = bultosTexto,
                restoFalta = unidadesTexto
            )
            when (resultado) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(guardandoCodart = null, mensaje = "Faltante registrado")
                    cargarDetalle()
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(guardandoCodart = null, error = resultado.mensaje)
                }
            }
        }
    }
}