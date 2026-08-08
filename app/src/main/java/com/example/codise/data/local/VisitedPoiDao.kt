package com.example.codise.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitedPoiDao {
    @Query("SELECT * FROM visited_pois")
    fun getAllVisited(): Flow<List<VisitedPoi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsVisited(poi: VisitedPoi)

    @Delete
    suspend fun unmarkAsVisited(poi: VisitedPoi)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pois: List<VisitedPoi>)

    @Query("DELETE FROM visited_pois")
    suspend fun deleteAll()

    @Query("SELECT EXISTS(SELECT 1 FROM visited_pois WHERE poiId = :poiId)")
    suspend fun isVisited(poiId: Int): Boolean
}
