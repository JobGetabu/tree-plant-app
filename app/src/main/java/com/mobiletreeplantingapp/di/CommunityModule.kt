package com.mobiletreeplantingapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobiletreeplantingapp.data.repository.CommunityRepository
import com.mobiletreeplantingapp.data.repository.CommunityRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Provides
    @Singleton
    fun provideCommunityRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CommunityRepository {
        return CommunityRepositoryImpl(firestore, auth)
    }
} 