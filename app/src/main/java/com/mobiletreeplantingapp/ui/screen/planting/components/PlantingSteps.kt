package com.mobiletreeplantingapp.ui.screen.planting.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobiletreeplantingapp.data.model.GuideStep

@Composable
fun PlantingSteps(
    steps: List<GuideStep>,
    onStepCompleted: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(steps) { step ->
            StepCard(
                step = step,
                onComplete = { onStepCompleted(step.id) }
            )
        }
    }
}

@Composable
private fun StepCard(
    step: GuideStep,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    step.estimatedTimeMinutes?.let { time ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Estimated time: $time minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(
                    onClick = onComplete,
                    enabled = !step.isCompleted
                ) {
                    Icon(
                        imageVector = if (step.isCompleted) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = if (step.isCompleted) {
                            "Step completed"
                        } else {
                            "Mark step as completed"
                        },
                        tint = if (step.isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            step.videoUrl?.let { url ->
                Spacer(modifier = Modifier.height(16.dp))
                // You can add video player implementation here
                // For now, just showing a placeholder
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Video Tutorial")
                    }
                }
            }
        }
    }
} 