package com.mobiletreeplantingapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) : StorageRepository {

    private val storageRef = storage.reference

    override suspend fun uploadTreePhoto(treeId: String, photoUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting photo upload for tree: $treeId")
            
            // Validate inputs
            if (treeId.isBlank()) {
                throw IllegalArgumentException("Tree ID cannot be empty")
            }

            // Create organized file structure
            val timestamp = System.currentTimeMillis()
            val fileName = "photo_$timestamp.jpg"
            val photoRef = storageRef
                .child("trees")
                .child(treeId)
                .child("photos")
                .child(fileName)
            
            Log.d(TAG, "Uploading to path: ${photoRef.path}")

            // Read and compress the image
            val bitmap = context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: throw IllegalStateException("Could not read file")

            // Compress image
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val compressedBytes = baos.toByteArray()

            // Upload bytes
            val uploadTask = photoRef.putBytes(compressedBytes)
            
            // Monitor upload
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.d(TAG, "Upload is $progress% done")
            }

            // Wait for completion
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = photoRef.downloadUrl.await().toString()
            Log.d(TAG, "Upload successful. Download URL: $downloadUrl")

            // Cleanup
            bitmap.recycle()
            baos.close()
            
            return@withContext downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo. TreeId: $treeId, Error: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getPhotosForTree(treeId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            if (treeId.isBlank()) {
                Log.e(TAG, "getPhotosForTree: Tree ID is blank")
                return@withContext emptyList()
            }

            Log.d(TAG, "Starting to fetch photos for tree: $treeId")
            
            val photosRef = storageRef
                .child("trees")
                .child(treeId)
                .child("photos")

            Log.d(TAG, "Created reference: ${photosRef.path}")

            // List all files in the tree's photos directory
            val result = photosRef.listAll().await()
            Log.d(TAG, "ListAll completed. Found ${result.items.size} photos")
            
            if (result.items.isEmpty()) {
                Log.d(TAG, "No photos found for tree: $treeId")
                return@withContext emptyList()
            }

            // Get download URLs for all photos
            val urls = result.items.mapNotNull { item ->
                try {
                    Log.d(TAG, "Fetching URL for item: ${item.name}")
                    val url = item.downloadUrl.await().toString()
                    Log.d(TAG, "Got URL: $url")
                    url
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get URL for photo: ${item.name}", e)
                    null
                }
            }
            
            Log.d(TAG, "Successfully fetched ${urls.size} photo URLs for tree: $treeId")
            urls
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching photos for tree: $treeId", e)
            emptyList()
        }
    }

    override suspend fun deleteTreePhoto(photoUrl: String) {
        try {
            val photoRef = storage.getReferenceFromUrl(photoUrl)
            photoRef.delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete photo: ${e.message}")
        }
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

    override suspend fun uploadProfileImage(photoUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid 
                ?: throw IllegalStateException("User not authenticated")
            
            Log.d(TAG, "Starting profile image upload for user: $userId")
            
            // Create organized file structure
            val timestamp = System.currentTimeMillis()
            val fileName = "profile_$timestamp.jpg"
            val photoRef = storageRef
                .child("users")
                .child(userId)
                .child("profile")
                .child(fileName)
            
            Log.d(TAG, "Uploading to path: ${photoRef.path}")

            // Read and compress the image
            val bitmap = context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: throw IllegalStateException("Could not read file")

            // Compress image
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val compressedBytes = baos.toByteArray()

            // Upload bytes
            val uploadTask = photoRef.putBytes(compressedBytes)
            
            // Monitor upload
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.d(TAG, "Upload is $progress% done")
            }

            // Wait for completion
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = photoRef.downloadUrl.await().toString()
            Log.d(TAG, "Profile image upload successful. Download URL: $downloadUrl")

            // Cleanup
            bitmap.recycle()
            baos.close()
            
            return@withContext downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image: ${e.message}", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "StorageRepositoryImpl"
    }
} 