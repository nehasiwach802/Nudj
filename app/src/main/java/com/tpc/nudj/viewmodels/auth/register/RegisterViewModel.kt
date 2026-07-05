package com.tpc.nudj.viewmodels.auth.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpc.nudj.model.AuthResult
import com.tpc.nudj.model.enums.Role
import com.tpc.nudj.repository.auth.AuthRepository
import com.tpc.nudj.repository.auth.GoogleSignInClient
import com.tpc.nudj.repository.user.UserRepository
import com.tpc.nudj.ui.screen.auth.register.RegisterEvent
import com.tpc.nudj.ui.screen.auth.register.RegisterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import com.tpc.nudj.utils.Validator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()
    private val _events = MutableSharedFlow<RegisterEvent>()
    val events = _events.asSharedFlow()

    fun onEmailChange(newEmail: String) {
        _registerUiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChange(newPassword: String) {
        _registerUiState.update { it.copy(password = newPassword) }
    }

    fun onConfirmPasswordChange(newPassword: String) {
        _registerUiState.update { it.copy(confirmPassword = newPassword) }
    }
    
    fun onRoleChange(newRole: Role) {
        _registerUiState.update { it.copy(role = newRole) }
    }

    fun onRegisterClick() {

        viewModelScope.launch {
            Validator.isValidEmail(registerUiState.value.email.trim())
                .onFailure {
                    _events.emit(
                        RegisterEvent.ShowSnackBar(
                            "Invalid Email"
                        )
                    )
                    return@launch
                }

            if(registerUiState.value.email.isBlank()){
                _events.emit(RegisterEvent.ShowSnackBar("Please Enter Email"))
                return@launch
            }
            if(registerUiState.value.password.isBlank()){
                _events.emit(RegisterEvent.ShowSnackBar("Please Enter Password"))
                return@launch
            }
            if (registerUiState.value.password.length < 6) {
                _events.emit(RegisterEvent.ShowSnackBar("Password must be at least 6 characters"))
                return@launch
            }

            if (registerUiState.value.confirmPassword.isBlank()) {
                _events.emit(RegisterEvent.ShowSnackBar("Please confirm your password"))
                return@launch
            }

            if (registerUiState.value.password != registerUiState.value.confirmPassword) {
                _events.emit(RegisterEvent.ShowSnackBar("Passwords do not match"))
                return@launch
            }


            try{
             authRepository.createUserWithEmailAndPassword(
                    email = registerUiState.value.email,
                    password = registerUiState.value.password,
                    displayName = registerUiState.value.email.substringBefore("@"),
                    role = registerUiState.value.role
                ).collect{result ->
                    when(result){
                        is AuthResult.Loading->{
                            _registerUiState.update {
                                it.copy(isLoading = true)
                             }
                        }
                        is AuthResult.VerificationNeeded ->{
                            val currentUser = authRepository.getCurrentUser().firstOrNull()
                            if (currentUser == null) {
                                _registerUiState.update {
                                    it.copy(isLoading = false)
                                }
                                _events.emit(
                                    RegisterEvent.ShowSnackBar("User not found after signup")
                                )
                                return@collect
                            }
                            val isProfileCreated = userRepository.createUserProfile(
                                uid = currentUser.uid,
                                email = currentUser.email,
                                role = registerUiState.value.role
                            )

                            if (isProfileCreated) {
                                _events.emit(
                                    RegisterEvent.ShowSnackBar("Verification email sent")
                                )
                                _events.emit(
                                    RegisterEvent.NavigateToEmailVerification
                                )
                            } else {
                                _events.emit(
                                    RegisterEvent.ShowSnackBar("Failed to create user profile")
                                )
                            }
                        }
                        is AuthResult.Success ->{
                            _registerUiState.update {
                                it.copy(isLoading = false)
                            }

                        }
                        is AuthResult.Error -> {
                            _registerUiState.update {
                                it.copy(isLoading = false)
                            }
                            _events.emit(
                                RegisterEvent.ShowSnackBar(result.message)
                            )
                        }
                        else ->Unit
                    }
             }



            }catch (e: Exception){
                _registerUiState.update {
                    it.copy(isLoading = false)
                }

                _events.emit(
                    RegisterEvent.ShowSnackBar("Registration failed")
                )

            }
        }
    }


    fun onGoogleClick(context: Context) {
        viewModelScope.launch {
            val idToken = googleSignInClient.signIn(context)
            if (idToken.isNullOrBlank()){
                _events.emit(
                    RegisterEvent.ShowSnackBar("Google sign-in failed")
                )
                return@launch
            }
            try {
                authRepository.signInWithGoogle(idToken).collect { result ->
                    when (result) {
                        is AuthResult.Loading -> {
                            _registerUiState.update {
                                it.copy(isLoading = true)
                            }
                        }

                        is AuthResult.Success -> {
                            val currentUser = authRepository.getCurrentUser().firstOrNull()
                            if (currentUser == null) {
                                _registerUiState.update {
                                    it.copy(isLoading = false)
                                }
                                _events.emit(
                                    RegisterEvent.ShowSnackBar("User not found after Google sign-in")
                                )
                                return@collect
                            }
                            val isProfileCreated = userRepository.createUserProfile(
                                uid = currentUser.uid,
                                email = currentUser.email,
                                role = registerUiState.value.role
                            )
                            if (isProfileCreated) {
                                _events.emit(
                                    RegisterEvent.ShowSnackBar("Google registration successful")
                                )
                                when (registerUiState.value.role) {
                                    Role.USER -> {
                                        _events.emit(RegisterEvent.NavigateToUserDetailsInput)
                                    }

                                    Role.CLUB -> {
                                        _events.emit(RegisterEvent.NavigateToClubVerificationScreen)
                                    }
                                }
                            } else {
                                _registerUiState.update {
                                    it.copy(isLoading = false)
                                }
                                _events.emit(
                                    RegisterEvent.ShowSnackBar("Failed to create user profile")
                                )

                            }


                        }

                        is AuthResult.Error -> {
                            _registerUiState.update {
                                it.copy(isLoading = false)
                            }
                            _events.emit(
                                RegisterEvent.ShowSnackBar(result.message)
                            )
                        }
                        else -> Unit
                    }
                }
            }catch (e: Exception){
                _registerUiState.update {
                it.copy(isLoading = false)
            }
                _events.emit(
                    RegisterEvent.ShowSnackBar("Google registration failed")
                )

            }


        }
    }


    fun onPasswordVisibilityToggle(){
        _registerUiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle(){
        _registerUiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }




}
