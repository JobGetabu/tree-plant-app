package com.mobiletreeplantingapp.ui.screen.planting.components

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoGallery(
    photos: List<String>,
    onAddPhoto: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                onAddPhoto(uri)
            }
        }
    }


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos) { photoUrl ->
            PhotoItem(
                photoUrl = photoUrl,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
        }

        item {
            AddPhotoButton(
                onClick = {
                    when {
                        cameraPermissionState.status.isGranted -> {
                            val uri = context.createImageUri()
                            photoUri = uri
                            cameraLauncher.launch(uri)
                        }
                        else -> {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
                },
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PhotoItem(
    photoUrl: String,
    modifier: Modifier = Modifier
) {
    var showFullscreen by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Tree photo",
            modifier = Modifier
                .fillMaxSize()
                .clickable { showFullscreen = true },
            contentScale = ContentScale.Crop
        )
    }

    if (showFullscreen) {
        Dialog(
            onDismissRequest = { showFullscreen = false }
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Full screen tree photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun AddPhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add photo",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Extension function to create image URI
private fun Context.createImageUri(): Uri {
    val imageFile = File(
        cacheDir,
        "tree_photo_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(
        this,
        "${packageName}.provider",
        imageFile
    )
} 