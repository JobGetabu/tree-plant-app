package com.mobiletreeplantingapp.ui.screen.onboarding

import androidx.annotation.DrawableRes
import com.mobiletreeplantingapp.R

sealed class OnboardingPage(
    @DrawableRes val image: Int,
    val title: String,
    val description: String
) {
    object First : OnboardingPage(
        image = R.drawable.image,
        title = "Find Your Planting Spot!",
        description = "Use our GPS and interactive maps to select the perfect area for your trees. Get detailed info on size, geography, and more!"
    )
    
    object Second : OnboardingPage(
        image = R.drawable.image1,
        title = "Pick the Right Trees!",
        description = "Browse our database of tree species and get recommendations based on your area’s climate, altitude, and soil."
    )
    
    object Third : OnboardingPage(
        image = R.drawable.image2,
        title = "Watch Your Trees Thrive!",
        description = "Track growth, follow care tips, and stay on top of watering, pruning, and protection with our step-by-step guides."
    )
} 