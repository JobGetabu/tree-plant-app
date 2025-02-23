package com.mobiletreeplantingapp.data.model

data class UserStats(
    val treesPlanted: Int = 0,
    val co2Offset: Int = 0,
    val totalArea: Double = 0.0,
    val lastPlantingDate: Long? = null
) 