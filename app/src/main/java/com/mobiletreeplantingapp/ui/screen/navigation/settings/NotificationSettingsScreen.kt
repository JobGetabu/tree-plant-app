package com.mobiletreeplantingapp.ui.screen.navigation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            NotificationSettingItem(
                title = "Watering Reminders",
                checked = state.preferences.wateringEnabled,
                onCheckedChange = { enabled -> 
                    viewModel.updatePreference { it.copy(wateringEnabled = enabled) }
                }
            )

            NotificationSettingItem(
                title = "Pruning Reminders",
                checked = state.preferences.pruningEnabled,
                onCheckedChange = { enabled ->
                    viewModel.updatePreference { it.copy(pruningEnabled = enabled) }
                }
            )

            NotificationSettingItem(
                title = "Fertilizing Reminders",
                checked = state.preferences.fertilizingEnabled,
                onCheckedChange = { enabled ->
                    viewModel.updatePreference { it.copy(fertilizingEnabled = enabled) }
                }
            )

            NotificationSettingItem(
                title = "Inspection Reminders",
                checked = state.preferences.inspectionEnabled,
                onCheckedChange = { enabled ->
                    viewModel.updatePreference { it.copy(inspectionEnabled = enabled) }
                }
            )

            NotificationSettingItem(
                title = "Growth Check Reminders",
                checked = state.preferences.growthCheckEnabled,
                onCheckedChange = { enabled ->
                    viewModel.updatePreference { it.copy(growthCheckEnabled = enabled) }
                }
            )
        }
    }
}

@Composable
private fun NotificationSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
} 