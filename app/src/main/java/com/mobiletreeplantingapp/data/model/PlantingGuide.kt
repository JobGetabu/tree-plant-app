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





