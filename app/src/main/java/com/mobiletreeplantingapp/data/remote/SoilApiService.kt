package com.mobiletreeplantingapp.data.remote

import com.mobiletreeplantingapp.data.model.SoilData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SoilApiService {
    @GET("v2/properties/query")
    suspend fun getSoilData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("property") properties: String = "clay,sand"
    ): Response<SoilData>

    companion object {
        const val BASE_URL = "https://rest.soilgrids.org/"
    }
} 