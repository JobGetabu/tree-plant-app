package com.mobiletreeplantingapp.ui.screen.navigation.explore

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.TreeRecommendation
import com.mobiletreeplantingapp.data.repository.CoroutineDispatchers
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import com.mobiletreeplantingapp.data.repository.GeographicalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val geographicalDataRepository: GeographicalDataRepository,
    private val firestoreRepository: FirestoreRepository,
    private val auth: FirebaseAuth,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {
    var state by mutableStateOf(ExploreState())
        private set

    fun onEvent(event: ExploreEvent) {
        when (event) {
            is ExploreEvent.AddPolygonPoint -> {
                state = state.copy(
                    polygonPoints = state.polygonPoints + event.point
                )
                calculateAreaSize()
            }
            is ExploreEvent.ClearPolygon -> {
                state = state.copy(
                    polygonPoints = emptyList(),
                    areaSize = 0.0,
                    isAreaFinalized = false,
                    showBottomSheet = false
                )
            }
            is ExploreEvent.UpdateUserLocation -> {
                state = state.copy(userLocation = event.location)
            }
            is ExploreEvent.FinalizeArea -> {
                if (state.polygonPoints.size >= 3) {
                    state = state.copy(showBottomSheet = true)
                    finalizeArea()
                }
            }
            is ExploreEvent.ToggleBottomSheet -> {
                if (state.isAreaFinalized) {
                    state = state.copy(
                        showBottomSheet = !state.showBottomSheet,
                        showSaveDialog = false,
                        areaName = ""
                    )
                } else {
                    state = state.copy(
                        showBottomSheet = false,
                        showSaveDialog = false,
                        areaName = ""
                    )
                }
            }
            is ExploreEvent.UpdateAreaName -> {
                state = state.copy(areaName = event.name)
            }
            is ExploreEvent.ShowSaveDialog -> {
                state = state.copy(showSaveDialog = true)
            }
            is ExploreEvent.DismissSaveDialog -> {
                state = state.copy(showSaveDialog = false, areaName = "")
            }
            is ExploreEvent.SaveArea -> {
                saveArea()
            }
            is ExploreEvent.DismissError -> {
                state = state.copy(error = null)
            }
        }
    }

    private fun calculateAreaSize() {
        if (state.polygonPoints.size >= 3) {
            val areaInSquareMeters = SphericalUtil.computeArea(state.polygonPoints)
            state = state.copy(areaSize = areaInSquareMeters / 10000) // Convert to hectares
        }
    }

    private fun finalizeArea() {
        viewModelScope.launch(dispatchers.io) {
            Log.d("ExploreViewModel", "Starting area finalization")
            state = state.copy(isLoading = true, showBottomSheet = true, error = null)
            
            try {
                val centroid = calculateCentroid(state.polygonPoints)
                Log.d("ExploreViewModel", "Calculated centroid: $centroid")
                
                geographicalDataRepository.getGeographicalData(centroid).collect { result ->
                    Log.d("ExploreViewModel", "Received data result")
                    result.onSuccess { data ->
                        Log.d("ExploreViewModel", "Success: $data")
                        state = state.copy(
                            soilType = data.soilType,
                            altitude = "${data.elevation.toInt()}m",
                            climateZone = data.climateZone,
                            isAreaFinalized = true,
                            isLoading = false,
                            error = null
                        )
                    }.onFailure { error ->
                        Log.e("ExploreViewModel", "Error: ${error.message}", error)
                        state = state.copy(
                            isLoading = false,
                            error = "Failed to fetch area data. Please try again later.",
                            isAreaFinalized = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Error finalizing area", e)
                state = state.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again later.",
                    isAreaFinalized = false
                )
            }
        }
    }

    private fun calculateCentroid(points: List<LatLng>): LatLng {
        val lat = points.map { it.latitude }.average()
        val lng = points.map { it.longitude }.average()
        return LatLng(lat, lng)
    }

    private fun getTreeRecommendations(soilType: String, climateZone: String): List<TreeRecommendation> {
        // This is a simplified version. You might want to move this to a separate repository
        return when {
            soilType == "Loam" && climateZone == "Tropical" -> listOf(
                TreeRecommendation(
                    species = "Acacia",
                    suitabilityScore = 0.9f,
                    description = "Fast-growing, drought-resistant tree",
                    growthRate = "Fast",
                    maintainanceLevel = "Low",
                    soilPreference = "Adaptable to most soils",
                    climatePreference = "Tropical and subtropical"
                ),
                // Add more recommendations based on conditions
            )
            // Add more conditions
            else -> emptyList()
        }
    }

    private fun saveArea() {
        viewModelScope.launch(dispatchers.io) {
            try {
                Log.d("ExploreViewModel", "Starting to save area")
                state = state.copy(isSaving = true)
                
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Log.e("ExploreViewModel", "No authenticated user found")
                    throw Exception("User not authenticated")
                }
                Log.d("ExploreViewModel", "Current user: ${currentUser.uid}")

                // Remove 'm' suffix and convert to Double
                val elevation = try {
                    state.altitude.removeSuffix("m").toDoubleOrNull() ?: 0.0
                } catch (e: Exception) {
                    Log.e("ExploreViewModel", "Error parsing elevation: ${state.altitude}", e)
                    0.0
                }

                val area = SavedArea(
                    userId = currentUser.uid,
                    name = state.areaName,
                    points = state.polygonPoints.map { GeoPoint(it.latitude, it.longitude) },
                    areaSize = state.areaSize,
                    soilType = state.soilType,
                    elevation = elevation,
                    climateZone = state.climateZone,
                    timestamp = System.currentTimeMillis()
                )
                
                Log.d("ExploreViewModel", "Created SavedArea object: $area")

                firestoreRepository.saveArea(area).onSuccess {
                    Log.d("ExploreViewModel", "Successfully saved area")
                    state = state.copy(
                        showSaveDialog = false,
                        areaName = "",
                        isSaving = false,
                        error = null
                    )
                    // Clear the polygon after successful save
                    onEvent(ExploreEvent.ClearPolygon)
                }.onFailure { error ->
                    Log.e("ExploreViewModel", "Error saving area", error)
                    state = state.copy(
                        error = "Failed to save area: ${error.message}",
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Error in saveArea", e)
                state = state.copy(
                    error = "Error saving area: ${e.message}",
                    isSaving = false
                )
            }
        }
    }
}