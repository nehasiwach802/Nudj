package com.tpc.nudj.ui.screen.auth.reset

sealed interface ResetPasswordEvent {
    data class ShowSnackBar(val message: String) : ResetPasswordEvent
    data object NavigateToLogin : ResetPasswordEvent
}