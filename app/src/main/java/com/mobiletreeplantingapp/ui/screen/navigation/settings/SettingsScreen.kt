package com.mobiletreeplantingapp.ui.screen.navigation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobiletreeplantingapp.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.mobiletreeplantingapp.navigation.Screen
import com.mobiletreeplantingapp.ui.component.ThemeSelector
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = innerPadding.calculateTopPadding())  // Keep top padding for status bar
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp)
        ) {
            ProfileSection()
        }

        // Stats Cards with improved spacing and design
        StatisticsSection(
            treesPlanted = settingsViewModel.userStats.treesPlanted,
            co2Offset = settingsViewModel.userStats.co2Offset,
            totalArea = settingsViewModel.userStats.totalArea,
            modifier = Modifier.padding(16.dp)
        )

        // Settings Groups
        SettingsGroup(
            title = "Account",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    onClick = { navController.navigate(Screen.EditProfile.route) }
                ),
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    onClick = { navController.navigate(Screen.NotificationSettings.route) }
                )
            )
        )

        SettingsGroup(
            title = "Appearance",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    content = {
                        ThemeSelector(
                            currentTheme = settingsViewModel.currentTheme,
                            onThemeSelected = { settingsViewModel.setTheme(it) }
                        )
                    }
                )
            )
        )

        SettingsGroup(
            title = "Support",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Help & Support",
                    onClick = { navController.navigate(Screen.HelpAndSupport.route) }
                ),
                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    onClick = { navController.navigate(Screen.PrivacyPolicy.route) }
                ),
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    onClick = { navController.navigate(Screen.TermsOfService.route) }
                )
            )
        )

        // Sign Out Button
        Button(
            onClick = {
                settingsViewModel.logout()
                navigateToLogin()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.sign_out))
        }
        
    }
}


@Composable
private fun SettingsGroup(
    title: String,
    items: List<SettingsItem>
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(item = item)
                    if (index < items.size - 1) {
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.onClick != null, onClick = { item.onClick?.invoke() })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        item.content?.invoke()
    }
}

private data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val onClick: (() -> Unit)? = null,
    val content: (@Composable () -> Unit)? = null
)

@Composable
private fun ProfileSection() {
    val viewModel: SettingsViewModel = hiltViewModel()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (viewModel.userProfile.photoUrl != null) {
            AsyncImage(
                model = viewModel.userProfile.photoUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.placeholder_profile)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.placeholder_profile),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = viewModel.userProfile.displayName ?: "Tree Planter",  // Default name if null
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
            Text(
                text = viewModel.userProfile.email ?: "example@email.com",  // Default email if null
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Tree Planting Enthusiast",  // Added subtitle
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StatisticsSection(
    treesPlanted: Int,
    co2Offset: Int,
    totalArea: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(
            value = treesPlanted.toString(),
            label = "Trees Planted",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "${co2Offset}kg",
            label = "CO₂ Offset",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = String.format("%.1f m²", totalArea),
            label = "Total Area",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(100.dp),  // Fixed height for all cards
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()  // Fill the card's fixed size
                .padding(8.dp),  // Reduced padding to accommodate longer text
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center  // Center content vertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,  // Limit to one line
                overflow = TextOverflow.Ellipsis  // Add ellipsis if text is too long
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AchievementsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AchievementCard(
            title = "First Tree",
            modifier = Modifier.weight(1f)
        )
        AchievementCard(
            title = "Green Thumb",
            modifier = Modifier.weight(1f)
        )
        AchievementCard(
            title = "Forest Guard",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AchievementCard(
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder circle for achievement icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF9C4)) // Light yellow background
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ImpactMap() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // You would replace this with actual map implementation
        Image(
            painter = painterResource(id = R.drawable.img2),
            contentDescription = "Impact Map",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun SettingsOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}