package com.logex.fleteros.data

data class ClientePlanilla(
    val cliente: Int,
    val nombre: String,
    val total: Double,
    val liquidacion: Int,
    val en_liquidacion: Boolean,
    val tiene_registro: Boolean,
    val transferencia: Double,
    val efectivo: Double,
    val cta_cte: Double,
    val rechazo: Double,
    val rechazado: Boolean,
    val rechazado_parcial: Boolean
)

data class PlanillaCarga(
    val nplanilla: Int,
    val planillac: Int,
    val fletero: String,
    val total: Double,
    val clientes: List<ClientePlanilla>
)

data class ListarPlanillasResponse(
    val ok: Boolean,
    val fecha: String? = null,
    val planillas: List<PlanillaCarga>? = null,
    val error: String? = null
)