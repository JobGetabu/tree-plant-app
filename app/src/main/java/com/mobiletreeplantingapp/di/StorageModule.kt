package com.mobiletreeplantingapp.di

import com.google.firebase.storage.FirebaseStorage
import com.mobiletreeplantingapp.data.repository.StorageRepository
import com.mobiletreeplantingapp.data.repository.StorageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideStorageRepository(
        impl: StorageRepositoryImpl
    ): StorageRepository = impl
} 