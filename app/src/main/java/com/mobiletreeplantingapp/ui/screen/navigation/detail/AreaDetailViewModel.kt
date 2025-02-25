package com.mobiletreeplantingapp.ui.screen.navigation.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.SavedTree
import com.mobiletreeplantingapp.data.model.SoilAnalysis
import com.mobiletreeplantingapp.data.model.SoilData
import com.mobiletreeplantingapp.data.model.TreeRecommendation
import com.mobiletreeplantingapp.data.remote.SoilApiService
import com.mobiletreeplantingapp.data.repository.CoroutineDispatchers
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import com.mobiletreeplantingapp.navigation.Screen
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.TreeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    fun retryLoading(areaId: String) {
        loadArea(areaId)
    }

    fun dismissTransientError() {
        state = state.copy(transientError = null)
    }

    fun loadArea(areaId: String) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, error = null)
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
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = "Unable to load area data. Please try again later."
                )
                Log.e("AreaDetailViewModel", "Error loading area", e)
            }
        }
    }

    fun loadTreeRecommendations(area: SavedArea) {
        viewModelScope.launch {
            state = state.copy(isLoadingRecommendations = true, error = null)

            try {
                val centroid = area.points.let { points ->
                    val lat = points.map { it.latitude }.average()
                    val lng = points.map { it.longitude }.average()
                    LatLng(lat, lng)
                }

                Log.d("AreaDetailViewModel", "Fetching soil data for: $centroid")
                
                // Retry mechanism for soil API
                var soilData: SoilData? = null
                var retryCount = 0
                val maxRetries = 3
                
                while (soilData == null && retryCount < maxRetries) {
                    try {
                        val response = soilApiService.getSoilData(
                            latitude = centroid.latitude,
                            longitude = centroid.longitude
                        )
                        if (response.isSuccessful && response.body() != null) {
                            soilData = response.body()
                            Log.d("AreaDetailViewModel", "Received soil data: $soilData")
                        }
                    } catch (e: Exception) {
                        Log.e("AreaDetailViewModel", "Soil API attempt ${retryCount + 1} failed", e)
                        delay(1000L * (retryCount + 1)) // Exponential backoff
                    }
                    retryCount++
                }

                if (soilData == null) {
                    // Fallback to area's existing soil type if API fails
                    Log.d("AreaDetailViewModel", "Using fallback soil type: ${area.soilType}")
                    getRecommendationsWithSoilType(area.soilType, area)
                } else {
                    val soilAnalysis = createSoilAnalysis(soilData)
                    val soilType = determineSoilType(soilData)
                    Log.d("AreaDetailViewModel", "Determined soil type: $soilType")
                    
                    val updatedArea = area.copy(soilAnalysis = soilAnalysis)
                    state = state.copy(area = updatedArea, soilData = soilData)
                    
                    getRecommendationsWithSoilType(soilType, updatedArea)
                }
            } catch (e: Exception) {
                Log.e("AreaDetailViewModel", "Error loading recommendations", e)
                handleRecommendationError(e, area)
            }
        }
    }

    private suspend fun getRecommendationsWithSoilType(soilType: String, area: SavedArea) {
        firestoreRepository.getTreeRecommendations(
            soilType = soilType,
            elevation = area.elevation,
            climateZone = area.climateZone
        ).catch { e ->
            Log.e("AreaDetailViewModel", "Error getting recommendations", e)
            handleRecommendationError(e, area)
        }.collect { result ->
            result.onSuccess { recommendations ->
                if (recommendations.isEmpty()) {
                    // If no exact matches, try with more relaxed criteria
                    Log.d("AreaDetailViewModel", "No recommendations found, trying relaxed criteria")
                    getRelaxedRecommendations(area)
                } else {
                    state = state.copy(
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
                }
            }.onFailure { error ->
                handleRecommendationError(error, area)
            }
        }
    }

    private suspend fun getRelaxedRecommendations(area: SavedArea) {
        // Try with more general soil type and climate zone
        val generalizedSoilType = when {
            area.soilType.contains("sandy") -> "sandy"
            area.soilType.contains("clay") -> "clay"
            else -> "loamy"
        }
        
        val generalizedClimateZone = when {
            area.climateZone.contains("tropical") -> "tropical"
            area.climateZone.contains("arid") -> "semi-arid"
            area.climateZone.contains("highland") -> "highland"
            else -> area.climateZone
        }

        firestoreRepository.getTreeRecommendations(
            soilType = generalizedSoilType,
            elevation = area.elevation,
            climateZone = generalizedClimateZone
        ).collect { result ->
            result.onSuccess { recommendations ->
                state = state.copy(
                    treeRecommendations = recommendations.map { data ->
                        TreeRecommendation(
                            id = data.id,
                            species = data.species,
                            suitabilityScore = data.suitabilityScore * 0.9f, // Slightly lower confidence
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

    private fun createSoilAnalysis(soilData: SoilData): SoilAnalysis {
        val clayLayer = soilData.properties.layers.find { it.name == "clay" }
        val sandLayer = soilData.properties.layers.find { it.name == "sand" }
        val phLayer = soilData.properties.layers.find { it.name == "phh2o" }

        val clayContent = clayLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0
        val sandContent = sandLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0
        val ph = phLayer?.depths?.firstOrNull()?.values?.mean ?: 0.0

        // Calculate estimated nutrient levels based on soil composition
        val nitrogen = calculateNutrientLevel(clayContent, sandContent, 30.0)
        val phosphorus = calculateNutrientLevel(clayContent, sandContent, 20.0)
        val potassium = calculateNutrientLevel(clayContent, sandContent, 25.0)

        return SoilAnalysis(
            ph = ph,
            nitrogen = nitrogen,
            phosphorus = phosphorus,
            potassium = potassium,
            texture = determineTexture(clayContent, sandContent),
            drainage = determineDrainage(clayContent, sandContent),
            depth = 30.0  // Default depth from API query
        )
    }

    private fun calculateNutrientLevel(clay: Double, sand: Double, baseLevel: Double): Double {
        // Higher clay content generally means higher nutrient retention
        val clayFactor = clay / 100.0
        val sandFactor = sand / 100.0
        return (baseLevel * (1 + clayFactor - sandFactor)).coerceIn(5.0, 50.0)
    }

    private fun determineTexture(clay: Double, sand: Double): String {
        return when {
            clay > 40 -> "Clay"
            sand > 50 -> "Sandy"
            else -> "Loamy"
        }
    }

    private fun determineDrainage(clay: Double, sand: Double): String {
        return when {
            sand > 50 -> "Well-drained"
            clay > 40 -> "Poorly drained"
            else -> "Moderately drained"
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

    fun onStartPlanting(recommendation: TreeRecommendation) {
        viewModelScope.launch {
            val newTree = SavedTree(
                id = UUID.randomUUID().toString(),
                species = recommendation.species,
                notes = "Recommended tree - ${recommendation.description}",
                areaId = state.area?.id ?: return@launch,
                dateAdded = System.currentTimeMillis(),
                suitabilityScore = recommendation.suitabilityScore,
                growthRate = recommendation.growthRate,
                maintainanceLevel = recommendation.maintainanceLevel,
                soilPreference = recommendation.soilPreference,
                climatePreference = recommendation.climatePreference
            )

            firestoreRepository.saveTree(newTree)
                .onSuccess {
                    // Update state with new tree
                    state = state.copy(
                        savedTrees = state.savedTrees + newTree
                    )

                    // Navigate to planting guide
                    navigateToPlantingGuide(newTree.id, newTree.species)
                }
                .onFailure { error ->
                    state = state.copy(
                        error = "Failed to save tree: ${error.message}"
                    )
                }
        }
    }

    private fun navigateToPlantingGuide(treeId: String, species: String) {
        state = state.copy(
            navigationEvent = Screen.PlantingGuide.createRoute(
                treeId = treeId,
                species = species
            )
        )
    }

    fun resetNavigationEvent() {
        state = state.copy(navigationEvent = null)
    }

    fun deleteArea(areaId: String) {
        viewModelScope.launch {
            try {
                state = state.copy(isDeleting = true)
                // Delete all trees associated with this area first
                firestoreRepository.deleteTreesByAreaId(areaId).getOrThrow()
                // Then delete the area itself
                firestoreRepository.deleteArea(areaId).getOrThrow()
                // Navigate back after successful deletion
                state = state.copy(
                    isDeleting = false,
                    navigationEvent = Screen.Home.route
                )
            } catch (e: Exception) {
                handleError(e, isTransient = true)
            } finally {
                state = state.copy(isDeleting = false)
            }
        }
    }

    private fun handleError(error: Throwable, isTransient: Boolean = false) {
        val errorMessage = "An error occurred. Please try again later."
        if (isTransient) {
            state = state.copy(transientError = errorMessage)
        } else {
            state = state.copy(error = errorMessage)
        }
        Log.e("AreaDetailViewModel", "Error in AreaDetailViewModel", error)
    }
}

