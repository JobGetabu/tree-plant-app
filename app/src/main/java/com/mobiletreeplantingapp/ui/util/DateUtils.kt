package com.mobiletreeplantingapp.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateWithTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun calculateDaysUntil(timestamp: Long): Int {
    val now = System.currentTimeMillis()
    return ((timestamp - now) / (1000 * 60 * 60 * 24)).toInt()
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 1.minutes.inWholeMilliseconds -> "just now"
        diff < 1.hours.inWholeMilliseconds -> {
            val minutes = diff / (1000 * 60)
            "$minutes min${if (minutes > 1) "s" else ""} ago"
        }
        diff < 1.days.inWholeMilliseconds -> {
            val hours = diff / (1000 * 60 * 60)
            "$hours hour${if (hours > 1) "s" else ""} ago"
        }
        diff < 7.days.inWholeMilliseconds -> {
            val days = diff / (1000 * 60 * 60 * 24)
            "$days day${if (days > 1) "s" else ""} ago"
        }
        else -> formatDate(timestamp)
    }
} 