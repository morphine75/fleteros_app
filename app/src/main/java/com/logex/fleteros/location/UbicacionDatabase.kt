package com.logex.fleteros.location

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UbicacionPendiente::class], version = 1, exportSchema = false)
abstract class UbicacionDatabase : RoomDatabase() {

    abstract fun ubicacionPendienteDao(): UbicacionPendienteDao

    companion object {
        @Volatile
        private var instancia: UbicacionDatabase? = null

        fun obtener(context: Context): UbicacionDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    UbicacionDatabase::class.java,
                    "ubicaciones_pendientes.db"
                ).build().also { instancia = it }
            }
        }
    }
}