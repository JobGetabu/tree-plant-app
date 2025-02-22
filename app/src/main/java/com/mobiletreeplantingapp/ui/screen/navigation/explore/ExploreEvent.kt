package com.mobiletreeplantingapp.ui.screen.navigation.explore

import com.google.android.gms.maps.model.LatLng

sealed class ExploreEvent {
    data class AddPolygonPoint(val point: LatLng) : ExploreEvent()
    data object ClearPolygon : ExploreEvent()
    data class UpdateUserLocation(val location: LatLng) : ExploreEvent()
    data object ToggleBottomSheet : ExploreEvent()
    data object FinalizeArea : ExploreEvent()
} 