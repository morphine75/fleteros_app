package com.logex.fleteros.data

data class ClienteCobranza(
    val cliente: Int,
    val nombre: String,
    val total: Double,
    val estado: String, // ENTREGADO | RECHAZADO | RECHAZADO_PARCIAL | SIN_INFORMAR
    val efectivo: Double,
    val transferencia: Double,
    val cheque: Double,
    val cta_cte: Double,
    val retencion: Double,
    val rechazo: Double,
    val total_pagado: Double
)

data class CobranzaPorClienteResponse(
    val ok: Boolean,
    val clientes: List<ClienteCobranza>? = null,
    val error: String? = null
)