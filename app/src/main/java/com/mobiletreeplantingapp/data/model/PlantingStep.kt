package com.mobiletreeplantingapp.data.model

data class PlantingStep(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val completedDate: Long? = null,
    val imageUrl: String? = null
) 