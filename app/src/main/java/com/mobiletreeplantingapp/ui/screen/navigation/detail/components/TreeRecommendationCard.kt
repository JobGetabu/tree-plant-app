package com.mobiletreeplantingapp.ui.screen.navigation.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiletreeplantingapp.data.model.TreeRecommendation

@Composable
fun TreeRecommendationCard(
    recommendation: TreeRecommendation,
    onStartPlanting: (TreeRecommendation) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStartPlanting(recommendation) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recommendation.species,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${(recommendation.suitabilityScore * 100).toInt()}% Match",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Growth Rate",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = recommendation.growthRate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        text = "Maintenance",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = recommendation.maintainanceLevel,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onStartPlanting(recommendation) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Planting")
            }
        }
    }
} 