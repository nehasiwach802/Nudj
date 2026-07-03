package com.tpc.nudj.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tpc.nudj.model.User
import com.tpc.nudj.model.enums.Role
import com.tpc.nudj.repository.auth.AuthRepository
import com.tpc.nudj.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser = authRepository.getCurrentUser()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    init {
        observeAuthState()
        refreshAuthState()
    }

    fun refreshAuthState() {
        viewModelScope.launch {
            resolveInitialDestination()
        }
    }

    private suspend fun resolveInitialDestination() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        try {
            firebaseUser.reload().await()
        } catch (_: Exception) { }

        if (!firebaseUser.isEmailVerified) {
            _authState.value = AuthState.EmailNotVerified
            return
        }

        val role = userRepository.fetchUserRole(firebaseUser.uid)

        when (role) {
            Role.CLUB -> {
                val status = userRepository.fetchClubVerificationStatus(firebaseUser.uid)
                _authState.value = if (status == "approved") {
                    AuthState.Authenticated(
                        User(firebaseUser.uid, firebaseUser.email ?: ""),
                        Destination.ClubDashboard
                    )
                } else {
                    AuthState.Authenticated(
                        User(firebaseUser.uid, firebaseUser.email ?: ""),
                        Destination.ClubPending
                    )
                }
            }
            Role.USER -> {
                val userDetails = userRepository.fetchUserById(firebaseUser.uid)
                val isProfileIncomplete = userDetails == null || userDetails.name.isNullOrEmpty()

                _authState.value = if (isProfileIncomplete) {
                    AuthState.Authenticated(
                        User(firebaseUser.uid, firebaseUser.email ?: ""),
                        Destination.StudentDetailsInput
                    )
                } else {
                    AuthState.Authenticated(
                        User(firebaseUser.uid, firebaseUser.email ?: ""),
                        Destination.StudentDashboard
                    )
                }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user == null) {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    enum class Destination {
        StudentDashboard,
        StudentDetailsInput,
        ClubDashboard,
        ClubPending
    }

    sealed class AuthState {
        data object Initial : AuthState()
        data object Unauthenticated : AuthState()
        data object EmailNotVerified : AuthState()
        data class Authenticated(
            val user: User,
            val destination: Destination
        ) : AuthState()
    }
}