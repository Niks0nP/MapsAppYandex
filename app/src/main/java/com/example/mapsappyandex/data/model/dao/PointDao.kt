package com.example.mapsappyandex.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapsappyandex.data.model.entity.PointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewPoint(pointEntity: PointEntity)

    @Query("SELECT * FROM points_location")
    fun readAllPointsLocation(): Flow<List<PointEntity>>

    @Query("DELETE FROM points_location WHERE id==:pointId")
    suspend fun deletePoint(pointId: Long)
}