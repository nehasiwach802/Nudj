package com.tpc.nudj.viewmodels.auth.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpc.nudj.model.AuthResult
import com.tpc.nudj.repository.auth.AuthRepository
import com.tpc.nudj.ui.screen.auth.forgotPassword.ForgotPasswordEvents
import com.tpc.nudj.ui.screen.auth.forgotPassword.ForgotPasswordUiState
import com.tpc.nudj.utils.Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _forgotPasswordUiState = MutableStateFlow(ForgotPasswordUiState())

    val forgotPasswordUiState: StateFlow<ForgotPasswordUiState> = _forgotPasswordUiState.asStateFlow()
    private val _events = MutableSharedFlow<ForgotPasswordEvents>()
    val events = _events.asSharedFlow()
    fun onEmailChange(email: String) {
        _forgotPasswordUiState.update {
            it.copy(email = email)
        }
    }
    fun onSendEmailClick(){
        viewModelScope.launch {

            if (forgotPasswordUiState.value.email.isBlank()) {
                _events.emit(
                    ForgotPasswordEvents.ShowSnackBar("Please enter email")
                )
                return@launch
            }
            Validator.isValidEmail(forgotPasswordUiState.value.email.trim())
                .onFailure {
                    _events.emit(
                        ForgotPasswordEvents.ShowSnackBar("Invalid Email")
                    )
                    return@launch
                }
            authRepository.sendPasswordResetEmail(forgotPasswordUiState.value.email.trim()).collect{result ->
                when(result){
                    is AuthResult.Loading -> {
                        _forgotPasswordUiState.update{
                            it.copy(isLoading = true)
                        }
                    }

                    is AuthResult.Success ->{
                        _forgotPasswordUiState.update {
                            it.copy(isLoading = false)
                        }

                        _events.emit(
                            ForgotPasswordEvents.ShowSnackBar("Password reset email sent")
                        )

                        _events.emit(
                            ForgotPasswordEvents.NavigateToEmailVerification(forgotPasswordUiState.value.email.trim())
                        )
                    }
                    is AuthResult.Error -> {
                        _forgotPasswordUiState.update {
                            it.copy(isLoading = false)
                        }

                        _events.emit(
                            ForgotPasswordEvents.ShowSnackBar("Failed to send reset password mail .Please try again later.")
                        )
                    }

                    else -> Unit

                }

            }
        }

    }

}