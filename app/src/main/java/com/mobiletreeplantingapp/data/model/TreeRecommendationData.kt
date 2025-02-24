package com.mobiletreeplantingapp.data.model

data class TreeRecommendationData(
    val id: String = "",
    val species: String = "",
    val description: String = "",
    val growthRate: String = "",
    val maintainanceLevel: String = "",
    val soilPreference: String = "",
    val climatePreference: String = "",
    val minElevation: Double = 0.0,
    val maxElevation: Double = 3000.0,
    val suitableSoilTypes: List<String> = emptyList(),
    val suitableClimateZones: List<String> = emptyList(),
    val waterRequirement: String = "",
    val suitabilityScore: Float = 0.0f
) 