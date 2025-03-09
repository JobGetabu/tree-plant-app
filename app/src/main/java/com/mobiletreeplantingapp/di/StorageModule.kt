package com.mobiletreeplantingapp.di

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.mobiletreeplantingapp.data.repository.StorageRepository
import com.mobiletreeplantingapp.data.repository.StorageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    
    // Firebase Storage bucket URL
    private const val STORAGE_BUCKET_URL = "gs://fir-nodejs-api.appspot.com"
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance(STORAGE_BUCKET_URL)
    }
    
    @Provides
    @Singleton
    fun provideStorageRepository(
        @ApplicationContext context: Context,
        storage: FirebaseStorage
    ): StorageRepository {
        return StorageRepositoryImpl(context, storage)
    }
} 