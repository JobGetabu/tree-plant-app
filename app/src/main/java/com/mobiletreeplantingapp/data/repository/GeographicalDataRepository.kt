package com.mobiletreeplantingapp.data.repository

import com.google.android.gms.maps.model.LatLng
import com.mobiletreeplantingapp.data.local.dao.GeographicalDataDao
import com.mobiletreeplantingapp.data.local.entity.GeographicalDataEntity
import com.mobiletreeplantingapp.data.model.GeographicalData
import com.mobiletreeplantingapp.data.model.SoilProperties
import com.mobiletreeplantingapp.data.remote.ElevationApiService
import com.mobiletreeplantingapp.data.remote.SoilApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.abs
import android.util.Log

class GeographicalDataRepository @Inject constructor(
    private val geographicalDataDao: GeographicalDataDao,
    private val soilApiService: SoilApiService,
    private val elevationApiService: ElevationApiService
) {
    suspend fun getGeographicalData(location: LatLng): Flow<Result<GeographicalData>> = flow {
        try {
            Log.d("GeoRepository", "Starting to fetch geographical data for $location")
            
            // For testing, emit mock data immediately
            val mockData = GeographicalData(
                soilType = "Loam",
                elevation = 1234.0,
                climateZone = determineClimateZone(location.latitude)
            )
            
            Log.d("GeoRepository", "Emitting mock data: $mockData")
            emit(Result.success(mockData))

            // Cache the mock data
            try {
                geographicalDataDao.safeInsertGeographicalData(
                    GeographicalDataEntity(
                        id = "${location.latitude},${location.longitude}",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        soilType = mockData.soilType,
                        elevation = mockData.elevation,
                        climateZone = mockData.climateZone
                    )
                )
                Log.d("GeoRepository", "Cached mock data successfully")
            } catch (e: Exception) {
                Log.e("GeoRepository", "Error caching data", e)
                // Continue without caching
            }

        } catch (e: Exception) {
            Log.e("GeoRepository", "Error in getGeographicalData", e)
            emit(Result.failure(e))
        }
    }

    private fun determineSoilType(properties: SoilProperties): String {
        return when {
            properties.clayContent > 40 -> "Clay"
            properties.sandContent > 50 -> "Sandy"
            else -> "Loam"
        }
    }

    private fun determineClimateZone(latitude: Double): String {
        return when (abs(latitude)) {
            in 0.0..23.5 -> "Tropical"
            in 23.5..35.0 -> "Subtropical"
            in 35.0..66.5 -> "Temperate"
            else -> "Polar"
        }
    }

    suspend fun cleanOldCache() {
        // Delete data older than 24 hours
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        geographicalDataDao.deleteOldData(oneDayAgo)
    }
} 