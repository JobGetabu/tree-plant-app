package com.mobiletreeplantingapp.data.repository

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    suspend fun uploadTreePhoto(treeId: String, photoUri: Uri): String
    suspend fun getPhotosForTree(treeId: String): List<String>
    suspend fun deleteTreePhoto(photoUrl: String)
    suspend fun compressImage(uri: Uri): Uri
} 