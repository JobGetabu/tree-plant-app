package com.mobiletreeplantingapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobiletreeplantingapp.data.remote.ElevationApiService
import com.mobiletreeplantingapp.data.remote.SoilApiService
import com.mobiletreeplantingapp.data.repository.GeographicalDataRepository
import com.mobiletreeplantingapp.data.local.dao.GeographicalDataDao
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import com.mobiletreeplantingapp.data.repository.FirestoreRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSoilApiService(okHttpClient: OkHttpClient): SoilApiService {
        return Retrofit.Builder()
            .baseUrl(SoilApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SoilApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideElevationApiService(okHttpClient: OkHttpClient): ElevationApiService {
        return Retrofit.Builder()
            .baseUrl(ElevationApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ElevationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeographicalDataRepository(
        geographicalDataDao: GeographicalDataDao,
        soilApiService: SoilApiService,
        elevationApiService: ElevationApiService
    ): GeographicalDataRepository {
        return GeographicalDataRepository(
            geographicalDataDao,
            soilApiService,
            elevationApiService
        )
    }



    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirestoreRepository(
            firestore: FirebaseFirestore,
            auth: FirebaseAuth
    ): FirestoreRepository = FirestoreRepositoryImpl(firestore, auth)
} 