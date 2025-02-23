package com.mobiletreeplantingapp.ui.screen.article

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobiletreeplantingapp.ui.component.ArticleListItem
import com.mobiletreeplantingapp.ui.component.ErrorView
import com.mobiletreeplantingapp.ui.component.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllArticlesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToArticle: (String) -> Unit,
    viewModel: AllArticlesViewModel = hiltViewModel()
) {
    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Articles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingView()
            }
            state.error != null -> {
                ErrorView(
                    message = state.error,
                    onRetry = { viewModel.loadArticles() }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.articles) { article ->
                        ArticleListItem(
                            article = article,
                            onClick = { onNavigateToArticle(article.id) }
                        )
                    }
                }
            }
        }
    }
} 