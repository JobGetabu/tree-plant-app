package com.mobiletreeplantingapp.services

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mobiletreeplantingapp.data.model.SavedTree
import com.mobiletreeplantingapp.workers.ReminderWorker
import com.mobiletreeplantingapp.ui.util.NotificationPermissionHandler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationService @Inject constructor(
    private val context: Context,
    private val permissionHandler: NotificationPermissionHandler
) {
    fun scheduleAllReminders(tree: SavedTree, isTestMode: Boolean = false) {
        if (!permissionHandler.hasNotificationPermission()) {
            Log.w(TAG, "Notification permission not granted")
            return
        }
        
        Log.d(TAG, "Scheduling all reminders for tree: ${tree.id}, test mode: $isTestMode")
        
        scheduleWateringReminders(tree, isTestMode)
        schedulePruningReminders(tree, isTestMode)
        scheduleFertilizingReminders(tree, isTestMode)
        scheduleInspectionReminders(tree, isTestMode)
        scheduleGrowthCheckReminders(tree, isTestMode)
    }

    fun scheduleWateringReminders(tree: SavedTree, isTestMode: Boolean = false) {
        val wateringDays = if (isTestMode) {
            listOf(2L, 4L, 6L, 8L) // Minutes for testing
        } else {
            listOf(7L, 14L, 21L, 28L, 35L, 42L) // Days for production
        }
        
        wateringDays.forEach { delay ->
            scheduleReminder(
                title = "Water your ${tree.species}",
                message = "Time to water your tree! Regular watering helps establish strong roots.",
                treeId = "${tree.id}_water_$delay",
                delay = delay,
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS
            )
        }
    }

    fun schedulePruningReminders(tree: SavedTree, isTestMode: Boolean = false) {
        val pruningDelays = if (isTestMode) {
            listOf(10L) // Minutes for testing
        } else {
            listOf(90L, 180L, 270L) // Days for production
        }

        pruningDelays.forEach { delay ->
            scheduleReminder(
                title = "Prune your ${tree.species}",
                message = "Time for pruning! Remove any dead or crossing branches for healthy growth.",
                treeId = "${tree.id}_prune_$delay",
                delay = delay,
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS
            )
        }
    }

    fun scheduleFertilizingReminders(tree: SavedTree, isTestMode: Boolean = false) {
        val fertilizingDelays = if (isTestMode) {
            listOf(12L) // Minutes for testing
        } else {
            listOf(30L, 90L, 180L) // Days for production
        }

        fertilizingDelays.forEach { delay ->
            scheduleReminder(
                title = "Fertilize your ${tree.species}",
                message = "Time to add nutrients! Proper fertilization promotes strong growth.",
                treeId = "${tree.id}_fertilize_$delay",
                delay = delay,
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS
            )
        }
    }

    fun scheduleInspectionReminders(tree: SavedTree, isTestMode: Boolean = false) {
        val inspectionDelays = if (isTestMode) {
            listOf(14L) // Minutes for testing
        } else {
            listOf(60L, 120L, 240L) // Days for production
        }

        inspectionDelays.forEach { delay ->
            scheduleReminder(
                title = "Inspect your ${tree.species}",
                message = "Time for a health check! Look for signs of disease or pest problems.",
                treeId = "${tree.id}_inspect_$delay",
                delay = delay,
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS
            )
        }
    }

    fun scheduleGrowthCheckReminders(tree: SavedTree, isTestMode: Boolean = false) {
        val growthCheckDelays = if (isTestMode) {
            listOf(16L) // Minutes for testing
        } else {
            listOf(45L, 90L, 180L, 365L) // Days for production
        }

        growthCheckDelays.forEach { delay ->
            scheduleReminder(
                title = "Check ${tree.species} Growth",
                message = "Time to measure growth! Track your tree's progress and take photos.",
                treeId = "${tree.id}_growth_$delay",
                delay = delay,
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS
            )
        }
    }

    private fun scheduleReminder(
        title: String,
        message: String,
        treeId: String,
        delay: Long,
        timeUnit: TimeUnit
    ) {
        Log.d(TAG, "Scheduling reminder: $title for tree: $treeId with delay: $delay ${timeUnit.name}")
        
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putString("treeId", treeId)
            .build()

        val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, timeUnit)
            .build()

        WorkManager.getInstance(context).enqueue(reminderWork)
        Log.d(TAG, "Reminder work enqueued with ID: ${reminderWork.id}")
    }

    companion object {
        private const val TAG = "NotificationService"
    }
} 