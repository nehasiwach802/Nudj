package com.tpc.nudj.ui.screen.auth.emailVerification

sealed interface EmailVerificationEvent {
    data object NavigateToEmailVerified : EmailVerificationEvent
    data class showSnackBar(val message: String) : EmailVerificationEvent
}