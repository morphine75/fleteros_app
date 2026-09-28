package com.logex.fleteros.data

data class DetalleImporte(val cliente: Int, val nombre: String, val importe: Double)

data class TotalesResumen(
    val efectivo: Double,
    val transferencia: Double,
    val cheque: Double,
    val cta_cte: Double,
    val retencion: Double,
    val rechazo: Double,
    val rechazo_parcial: Double,
    val total_cobrado: Double,
    val total_planilla: Double,
    val total_planilla_neto: Double
)

data class ResumenCobranza(
    val totales: TotalesResumen,
    val detalle_efectivo: List<DetalleImporte>,
    val detalle_transferencia: List<DetalleImporte>,
    val detalle_rechazo: List<DetalleImporte>,
    val detalle_rechazo_parcial: List<DetalleImporte>
)

data class ResumenCobranzaResponse(
    val ok: Boolean,
    val resumen: ResumenCobranza? = null,
    val error: String? = null
)