package com.mobiletreeplantingapp.ui.screen.navigation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.TreeRecommendation
import com.mobiletreeplantingapp.navigation.Screen
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.AddTreeDialog
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.TreeRecommendationCard
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.EditTreeDialog
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.SavedTreeCard
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.TopographyAndSoilCard
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDetailScreen(
    areaId: String,
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: AreaDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state

    // Handle navigation events
    LaunchedEffect(state.navigationEvent) {
        state.navigationEvent?.let { route ->
            navController.navigate(route)
            // Reset navigation event after handling
            viewModel.resetNavigationEvent()
        }
    }

    LaunchedEffect(areaId) {
        viewModel.loadArea(areaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${state.area?.name ?: ""} Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.state = viewModel.state.copy(showDeleteDialog = true) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Area",
                            tint = MaterialTheme.colorScheme.error
                        )
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
            if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "An error occurred while loading the data.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please try again later.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retryLoading(areaId) }
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                state.area?.let { area ->
                    // Show delete confirmation dialog
                    if (state.showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                viewModel.state = viewModel.state.copy(showDeleteDialog = false)
                            },
                            title = {
                                Text("Delete Area")
                            },
                            text = {
                                Text("Are you sure you want to delete '${area.name}'? This will also delete all trees planted in this area. This action cannot be undone.")
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deleteArea(area.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.state = viewModel.state.copy(showDeleteDialog = false)
                                    }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // Show loading indicator while deleting
                    if (state.isDeleting) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    LaunchedEffect(area) {
                        viewModel.loadTreeRecommendations(area)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AreaMap(
                                area = area,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            AreaInfoCard(area)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            TopographyAndSoilCard(
                                elevation = area.elevation,
                                slope = area.slope,
                                soilAnalysis = area.soilAnalysis
                            )
                        }

                        item {
                            Text(
                                text = "Recommended Trees",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        if (state.isLoadingRecommendations) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else {
                            if (state.treeRecommendations.isNotEmpty()) {
                                items(state.treeRecommendations) { recommendation ->
                                    TreeRecommendationCard(
                                        recommendation = recommendation,
                                        onStartPlanting = viewModel::onStartPlanting
                                    )
                                }
                            } else {
                                item {
                                    Text(
                                        text = "No suitable trees found.",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "My Trees",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        if (state.isLoadingTrees) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else {
                            if (state.savedTrees.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.onShowAddTreeDialog() },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Tree",
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .padding(8.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Add Your First Tree",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Tap here to start tracking your trees",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(state.savedTrees) { tree ->
                                    SavedTreeCard(
                                        tree = tree,
                                        onEdit = { viewModel.onEditTree(it) },
                                        onDelete = { viewModel.onDeleteTree(it) },
                                        onStartPlanting = {
                                            navController.navigate(
                                                Screen.PlantingGuide.createRoute(
                                                    treeId = it.id,
                                                    species = it.species
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Add some bottom padding
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                        }
                    }
                }
            }

            // Add Snackbar for transient errors
            if (state.transientError != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.dismissTransientError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(text = state.transientError)
                }
            }

            // FAB stays at the bottom
            FloatingActionButton(
                onClick = { viewModel.onShowAddTreeDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Custom Tree")
            }

            // Dialogs
            if (state.showAddTreeDialog) {
                AddTreeDialog(
                    onDismiss = { viewModel.onDismissAddTreeDialog() },
                    onConfirm = { treeData ->
                        viewModel.onAddCustomTree(treeData)
                    }
                )
            }

            if (state.showEditDialog && state.treeToEdit != null) {
                EditTreeDialog(
                    tree = state.treeToEdit,
                    onDismiss = { viewModel.onDismissEditDialog() },
                    onConfirm = { updatedTree ->
                        viewModel.onUpdateTree(updatedTree)
                    }
                )
            }
        }
    }
}

@Composable
private fun AreaMap(
    area: SavedArea,
    modifier: Modifier = Modifier
) {
    // Convert GeoPoints to LatLng for the map
    val polygonPoints = area.points.map { LatLng(it.latitude, it.longitude) }

    // Calculate center point of the area
    val center = if (polygonPoints.isNotEmpty()) {
        val latSum = polygonPoints.sumOf { it.latitude }
        val lngSum = polygonPoints.sumOf { it.longitude }
        LatLng(latSum / polygonPoints.size, lngSum / polygonPoints.size)
    } else {
        LatLng(0.0, 0.0)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 7f)
    }

    // Zoom to area when map is ready with smoother animation
    LaunchedEffect(center) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(center, 10f),
            durationMs = 3000  // Increased duration to 2 seconds
        )
    }

    Card(
        modifier = modifier.height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.HYBRID
            )
        ) {
            Polygon(
                points = polygonPoints,
                fillColor = Color(0x4DFFD700),  // Semi-transparent golden yellow
                strokeColor = Color(0xFFFFD700), // Solid golden yellow
                strokeWidth = 2f
            )
        }
    }
}

@Composable
private fun AreaInfoCard(
    area: SavedArea,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Area Information",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow("Size", "${String.format("%.2f", area.areaSize)} hectares")
            DetailRow("Soil Type", area.soilType)
            DetailRow("Elevation", "${area.elevation.toInt()}m")
            DetailRow("Climate Zone", area.climateZone)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}