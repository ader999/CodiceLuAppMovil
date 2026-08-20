package com.example.codise.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PuntoVisitado::class], version = 2, exportSchema = false)
abstract class BaseDeDatosApp : RoomDatabase() {
    abstract fun puntoVisitadoDao(): PuntoVisitadoDao

    companion object {
        @Volatile
        private var INSTANCIA: BaseDeDatosApp? = null

        fun obtenerBaseDeDatos(contexto: Context): BaseDeDatosApp {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDeDatosApp::class.java,
                    "codise_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
