package com.mobiletreeplantingapp.data.model

import java.util.concurrent.TimeUnit

data class TreeProgress(
    val treeId: String = "",
    val species: String = "",
    val startDate: Long = 0,
    val plantedDate: Long = 0L,
    val completedSteps: List<Int> = emptyList(),
    val milestones: List<MilestoneType> = emptyList(),
    val photos: List<String> = emptyList(),
    val lastUpdated: Long = 0
) {
    val nextMilestone: Milestone?
        get() {
            val completedMilestones = milestones.map { it.name }.toSet()
            return MilestoneType.values()
                .firstOrNull { it.name !in completedMilestones }
                ?.let { milestone ->
                    Milestone(
                        title = milestone.title,
                        description = milestone.description,
                        dueDate = calculateNextMilestoneDueDate(milestone)
                    )
                }
        }

    private fun calculateNextMilestoneDueDate(milestone: MilestoneType): Long {
        // Calculate due date based on milestone type and start date
        return when (milestone) {
            MilestoneType.PLANTING_COMPLETE -> startDate + TimeUnit.DAYS.toMillis(1)
            MilestoneType.FIRST_LEAVES -> startDate + TimeUnit.DAYS.toMillis(14)
            MilestoneType.FIRST_GROWTH -> startDate + TimeUnit.DAYS.toMillis(30)
            MilestoneType.ESTABLISHED -> startDate + TimeUnit.DAYS.toMillis(90)
            MilestoneType.FLOWERING -> startDate + TimeUnit.DAYS.toMillis(180)
            MilestoneType.FRUITING -> startDate + TimeUnit.DAYS.toMillis(365)
        }
    }

    fun getTimelineEvents(): List<TimelineEvent> {
        val events = mutableListOf<TimelineEvent>()
        
        // Add planting start event
        events.add(
            TimelineEvent(
                id = "${treeId}_start",
                title = "Started Planting $species",
                date = startDate,
                type = TimelineEvent.EventType.PLANTING
            )
        )

        // Add milestone events
        milestones.forEach { milestone ->
            events.add(
                TimelineEvent(
                    id = "${treeId}_${milestone.name}",
                    title = milestone.title,
                    description = milestone.description,
                    date = milestone.date,
                    type = TimelineEvent.EventType.MILESTONE
                )
            )
        }

        // Add photo events
        photos.forEachIndexed { index, photoUrl ->
            events.add(
                TimelineEvent(
                    id = "${treeId}_photo_$index",
                    title = "New Photo Added",
                    date = lastUpdated,
                    type = TimelineEvent.EventType.PHOTO,
                    imageUrl = photoUrl
                )
            )
        }

        // Sort events by date
        return events.sortedByDescending { it.date }
    }
}

data class Milestone(
    val title: String,
    val description: String,
    val dueDate: Long
)

enum class MilestoneType(
    val title: String,
    val description: String,
    var date: Long = 0
) {
    PLANTING_COMPLETE("Planting Complete", "Successfully planted the tree"),
    FIRST_LEAVES("First Leaves", "First leaves have appeared"),
    FIRST_GROWTH("First Growth", "Tree has shown first signs of growth"),
    ESTABLISHED("Tree Established", "Tree is now well established"),
    FLOWERING("First Flowering", "Tree has started flowering"),
    FRUITING("First Fruits", "Tree has started bearing fruits")
} 