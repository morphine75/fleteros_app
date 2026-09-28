package com.logex.fleteros.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UbicacionPendienteDao {

    @Insert
    suspend fun insertar(punto: UbicacionPendiente)

    // Ordenado por timestamp para respetar el orden real en que se
    // capturaron, aunque se hayan guardado en desorden por reintentos.
    @Query("SELECT * FROM ubicaciones_pendientes ORDER BY timestamp ASC")
    suspend fun obtenerTodas(): List<UbicacionPendiente>

    @Delete
    suspend fun eliminar(punto: UbicacionPendiente)

    @Query("SELECT COUNT(*) FROM ubicaciones_pendientes")
    suspend fun contar(): Int

    // Por las dudas la cola crezca mucho (varios dias sin conexion,
    // celular olvidado, etc), no la dejamos crecer sin limite.
    @Query("DELETE FROM ubicaciones_pendientes WHERE id IN (SELECT id FROM ubicaciones_pendientes ORDER BY timestamp ASC LIMIT :cantidad)")
    suspend fun eliminarMasViejas(cantidad: Int)
}