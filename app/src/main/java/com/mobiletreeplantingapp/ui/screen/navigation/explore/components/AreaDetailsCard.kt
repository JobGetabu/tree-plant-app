package com.mobiletreeplantingapp.ui.screen.navigation.explore.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiletreeplantingapp.ui.screen.navigation.explore.ExploreState
import java.text.DecimalFormat

@Composable
fun AreaDetailsCard(
    modifier: Modifier = Modifier,
    state: ExploreState,
    onToggleBottomSheet: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Selected Area",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AreaDetailItem(
                    value = "${DecimalFormat("#.##").format(state.areaSize)} ha",
                    label = "Area Size"
                )
                AreaDetailItem(
                    value = state.soilType,
                    label = "Soil Type"
                )
                AreaDetailItem(
                    value = state.altitude,
                    label = "Altitude"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onToggleBottomSheet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Details")
            }
        }
    }
}

@Composable
private fun AreaDetailItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}