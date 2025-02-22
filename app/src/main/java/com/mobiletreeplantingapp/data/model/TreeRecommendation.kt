package com.mobiletreeplantingapp.data.model

data class TreeRecommendation(
    val species: String,
    val suitabilityScore: Float,
    val description: String,
    val growthRate: String,
    val maintainanceLevel: String,
    val soilPreference: String,
    val climatePreference: String
)