package com.tpc.nudj.ui.screen.auth.emailVerification

sealed interface EmailVerificationEvent {
    data class ShowSnackBar(val message: String) : EmailVerificationEvent
    data object RegistrationVerificationCompleted : EmailVerificationEvent
    data class NavigateToResetPassword(val oobCode: String) : EmailVerificationEvent
}