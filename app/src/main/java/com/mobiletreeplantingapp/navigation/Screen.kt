package com.mobiletreeplantingapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Search
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
        route = "planting_guide/{treeId}/{species}",
        title = "Planting Guide",
        selectedIcon = Icons.Filled.Park,  // or another appropriate icon
        unselectedIcon = Icons.Outlined.Park  // or another appropriate icon
    ) {
        fun createRoute(treeId: String, species: String) = "planting_guide/$treeId/$species"
    }
    
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

    object Article : Screen("article/{articleId}", "Article", Icons.Default.Article, Icons.Outlined.Article) {
        fun createRoute(articleId: String) = "article/$articleId"
    }

    object Forum : Screen("forum", "Forum", Icons.Default.Forum, Icons.Outlined.Forum)
    object ForumPost : Screen("forum_post/{postId}", "Post", Icons.Default.Forum, Icons.Outlined.Forum) {
        fun createRoute(postId: String) = "forum_post/$postId"
    }

    object AllArticles : Screen("all_articles", "All Articles", Icons.Filled.Article, Icons.Outlined.Article)

    object NotificationSettings : Screen(
        route = "notification_settings",
        title = "Notification Settings",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    )

    object EditProfile : Screen(
        route = "edit_profile",
        title = "Edit Profile",
        selectedIcon = Icons.Filled.Edit,
        unselectedIcon = Icons.Outlined.Edit
    )

    object HelpAndSupport : Screen(
        route = "help_and_support",
        title = "Help & Support",
        selectedIcon = Icons.Default.Info,
        unselectedIcon = Icons.Outlined.Info
    )
    
    object PrivacyPolicy : Screen(
        route = "privacy_policy",
        title = "Privacy Policy",
        selectedIcon = Icons.Default.Policy,
        unselectedIcon = Icons.Outlined.Policy
    )
    
    object TermsOfService : Screen(
        route = "terms_of_service",
        title = "Terms of Service",
        selectedIcon = Icons.Default.Description,
        unselectedIcon = Icons.Outlined.Description
    )
    
    object AboutUs : Screen(
        route = "about_us",
        title = "About Us",
        selectedIcon = Icons.Default.Info,
        unselectedIcon = Icons.Outlined.Info
    )
}