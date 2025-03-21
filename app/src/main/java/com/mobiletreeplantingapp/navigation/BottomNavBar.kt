package com.mobiletreeplantingapp.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    val screens = listOf(
        Screen.Home,
        Screen.Explore,
        Screen.MyTrees,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar for detail screens
    if (currentRoute?.startsWith("area_detail") == true || 
        currentRoute?.startsWith("planting_guide") == true ||
        currentRoute?.startsWith("forum_post") == true ||
        currentRoute?.startsWith("article") == true ||
        currentRoute?.startsWith("all_articles") == true) {
        return
    }

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (currentRoute == screen.route) {
                            screen.selectedIcon
                        } else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
} 