package com.mobiletreeplantingapp.di

import android.content.Context
import androidx.room.Room
import com.mobiletreeplantingapp.data.local.AppDatabase
import com.mobiletreeplantingapp.data.repository.CoroutineDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideGeographicalDataDao(database: AppDatabase) = database.geographicalDataDao

    @Provides
    @Singleton
    fun provideCoroutineDispatchers() = CoroutineDispatchers()
} 