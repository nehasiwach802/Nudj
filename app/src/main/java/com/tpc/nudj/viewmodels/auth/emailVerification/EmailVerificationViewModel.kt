package com.tpc.nudj.viewmodels.auth.emailVerification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpc.nudj.model.AuthResult
import com.tpc.nudj.repository.auth.AuthRepository
import com.tpc.nudj.ui.navigation.VerificationPurpose
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
    fun onResendEmailClick(email: String, purpose: VerificationPurpose) {
        viewModelScope.launch {
            if (!uiState.value.isResendEnabled) {
                return@launch
            }
            val emailVerificationFlow = when (purpose) {
                VerificationPurpose.REGISTRATION ->
                    authRepository.sendEmailVerification()

                VerificationPurpose.PASSWORD_RESET ->
                    authRepository.sendPasswordResetEmail(email)
            }
            emailVerificationFlow.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }

                    is AuthResult.Success,
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
    private var lastHandledOobCode: String? = null
    fun onScreenOpened(email: String, purpose: VerificationPurpose, oobCode: String? = null) {
        if (!hasStartedTimer) {
            hasStartedTimer = true
            startTimer()
            if (purpose == VerificationPurpose.REGISTRATION) {
                startCheckingEmailVerification()
            }
        }

        if (!oobCode.isNullOrBlank() && oobCode != lastHandledOobCode) {
            lastHandledOobCode = oobCode
            when (purpose) {
                VerificationPurpose.REGISTRATION -> handleEmailVerificationLink(oobCode)
                VerificationPurpose.PASSWORD_RESET -> handlePasswordResetLink(oobCode)
            }
        }
    }

    private fun handleEmailVerificationLink(oobCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.applyEmailVerificationCode(oobCode)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(EmailVerificationEvent.NavigateToEmailVerified)
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(
                        EmailVerificationEvent.showSnackBar(
                            "Invalid link, try again later."
                        )
                    )
                }
        }
    }

    private fun handlePasswordResetLink(oobCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.verifyPasswordResetCode(oobCode)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    _events.emit(EmailVerificationEvent.NavigateToResetPassword(oobCode))
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    _events.emit(
                        EmailVerificationEvent.showSnackBar("Invalid link, try again later.")
                    )
                }
        }
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
