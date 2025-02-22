package com.mobiletreeplantingapp.data.model
import com.google.firebase.firestore.GeoPoint

data class SavedArea(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val points: List<GeoPoint> = emptyList(),
    val areaSize: Double = 0.0,
    val soilType: String = "",
    val elevation: Double = 0.0,
    val climateZone: String = "",
    val timestamp: Long = System.currentTimeMillis()
)