package com.logex.fleteros.data

data class TicketDeposito(
    val id_ticket: Int,
    val fecha: String,
    val observaciones: String,
    val importe: Double,
    val imagenes: List<String>
)

data class ListarTicketsDepositoResponse(
    val ok: Boolean,
    val tickets: List<TicketDeposito>? = null,
    val error: String? = null
)