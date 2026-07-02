package com.tpc.nudj.ui.screen.auth.register

sealed interface RegisterEvent {
    data class ShowSnackBar(val message: String) : RegisterEvent
    data object NavigateToEmailVerification : RegisterEvent
}