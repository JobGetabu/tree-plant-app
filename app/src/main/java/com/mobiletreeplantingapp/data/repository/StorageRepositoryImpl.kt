package com.mobiletreeplantingapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) : StorageRepository {

    private val treesRef = storage.reference.child("trees")

    override suspend fun uploadPhoto(treeId: String, imageUri: Uri): String {
        return try {
            Log.d(TAG, "Starting photo upload for tree: $treeId")
            
            // Compress the image before uploading
            val compressedUri = compressImage(imageUri)
            
            // Create a unique filename
            val filename = "${UUID.randomUUID()}.jpg"
            val photoRef = treesRef.child(treeId).child(filename)
            
            // Upload the file
            val uploadTask = photoRef.putFile(compressedUri).await()
            
            // Get the download URL
            val downloadUrl = photoRef.downloadUrl.await().toString()
            Log.d(TAG, "Photo uploaded successfully. URL: $downloadUrl")
            
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo", e)
            throw e
        }
    }

    override suspend fun deletePhoto(photoUrl: String) {
        try {
            // Extract the path from the URL and get the reference
            val photoRef = storage.getReferenceFromUrl(photoUrl)
            photoRef.delete().await()
            Log.d(TAG, "Photo deleted successfully: $photoUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting photo", e)
            throw e
        }
    }

    override fun getPhotosForTree(treeId: String): Flow<Result<List<String>>> = callbackFlow {
        val photosRef = treesRef.child(treeId)
        
        try {
            // List all files in the tree's directory
            val result = photosRef.listAll().await()
            
            // Get download URLs for all photos
            val urls = result.items.map { it.downloadUrl.await().toString() }
            
            trySend(Result.success(urls))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching photos", e)
            trySend(Result.failure(e))
        }
        
        awaitClose()
    }

    override suspend fun compressImage(uri: Uri): Uri = withContext(Dispatchers.IO) {
        try {
            // Read the input stream from the URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            // Create a compressed JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            
            // Create a temporary file for the compressed image
            val tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            tempFile.writeBytes(outputStream.toByteArray())
            
            // Return the URI of the compressed file
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "StorageRepository"
    }
} 