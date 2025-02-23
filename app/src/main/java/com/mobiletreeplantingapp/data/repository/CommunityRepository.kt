package com.mobiletreeplantingapp.data.repository

import com.mobiletreeplantingapp.data.model.Article
import com.mobiletreeplantingapp.data.model.ArticleCategory
import com.mobiletreeplantingapp.data.model.Comment
import com.mobiletreeplantingapp.data.model.ForumPost
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getLatestArticles(): Flow<Result<List<Article>>>
    fun getArticlesByCategory(category: ArticleCategory): Flow<Result<List<Article>>>
    fun getArticle(articleId: String): Flow<Result<Article>>
    suspend fun likeArticle(articleId: String)
    
    fun getLatestForumPosts(): Flow<Result<List<ForumPost>>>
    fun getForumPost(postId: String): Flow<Result<ForumPost>>
    fun getPostComments(postId: String): Flow<Result<List<Comment>>>
    suspend fun createForumPost(title: String, content: String): Result<String>
    suspend fun addComment(postId: String, text: String): Result<String>
    suspend fun upvotePost(postId: String)
    suspend fun deleteComment(postId: String, commentId: String)
    suspend fun deleteForumPost(postId: String)
} 