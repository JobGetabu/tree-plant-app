package com.mobiletreeplantingapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobiletreeplantingapp.R
import com.mobiletreeplantingapp.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting reminder work")
        
        val title = inputData.getString("title") ?: return Result.failure()
        val message = inputData.getString("message") ?: return Result.failure()
        val treeId = inputData.getString("treeId") ?: return Result.failure()

        Log.d(TAG, "Showing notification for tree: $treeId")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Message: $message")

        createNotificationChannel()
        showNotification(title, message, treeId)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tree Care Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for tree care and maintenance"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun showNotification(title: String, message: String, treeId: String) {
        // Create an intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("treeId", treeId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tree)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .build()

        val notificationId = treeId.hashCode()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown with ID: $notificationId")
    }

    companion object {
        const val CHANNEL_ID = "tree_care_channel"
        private const val TAG = "ReminderWorker"
    }
} 