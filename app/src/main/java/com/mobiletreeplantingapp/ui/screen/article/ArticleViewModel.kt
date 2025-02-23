package com.mobiletreeplantingapp.ui.screen.article

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletreeplantingapp.data.model.Article
import com.mobiletreeplantingapp.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    var state by mutableStateOf(ArticleState())
        private set

    fun likeArticle() {
        val article = state.article ?: return
        
        // Update UI optimistically
        state = state.copy(
            article = article.copy(
                isLiked = !article.isLiked,
                likes = if (article.isLiked) article.likes - 1 else article.likes + 1
            )
        )

        // Perform the actual like operation
        viewModelScope.launch {
            try {
                communityRepository.likeArticle(article.id)
            } catch (e: Exception) {
                // Revert on failure
                state = state.copy(
                    article = article,
                    error = e.message
                )
            }
        }
    }

    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            communityRepository.getArticle(articleId)
                .catch { e ->
                    state = state.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { result ->
                    state = state.copy(
                        isLoading = false,
                        article = result.getOrNull(),
                        error = result.exceptionOrNull()?.message
                    )
                }
        }
    }
}

data class ArticleState(
    val article: Article? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 