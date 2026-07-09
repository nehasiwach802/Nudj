package com.tpc.nudj.ui.screen.auth.forgotPassword

sealed interface ForgotPasswordEvents{
    data class ShowSnackBar(val message: String) : ForgotPasswordEvents
    data class NavigateToEmailVerification(val email: String): ForgotPasswordEvents
}