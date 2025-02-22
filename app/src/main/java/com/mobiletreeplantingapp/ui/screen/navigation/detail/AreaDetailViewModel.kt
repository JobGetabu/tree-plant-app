package com.mobiletreeplantingapp.ui.screen.navigation.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mobiletreeplantingapp.data.model.SoilData
import com.mobiletreeplantingapp.data.model.TreeRecommendation
import com.mobiletreeplantingapp.data.remote.SoilApiService
import com.mobiletreeplantingapp.data.repository.CoroutineDispatchers
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AreaDetailViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val soilApiService: SoilApiService,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {
    var state by mutableStateOf(AreaDetailState())
        private set

    fun loadAreaDetails(areaId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            firestoreRepository.getAreaById(areaId)
                .catch { e ->
                    state = state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
                .collect { result ->
                    result.onSuccess { area ->
                        // Get the centroid of the area for soil data
                        val centroid = area.points.let { points ->
                            val lat = points.map { it.latitude }.average()
                            val lng = points.map { it.longitude }.average()
                            LatLng(lat, lng)
                        }
                        
                        try {
                            val soilResponse = soilApiService.getSoilData(
                                latitude = centroid.latitude,
                                longitude = centroid.longitude
                            )

                            if (soilResponse.isSuccessful && soilResponse.body() != null) {
                                val soilData = soilResponse.body()!!
                                val recommendations = getTreeRecommendations(
                                    soilData = soilData,
                                    elevation = area.elevation,
                                    climateZone = area.climateZone
                                )
                                
                                state = state.copy(
                                    area = area,
                                    soilData = soilData,
                                    treeRecommendations = recommendations,
                                    isLoading = false,
                                    error = null
                                )
                            } else {
                                throw Exception("Failed to fetch soil data")
                            }
                        } catch (e: Exception) {
                            // Handle timeout specifically
                            if (e is java.net.SocketTimeoutException || e.message?.contains("No soil data available") == true) {
                                state = state.copy(
                                    area = area,
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
                                    isLoading = false
                                )
                            } else {
                                state = state.copy(
                                    area = area,
                                    error = "Error loading soil data: ${e.message}",
                                    isLoading = false
                                )
                            }
                        }
                    }.onFailure { error ->
                        state = state.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun getTreeRecommendations(
        soilData: SoilData,
        elevation: Double,
        climateZone: String
    ): List<TreeRecommendation> {
        // Check if soil data is valid
        if (soilData.properties.layers.isEmpty()) {
            Log.d("AreaDetailViewModel", "No soil data available")
            return listOf(
                TreeRecommendation(
                    species = "No Recommendations",
                    suitabilityScore = 0f,
                    description = "No soil data available to generate recommendations.",
                    growthRate = "-",
                    maintainanceLevel = "-",
                    soilPreference = "-",
                    climatePreference = "-"
                )
            )
        }

        try {
            // Get the top layer (0-30cm) values for each property
            val clayLayer = soilData.properties.layers.find { it.name == "clay" }
            val sandLayer = soilData.properties.layers.find { it.name == "sand" }
            val siltLayer = soilData.properties.layers.find { it.name == "silt" }
            val phLayer = soilData.properties.layers.find { it.name == "phh2o" }

            // Get mean values for the top layer (0-30cm)
            val clayContent = clayLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0
            val sandContent = sandLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0
            val siltContent = siltLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0
            val pH = phLayer?.depths?.firstOrNull()?.values?.mean?.div(10) ?: 0.0

            Log.d("AreaDetailViewModel", "Soil composition: Clay=$clayContent%, Sand=$sandContent%, Silt=$siltContent%, pH=$pH")
            
            // Determine soil texture
            val soilTexture = when {
                clayContent > 40 -> "clay"
                sandContent > 50 -> "sandy"
                else -> "loamy"
            }

            // Determine water retention based on soil composition
            val waterRetention = when {
                clayContent > 35 -> "high"
                clayContent > 25 -> "medium"
                else -> "low"
            }

            // Base recommendations on soil type and water retention
            val recommendations = when (soilTexture) {
                "sandy" -> when (waterRetention) {
                    "low" -> listOf(
                        createTreeRecommendation("Pine", 0.9f, "Drought-resistant conifer", "Fast", "Low"),
                        createTreeRecommendation("Juniper", 0.85f, "Hardy evergreen shrub/tree", "Slow", "Very Low"),
                        createTreeRecommendation("Acacia", 0.8f, "Drought-tolerant deciduous tree", "Medium", "Low"),
                        createTreeRecommendation("Desert Willow", 0.75f, "Desert-adapted flowering tree", "Medium", "Low")
                    )
                    "medium" -> listOf(
                        createTreeRecommendation("Oak", 0.9f, "Strong hardwood tree", "Slow", "Medium"),
                        createTreeRecommendation("Maple", 0.85f, "Versatile deciduous tree", "Medium", "Medium"),
                        createTreeRecommendation("Birch", 0.8f, "Elegant white-barked tree", "Fast", "Medium"),
                        createTreeRecommendation("Eucalyptus", 0.75f, "Fast-growing aromatic tree", "Very Fast", "Low")
                    )
                    else -> listOf(
                        createTreeRecommendation("Willow", 0.9f, "Water-loving tree", "Fast", "High"),
                        createTreeRecommendation("Poplar", 0.85f, "Fast-growing shade tree", "Very Fast", "Medium"),
                        createTreeRecommendation("Cherry", 0.8f, "Flowering fruit tree", "Medium", "Medium"),
                        createTreeRecommendation("Magnolia", 0.75f, "Flowering ornamental tree", "Slow", "Medium")
                    )
                }
                "clay" -> when (waterRetention) {
                    "low" -> listOf(
                        createTreeRecommendation("Oak", 0.9f, "Deep-rooted hardwood", "Slow", "Medium"),
                        createTreeRecommendation("Maple", 0.85f, "Adaptable deciduous tree", "Medium", "Medium"),
                        createTreeRecommendation("Honey Locust", 0.8f, "Tough urban tree", "Fast", "Low"),
                        createTreeRecommendation("Ginkgo", 0.75f, "Ancient species, very hardy", "Slow", "Low")
                    )
                    "medium" -> listOf(
                        createTreeRecommendation("Elm", 0.9f, "Classic shade tree", "Medium", "Medium"),
                        createTreeRecommendation("Linden", 0.85f, "Fragrant flowering tree", "Medium", "Medium"),
                        createTreeRecommendation("Hackberry", 0.8f, "Native hardy tree", "Fast", "Low"),
                        createTreeRecommendation("Kentucky Coffeetree", 0.75f, "Adaptable urban tree", "Medium", "Low")
                    )
                    else -> listOf(
                        createTreeRecommendation("River Birch", 0.9f, "Water-tolerant tree", "Fast", "Medium"),
                        createTreeRecommendation("Sweet Gum", 0.85f, "Colorful fall foliage", "Medium", "Medium"),
                        createTreeRecommendation("Red Maple", 0.8f, "Colorful adaptable tree", "Fast", "Medium"),
                        createTreeRecommendation("Dawn Redwood", 0.75f, "Ancient species, fast-growing", "Fast", "Medium")
                    )
                }
                else -> when (waterRetention) { // Loamy soil
                    "low" -> listOf(
                        createTreeRecommendation("Maple", 0.95f, "Versatile hardwood", "Medium", "Medium"),
                        createTreeRecommendation("Oak", 0.9f, "Long-lived shade tree", "Slow", "Medium"),
                        createTreeRecommendation("Dogwood", 0.85f, "Flowering ornamental", "Slow", "Medium"),
                        createTreeRecommendation("Redbud", 0.8f, "Spring flowering tree", "Medium", "Low")
                    )
                    "medium" -> listOf(
                        createTreeRecommendation("Cherry", 0.95f, "Ornamental fruit tree", "Medium", "Medium"),
                        createTreeRecommendation("Birch", 0.9f, "Distinctive bark tree", "Fast", "Medium"),
                        createTreeRecommendation("Magnolia", 0.85f, "Large flowering tree", "Slow", "Medium"),
                        createTreeRecommendation("Serviceberry", 0.8f, "Multi-season interest", "Medium", "Low")
                    )
                    else -> listOf(
                        createTreeRecommendation("Beech", 0.95f, "Majestic shade tree", "Slow", "Medium"),
                        createTreeRecommendation("Tulip Tree", 0.9f, "Tall flowering tree", "Fast", "Medium"),
                        createTreeRecommendation("Sweetgum", 0.85f, "Fall color champion", "Fast", "Medium"),
                        createTreeRecommendation("Yellowwood", 0.8f, "Rare flowering tree", "Medium", "Medium")
                    )
                }
            }

            // Filter recommendations based on elevation and climate zone
            val filteredRecommendations = recommendations.filter { recommendation ->
                val elevationSuitable = when (elevation) {
                    in 0.0..500.0 -> true  // Low elevation, most trees suitable
                    in 500.0..1500.0 -> recommendation.species !in listOf("Cherry", "Magnolia", "Sweetgum")
                    else -> recommendation.species in listOf("Pine", "Juniper", "Oak", "Maple") // High elevation
                }

                val climateSuitable = when (climateZone.lowercase()) {
                    "tropical" -> recommendation.species in listOf("Acacia", "Eucalyptus", "Magnolia", "Sweet Gum")
                    "temperate" -> recommendation.species !in listOf("Acacia", "Desert Willow")
                    else -> true
                }

                elevationSuitable && climateSuitable
            }.sortedByDescending { it.suitabilityScore }

            return if (filteredRecommendations.isEmpty()) {
                Log.d("AreaDetailViewModel", "No tree recommendations found for the given soil conditions")
                listOf(
                    TreeRecommendation(
                        species = "No Recommendations",
                        suitabilityScore = 0f,
                        description = "No suitable trees found for the current soil conditions: " +
                                "Clay: ${String.format("%.1f", clayContent)}%, " +
                                "Sand: ${String.format("%.1f", sandContent)}%, " +
                                "pH: ${String.format("%.1f", pH)}",
                        growthRate = "-",
                        maintainanceLevel = "-",
                        soilPreference = "-",
                        climatePreference = "-"
                    )
                )
            } else {
                filteredRecommendations
            }
        } catch (e: Exception) {
            Log.e("AreaDetailViewModel", "Error generating tree recommendations", e)
            return listOf(
                TreeRecommendation(
                    species = "Error",
                    suitabilityScore = 0f,
                    description = "Could not generate recommendations: ${e.message}",
                    growthRate = "-",
                    maintainanceLevel = "-",
                    soilPreference = "-",
                    climatePreference = "-"
                )
            )
        }
    }

    private fun createTreeRecommendation(
        species: String,
        suitabilityScore: Float,
        description: String,
        growthRate: String,
        maintenanceLevel: String
    ) = TreeRecommendation(
        species = species,
        suitabilityScore = suitabilityScore,
        description = description,
        growthRate = growthRate,
        maintainanceLevel = maintenanceLevel,
        soilPreference = "Adaptable",
        climatePreference = "Various"
    )
}