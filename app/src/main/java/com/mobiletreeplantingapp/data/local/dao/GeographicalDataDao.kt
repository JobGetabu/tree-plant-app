package com.mobiletreeplantingapp.data.local.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mobiletreeplantingapp.data.local.entity.GeographicalDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeographicalDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeographicalData(data: GeographicalDataEntity)

    @Query("SELECT * FROM geographical_data WHERE latitude = :lat AND longitude = :lng LIMIT 1")
    fun getGeographicalData(lat: Double, lng: Double): Flow<GeographicalDataEntity?>

    @Query("DELETE FROM geographical_data WHERE timestamp < :timestamp")
    suspend fun deleteOldData(timestamp: Long)

    @Transaction
    suspend fun safeInsertGeographicalData(data: GeographicalDataEntity) {
        try {
            insertGeographicalData(data)
        } catch (e: Exception) {
            Log.e("GeographicalDataDao", "Error inserting data", e)
            // Handle the error or rethrow if needed
        }
    }
} 