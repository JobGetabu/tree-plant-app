package com.mobiletreeplantingapp.ui.util

import com.google.firebase.firestore.FirebaseFirestore
import com.mobiletreeplantingapp.data.model.ArticleCategory
import kotlinx.coroutines.tasks.await

object SampleDataSeeder {
    private val firestore = FirebaseFirestore.getInstance()
    private val articlesCollection = firestore.collection("articles")

    suspend fun seedSampleArticles() {
        val sampleArticles = listOf(
            hashMapOf(
                "title" to "The Importance of Urban Trees",
                "content" to """
                    # Why Urban Trees Matter
                    
                    Trees in cities provide numerous benefits to our communities:
                    
                    - Improve air quality by filtering pollutants
                    - Reduce urban heat island effect
                    - Provide habitat for wildlife
                    - Increase property values
                    - Enhance mental well-being
                    
                    ## Impact on Climate Change
                    
                    Urban trees play a crucial role in fighting climate change by:
                    1. Absorbing CO2
                    2. Reducing energy consumption
                    3. Preventing soil erosion
                """.trimIndent(),
                "category" to ArticleCategory.ENVIRONMENTAL_BENEFITS.name,
                "imageUrl" to "https://example.com/urban-trees.jpg",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "likes" to 0
            ),
            hashMapOf(
                "title" to "Best Practices for Tree Planting",
                "content" to """
                    # Tree Planting Guide
                    
                    Follow these steps for successful tree planting:
                    
                    ## Preparation
                    1. Choose the right location
                    2. Check for underground utilities
                    3. Select appropriate species
                    
                    ## Planting Steps
                    1. Dig the proper size hole
                    2. Place the tree at the right depth
                    3. Backfill with quality soil
                    4. Water thoroughly
                    
                    ## Aftercare
                    - Regular watering
                    - Mulching
                    - Pruning when needed
                """.trimIndent(),
                "category" to ArticleCategory.HOW_TO_GUIDES.name,
                "imageUrl" to "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=400",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "likes" to 0
            ),
            hashMapOf(
                "title" to "Essential Tree Care Tips",
                "content" to """
                    # Caring for Your Trees
                    
                    ## Watering Guidelines
                    - New trees: Water deeply 2-3 times per week
                    - Established trees: Water during dry periods
                    - Morning watering is best
                    
                    ## Mulching
                    - Keep mulch 2-3 inches deep
                    - Avoid volcano mulching
                    - Maintain mulch-free area near trunk
                    
                    ## Common Problems
                    - Watch for pest infestations
                    - Monitor for disease symptoms
                    - Check for structural issues
                """.trimIndent(),
                "category" to ArticleCategory.CARE_TIPS.name,
                "imageUrl" to "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=400",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "likes" to 0
            )
        )

        sampleArticles.forEach { article ->
            try {
                articlesCollection.add(article).await()
            } catch (e: Exception) {
                println("Error adding article: ${e.message}")
            }
        }
    }
} 