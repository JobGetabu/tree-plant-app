package com.mobiletreeplantingapp.data.model

data class TimelineEvent(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: Long,
    val type: EventType,
    val imageUrl: String? = null
) {
    enum class EventType {
        PLANTING,
        WATERING,
        PRUNING,
        FERTILIZING,
        MILESTONE,
        PHOTO
    }
} 