package com.mobiletreeplantingapp.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.mobiletreeplantingapp.data.local.dao.GeographicalDataDao
import com.mobiletreeplantingapp.data.model.GeographicalData
import com.mobiletreeplantingapp.data.model.SoilProperties
import com.mobiletreeplantingapp.data.remote.ElevationApiService
import com.mobiletreeplantingapp.data.remote.SoilApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GeographicalDataRepository @Inject constructor(
    private val geographicalDataDao: GeographicalDataDao,
    private val soilApiService: SoilApiService,
    private val elevationApiService: ElevationApiService
) {
    suspend fun getGeographicalData(location: LatLng): Flow<Result<GeographicalData>> = flow {
        try {
            Log.d("GeoRepository", "Starting to fetch geographical data for $location")
            
            // Fetch from API directly
            Log.d("GeoRepository", "Fetching from API")
            val locationString = "${location.latitude},${location.longitude}"
            val elevationResponse = elevationApiService.getElevation(locationString)
            Log.d("GeoRepository", "Got elevation response: ${elevationResponse.isSuccessful}")

            if (elevationResponse.isSuccessful && elevationResponse.body() != null) {
                val elevation = elevationResponse.body()!!.results.firstOrNull()?.elevation
                    ?: throw Exception("No elevation data available")
                Log.d("GeoRepository", "Elevation: $elevation")
                
                val climateZone = determineClimateZone(location.latitude)
                Log.d("GeoRepository", "Climate zone: $climateZone")

                val geographicalData = GeographicalData(
                    soilType = "Loam", // We'll implement soil API later
                    elevation = elevation,
                    climateZone = climateZone
                )

                Log.d("GeoRepository", "Emitting success result")
                emit(Result.success(geographicalData))
            } else {
                val errorBody = elevationResponse.errorBody()?.string()
                Log.e("GeoRepository", "Failed to fetch elevation data: $errorBody")
                throw Exception("Failed to fetch elevation data: $errorBody")
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
        return when {
            latitude >= 66.5 -> "Arctic"
            latitude >= 23.5 -> "Temperate"
            latitude >= -23.5 -> "Tropical"
            latitude >= -66.5 -> "Temperate"
            else -> "Antarctic"
        }
    }

    suspend fun cleanOldCache() {
        // Delete data older than 24 hours
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        geographicalDataDao.deleteOldData(oneDayAgo)
    }
} 