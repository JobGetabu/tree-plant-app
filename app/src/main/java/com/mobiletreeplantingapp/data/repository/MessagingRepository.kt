package com.mobiletreeplantingapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagingRepository @Inject constructor(
    private val messaging: FirebaseMessaging,
    private val firestore: FirebaseFirestore
) {
    suspend fun getFCMToken(): String? {
        return try {
            messaging.token.await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun subscribeTopic(topic: String) {
        try {
            messaging.subscribeToTopic(topic).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun unsubscribeTopic(topic: String) {
        try {
            messaging.unsubscribeFromTopic(topic).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
} 