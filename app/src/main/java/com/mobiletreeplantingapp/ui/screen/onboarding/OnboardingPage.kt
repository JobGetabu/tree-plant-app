package com.mobiletreeplantingapp.ui.screen.onboarding

import androidx.annotation.DrawableRes
import com.mobiletreeplantingapp.R

sealed class OnboardingPage(
    @DrawableRes val image: Int,
    val title: String,
    val description: String
) {
    object First : OnboardingPage(
        image = R.drawable.img1,
        title = "Plant Trees",
        description = "Join our community in making the world greener, one tree at a time"
    )
    
    object Second : OnboardingPage(
        image = R.drawable.img1,
        title = "Track Growth",
        description = "Monitor your trees' growth and impact on the environment"
    )
    
    object Third : OnboardingPage(
        image = R.drawable.img1,
        title = "Join Community",
        description = "Connect with other tree planters and share your green journey"
    )
} 