package com.mobiletreeplantingapp.domain.usecase

data class ForgotUseCases(
    val resetPasswordUseCase: ResetPasswordUseCase,
    val validateEmailUseCase: ValidateEmailUseCase
)
