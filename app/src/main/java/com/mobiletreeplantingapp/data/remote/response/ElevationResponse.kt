package com.mobiletreeplantingapp.data.remote.response
data class ElevationResponse(
    val results: List<ElevationResult>
)

data class ElevationResult(
    val elevation: Double,
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
) 