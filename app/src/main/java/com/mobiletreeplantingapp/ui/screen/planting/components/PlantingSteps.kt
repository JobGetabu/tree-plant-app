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
    completedSteps: List<Int>,
    onStepCompleted: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(steps) { step ->
            StepCard(
                step = step,
                isCompleted = step.id in completedSteps,
                onComplete = { onStepCompleted(step.id) },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun StepCard(
    step: GuideStep,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { if (!isCompleted) onComplete() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 