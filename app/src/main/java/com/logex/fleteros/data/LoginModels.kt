package com.logex.fleteros.data

data class LoginResponse(
    val ok: Boolean,
    val token: String? = null,
    val usuario: Usuario? = null,
    val error: String? = null
)

data class Usuario(
    val id_usuario: Int,
    val usuario: String,
    val nombre: String,
    val id_tusuario: Int,
    val numero_vendedor: Int,
    val numero_guia: Int,
    val numero_auditor: Int,
    val sucursal: Int,
    val empresa: Int,
    val usa_movil: Int,
    val chequea_retorno: Int
)