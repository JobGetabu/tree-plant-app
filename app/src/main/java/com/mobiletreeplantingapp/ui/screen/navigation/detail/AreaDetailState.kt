package com.mobiletreeplantingapp.ui.screen.navigation.detail

import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.SavedTree
import com.mobiletreeplantingapp.data.model.TreeRecommendation
import com.mobiletreeplantingapp.data.model.SoilData

data class AreaDetailState(
    val area: SavedArea? = null,
    val soilData: SoilData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val treeRecommendations: List<TreeRecommendation> = emptyList(),
    val isLoadingRecommendations: Boolean = false,
    val isLoadingArea: Boolean = false,
    val savedTrees: List<SavedTree> = emptyList(),
    val isLoadingTrees: Boolean = false,
    val showAddTreeDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val treeToEdit: SavedTree? = null

    )


