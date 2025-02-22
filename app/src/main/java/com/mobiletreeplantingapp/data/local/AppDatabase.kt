package com.mobiletreeplantingapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mobiletreeplantingapp.data.local.dao.GeographicalDataDao
import com.mobiletreeplantingapp.data.local.entity.GeographicalDataEntity

@Database(
    entities = [GeographicalDataEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract val geographicalDataDao: GeographicalDataDao
} 