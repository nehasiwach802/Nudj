package com.tpc.nudj.ui.screen.auth.emailVerified
sealed interface EmailVerifiedEvent{
    data class ShowSnackBar(val message : String): EmailVerifiedEvent
    data object NavigateToUserDetailsInput : EmailVerifiedEvent
    data object NavigateToClubVerificationScreen : EmailVerifiedEvent
}