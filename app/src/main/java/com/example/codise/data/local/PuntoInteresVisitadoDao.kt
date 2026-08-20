package com.example.codise.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoVisitadoDao {
    @Query("SELECT * FROM visited_pois")
    fun obtenerTodosLosVisitados(): Flow<List<PuntoVisitado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun marcarComoVisitado(punto: PuntoVisitado)

    @Delete
    suspend fun desmarcarComoVisitado(punto: PuntoVisitado)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoVisitado>)

    @Query("DELETE FROM visited_pois")
    suspend fun eliminarTodos()

    @Query("SELECT EXISTS(SELECT 1 FROM visited_pois WHERE poiId = :puntoInteresId)")
    suspend fun estaVisitado(puntoInteresId: Int): Boolean
}
