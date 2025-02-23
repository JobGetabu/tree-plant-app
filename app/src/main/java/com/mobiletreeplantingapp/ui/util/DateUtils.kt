package com.mobiletreeplantingapp.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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