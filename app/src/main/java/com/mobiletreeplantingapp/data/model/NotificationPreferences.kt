package com.mobiletreeplantingapp.data.model

data class NotificationPreferences(
    val wateringEnabled: Boolean = true,
    val pruningEnabled: Boolean = true,
    val fertilizingEnabled: Boolean = true,
    val inspectionEnabled: Boolean = true,
    val growthCheckEnabled: Boolean = true
) 