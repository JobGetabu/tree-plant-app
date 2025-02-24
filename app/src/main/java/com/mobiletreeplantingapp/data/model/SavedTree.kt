package com.mobiletreeplantingapp.data.model

data class SavedTree(
    val id: String = "",
    val species: String = "",
    val notes: String = "",
    val areaId: String = "",
    val dateAdded: Long = 0,
    val suitabilityScore: Float = 0f,
    val growthRate: String = "",
    val maintainanceLevel: String = "",
    val soilPreference: String = "",
    val climatePreference: String = ""
) 