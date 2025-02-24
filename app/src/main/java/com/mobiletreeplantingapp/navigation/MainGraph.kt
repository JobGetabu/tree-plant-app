package com.mobiletreeplantingapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobiletreeplantingapp.ui.screen.article.ArticleScreen
import com.mobiletreeplantingapp.ui.screen.forum.ForumScreen
import com.mobiletreeplantingapp.ui.screen.forum.post.ForumPostScreen
import com.mobiletreeplantingapp.ui.screen.navigation.detail.AreaDetailScreen
import com.mobiletreeplantingapp.ui.screen.navigation.explore.ExploreScreen
import com.mobiletreeplantingapp.ui.screen.navigation.home.HomeScreen
import com.mobiletreeplantingapp.ui.screen.navigation.mytrees.MyTreesScreen
import com.mobiletreeplantingapp.ui.screen.navigation.saved.SavedAreasScreen
import com.mobiletreeplantingapp.ui.screen.navigation.settings.SettingsScreen
import com.mobiletreeplantingapp.ui.screen.planting.PlantingGuideScreen
import com.mobiletreeplantingapp.ui.screen.article.AllArticlesScreen
import com.mobiletreeplantingapp.ui.screen.navigation.settings.NotificationSettingsScreen

@Composable
fun MainGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToArticle = { articleId ->
                    navController.navigate(Screen.Article.createRoute(articleId))
                },
                onNavigateToForum = {
                    navController.navigate(Screen.Forum.route)
                },
                onNavigateToAllArticles = {
                    navController.navigate(Screen.AllArticles.route)
                },
                onNavigateToForumPost = { postId ->
                    navController.navigate(Screen.ForumPost.createRoute(postId))
                },
                onNavigateToExplore = {
                    navController.navigate(Screen.Explore.route)
                },
                onNavigateToSavedAreas = {
                    navController.navigate(Screen.MyTrees.route)
                }
            )
        }

        // Add new routes for articles and forum
        composable(
            route = Screen.Article.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            ArticleScreen(
                articleId = articleId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Forum.route) {
            ForumScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPost = { postId ->
                    navController.navigate(Screen.ForumPost.createRoute(postId))
                }
            )
        }

        composable(
            route = Screen.ForumPost.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            ForumPostScreen(
                postId = postId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Explore.route) {
            ExploreScreen(
                innerPadding = innerPadding,
                navController = navController
            )
        }
        composable(route = Screen.MyTrees.route) {
            SavedAreasScreen(
                onAreaClick = { areaId ->
                    navController.navigate(Screen.AreaDetail.createRoute(areaId))
                }
            )
        }
        composable(route = Screen.Profile.route) {
            SettingsScreen(
                innerPadding = innerPadding,
                navigateToLogin = {
                    rootNavController.navigate(Graph.LOGIN) {
                        popUpTo(Graph.ROOT) { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        
        // Update area detail route
        composable(
            route = Screen.AreaDetail.route,
            arguments = listOf(
                navArgument("areaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val areaId = backStackEntry.arguments?.getString("areaId")
                ?: return@composable
            AreaDetailScreen(
                areaId = areaId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                navController = navController
            )
        }

        composable(
            route = Screen.PlantingGuide.route,
            arguments = listOf(
                navArgument("treeId") { type = NavType.StringType },
                navArgument("species") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val treeId = backStackEntry.arguments?.getString("treeId") ?: return@composable
            val species = backStackEntry.arguments?.getString("species") ?: return@composable
            PlantingGuideScreen(
                treeId = treeId,
                species = species,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Add the AllArticles route
        composable(Screen.AllArticles.route) {
            AllArticlesScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToArticle = { articleId ->
                    navController.navigate(Screen.Article.createRoute(articleId))
                }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController() // This controller is for bottom nav only
    
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        MainGraph(
            navController = navController,
            rootNavController = rootNavController, // Pass the root controller
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}