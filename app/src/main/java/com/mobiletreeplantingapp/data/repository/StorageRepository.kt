package com.mobiletreeplantingapp.data.repository

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    suspend fun uploadPhoto(treeId: String, imageUri: Uri): String
    suspend fun deletePhoto(photoUrl: String)
    fun getPhotosForTree(treeId: String): Flow<Result<List<String>>>
    suspend fun compressImage(uri: Uri): Uri
} 