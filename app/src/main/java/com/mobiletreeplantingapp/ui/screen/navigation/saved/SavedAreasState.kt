package com.mobiletreeplantingapp.ui.screen.navigation.saved

import com.mobiletreeplantingapp.data.model.SavedArea

data class SavedAreasState(
    val areas: List<SavedArea> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 