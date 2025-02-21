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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobiletreeplantingapp.ui.components.BottomNavBar
import com.mobiletreeplantingapp.ui.screen.navigation.explore.ExploreScreen
import com.mobiletreeplantingapp.ui.screen.navigation.home.HomeScreen
import com.mobiletreeplantingapp.ui.screen.navigation.mytrees.MyTreesScreen
import com.mobiletreeplantingapp.ui.screen.navigation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Map : Screen("map")
    object TreeDatabase : Screen("tree_database")
    object PlantingGuide : Screen("planting_guide")
    object Community : Screen("community")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Explore : Screen("explore")
    object MyTrees : Screen("my_trees")
}

@Composable
fun MainGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        route = Graph.MAIN,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen()
        }
        composable(route = Screen.Explore.route) {
            ExploreScreen(innerPadding = innerPadding)
        }
        composable(route = Screen.MyTrees.route) {
            MyTreesScreen(innerPadding = innerPadding)
        }
        composable(route = Screen.Profile.route) {
            SettingsScreen(
                innerPadding = innerPadding,
                navigateToLogin = {
                    rootNavController.navigate(Graph.LOGIN) {
                        popUpTo(Graph.ROOT) { inclusive = true }
                    }
                }
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