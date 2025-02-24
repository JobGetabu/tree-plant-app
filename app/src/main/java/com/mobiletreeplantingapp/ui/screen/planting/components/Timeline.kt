package com.mobiletreeplantingapp.ui.screen.planting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mobiletreeplantingapp.data.model.GuideStep
import com.mobiletreeplantingapp.data.model.MilestoneType
import com.mobiletreeplantingapp.data.model.TimelineEvent
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.ui.util.formatDate
import coil.compose.AsyncImage

@Composable
fun Timeline(
    progress: TreeProgress,
    guideSteps: List<GuideStep>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(getTimelineEvents(progress, guideSteps)) { event ->
            TimelineEvent(
                event = event,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

private fun getTimelineEvents(
    progress: TreeProgress,
    guideSteps: List<GuideStep>
): List<TimelineEvent> {
    val events = mutableListOf<TimelineEvent>()
    
    // Add planting start event
    events.add(
        TimelineEvent(
            id = "${progress.treeId}_start",
            title = "Started Planting ${progress.species}",
            date = progress.startDate,
            type = TimelineEvent.EventType.PLANTING
        )
    )

    // Add completed steps
    progress.completedSteps.forEach { stepId ->
        val step = guideSteps.find { it.id == stepId }
        step?.let {
            events.add(
                TimelineEvent(
                    id = "${progress.treeId}_step_$stepId",
                    title = "Completed: ${step.title}",
                    description = step.description,
                    date = progress.lastUpdated,
                    type = TimelineEvent.EventType.MILESTONE
                )
            )
        }
    }

    // Add photo events
    progress.photos.forEachIndexed { index, photoUrl ->
        events.add(
            TimelineEvent(
                id = "${progress.treeId}_photo_$index",
                title = "Added Photo",
                date = progress.lastUpdated,
                type = TimelineEvent.EventType.PHOTO,
                imageUrl = photoUrl
            )
        )
    }

    return events.sortedByDescending { it.date }
}

@Composable
private fun TimelineEvent(
    event: TimelineEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline line and dot
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(80.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .align(Alignment.Center)
            )
        }
        
        // Event content
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall
                )
                event.description?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(event.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                // Show image if available
                event.imageUrl?.let { url ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "Timeline photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
} 