package com.mobiletreeplantingapp.domain.usecase

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.mobiletreeplantingapp.domain.repository.AuthRepository

class LoginWithGoogleCredential(private val repository: AuthRepository) {
    suspend operator fun invoke(credential: AuthCredential): Result<FirebaseUser> {
        return repository.signInWithGoogleCredential(credential = credential)
    }
}