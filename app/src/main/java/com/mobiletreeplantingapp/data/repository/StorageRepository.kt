package com.mobiletreeplantingapp.data.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadTreePhoto(treeId: String, photoUri: Uri): String
    suspend fun getPhotosForTree(treeId: String): List<String>
    suspend fun deleteTreePhoto(photoUrl: String)
    suspend fun compressImage(uri: Uri): Uri
    suspend fun uploadProfileImage(photoUri: Uri): String
} 