package com.logex.fleteros.data

data class TicketGasto(
    val id_ticket: Int,
    val fecha: String,
    val observaciones: String,
    val importe: Double,
    val imagenes: List<String>
)

data class ListarTicketsResponse(
    val ok: Boolean,
    val tickets: List<TicketGasto>? = null,
    val error: String? = null
)

data class GuardarTicketResponse(
    val ok: Boolean,
    val id_ticket: Int? = null,
    val imagenes_guardadas: Int? = null,
    val hubo_error_imagen: Boolean? = null,
    val error: String? = null
)

data class SimpleResponse(
    val ok: Boolean,
    val error: String? = null
)