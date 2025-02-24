package com.mobiletreeplantingapp.data.model
import com.google.firebase.firestore.GeoPoint

data class SavedArea(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val points: List<GeoPoint> = emptyList(),
    val areaSize: Double = 0.0,
    val elevation: Double = 0.0,
    val slope: Double = 0.0,
    val soilType: String = "",
    val climateZone: String = "",
    val soilAnalysis: SoilAnalysis = SoilAnalysis(0.0, 0.0, 0.0, 0.0, "", "", 0.0),
    val timestamp: Long = System.currentTimeMillis()
)