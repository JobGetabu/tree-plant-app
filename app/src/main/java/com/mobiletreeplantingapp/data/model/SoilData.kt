package com.mobiletreeplantingapp.data.model

data class SoilData(
    val soilType: String,
    val properties: SoilProperties
)

data class SoilProperties(
    val clayContent: Double,
    val sandContent: Double,
    val organicMatter: Double,
    val ph: Double
) 