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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
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
import kotlin.random.Random
import androidx.compose.material.icons.filled.Park
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.rememberMarkerState
import com.mobiletreeplantingapp.R
import android.content.Context
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
                                modifier = Modifier.fillMaxWidth(),
                                viewModel = viewModel // Pass ViewModel to AreaMap
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
    modifier: Modifier = Modifier,
    viewModel: AreaDetailViewModel = hiltViewModel() // Add ViewModel parameter
) {
    val state = viewModel.state // Get state from ViewModel
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isMapReady by remember { mutableStateOf(false) }
    var treeIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    
    val polygonPoints = area.points.map { LatLng(it.latitude, it.longitude) }
    
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

    // Generate random points within the polygon based on the number of trees
    val treeMarkers = remember(state.savedTrees.size) {
        generateRandomPoints(polygonPoints, state.savedTrees.size)
    }

    Card(
        modifier = modifier.height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.HYBRID),
                onMapLoaded = {
                    scope.launch {
                        // Initialize tree icon after map is loaded
                        treeIcon = bitmapDescriptorFromVector(context, R.drawable.ic_tree_marker)
                        isMapReady = true
                    }
                }
            ) {
                // Draw area polygon
                Polygon(
                    points = polygonPoints,
                    fillColor = Color(0x4DFFD700),
                    strokeColor = Color(0xFFFFD700),
                    strokeWidth = 2f
                )

                // Only show markers when map and icon are ready
                if (isMapReady && treeIcon != null) {
                    treeMarkers.forEach { treePosition ->
                        Marker(
                            state = rememberMarkerState(position = treePosition),
                            icon = treeIcon,
                            title = "Tree"
                        )
                    }
                }
            }

            // Show tree count overlay
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "Trees: ${state.savedTrees.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun generateRandomPoints(polygonPoints: List<LatLng>, count: Int): List<LatLng> {
    if (count == 0 || polygonPoints.size < 3) return emptyList()

    val bounds = getBoundingBox(polygonPoints)
    val points = mutableListOf<LatLng>()
    
    while (points.size < count) {
        val lat = Random.nextDouble(bounds.first.latitude, bounds.second.latitude)
        val lng = Random.nextDouble(bounds.first.longitude, bounds.second.longitude)
        val point = LatLng(lat, lng)
        
        if (isPointInPolygon(point, polygonPoints)) {
            points.add(point)
        }
    }
    
    return points
}

private fun getBoundingBox(points: List<LatLng>): Pair<LatLng, LatLng> {
    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    var minLng = Double.POSITIVE_INFINITY
    var maxLng = Double.NEGATIVE_INFINITY

    points.forEach { point ->
        minLat = minOf(minLat, point.latitude)
        maxLat = maxOf(maxLat, point.latitude)
        minLng = minOf(minLng, point.longitude)
        maxLng = maxOf(maxLng, point.longitude)
    }

    return Pair(
        LatLng(minLat, minLng), // Southwest corner
        LatLng(maxLat, maxLng)  // Northeast corner
    )
}

private fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
    var inside = false
    var j = polygon.size - 1
    
    for (i in polygon.indices) {
        if ((polygon[i].longitude > point.longitude) != (polygon[j].longitude > point.longitude) &&
            point.latitude < (polygon[j].latitude - polygon[i].latitude) * 
            (point.longitude - polygon[i].longitude) / 
            (polygon[j].longitude - polygon[i].longitude) + polygon[i].latitude
        ) {
            inside = !inside
        }
        j = i
    }
    
    return inside
}

private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    return try {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bm = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bm)
        drawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bm)
    } catch (e: Exception) {
        null
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