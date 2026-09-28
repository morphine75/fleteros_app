package com.logex.fleteros.data

data class FaltanteMarcado(val bultos_falta: Double, val resto_falta: Double)

data class ArticuloFactura(
    val codart: Int,
    val descripcion: String,
    val cant: Double,
    val resto: Double,
    val faltante: FaltanteMarcado?
)

data class FacturaConArticulos(
    val nrodoc: Int,
    val letra: String,
    val articulos: List<ArticuloFactura>
)

data class DetalleFaltantesResponse(
    val ok: Boolean,
    val facturas: List<FacturaConArticulos>? = null,
    val error: String? = null
)