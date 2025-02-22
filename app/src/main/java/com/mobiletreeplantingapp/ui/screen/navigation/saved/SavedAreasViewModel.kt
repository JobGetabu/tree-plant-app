package com.mobiletreeplantingapp.ui.screen.navigation.saved

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedAreasViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    var state by mutableStateOf(SavedAreasState())
        private set

    init {
        loadSavedAreas()
    }

    private fun loadSavedAreas() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            firestoreRepository.getUserAreas()
                .catch { e ->
                    state = state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
                .collect { result ->
                    result.onSuccess { areas ->
                        state = state.copy(
                            areas = areas,
                            isLoading = false,
                            error = null
                        )
                    }.onFailure { error ->
                        state = state.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }
}