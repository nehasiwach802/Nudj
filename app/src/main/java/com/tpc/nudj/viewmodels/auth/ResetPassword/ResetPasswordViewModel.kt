package com.tpc.nudj.viewmodels.auth.ResetPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpc.nudj.model.AuthResult
import com.tpc.nudj.repository.auth.AuthRepository
import com.tpc.nudj.ui.screen.auth.reset.ResetPasswordEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.tpc.nudj.ui.screen.auth.reset.ResetPasswordUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _resetPasswordUiState = MutableStateFlow(ResetPasswordUiState())
    val resetPasswordUiState: StateFlow<ResetPasswordUiState> = _resetPasswordUiState.asStateFlow()
    private val _events = MutableSharedFlow<ResetPasswordEvent>()
    val events = _events.asSharedFlow()
    fun onPasswordChange(password: String) {
        _resetPasswordUiState.update {
            it.copy(password = password)
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _resetPasswordUiState.update {
            it.copy(confirmPassword = confirmPassword)
        }
    }

    fun togglePasswordVisibility() {
        _resetPasswordUiState.update {
            it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    fun toggleConfirmPasswordVisibility() {
        _resetPasswordUiState.update {
            it.copy(
                confirmPasswordVisible = !it.confirmPasswordVisible
            )
        }
    }

    fun onSubmitClick(oobCode: String) {
        viewModelScope.launch {
            if (resetPasswordUiState.value.password.isBlank()) {
                _events.emit(ResetPasswordEvent.ShowSnackBar("Please Enter password"))
                return@launch
            }
            if (resetPasswordUiState.value.confirmPassword.isBlank()) {
                _events.emit(ResetPasswordEvent.ShowSnackBar("Please Enter confirm Password"))
                return@launch
            }
            if (resetPasswordUiState.value.password.length < 6) {
                _events.emit(ResetPasswordEvent.ShowSnackBar("Password must be at least 6 characters"))
                return@launch
            }

            if (resetPasswordUiState.value.password != resetPasswordUiState.value.confirmPassword) {
                _events.emit(ResetPasswordEvent.ShowSnackBar("Passwords do not match"))
                return@launch
            }

            authRepository.resetPassword(oobCode, resetPasswordUiState.value.password)
                .collect { result ->
                    when (result) {
                        AuthResult.Loading -> {
                            _resetPasswordUiState.update { it.copy(isLoading = true) }
                        }

                        is AuthResult.Success -> {
                            _resetPasswordUiState.update { it.copy(isLoading = false) }
                            _events.emit(ResetPasswordEvent.ShowSnackBar("Password reset successfully"))
                            _events.emit(ResetPasswordEvent.NavigateToLogin)
                        }

                        is AuthResult.Error -> {
                            _resetPasswordUiState.update { it.copy(isLoading = false) }
                            _events.emit(ResetPasswordEvent.ShowSnackBar(result.message))
                        }

                        else -> Unit
                    }
                }

        }

    }
}