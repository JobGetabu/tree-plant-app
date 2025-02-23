package com.mobiletreeplantingapp.ui.screen.navigation.home

import com.mobiletreeplantingapp.data.model.Article
import com.mobiletreeplantingapp.data.model.ForumPost

data class HomeState(
    val latestArticles: List<Article> = emptyList(),
    val latestPosts: List<ForumPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val treesPlanted: Int = 0,
    val co2Offset: Int = 0
) 