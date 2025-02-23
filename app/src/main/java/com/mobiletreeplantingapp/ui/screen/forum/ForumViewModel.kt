package com.mobiletreeplantingapp.ui.screen.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletreeplantingapp.data.model.ForumPost
import com.mobiletreeplantingapp.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    var state by mutableStateOf(ForumState())
        private set

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            communityRepository.getLatestForumPosts()
                .catch { e ->
                    state = state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load posts"
                    )
                }
                .collect { result ->
                    state = state.copy(
                        isLoading = false,
                        posts = result.getOrNull() ?: emptyList(),
                        error = result.exceptionOrNull()?.message
                    )
                }
        }
    }

    fun createPost(title: String, content: String) {
        viewModelScope.launch {
            try {
                communityRepository.createForumPost(title, content)
                loadPosts() // Reload posts after creating new one
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun upvotePost(postId: String) {
        viewModelScope.launch {
            try {
                communityRepository.upvotePost(postId)
                // Update local state optimistically
                state = state.copy(
                    posts = state.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(upvotes = post.upvotes + 1)
                        } else post
                    }
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

data class ForumState(
    val posts: List<ForumPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 