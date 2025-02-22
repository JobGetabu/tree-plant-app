package com.mobiletreeplantingapp.ui.screen.navigation.explore

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mobiletreeplantingapp.data.repository.GeographicalDataRepository
import com.mobiletreeplantingapp.data.model.SoilProperties
import kotlinx.coroutines.flow.collectLatest
import com.mobiletreeplantingapp.data.model.GeographicalData
import com.mobiletreeplantingapp.data.repository.CoroutineDispatchers
import android.util.Log

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val geographicalDataRepository: GeographicalDataRepository,
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
                    isAreaFinalized = false
                )
            }
            is ExploreEvent.UpdateUserLocation -> {
                state = state.copy(userLocation = event.location)
            }
            is ExploreEvent.FinalizeArea -> {
                if (state.polygonPoints.size >= 3) {
                    finalizeArea()
                }
            }
            is ExploreEvent.ToggleBottomSheet -> {
                state = state.copy(isBottomSheetVisible = !state.isBottomSheetVisible)
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
            state = state.copy(isLoading = true, showBottomSheet = true)
            
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
                            soilType = "Data unavailable",
                            altitude = "Data unavailable",
                            climateZone = "Data unavailable",
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Error finalizing area", e)
                state = state.copy(
                    error = e.message,
                    soilType = "Data unavailable",
                    altitude = "Data unavailable",
                    climateZone = "Data unavailable",
                    isLoading = false
                )
            }
        }
    }

    private fun calculateCentroid(points: List<LatLng>): LatLng {
        val lat = points.map { it.latitude }.average()
        val lng = points.map { it.longitude }.average()
        return LatLng(lat, lng)
    }
} 