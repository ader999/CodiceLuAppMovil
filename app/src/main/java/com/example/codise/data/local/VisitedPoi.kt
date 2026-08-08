package com.example.codise.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visited_pois")
data class VisitedPoi(
    @PrimaryKey val poiId: Int,
    val visitedAt: Long = System.currentTimeMillis()
)
