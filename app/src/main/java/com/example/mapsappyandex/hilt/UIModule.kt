package com.example.mapsappyandex.hilt

import android.content.Context
import androidx.room.Room
import com.example.mapsappyandex.data.model.appDB.AppDatabase
import com.example.mapsappyandex.data.repository.PointsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UIModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context) : AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "points_database.db"
        ).build()
    }

    @Provides
    fun provideRepository(appDatabase: AppDatabase) : PointsRepository {
        return PointsRepository(appDatabase.getPointsDao())
    }
}