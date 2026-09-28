package com.logex.fleteros.data

data class MotivoRechazo(val id: Int, val descripcion: String)

data class FacturaPlanilla(val nrodoc: Int, val letra: String, val total: Double)

data class FacturaCobrada(val nrodoc: Int, val cobrado: Double)

data class RegistroExistente(
    val estado: String, // ENTREGADO | RECHAZADO | RECHAZADO_PARCIAL | NINGUNO
    val importe_efectivo: Double,
    val importe_cheque: Double,
    val importe_transferencia: Double,
    val importe_cta_cte: Double,
    val importe_retencion: Double,
    val banco_transferencia: String? = null,
    val fecha_transferencia: String? = null,
    val id_motivo_rechazo: Int,
    val motivo_rechazo: String? = null,
    val facturas_cobradas: List<FacturaCobrada>
)

data class ImagenPlanilla(val id_detalle_img: Int, val nombre_archivo: String)

data class DetalleSeguimientoResponse(
    val ok: Boolean,
    val facturas: List<FacturaPlanilla>? = null,
    val motivos: List<MotivoRechazo>? = null,
    val registro_existente: RegistroExistente? = null,
    val imagenes: List<ImagenPlanilla>? = null,
    val error: String? = null
)

data class GuardarSeguimientoResponse(
    val ok: Boolean,
    val id_detalle_cli: Int? = null,
    val imagenes_guardadas: Int? = null,
    val error: String? = null
)