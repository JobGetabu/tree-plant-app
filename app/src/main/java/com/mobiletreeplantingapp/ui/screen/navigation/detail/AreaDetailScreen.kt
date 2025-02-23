package com.mobiletreeplantingapp.ui.screen.navigation.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mobiletreeplantingapp.ui.screen.navigation.detail.components.SavedTreeCard
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

    LaunchedEffect(areaId) {
        viewModel.loadArea(areaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Area Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                state.area?.let { area ->
                    LaunchedEffect(area) {
                        viewModel.loadTreeRecommendations(area)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        AreaMap(
                            area = area,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AreaInfoCard(area)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Recommended Trees",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        if (state.isLoadingRecommendations) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            if (state.treeRecommendations.isNotEmpty()) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.treeRecommendations) { recommendation ->
                                        TreeRecommendationCard(
                                            recommendation = recommendation,
                                            onStartPlanting = { selectedTree ->
                                                navController.navigate(
                                                    Screen.PlantingGuide.createRoute(
                                                        treeId = selectedTree.id,
                                                        species = selectedTree.species
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "No suitable trees found.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "My Trees",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        if (state.isLoadingTrees) {
                            CircularProgressIndicator()
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        FloatingActionButton(
                            onClick = { viewModel.onShowAddTreeDialog() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Add, "Add Custom Tree")
                        }
                    }

                    if (state.showAddTreeDialog) {
                        AddTreeDialog(
                            onDismiss = { viewModel.onDismissAddTreeDialog() },
                            onConfirm = { treeData -> 
                                viewModel.onAddCustomTree(treeData)
                            }
                        )
                    }
                }
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
        position = CameraPosition.fromLatLngZoom(center, 15f)
    }

    // Zoom to area when map is ready
    LaunchedEffect(center) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(center, 15f),
            durationMs = 1000
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
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                strokeColor = MaterialTheme.colorScheme.primary,
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