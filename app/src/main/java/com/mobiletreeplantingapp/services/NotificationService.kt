package com.mobiletreeplantingapp.services

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mobiletreeplantingapp.data.model.NotificationPreferences
import com.mobiletreeplantingapp.data.model.SavedTree
import com.mobiletreeplantingapp.data.repository.PreferencesRepository
import com.mobiletreeplantingapp.ui.util.NotificationPermissionHandler
import com.mobiletreeplantingapp.workers.ReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationService @Inject constructor(
    private val context: Context,
    private val permissionHandler: NotificationPermissionHandler,
    private val preferencesRepository: PreferencesRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val workManager = WorkManager.getInstance(context)

    init {
        scope.launch {
            preferencesRepository.getNotificationPreferencesFlow().collect { preferences ->
                handlePreferenceChanges(preferences)
            }
        }
    }

    private fun handlePreferenceChanges(preferences: NotificationPreferences) {
        if (!preferences.wateringEnabled) cancelWateringReminders()
        if (!preferences.pruningEnabled) cancelPruningReminders()
        if (!preferences.fertilizingEnabled) cancelFertilizingReminders()
        if (!preferences.inspectionEnabled) cancelInspectionReminders()
        if (!preferences.growthCheckEnabled) cancelGrowthCheckReminders()
    }

    private fun cancelWateringReminders() {
        workManager.cancelAllWorkByTag("water_reminder")
    }

    private fun cancelPruningReminders() {
        workManager.cancelAllWorkByTag("pruning_reminder")
    }

    private fun cancelFertilizingReminders() {
        workManager.cancelAllWorkByTag("fertilizing_reminder")
    }

    private fun cancelInspectionReminders() {
        workManager.cancelAllWorkByTag("inspection_reminder")
    }

    private fun cancelGrowthCheckReminders() {
        workManager.cancelAllWorkByTag("growth_check_reminder")
    }

    fun scheduleAllReminders(tree: SavedTree, isTestMode: Boolean = false) {
        if (!permissionHandler.hasNotificationPermission()) {
            Log.w(TAG, "Notification permission not granted")
            return
        }

        scope.launch {
            try {
                val preferences = preferencesRepository.getNotificationPreferences()
                
                if (preferences.wateringEnabled) {
                    scheduleWateringReminders(tree, isTestMode)
                }
                if (preferences.pruningEnabled) {
                    schedulePruningReminders(tree, isTestMode)
                }
                if (preferences.fertilizingEnabled) {
                    scheduleFertilizingReminders(tree, isTestMode)
                }
                if (preferences.inspectionEnabled) {
                    scheduleInspectionReminders(tree, isTestMode)
                }
                if (preferences.growthCheckEnabled) {
                    scheduleGrowthCheckReminders(tree, isTestMode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling reminders", e)
            }
        }
    }

    fun scheduleWateringReminders(tree: SavedTree, isTestMode: Boolean = false) {
        val wateringDays = if (isTestMode) {
            listOf(1L, 2L, 3L, 4L) // Minutes for testing
        } else {
            listOf(7L, 14L, 21L, 28L, 35L, 42L) // Days for production
        }
        
        wateringDays.forEach { delay ->
            scheduleReminder(
                title = "Water your ${tree.species}",
                message = "Time to water your tree! Regular watering helps establish strong roots.",
                treeId = "${tree.id}_water_$delay",
                delay = delay,
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS,
                tag = "water_reminder"
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
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS,
                tag = "pruning_reminder"
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
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS,
                tag = "fertilizing_reminder"
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
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS,
                tag = "inspection_reminder"
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
                timeUnit = if (isTestMode) TimeUnit.MINUTES else TimeUnit.DAYS,
                tag = "growth_check_reminder"
            )
        }
    }

    private fun scheduleReminder(
        title: String,
        message: String,
        treeId: String,
        delay: Long,
        timeUnit: TimeUnit,
        tag: String
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
            .addTag(tag)
            .build()

        workManager.enqueue(reminderWork)
        Log.d(TAG, "Reminder work enqueued with ID: ${reminderWork.id}")
    }

    companion object {
        private const val TAG = "NotificationService"
    }
} 