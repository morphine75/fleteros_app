package com.logex.fleteros.location

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un punto de GPS capturado que todavia no se pudo mandar al servidor
 * (sin señal, timeout, etc). Se borra de la base apenas se confirma que
 * el servidor lo recibio -- no se guarda historial local, solo la cola
 * de pendientes.
 */
@Entity(tableName = "ubicaciones_pendientes")
data class UbicacionPendiente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitud: Double,
    val longitud: Double,
    val precision: Double,
    val velocidad: Double,
    val rumbo: Double?,
    val altitud: Double?,
    val timestamp: Long, // System.currentTimeMillis() en el momento de la captura
    val fechaHoraTexto: String // "yyyy-MM-dd HH:mm:ss", lo que espera el servidor
)