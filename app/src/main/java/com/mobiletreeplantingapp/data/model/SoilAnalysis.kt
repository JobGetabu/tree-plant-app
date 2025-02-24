package com.mobiletreeplantingapp.data.model

data class SoilAnalysis(
    val ph: Double,
    val nitrogen: Double, // ppm
    val phosphorus: Double, // ppm
    val potassium: Double, // ppm
    val texture: String, // e.g., "Sandy Loam", "Clay", etc.
    val drainage: String, // e.g., "Well-drained", "Poorly drained"
    val depth: Double // cm
) 