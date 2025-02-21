package com.mobiletreeplantingapp.ui.screen.navigation.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
//    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
//    val weatherState = _weatherState.asStateFlow()
//
//    private val _recentPlantings = MutableStateFlow<List<PlantingData>>(emptyList())
//    val recentPlantings = _recentPlantings.asStateFlow()

    init {
        fetchWeatherData()
        fetchRecentPlantings()
    }

    private fun fetchWeatherData() {
        // Will implement weather API integration later
    }

    private fun fetchRecentPlantings() {
        // Will implement Firestore fetch later
    }
} 