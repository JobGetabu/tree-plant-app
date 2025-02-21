package com.mobiletreeplantingapp.domain.usecase

import com.mobiletreeplantingapp.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.resetPassword(email = email)
    }
}