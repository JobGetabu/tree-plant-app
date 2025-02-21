package com.mobiletreeplantingapp.domain.usecase

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.mobiletreeplantingapp.domain.repository.AuthRepository

class LoginWithGoogleLauncherUseCase(private val repository: AuthRepository) {
    operator fun invoke(googleSignInLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        return repository.signInWithGoogle(googleSignInLauncher = googleSignInLauncher)
    }
}