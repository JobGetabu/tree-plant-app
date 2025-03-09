package com.mobiletreeplantingapp.ui.screen.navigation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletreeplantingapp.data.repository.CommunityRepository
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import com.mobiletreeplantingapp.ui.util.SampleDataSeeder

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
//    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
//    val weatherState = _weatherState.asStateFlow()
//
//    private val _recentPlantings = MutableStateFlow<List<PlantingData>>(emptyList())
//    val recentPlantings = _recentPlantings.asStateFlow()

    var state by mutableStateOf(HomeState())
        private set

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // Load articles
            launch {
                communityRepository.getLatestArticles()
                    .catch { e ->
                        state = state.copy(
                            error = "Failed to load articles: ${e.message}"
                        )
                    }
                    .collect { result ->
                        result.onSuccess { articles ->
                            state = state.copy(latestArticles = articles)
                        }.onFailure { e ->
                            state = state.copy(
                                error = "Failed to load articles: ${e.message}"
                            )
                        }
                    }
            }

            // Load forum posts
            launch {
                communityRepository.getLatestForumPosts()
                    .catch { e ->
                        state = state.copy(
                            error = "Failed to load forum posts: ${e.message}"
                        )
                    }
                    .collect { result ->
                        result.onSuccess { posts ->
                            state = state.copy(latestPosts = posts)
                        }.onFailure { e ->
                            state = state.copy(
                                error = "Failed to load forum posts: ${e.message}"
                            )
                        }
                    }
            }

            // Load global stats
            launch {
                firestoreRepository.getGlobalStats()
                    .catch { e ->
                        state = state.copy(
                            error = "Failed to load global stats: ${e.message}"
                        )
                    }
                    .collect { result ->
                        result.onSuccess { stats ->
                            state = state.copy(
                                globalTreesPlanted = stats.treesPlanted,
                                globalCo2Offset = stats.co2Offset
                            )
                        }
                    }
            }

            // Load user stats
            launch {
                firestoreRepository.getUserStats()
                    .catch { e ->
                        state = state.copy(
                            error = "Failed to load user stats: ${e.message}"
                        )
                    }
                    .collect { result ->
                        result.onSuccess { stats ->
                            state = state.copy(
                                userTreesPlanted = stats.treesPlanted,
                                userCo2Offset = stats.co2Offset
                            )
                        }
                    }
            }

            state = state.copy(isLoading = false)
        }
    }

    fun retryLoading() {
        loadHomeData()
    }

    fun seedSampleData() {
        viewModelScope.launch {
            try {
                SampleDataSeeder.seedSampleArticles()
            } catch (e: Exception) {
                state = state.copy(error = "Failed to seed sample data: ${e.message}")
            }
        }
    }
} 