package com.example.mapsappyandex.data.repository

import com.example.mapsappyandex.data.model.dao.PointDao
import com.example.mapsappyandex.data.model.entity.PointEntity
import kotlinx.coroutines.flow.Flow

class PointsRepository(private val pointDao: PointDao)  {

    val readAllPoints: Flow<List<PointEntity>> = pointDao.readAllPointsLocation()

    suspend fun insertNewPoint(pointEntity: PointEntity) {
        pointDao.insertNewPoint(pointEntity)
    }

    suspend fun deletePoint(pointId: Long) {
        pointDao.deletePoint(pointId)
    }
}