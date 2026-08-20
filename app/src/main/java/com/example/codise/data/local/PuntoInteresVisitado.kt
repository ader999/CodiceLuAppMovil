package com.example.codise.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visited_pois")
data class PuntoVisitado(
    @PrimaryKey @ColumnInfo(name = "poiId") val puntoInteresId: Int,
    @ColumnInfo(name = "visitedAt") val fechaVisita: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isValidated") val estaValidado: Boolean = false
)
