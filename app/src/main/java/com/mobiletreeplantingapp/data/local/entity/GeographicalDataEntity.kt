package com.mobiletreeplantingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geographical_data")
data class GeographicalDataEntity(
    @PrimaryKey
    val id: String, // Composite of lat/lng
    val latitude: Double,
    val longitude: Double,
    val soilType: String,
    val elevation: Double,
    val climateZone: String,
    val timestamp: Long = System.currentTimeMillis()
) 