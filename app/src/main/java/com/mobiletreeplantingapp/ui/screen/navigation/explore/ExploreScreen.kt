package com.mobiletreeplantingapp.ui.screen.navigation.explore

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.mobiletreeplantingapp.navigation.Screen
import java.util.UUID

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    viewModel: ExploreViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val state = viewModel.state
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    viewModel.onEvent(ExploreEvent.UpdateUserLocation(latLng))
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            }
        }
    }

    // Handle bottom sheet dismissal
    LaunchedEffect(bottomSheetState.currentValue) {
        if (bottomSheetState.currentValue == SheetValue.Hidden) {
            viewModel.onEvent(ExploreEvent.ToggleBottomSheet)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.TERRAIN,
                isMyLocationEnabled = locationPermissionState.status.isGranted
            ),
            onMapClick = { latLng ->
                viewModel.onEvent(ExploreEvent.AddPolygonPoint(latLng))
            }
        ) {
            if (state.polygonPoints.isNotEmpty()) {
                Polygon(
                    points = state.polygonPoints,
                    fillColor = Color.Green.copy(alpha = 0.3f),
                    strokeColor = Color.Green
                )
            }
        }

        // Instructions card when no points are selected
        if (state.polygonPoints.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = "Draw Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap on the map to start drawing your planting area",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Area details card at the bottom
        if (state.polygonPoints.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selected Area",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Area Size: ${String.format("%.2f", state.areaSize)} hectares")
                    
                    if (state.isAreaFinalized) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Soil Type: ${state.soilType}")
                            Text("Altitude: ${state.altitude}")
                            Text("Climate Zone: ${state.climateZone}")
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.onEvent(ExploreEvent.ShowSaveDialog) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Area")
                            }
                        }
                    }
                }
            }
        }

        // Floating action buttons
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        state.userLocation?.let {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                        }
                    }
                }
            ) {
                Icon(Icons.Default.MyLocation, "My Location")
            }

            if (state.polygonPoints.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.onEvent(ExploreEvent.ClearPolygon) }
                ) {
                    Icon(Icons.Default.Clear, "Clear Selection")
                }
            }

            if (state.polygonPoints.size >= 3 && !state.isAreaFinalized) {
                FloatingActionButton(
                    onClick = { 
                        Log.d("ExploreScreen", "Finalizing area with ${state.polygonPoints.size} points")
                        viewModel.onEvent(ExploreEvent.FinalizeArea) 
                    }
                ) {
                    Icon(Icons.Default.Check, "Finalize Area")
                }
            }
        }

        // Only show bottom sheet if area is finalized and showBottomSheet is true
        if (state.isAreaFinalized && state.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    viewModel.onEvent(ExploreEvent.ToggleBottomSheet)
                },
                sheetState = bottomSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Area Details",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        DetailItem(
                            icon = Icons.Default.Landscape,
                            label = "Area Size",
                            value = "${String.format("%.2f", state.areaSize)} hectares"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailItem(
                            icon = Icons.Default.Terrain,
                            label = "Soil Type",
                            value = state.soilType
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailItem(
                            icon = Icons.Default.Height,
                            label = "Altitude",
                            value = state.altitude
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailItem(
                            icon = Icons.Default.WbSunny,
                            label = "Climate Zone",
                            value = state.climateZone
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.onEvent(ExploreEvent.ShowSaveDialog) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Area")
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // Save dialog
        if (state.showSaveDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(ExploreEvent.DismissSaveDialog) },
                title = { Text("Save Area") },
                text = {
                    OutlinedTextField(
                        value = state.areaName,
                        onValueChange = { viewModel.onEvent(ExploreEvent.UpdateAreaName(it)) },
                        label = { Text("Area Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onEvent(ExploreEvent.SaveArea) },
                        enabled = state.areaName.isNotBlank() && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onEvent(ExploreEvent.DismissSaveDialog) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


