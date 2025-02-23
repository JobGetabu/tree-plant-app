package com.mobiletreeplantingapp.data.model

data class Article(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: ArticleCategory = ArticleCategory.OTHER,
    val imageUrl: String = "",
    val authorName: String = "Admin",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val isLiked: Boolean = false
) {
    companion object {
        fun fromFirestore(
            id: String,
            data: Map<String, Any?>,
            likedByCurrentUser: Boolean
        ): Article {
            return Article(
                id = id,
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                category = try {
                    ArticleCategory.valueOf(data["category"] as? String ?: ArticleCategory.OTHER.name)
                } catch (e: Exception) {
                    ArticleCategory.OTHER
                },
                imageUrl = data["imageUrl"] as? String ?: "",
                authorName = data["authorName"] as? String ?: "Admin",
                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time 
                    ?: System.currentTimeMillis(),
                likes = (data["likes"] as? Number)?.toInt() ?: 0,
                isLiked = likedByCurrentUser
            )
        }
    }
}

enum class ArticleCategory {
    CARE_TIPS,
    ENVIRONMENTAL_BENEFITS,
    HOW_TO_GUIDES,
    OTHER
} 