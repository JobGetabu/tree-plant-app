package com.mobiletreeplantingapp.ui.screen.planting.components

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoGallery(
    photos: List<String>,
    isUploading: Boolean,
    onAddPhoto: (Uri) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onLoadPhotos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    // Permission states
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    
    // Activity result launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddPhoto(it) }
    }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { onAddPhoto(it) }
        }
    }
    

    LaunchedEffect(Unit) {
        Log.d("PhotoGallery", "Loading photos")
        onLoadPhotos()
        isLoading = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Add Photo Button - Always visible at the top
            AddPhotoButton(
                onClick = { showPhotoSourceDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content area
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading photos...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                photos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No photos yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(photos) { photoUrl ->
                            PhotoItem(
                                photoUrl = photoUrl,
                                onClick = { selectedPhotoUrl = photoUrl },
                                onLongClick = { onDeletePhoto(photoUrl) },
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Upload overlay
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Uploading photo...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Add Photo") },
            text = { Text("Choose photo source") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        if (cameraPermissionState.status.isGranted) {
                            tempImageUri = context.createImageUri()
                            cameraLauncher.launch(tempImageUri!!)
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
        )
    }

    // Photo preview dialog
    if (selectedPhotoUrl != null) {
        Dialog(
            onDismissRequest = { selectedPhotoUrl = null },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { selectedPhotoUrl = null }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedPhotoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full size photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                
                // Close button
                IconButton(
                    onClick = { selectedPhotoUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoItem(
    photoUrl: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Tree photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun AddPhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add photo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Photo")
        }
    }
}

// Extension function to create image URI
fun Context.createImageUri(): Uri {
    val timestamp = System.currentTimeMillis()
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "tree_photo_$timestamp.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
    }
    return contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IllegalStateException("Failed to create image URI")
} 