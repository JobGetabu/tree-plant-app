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
class AllArticlesViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    var state by mutableStateOf(AllArticlesState())
        private set

    init {
        loadArticles()
    }

    fun loadArticles() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            communityRepository.getLatestArticles()
                .catch { e ->
                    state = state.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { result ->
                    state = state.copy(
                        isLoading = false,
                        articles = result.getOrNull() ?: emptyList(),
                        error = result.exceptionOrNull()?.message
                    )
                }
        }
    }
}

data class AllArticlesState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 