package com.mobiletreeplantingapp.data.model

import java.util.*

data class TreeRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val species: String,
    val suitabilityScore: Float,
    val description: String,
    val growthRate: String,
    val maintainanceLevel: String,
    val soilPreference: String,
    val climatePreference: String
)