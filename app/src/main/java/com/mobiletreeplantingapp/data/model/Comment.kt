package com.mobiletreeplantingapp.data.model

data class Comment(
    val id: String = "",
    val text: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCurrentUserAuthor: Boolean = false
) {
    companion object {
        fun fromFirestore(
            id: String,
            data: Map<String, Any?>,
            currentUserId: String
        ): Comment {
            return Comment(
                id = id,
                text = data["text"] as? String ?: "",
                authorId = data["authorId"] as? String ?: "",
                authorName = data["authorName"] as? String ?: "Anonymous",
                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time 
                    ?: System.currentTimeMillis(),
                isCurrentUserAuthor = data["authorId"] as? String == currentUserId
            )
        }
    }
} 