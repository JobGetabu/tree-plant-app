package com.mobiletreeplantingapp.data.remote

import com.mobiletreeplantingapp.data.model.ElevationData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ElevationApiService {
    @GET("v1/test-dataset")
    suspend fun getElevation(
        @Query("locations") locations: String
    ): Response<ElevationData>

    companion object {
        const val BASE_URL = "https://api.opentopodata.org/"
    }
} 