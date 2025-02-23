package com.mobiletreeplantingapp.ui.screen.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobiletreeplantingapp.data.model.Article
import com.mobiletreeplantingapp.ui.component.LoadingView
import com.mobiletreeplantingapp.ui.component.MarkdownText
import com.mobiletreeplantingapp.ui.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    articleId: String,
    onNavigateBack: () -> Unit,
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val state = viewModel.state
    
    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Article") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    LikeButton(
                        isLiked = state.article?.isLiked ?: false,
                        likesCount = state.article?.likes ?: 0,
                        onLikeClick = { viewModel.likeArticle() },
                        modifier = Modifier
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    LoadingView()
                }
                state.error != null -> {
                    ErrorView(
                        message = state.error,
                        onRetry = { viewModel.loadArticle(articleId) }
                    )
                }
                state.article != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Article Header Image
                        AsyncImage(
                            model = state.article.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Category
                            Text(
                                text = state.article.category.name.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Title
                            Text(
                                text = state.article.title,
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Author and Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "By ${state.article.authorName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatDate(state.article.timestamp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Video Player (if available)
//                            if (state.article.videoUrl != null) {
//                                VideoPlayer(
//                                    videoUrl = state.article.videoUrl,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .aspectRatio(16f/9f)
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//                            }

                            // Article Content
                            MarkdownText(
                                markdown = state.article.content,
                                modifier = Modifier.fillMaxWidth()
                            )

                            LikeButton(
                                isLiked = state.article.isLiked,
                                likesCount = state.article.likes,
                                onLikeClick = { viewModel.likeArticle() },
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleContent(
    article: Article,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Article Header Image
        AsyncImage(
            model = article.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category
            Text(
                text = article.category.name.replace("_", " "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Author and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By ${article.authorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(article.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Player (if available)
//            if (article.videoUrl != null) {
//                VideoPlayer(
//                    videoUrl = article.videoUrl,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .aspectRatio(16f/9f)
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }

            // Article Content
            MarkdownText(
                markdown = article.content,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    // Implement video player using ExoPlayer or WebView
    // This is a placeholder
    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable { /* Open video player */ }
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun LikeButton(
    isLiked: Boolean,
    likesCount: Int,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onLikeClick) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = likesCount.toString(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 