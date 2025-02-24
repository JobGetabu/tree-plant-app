package com.mobiletreeplantingapp.ui.util

import com.google.firebase.firestore.FirebaseFirestore
import com.mobiletreeplantingapp.data.model.TreeRecommendationData
import kotlinx.coroutines.tasks.await
import java.util.UUID

object TreeRecommendationSeeder {
    private val firestore = FirebaseFirestore.getInstance()
    private val recommendationsCollection = firestore.collection("tree_recommendations")

    suspend fun seedTreeRecommendations() {
        val recommendations = listOf(
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Acacia",
                description = "Native to Africa, excellent for arid and semi-arid regions. Provides good shade and helps in soil nitrogen fixation.",
                growthRate = "Medium to Fast",
                maintainanceLevel = "Low",
                soilPreference = "Sandy to Clay",
                climatePreference = "Tropical to Semi-arid",
                minElevation = 0.0,
                maxElevation = 2000.0,
                suitableSoilTypes = listOf("sandy", "clay", "loamy"),
                suitableClimateZones = listOf("tropical", "semi-arid"),
                waterRequirement = "low",
                suitabilityScore = 0.95f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Grevillea Robusta (Silky Oak)",
                description = "Fast-growing timber tree, good for windbreaks and soil conservation. Popular in agroforestry.",
                growthRate = "Fast",
                maintainanceLevel = "Low",
                soilPreference = "Well-drained, acidic to neutral",
                climatePreference = "Tropical Highland",
                minElevation = 500.0,
                maxElevation = 2300.0,
                suitableSoilTypes = listOf("loamy", "sandy"),
                suitableClimateZones = listOf("tropical", "subtropical"),
                waterRequirement = "medium",
                suitabilityScore = 0.9f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Eucalyptus",
                description = "Fast-growing tree excellent for timber and poles. Good for higher altitude areas.",
                growthRate = "Very Fast",
                maintainanceLevel = "Low",
                soilPreference = "Well-drained, various types",
                climatePreference = "Tropical Highland",
                minElevation = 1000.0,
                maxElevation = 2500.0,
                suitableSoilTypes = listOf("sandy", "loamy", "clay"),
                suitableClimateZones = listOf("tropical", "subtropical"),
                waterRequirement = "medium",
                suitabilityScore = 0.85f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Markhamia Lutea",
                description = "Indigenous tree good for timber and soil conservation. Excellent for agroforestry.",
                growthRate = "Medium",
                maintainanceLevel = "Low",
                soilPreference = "Well-drained soils",
                climatePreference = "Tropical Highland",
                minElevation = 1000.0,
                maxElevation = 2000.0,
                suitableSoilTypes = listOf("loamy", "clay"),
                suitableClimateZones = listOf("tropical"),
                waterRequirement = "medium",
                suitabilityScore = 0.88f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Croton Megalocarpus",
                description = "Native tree good for shade and soil improvement. Popular in coffee growing areas.",
                growthRate = "Medium",
                maintainanceLevel = "Low",
                soilPreference = "Various soil types",
                climatePreference = "Tropical Highland",
                minElevation = 900.0,
                maxElevation = 2100.0,
                suitableSoilTypes = listOf("loamy", "clay", "sandy"),
                suitableClimateZones = listOf("tropical"),
                waterRequirement = "medium",
                suitabilityScore = 0.87f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Melia Volkensii",
                description = "Drought-resistant tree with valuable timber. Excellent for semi-arid areas.",
                growthRate = "Fast",
                maintainanceLevel = "Low",
                soilPreference = "Well-drained soils",
                climatePreference = "Semi-arid",
                minElevation = 0.0,
                maxElevation = 1600.0,
                suitableSoilTypes = listOf("sandy", "loamy"),
                suitableClimateZones = listOf("semi-arid"),
                waterRequirement = "low",
                suitabilityScore = 0.92f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Prunus Africana",
                description = "Indigenous medicinal tree. Good for high rainfall areas and forest restoration.",
                growthRate = "Slow",
                maintainanceLevel = "Medium",
                soilPreference = "Rich, well-drained soils",
                climatePreference = "Tropical Highland",
                minElevation = 1500.0,
                maxElevation = 3000.0,
                suitableSoilTypes = listOf("loamy"),
                suitableClimateZones = listOf("tropical"),
                waterRequirement = "high",
                suitabilityScore = 0.83f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Casuarina",
                description = "Fast-growing tree good for windbreaks and coastal areas. Tolerates saline soils.",
                growthRate = "Fast",
                maintainanceLevel = "Low",
                soilPreference = "Sandy to loamy",
                climatePreference = "Coastal to Semi-arid",
                minElevation = 0.0,
                maxElevation = 1500.0,
                suitableSoilTypes = listOf("sandy", "loamy"),
                suitableClimateZones = listOf("coastal", "semi-arid"),
                waterRequirement = "low",
                suitabilityScore = 0.89f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Moringa Oleifera",
                description = "Fast-growing nutritious tree. Good for arid areas and has multiple uses.",
                growthRate = "Very Fast",
                maintainanceLevel = "Low",
                soilPreference = "Well-drained, sandy to loamy",
                climatePreference = "Tropical to Semi-arid",
                minElevation = 0.0,
                maxElevation = 1200.0,
                suitableSoilTypes = listOf("sandy", "loamy"),
                suitableClimateZones = listOf("tropical", "semi-arid"),
                waterRequirement = "low",
                suitabilityScore = 0.91f
            ),
            TreeRecommendationData(
                id = UUID.randomUUID().toString(),
                species = "Warburgia Ugandensis",
                description = "Indigenous medicinal tree. Good for medium to high altitude areas.",
                growthRate = "Medium",
                maintainanceLevel = "Medium",
                soilPreference = "Well-drained soils",
                climatePreference = "Tropical Highland",
                minElevation = 1200.0,
                maxElevation = 2200.0,
                suitableSoilTypes = listOf("loamy", "clay"),
                suitableClimateZones = listOf("tropical"),
                waterRequirement = "medium",
                suitabilityScore = 0.86f
            )
        )

        recommendations.forEach { recommendation ->
            try {
                recommendationsCollection
                    .document(recommendation.id)
                    .set(recommendation)
                    .await()
                println("Successfully added recommendation for: ${recommendation.species}")
            } catch (e: Exception) {
                println("Error adding recommendation for ${recommendation.species}: ${e.message}")
            }
        }
    }
}