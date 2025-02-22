package com.mobiletreeplantingapp.ui.screen.navigation.explore

import com.google.android.gms.maps.model.LatLng
import com.mobiletreeplantingapp.data.model.TreeRecommendation

data class ExploreState(
    val polygonPoints: List<LatLng> = emptyList(),
    val areaSize: Double = 0.0,
    val userLocation: LatLng? = null,
    val isBottomSheetVisible: Boolean = false,
    val soilType: String = "--",
    val altitude: String = "--",
    val climateZone: String = "--",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAreaFinalized: Boolean = false,
    val showBottomSheet: Boolean = false,
    val treeRecommendations: List<TreeRecommendation> = emptyList(),
    val isSaving: Boolean = false,
    val areaName: String = "",
    val showSaveDialog: Boolean = false
) 