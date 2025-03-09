package com.mobiletreeplantingapp.data.model

data class ForumPost(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 0,
    val commentCount: Int = 0,
    val isUpvotedByUser: Boolean = false,
    val isCurrentUserAuthor: Boolean = false,
    val imageUrl: String? = null
) {
    // Add a companion object for Firestore serialization
    companion object {
        fun fromFirestore(
            id: String,
            data: Map<String, Any?>,
            currentUserId: String
        ): ForumPost {
            return ForumPost(
                id = id,
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                authorId = data["authorId"] as? String ?: "",
                authorName = data["authorName"] as? String ?: "Anonymous",
                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time 
                    ?: System.currentTimeMillis(),
                upvotes = (data["upvotes"] as? Number)?.toInt() ?: 0,
                commentCount = (data["commentCount"] as? Number)?.toInt() ?: 0,
                isUpvotedByUser = false, // This will be set separately
                isCurrentUserAuthor = data["authorId"] as? String == currentUserId,
                imageUrl = data["imageUrl"] as? String
            )
        }
    }
}

