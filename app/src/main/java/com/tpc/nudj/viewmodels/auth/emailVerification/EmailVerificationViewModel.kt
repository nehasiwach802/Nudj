package com.tpc.nudj.viewmodels.auth.emailVerification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tpc.nudj.model.AuthResult
import com.tpc.nudj.repository.auth.AuthRepository
import com.tpc.nudj.ui.screen.auth.emailVerification.EmailVerificationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.tpc.nudj.ui.screen.auth.emailVerification.EmailVerificationUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()
    fun onResendEmailClick() {
        viewModelScope.launch {
            if (!uiState.value.isResendEnabled) {
                return@launch
            }
            authRepository.sendEmailVerification().collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }

                    is AuthResult.VerificationNeeded -> {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }

                        _events.emit(
                            EmailVerificationEvent.showSnackBar("Verification email sent again")
                        )

                        startTimer()
                    }

                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }

                        _events.emit(
                            EmailVerificationEvent.showSnackBar("Some error occurred while verifying the email. Please try again later.")
                        )
                    }

                    else -> Unit
                }
            }
        }

    }

    private val _events = MutableSharedFlow<EmailVerificationEvent>()
    val events = _events.asSharedFlow()


    private var timerJob: Job? = null

    fun startTimer(){
        timerJob?.cancel()
        timerJob = viewModelScope.launch {

            _uiState.update {
                it.copy(isResendEnabled = false, timerInSeconds = 60)
            }
            for(i in 59 downTo 0){
                delay(1000)
                _uiState.update { it.copy(timerInSeconds = i) }
            }
            _uiState.update {
                it.copy(isResendEnabled = true)
            }
        }

    }
    private var hasStartedTimer = false
    fun onScreenOpened() {
        if (hasStartedTimer) return

        hasStartedTimer = true
        startTimer()
        startCheckingEmailVerification()
    }
    private fun startCheckingEmailVerification() {
        viewModelScope.launch {
            while (true) {
                try {
                    delay(3000)
                    val isVerified = authRepository.reloadAndCheckEmailVerified()

                    if (isVerified) {
                        _events.emit(
                            EmailVerificationEvent.NavigateToEmailVerified
                        )
                        break
                    }

                } catch (e: Exception) {
                    _events.emit(
                        EmailVerificationEvent.showSnackBar("Unable to check email verification")
                    )
                }
            }
        }
    }
}
