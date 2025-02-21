package com.mobiletreeplantingapp.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.mobiletreeplantingapp.domain.repository.AuthRepository

class LoginWithEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser?> {
        return repository.signInWithEmailAndPassword(email, password)
    }
}