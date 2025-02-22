package com.mobiletreeplantingapp.ui.screen.navigation.explore

import com.google.android.gms.maps.model.LatLng

sealed class ExploreEvent {
    data class AddPolygonPoint(val point: LatLng) : ExploreEvent()
    data object ClearPolygon : ExploreEvent()
    data class UpdateUserLocation(val location: LatLng) : ExploreEvent()
    data object ToggleBottomSheet : ExploreEvent()
    data object FinalizeArea : ExploreEvent()
    data class UpdateAreaName(val name: String) : ExploreEvent()
    object ShowSaveDialog : ExploreEvent()
    object DismissSaveDialog : ExploreEvent()
    object SaveArea : ExploreEvent()
} 