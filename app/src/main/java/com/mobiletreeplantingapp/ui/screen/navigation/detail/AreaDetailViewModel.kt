package com.mobiletreeplantingapp.ui.screen.navigation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.SavedTree
import com.mobiletreeplantingapp.data.model.SoilData
import com.mobiletreeplantingapp.data.model.TreeRecommendation
import com.mobiletreeplantingapp.data.remote.SoilApiService
import com.mobiletreeplantingapp.data.repository.CoroutineDispatchers
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.TreeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class AreaDetailViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val soilApiService: SoilApiService,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {
    var state by mutableStateOf(AreaDetailState())
        private set

    fun loadArea(areaId: String) {
        viewModelScope.launch {
            state = state.copy(isLoadingArea = true)
            firestoreRepository.getAreaById(areaId)
                .catch { e ->
                    state = state.copy(
                        error = e.message,
                        isLoadingArea = false
                    )
                }
                .collect { result ->
                    result.onSuccess { area ->
                        state = state.copy(
                            area = area,
                            isLoadingArea = false,
                            error = null
                        )
                        // Load trees for this area
                        loadSavedTrees(areaId)
                    }.onFailure { error ->
                        state = state.copy(
                            error = error.message,
                            isLoadingArea = false
                        )
                    }
                }
        }
    }

    fun loadTreeRecommendations(area: SavedArea) {
        viewModelScope.launch {
            state = state.copy(isLoadingRecommendations = true)
            
            try {
                // Get the centroid of the area for soil data
                val centroid = area.points.let { points ->
                    val lat = points.map { it.latitude }.average()
                    val lng = points.map { it.longitude }.average()
                    LatLng(lat, lng)
                }
                
                val soilResponse = soilApiService.getSoilData(
                    latitude = centroid.latitude,
                    longitude = centroid.longitude
                )

                if (soilResponse.isSuccessful && soilResponse.body() != null) {
                    val soilData = soilResponse.body()!!
                    
                    // Determine soil type from soil data
                    val soilType = determineSoilType(soilData)
                    
                    // Get recommendations from Firestore
                    firestoreRepository.getTreeRecommendations(
                        soilType = soilType,
                        elevation = area.elevation,
                        climateZone = area.climateZone
                    ).collect { result ->
                        result.onSuccess { recommendations ->
                            state = state.copy(
                                soilData = soilData,
                                treeRecommendations = recommendations.map { data ->
                                    TreeRecommendation(
                                        id = data.id,
                                        species = data.species,
                                        suitabilityScore = data.suitabilityScore,
                                        description = data.description,
                                        growthRate = data.growthRate,
                                        maintainanceLevel = data.maintainanceLevel,
                                        soilPreference = data.soilPreference,
                                        climatePreference = data.climatePreference
                                    )
                                },
                                isLoadingRecommendations = false,
                                error = null
                            )
                        }.onFailure { error ->
                            handleRecommendationError(error, area)
                        }
                    }
                } else {
                    throw Exception("Failed to fetch soil data")
                }
            } catch (e: Exception) {
                handleRecommendationError(e, area)
            }
        }
    }

    private fun determineSoilType(soilData: SoilData): String {
        val clayLayer = soilData.properties.layers.find { it.name == "clay" }
        val sandLayer = soilData.properties.layers.find { it.name == "sand" }
        
        val clayContent = clayLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0
        val sandContent = sandLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0

        return when {
            clayContent > 40 -> "clay"
            sandContent > 50 -> "sandy"
            else -> "loamy"
        }
    }

    private fun handleRecommendationError(e: Throwable, area: SavedArea) {
        if (e is java.net.SocketTimeoutException || e.message?.contains("No soil data available") == true) {
            state = state.copy(
                treeRecommendations = listOf(
                    TreeRecommendation(
                        species = "No Recommendations",
                        suitabilityScore = 0f,
                        description = "Request timed out. No suitable trees found.",
                        growthRate = "-",
                        maintainanceLevel = "-",
                        soilPreference = "-",
                        climatePreference = "-"
                    )
                ),
                isLoadingRecommendations = false
            )
        } else {
            state = state.copy(
                error = "Error loading soil data: ${e.message}",
                isLoadingRecommendations = false
            )
        }
    }

    fun onShowAddTreeDialog() {
        state = state.copy(showAddTreeDialog = true)
    }

    fun onDismissAddTreeDialog() {
        state = state.copy(showAddTreeDialog = false)
    }

    fun onAddCustomTree(treeData: TreeData) {
        viewModelScope.launch {
            val newTree = SavedTree(
                id = UUID.randomUUID().toString(),
                species = treeData.species,
                notes = treeData.notes,
                areaId = state.area?.id ?: return@launch,
                dateAdded = System.currentTimeMillis()
            )
            firestoreRepository.saveTree(newTree)
        }
    }

    fun onEditTree(tree: SavedTree) {
        state = state.copy(
            showEditDialog = true,
            treeToEdit = tree
        )
    }

    fun onDismissEditDialog() {
        state = state.copy(
            showEditDialog = false,
            treeToEdit = null
        )
    }

    fun onUpdateTree(updatedTree: SavedTree) {
        viewModelScope.launch {
            firestoreRepository.updateTree(updatedTree)
                .onSuccess {
                    state = state.copy(
                        showEditDialog = false,
                        treeToEdit = null
                    )
                }
                .onFailure { error ->
                    state = state.copy(
                        error = error.message
                    )
                }
        }
    }

    fun onDeleteTree(tree: SavedTree) {
        viewModelScope.launch {
            firestoreRepository.deleteTree(tree.id)
        }
    }

    private fun loadSavedTrees(areaId: String) {
        viewModelScope.launch {
            state = state.copy(isLoadingTrees = true)
            firestoreRepository.getAreaTrees(areaId)
                .collect { result ->
                    result.onSuccess { trees ->
                        state = state.copy(
                            savedTrees = trees,
                            isLoadingTrees = false
                        )
                    }.onFailure { error ->
                        state = state.copy(
                            error = error.message,
                            isLoadingTrees = false
                        )
                    }
                }
        }
    }
}