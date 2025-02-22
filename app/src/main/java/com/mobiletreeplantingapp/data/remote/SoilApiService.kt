package com.mobiletreeplantingapp.data.remote

import com.mobiletreeplantingapp.data.model.SoilData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SoilApiService {
    @GET("soilgrids/v2.0/properties/query")
    suspend fun getSoilData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("properties") properties: String = "clay,sand,silt,phh2o",
        @Query("depths") depths: String = "0-30",
        @Query("values") values: String = "mean"
    ): Response<SoilData>

    companion object {
        const val BASE_URL = "https://rest.isric.org/"
    }
} 