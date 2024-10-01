package com.example.mapsappyandex.data.model.appDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mapsappyandex.data.model.dao.PointDao
import com.example.mapsappyandex.data.model.entity.PointEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    version = 1,
    entities = [
        PointEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getPointsDao(): PointDao
}