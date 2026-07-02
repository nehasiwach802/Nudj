package com.tpc.nudj.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenRoute : NavKey {

    @Serializable
    sealed interface Auth : ScreenRoute {
        @Serializable
        data object SplashScreen : Auth

        @Serializable
        data object Landing : Auth

        @Serializable
        data object Login : Auth

        @Serializable
        data object Register : Auth

        @Serializable
        data object EmailVerification : Auth

        @Serializable
        data object EmailVerified : Auth

        @Serializable
        data object ForgotPassword : Auth

        @Serializable
        data object ResetPassword : Auth
    }

    @Serializable
    sealed interface App : ScreenRoute {
        @Serializable
        data object StudentDashboard : App

        @Serializable
        data object ClubDashboard : App

        @Serializable
        data object ClubVerificationScreen : App

        @Serializable
        data object UserDetailsInput : App
    }
}
