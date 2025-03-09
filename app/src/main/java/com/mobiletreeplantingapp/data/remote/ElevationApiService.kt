package com.mobiletreeplantingapp.data.remote

import com.mobiletreeplantingapp.data.remote.response.ElevationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ElevationApiService {
    @GET("api/v1/lookup")
    suspend fun getElevation(
        @Query("locations") locations: String
    ): Response<ElevationResponse>

    companion object {
        const val BASE_URL = "https://api.open-elevation.com/"
    }
} 