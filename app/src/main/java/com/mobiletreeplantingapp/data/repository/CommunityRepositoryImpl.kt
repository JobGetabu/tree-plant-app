package com.mobiletreeplantingapp.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.mobiletreeplantingapp.data.model.Article
import com.mobiletreeplantingapp.data.model.ArticleCategory
import com.mobiletreeplantingapp.data.model.Comment
import com.mobiletreeplantingapp.data.model.ForumPost
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : CommunityRepository {

    private val articlesCollection = firestore.collection("articles")
    private val forumCollection = firestore.collection("forum_posts")
    private val storageRef = storage.reference

    override fun getLatestArticles(): Flow<Result<List<Article>>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = articlesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                // Launch a coroutine to handle the async operations
                launch {
                    try {
                        // Get all articles first
                        val articleDocs = snapshot?.documents ?: emptyList()
                        
                        // For each article, check if it's liked by the current user
                        val articles = articleDocs.mapNotNull { doc ->
                            val likedByCurrentUser = try {
                                val likedDoc = doc.reference.collection("liked_by")
                                    .document(userId)
                                    .get()
                                    .await()
                                likedDoc.exists()
                            } catch (e: Exception) {
                                false
                            }

                            Article.fromFirestore(
                                id = doc.id,
                                data = doc.data ?: return@mapNotNull null,
                                likedByCurrentUser = likedByCurrentUser
                            )
                        }
                        
                        trySend(Result.success(articles))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getArticlesByCategory(category: ArticleCategory): Flow<Result<List<Article>>> = callbackFlow {
        val listener = articlesCollection
            .whereEqualTo("category", category.name)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val articles = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Article::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(Result.success(articles))
            }

        awaitClose { listener.remove() }
    }

    override fun getArticle(articleId: String): Flow<Result<Article>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = articlesCollection.document(articleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(Result.failure(NoSuchElementException("Article not found")))
                    return@addSnapshotListener
                }

                // Launch a coroutine to handle the async operations
                launch {
                    try {
                        // Check if article is liked by current user
                        val likedByCurrentUser = try {
                            val likedDoc = snapshot.reference.collection("liked_by")
                                .document(userId)
                                .get()
                                .await()
                            likedDoc.exists()
                        } catch (e: Exception) {
                            false
                        }

                        val article = Article.fromFirestore(
                            id = snapshot.id,
                            data = snapshot.data ?: emptyMap(),
                            likedByCurrentUser = likedByCurrentUser
                        )
                        trySend(Result.success(article))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun likeArticle(articleId: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        firestore.runTransaction { transaction ->
            val articleRef = articlesCollection.document(articleId)
            val likedByRef = articleRef.collection("liked_by").document(userId)
            val isLiked = transaction.get(likedByRef).exists()
            
            if (isLiked) {
                transaction.delete(likedByRef)
                transaction.update(articleRef, "likes", FieldValue.increment(-1))
            } else {
                transaction.set(likedByRef, hashMapOf(
                    "timestamp" to FieldValue.serverTimestamp()
                ))
                transaction.update(articleRef, "likes", FieldValue.increment(1))
            }
        }.await()
    }

    override fun getLatestForumPosts(): Flow<Result<List<ForumPost>>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = forumCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    ForumPost.fromFirestore(
                        id = doc.id,
                        data = doc.data ?: return@mapNotNull null,
                        currentUserId = userId
                    )
                } ?: emptyList()
                
                trySend(Result.success(posts))
            }

        awaitClose { listener.remove() }
    }

    override fun getForumPost(postId: String): Flow<Result<ForumPost>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = forumCollection.document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(Result.failure(NoSuchElementException("Post not found")))
                    return@addSnapshotListener
                }

                val post = ForumPost.fromFirestore(
                    id = snapshot.id,
                    data = snapshot.data ?: emptyMap(),
                    currentUserId = userId
                )
                trySend(Result.success(post))
            }

        awaitClose { listener.remove() }
    }

    override fun getPostComments(postId: String): Flow<Result<List<Comment>>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = forumCollection.document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { doc ->
                    Comment.fromFirestore(
                        id = doc.id,
                        data = doc.data ?: return@mapNotNull null,
                        currentUserId = userId
                    )
                } ?: emptyList()
                
                trySend(Result.success(comments))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun createForumPost(title: String, content: String): Result<String> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val userName = auth.currentUser?.displayName ?: "Anonymous"

        val post = hashMapOf(
            "title" to title,
            "content" to content,
            "authorId" to userId,
            "authorName" to userName,
            "timestamp" to FieldValue.serverTimestamp(),
            "upvotes" to 0,
            "commentCount" to 0
        )

        val docRef = forumCollection.add(post).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addComment(postId: String, text: String): Result<String> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val userName = auth.currentUser?.displayName ?: "Anonymous"

        val comment = hashMapOf(
            "text" to text,
            "authorId" to userId,
            "authorName" to userName,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.runTransaction { transaction ->
            val postRef = forumCollection.document(postId)
            transaction.update(postRef, "commentCount", FieldValue.increment(1))
            
            val commentRef = postRef.collection("comments").document()
            transaction.set(commentRef, comment)
            commentRef.id
        }.await()
        
        Result.success(postId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun upvotePost(postId: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        firestore.runTransaction { transaction ->
            val postRef = forumCollection.document(postId)
            val upvotesRef = postRef.collection("upvotes").document(userId)
            
            if (transaction.get(upvotesRef).exists()) {
                transaction.delete(upvotesRef)
                transaction.update(postRef, "upvotes", FieldValue.increment(-1))
            } else {
                transaction.set(upvotesRef, hashMapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.update(postRef, "upvotes", FieldValue.increment(1))
            }
        }.await()
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val commentRef = forumCollection.document(postId)
            .collection("comments")
            .document(commentId)
        
        val comment = commentRef.get().await()
        if (comment.getString("authorId") != userId) {
            throw IllegalStateException("Not authorized to delete this comment")
        }
        
        firestore.runTransaction { transaction ->
            transaction.delete(commentRef)
            transaction.update(
                forumCollection.document(postId),
                "commentCount",
                FieldValue.increment(-1)
            )
        }.await()
    }

    override suspend fun deleteForumPost(postId: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val postRef = forumCollection.document(postId)
        val post = postRef.get().await()
        
        if (post.getString("authorId") != userId) {
            throw IllegalStateException("Not authorized to delete this post")
        }
        
        // Delete the post and all its subcollections
        coroutineScope {
            // Get all subcollections first
            val comments = async { postRef.collection("comments").get().await() }
            val upvotes = async { postRef.collection("upvotes").get().await() }
            
            // Wait for all subcollection queries to complete
            val (commentsResult, upvotesResult) = awaitAll(comments, upvotes)
            
            // Perform the deletion in a transaction
            firestore.runTransaction { transaction ->
                // Delete all comments
                commentsResult.documents.forEach { doc ->
                    transaction.delete(doc.reference)
                }
                
                // Delete all upvotes
                upvotesResult.documents.forEach { doc ->
                    transaction.delete(doc.reference)
                }
                
                // Delete the post itself
                transaction.delete(postRef)
            }.await()
        }
    }

    override suspend fun createForumPostWithImage(title: String, content: String, imageUri: Uri): Result<String> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val userName = auth.currentUser?.displayName ?: "Anonymous"

        // Upload image to Firebase Storage
        val imageFileName = "forum_posts/${UUID.randomUUID()}"
        val imageRef = storageRef.child(imageFileName)
        val uploadTask = imageRef.putFile(imageUri).await()
        val imageUrl = imageRef.downloadUrl.await().toString()

        val post = hashMapOf(
            "title" to title,
            "content" to content,
            "authorId" to userId,
            "authorName" to userName,
            "timestamp" to FieldValue.serverTimestamp(),
            "upvotes" to 0,
            "commentCount" to 0,
            "imageUrl" to imageUrl
        )

        val docRef = forumCollection.add(post).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addCommentWithImage(postId: String, text: String, imageUri: Uri): Result<String> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val userName = auth.currentUser?.displayName ?: "Anonymous"

        // Upload image to Firebase Storage
        val imageFileName = "forum_comments/${UUID.randomUUID()}"
        val imageRef = storageRef.child(imageFileName)
        val uploadTask = imageRef.putFile(imageUri).await()
        val imageUrl = imageRef.downloadUrl.await().toString()

        val comment = hashMapOf(
            "text" to text,
            "authorId" to userId,
            "authorName" to userName,
            "timestamp" to FieldValue.serverTimestamp(),
            "imageUrl" to imageUrl
        )

        firestore.runTransaction { transaction ->
            val postRef = forumCollection.document(postId)
            transaction.update(postRef, "commentCount", FieldValue.increment(1))
            
            val commentRef = postRef.collection("comments").document()
            transaction.set(commentRef, comment)
            commentRef.id
        }.await()
        
        Result.success(postId)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 