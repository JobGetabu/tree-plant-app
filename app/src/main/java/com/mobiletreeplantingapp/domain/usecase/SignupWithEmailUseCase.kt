package com.mobiletreeplantingapp.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.mobiletreeplantingapp.domain.repository.AuthRepository

class SignupWithEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser?> {
        return repository.signUpWithEmailAndPassword(email, password)
    }
}
