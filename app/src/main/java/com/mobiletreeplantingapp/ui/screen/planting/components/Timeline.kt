package com.mobiletreeplantingapp.ui.screen.planting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mobiletreeplantingapp.data.model.MilestoneType
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.ui.util.formatDate

@Composable
fun Timeline(
    progress: TreeProgress,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TimelineItem(
                title = "Planted",
                description = "Tree planted on ${formatDate(progress.plantedDate)}",
                icon = Icons.Default.Park,
                isCompleted = true
            )
        }

        items(progress.completedSteps.sortedBy { it }) { stepId ->
            TimelineItem(
                title = "Step ${stepId + 1} Completed",
                description = formatDate(System.currentTimeMillis()),
                icon = Icons.Default.CheckCircle,
                isCompleted = true
            )
        }

        progress.nextMilestone?.let { milestone ->
            item {
                TimelineItem(
                    title = milestone.title,
                    description = "Due: ${formatDate(milestone.dueDate)}",
                    icon = when (milestone.type) {
                        MilestoneType.WATERING -> Icons.Default.WaterDrop
                        MilestoneType.PRUNING -> Icons.Default.ContentCut
                        MilestoneType.FERTILIZING -> Icons.Default.Grass
                        MilestoneType.MAINTENANCE -> Icons.Default.Build
                        else -> Icons.Default.Event
                    },
                    isCompleted = false,
                    isNext = true
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    title: String,
    description: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isNext: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline line with dot
        Box(
            modifier = Modifier.width(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isNext -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        }

        // Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isNext -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 