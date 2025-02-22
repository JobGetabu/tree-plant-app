package com.mobiletreeplantingapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object Explore : Screen(
        route = "explore",
        title = "Explore",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    
    object MyTrees : Screen(
        route = "my_trees",
        title = "My Trees",
        selectedIcon = Icons.Filled.Forest,
        unselectedIcon = Icons.Outlined.Forest
    )
    
    object Profile : Screen(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    // Additional routes that don't appear in bottom navigation
    object Map : Screen(
        route = "map",
        title = "Map",
        selectedIcon = Icons.Filled.Home, // Use appropriate icons
        unselectedIcon = Icons.Outlined.Home
    )
    
    object TreeDatabase : Screen(
        route = "tree_database",
        title = "Tree Database",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object PlantingGuide : Screen(
        route = "planting_guide",
        title = "Planting Guide",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object Community : Screen(
        route = "community",
        title = "Community",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    // New screen for area details
    object AreaDetail : Screen(
        route = "area_detail/{areaId}",
        title = "Area Details",
        selectedIcon = Icons.Filled.Landscape,
        unselectedIcon = Icons.Outlined.Landscape
    ) {
        fun createRoute(areaId: String) = "area_detail/$areaId"
    }
} 