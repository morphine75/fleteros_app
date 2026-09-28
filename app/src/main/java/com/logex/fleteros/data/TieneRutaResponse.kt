package com.logex.fleteros.data

data class TieneRutaResponse(
    val ok: Boolean,
    val tiene_ruta: Boolean? = null,
    val error: String? = null
)