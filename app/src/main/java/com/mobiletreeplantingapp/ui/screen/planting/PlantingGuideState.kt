package com.mobiletreeplantingapp.ui.screen.planting

import com.mobiletreeplantingapp.data.model.GuideStep
import com.mobiletreeplantingapp.data.model.TreeProgress

data class PlantingGuideState(
    val progress: TreeProgress = TreeProgress(
        treeId = "",
        plantedDate = 0L,
        species = "",
        completedSteps = emptyList(),
        photos = emptyList(),
    ),
    val guideSteps: List<GuideStep> = emptyList(),
    val isLoading: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false,
)


