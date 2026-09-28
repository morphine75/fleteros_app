package com.logex.fleteros.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logex.fleteros.data.FacturaPlanilla
import com.logex.fleteros.data.ImagenPlanilla
import com.logex.fleteros.data.MotivoRechazo
import com.logex.fleteros.data.SeguimientoRepository
import com.logex.fleteros.data.TicketsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class EstadoSeguimiento { NINGUNO, ENTREGADO, RECHAZADO, RECHAZADO_PARCIAL }

data class SeguimientoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val estado: EstadoSeguimiento = EstadoSeguimiento.NINGUNO,

    val efectivoActivo: Boolean = false,
    val efectivoImporte: String = "",
    val chequeActivo: Boolean = false,
    val chequeImporte: String = "",
    val transferenciaActivo: Boolean = false,
    val transferenciaImporte: String = "",
    val bancoTransferencia: String = "",
    val fechaTransferencia: String = "",
    val ctaCteActivo: Boolean = false,
    val ctaCteImporte: String = "",
    val retencionActivo: Boolean = false,
    val retencionImporte: String = "",

    val motivos: List<MotivoRechazo> = emptyList(),
    val motivoSeleccionado: MotivoRechazo? = null,

    val facturas: List<FacturaPlanilla> = emptyList(),
    val importesCobrados: Map<Int, String> = emptyMap(),

    val imagenesExistentes: List<ImagenPlanilla> = emptyList(),
    val imagenesSeleccionadas: List<Uri> = emptyList(),
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class SeguimientoViewModel(
    private val repository: SeguimientoRepository,
    private val numplanilla: Int,
    private val idPlanillac: Int,
    private val idCliente: Int,
    private val nombreCliente: String,
    private val totalCobrar: Double
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeguimientoUiState())
    val uiState: StateFlow<SeguimientoUiState> = _uiState

    fun cargarDetalle() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val resultado = repository.obtenerDetalle(numplanilla, idCliente, idPlanillac)) {
                is TicketsResult.Exito -> {
                    val facturas = resultado.datos.facturas ?: emptyList()
                    val registro = resultado.datos.registro_existente
                    val motivos = resultado.datos.motivos ?: emptyList()

                    // Si hay un registro previo, precarga los importes cobrados por
                    // factura con lo que ya se habia guardado; si no, arranca con
                    // el total de cada factura (asume que se cobra todo por defecto)
                    val cobradosPrevios = registro?.facturas_cobradas?.associate { it.nrodoc to it.cobrado.toString() } ?: emptyMap()
                    val importesIniciales = facturas.associate { f ->
                        f.nrodoc to (cobradosPrevios[f.nrodoc] ?: f.total.toString())
                    }

                    var nuevoEstado = _uiState.value.copy(
                        cargando = false,
                        facturas = facturas,
                        motivos = motivos,
                        importesCobrados = importesIniciales,
                        imagenesExistentes = resultado.datos.imagenes ?: emptyList()
                    )

                    if (registro != null) {
                        nuevoEstado = nuevoEstado.copy(
                            estado = when (registro.estado) {
                                "ENTREGADO" -> EstadoSeguimiento.ENTREGADO
                                "RECHAZADO" -> EstadoSeguimiento.RECHAZADO
                                "RECHAZADO_PARCIAL" -> EstadoSeguimiento.RECHAZADO_PARCIAL
                                else -> EstadoSeguimiento.NINGUNO
                            },
                            efectivoActivo = registro.importe_efectivo > 0,
                            efectivoImporte = if (registro.importe_efectivo > 0) registro.importe_efectivo.toString() else "",
                            chequeActivo = registro.importe_cheque > 0,
                            chequeImporte = if (registro.importe_cheque > 0) registro.importe_cheque.toString() else "",
                            transferenciaActivo = registro.importe_transferencia > 0,
                            transferenciaImporte = if (registro.importe_transferencia > 0) registro.importe_transferencia.toString() else "",
                            bancoTransferencia = registro.banco_transferencia ?: "",
                            fechaTransferencia = registro.fecha_transferencia ?: "",
                            ctaCteActivo = registro.importe_cta_cte > 0,
                            ctaCteImporte = if (registro.importe_cta_cte > 0) registro.importe_cta_cte.toString() else "",
                            retencionActivo = registro.importe_retencion > 0,
                            retencionImporte = if (registro.importe_retencion > 0) registro.importe_retencion.toString() else "",
                            motivoSeleccionado = motivos.firstOrNull { it.id == registro.id_motivo_rechazo }
                        )
                    }

                    _uiState.value = nuevoEstado
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun eliminarImagenExistente(idDetalleImg: Int) {
        viewModelScope.launch {
            when (val resultado = repository.eliminarImagen(idDetalleImg)) {
                is TicketsResult.Exito -> {
                    _uiState.value = _uiState.value.copy(
                        imagenesExistentes = _uiState.value.imagenesExistentes.filter { it.id_detalle_img != idDetalleImg }
                    )
                }
                is TicketsResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = resultado.mensaje)
                }
            }
        }
    }

    fun seleccionarEstado(estado: EstadoSeguimiento) {
        _uiState.value = _uiState.value.copy(estado = estado, error = null)
    }

    private fun montoInicial(): String = totalCobrar.toString()

    fun toggleEfectivo(activo: Boolean) {
        _uiState.value = _uiState.value.copy(efectivoActivo = activo, efectivoImporte = if (activo) montoInicial() else "")
    }
    fun onEfectivoImporteChange(v: String) { _uiState.value = _uiState.value.copy(efectivoImporte = v) }

    fun toggleCheque(activo: Boolean) {
        _uiState.value = _uiState.value.copy(chequeActivo = activo, chequeImporte = if (activo) montoInicial() else "")
    }
    fun onChequeImporteChange(v: String) { _uiState.value = _uiState.value.copy(chequeImporte = v) }

    fun toggleTransferencia(activo: Boolean) {
        _uiState.value = _uiState.value.copy(transferenciaActivo = activo, transferenciaImporte = if (activo) montoInicial() else "")
    }
    fun onTransferenciaImporteChange(v: String) { _uiState.value = _uiState.value.copy(transferenciaImporte = v) }
    fun onBancoChange(v: String) { _uiState.value = _uiState.value.copy(bancoTransferencia = v) }
    fun onFechaTransferenciaChange(v: String) { _uiState.value = _uiState.value.copy(fechaTransferencia = v) }

    fun toggleCtaCte(activo: Boolean) {
        _uiState.value = _uiState.value.copy(ctaCteActivo = activo, ctaCteImporte = if (activo) montoInicial() else "")
    }
    fun onCtaCteImporteChange(v: String) { _uiState.value = _uiState.value.copy(ctaCteImporte = v) }

    fun toggleRetencion(activo: Boolean) {
        _uiState.value = _uiState.value.copy(retencionActivo = activo, retencionImporte = if (activo) montoInicial() else "")
    }
    fun onRetencionImporteChange(v: String) { _uiState.value = _uiState.value.copy(retencionImporte = v) }

    fun seleccionarMotivo(motivo: MotivoRechazo) {
        _uiState.value = _uiState.value.copy(motivoSeleccionado = motivo)
    }

    fun onImporteFacturaChange(nrodoc: Int, valor: String) {
        val nuevoMapa = _uiState.value.importesCobrados.toMutableMap()
        nuevoMapa[nrodoc] = valor
        _uiState.value = _uiState.value.copy(importesCobrados = nuevoMapa)
    }

    fun onImagenesCambiadas(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(imagenesSeleccionadas = uris)
    }

    fun guardar(context: Context) {
        val estado = _uiState.value

        if (estado.estado == EstadoSeguimiento.NINGUNO) {
            _uiState.value = estado.copy(error = "Indique el estado del pedido")
            return
        }
        if (estado.estado == EstadoSeguimiento.RECHAZADO && estado.motivoSeleccionado == null) {
            _uiState.value = estado.copy(error = "Seleccione el motivo del rechazo")
            return
        }

        val necesitaPago = estado.estado == EstadoSeguimiento.ENTREGADO || estado.estado == EstadoSeguimiento.RECHAZADO_PARCIAL
        if (necesitaPago) {
            val hayAlgunPago = estado.efectivoActivo || estado.chequeActivo || estado.transferenciaActivo || estado.ctaCteActivo || estado.retencionActivo
            if (!hayAlgunPago) {
                _uiState.value = estado.copy(error = "Marque al menos una opcion de pago")
                return
            }
        }

        _uiState.value = estado.copy(guardando = true, error = null)

        val campos = mutableMapOf(
            "id_planillac" to idPlanillac.toString(),
            "idcliente" to idCliente.toString(),
            "nomcli" to nombreCliente,
            "total_cobrar" to totalCobrar.toString(),
            "estado" to when (estado.estado) {
                EstadoSeguimiento.ENTREGADO -> "ENTREGADO"
                EstadoSeguimiento.RECHAZADO -> "RECHAZADO"
                EstadoSeguimiento.RECHAZADO_PARCIAL -> "RECHAZADO_PARCIAL"
                else -> ""
            },
            "importe_efectivo" to (if (estado.efectivoActivo) estado.efectivoImporte else "0"),
            "importe_cheque" to (if (estado.chequeActivo) estado.chequeImporte else "0"),
            "importe_transferencia" to (if (estado.transferenciaActivo) estado.transferenciaImporte else "0"),
            "importe_cta_cte" to (if (estado.ctaCteActivo) estado.ctaCteImporte else "0"),
            "importe_retencion" to (if (estado.retencionActivo) estado.retencionImporte else "0"),
            "banco_transferencia" to estado.bancoTransferencia,
            "fecha_transferencia" to estado.fechaTransferencia,
            "id_motivo_rechazo" to (estado.motivoSeleccionado?.id?.toString() ?: "0"),
            "motivo_rechazo" to (estado.motivoSeleccionado?.descripcion ?: "")
        )

        if (estado.estado == EstadoSeguimiento.RECHAZADO_PARCIAL) {
            val facturasJson = buildString {
                append("[")
                estado.facturas.forEachIndexed { indice, f ->
                    val cobrado = estado.importesCobrados[f.nrodoc]?.toDoubleOrNull() ?: f.total
                    if (indice > 0) append(",")
                    append("{\"nrodoc\":${f.nrodoc},\"total\":${f.total},\"cobrado\":$cobrado}")
                }
                append("]")
            }
            campos["facturas"] = facturasJson
        }

        viewModelScope.launch {
            val resultado = repository.guardar(context, campos, estado.imagenesSeleccionadas)
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