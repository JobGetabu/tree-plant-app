package com.mobiletreeplantingapp.ui.screen.planting

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobiletreeplantingapp.data.model.GuideStep
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.ui.screen.planting.components.PhotoGallery
import com.mobiletreeplantingapp.ui.screen.planting.components.PlantingSteps
import com.mobiletreeplantingapp.ui.screen.planting.components.Timeline
import com.mobiletreeplantingapp.ui.util.RequestNotificationPermission
import com.mobiletreeplantingapp.ui.util.formatDate

private const val totalSteps = 4 // Default number of steps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantingGuideScreen(
    treeId: String,
    species: String,
    viewModel: PlantingGuideViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Request notification permission
    RequestNotificationPermission(
        onPermissionGranted = {
            // Permission granted, notifications can be scheduled
            viewModel.onNotificationPermissionGranted()
        }
    )

    // Initialize the tree progress when the screen loads
    LaunchedEffect(Unit) {
        Log.d("PlantingGuideScreen", "Initializing with treeId: $treeId, species: $species")
        viewModel.initializeTreeProgress(treeId, species)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planting Guide") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Show loading state
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }

            // Show error state
            if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.initializeTreeProgress(treeId, species) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Retry")
                    }
                }
                return@Box
            }

            // Main content
            Column(modifier = Modifier.fillMaxSize()) {
                // Progress Overview with guideSteps
                if (state.progress.treeId.isNotBlank()) {
                    ProgressOverview(
                        progress = state.progress,
                        guideSteps = state.guideSteps,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                // Tabs
                var selectedTab by remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Guide") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Timeline") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Photos") }
                    )
                }

                // Tab content
                when (selectedTab) {
                    0 -> PlantingSteps(
                        steps = state.guideSteps,
                        completedSteps = state.progress.completedSteps,
                        onStepCompleted = viewModel::markStepCompleted
                    )
                    1 -> Timeline(
                        progress = state.progress,
                        guideSteps = state.guideSteps
                    )
                    2 -> PhotoGallery(
                        photos = state.progress.photos,
                        onAddPhoto = viewModel::addPhoto,
                        onDeletePhoto = viewModel::deletePhoto,
                        isUploading = state.isUploading,
                        onLoadPhotos = { viewModel.loadPhotos(state.progress.treeId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressOverview(
    progress: TreeProgress,
    guideSteps: List<GuideStep>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = progress.species,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress section with actual total steps
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${progress.completedSteps.size}/${guideSteps.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = if (guideSteps.isNotEmpty()) {
                        progress.completedSteps.size.toFloat() / guideSteps.size
                    } else {
                        0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Next milestone section
            progress.nextMilestone?.let { milestone ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Next Milestone",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = milestone.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatDate(milestone.dueDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
} 