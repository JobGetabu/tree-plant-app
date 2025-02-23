package com.mobiletreeplantingapp.ui.screen.planting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.ui.screen.planting.components.PhotoGallery
import com.mobiletreeplantingapp.ui.screen.planting.components.Timeline
import com.mobiletreeplantingapp.ui.screen.planting.components.PlantingSteps
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
    val state = viewModel.state
    
    LaunchedEffect(treeId, species) {
        viewModel.loadTreeProgress(treeId, species)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planting Guide - $species") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress Overview
            ProgressOverview(
                progress = state.progress,
                modifier = Modifier.padding(16.dp)
            )

            // Tabs for Guide/Timeline/Photos
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

            when (selectedTab) {
                0 -> PlantingSteps(
                    steps = state.guideSteps,
                    onStepCompleted = viewModel::markStepCompleted
                )
                1 -> Timeline(progress = state.progress)
                2 -> PhotoGallery(
                    photos = state.progress.photos,
                    onAddPhoto = viewModel::addPhoto
                )
            }
        }
    }
}

@Composable
private fun ProgressOverview(
    progress: TreeProgress,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = progress.species,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = if (totalSteps > 0) {
                    progress.completedSteps.size.toFloat() / totalSteps
                } else {
                    0f
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            progress.nextMilestone?.let { milestone ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Next: ${milestone.title}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Due: ${formatDate(milestone.dueDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
} 