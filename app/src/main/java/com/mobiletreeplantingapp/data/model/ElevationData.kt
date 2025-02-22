package com.mobiletreeplantingapp.data.model

data class ElevationData(
    val elevation: Double,
    val location: LocationData
)

data class LocationData(
    val latitude: Double,
    val longitude: Double
) 