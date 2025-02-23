package com.mobiletreeplantingapp.data.model

data class GuideStep(
    val id: Int,
    val title: String,
    val description: String,
    val videoUrl: String? = null,
    val isCompleted: Boolean = false,
    val required: Boolean = true,
    val estimatedTimeMinutes: Int = 15
)

data class TreeProgress(
     val treeId: String = "",
    val plantedDate: Long = 0L,
    val species: String = "",
    val completedSteps: List<Int> = emptyList(),
    val photos: List<String> = emptyList(),
    val nextMilestone: Milestone? = null
)

data class Milestone(
    val title: String,
    val description: String,
    val dueDate: Long,
    val type: MilestoneType,
    val isCompleted: Boolean = false
)

enum class MilestoneType {
    PLANTING,
    WATERING,
    PRUNING,
    FERTILIZING,
    MAINTENANCE
} 