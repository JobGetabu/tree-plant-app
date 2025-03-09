package com.mobiletreeplantingapp.ui.screen.forum.post

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobiletreeplantingapp.data.model.Comment
import com.mobiletreeplantingapp.data.model.ForumPost
import com.mobiletreeplantingapp.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumPostViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    var state by mutableStateOf(ForumPostState())
        private set

    fun loadPost(postId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // Load post details
            launch {
                communityRepository.getForumPost(postId)
                    .collect { result ->
                        result.onSuccess { post ->
                            state = state.copy(
                                post = post,
                                isLoading = false
                            )
                        }.onFailure { e ->
                            state = state.copy(
                                error = e.message,
                                isLoading = false
                            )
                        }
                    }
            }

            // Load comments
            launch {
                communityRepository.getPostComments(postId)
                    .collect { result ->
                        result.onSuccess { comments ->
                            state = state.copy(
                                comments = comments,
                                isLoading = false
                            )
                        }
                    }
            }
        }
    }

    fun upvotePost() {
        val post = state.post ?: return
        viewModelScope.launch {
            try {
                communityRepository.upvotePost(post.id)
                // Update local state optimistically
                state = state.copy(
                    post = state.post?.copy(
                        upvotes = post.upvotes + 1,
                        isUpvotedByUser = true
                    )
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addComment(text: String) {
        val post = state.post ?: return
        viewModelScope.launch {
            try {
                val commentId = communityRepository.addComment(post.id, text).getOrThrow()
                // Reload comments
                loadPost(post.id)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun addCommentWithImage(text: String, imageUri: Uri) {
        val post = state.post ?: return
        viewModelScope.launch {
            try {
                state = state.copy(isAddingComment = true)
                val commentId = communityRepository.addCommentWithImage(post.id, text, imageUri).getOrThrow()
                // Reload comments
                loadPost(post.id)
                state = state.copy(isAddingComment = false)
            } catch (e: Exception) {
                state = state.copy(error = e.message, isAddingComment = false)
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                communityRepository.deleteComment(state.post?.id ?: return@launch, commentId)
                // Update local state
                state = state.copy(
                    comments = state.comments.filter { it.id != commentId }
                )
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun deletePost() {
        val post = state.post ?: return
        viewModelScope.launch {
            try {
                communityRepository.deleteForumPost(post.id)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }
}

data class ForumPostState(
    val post: ForumPost? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingComment: Boolean = false,
    val error: String? = null
) 