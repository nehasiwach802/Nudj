package com.tpc.nudj.viewmodels.auth.emailVerified

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpc.nudj.repository.user.UserRepository
import com.tpc.nudj.ui.screen.auth.emailVerified.EmailVerifiedEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerifiedViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel(){
    private val _events = MutableSharedFlow<EmailVerifiedEvent>()
    val events = _events.asSharedFlow()
    private var hasStarted = false
    fun OnScreenOpened(){
        if(hasStarted){
            return
        }
        hasStarted = true
        viewModelScope.launch{
            delay(1000)
            try{
                userRepository.checkUserTypeAndNavigate(
                    onNormalUser = {
                        viewModelScope.launch {
                            _events.emit(
                                EmailVerifiedEvent.NavigateToUserDetailsInput
                            )
                        }
                    },
                    onClubUser = {
                        viewModelScope.launch {
                            _events.emit(
                                EmailVerifiedEvent.NavigateToClubVerificationScreen
                            )
                        }
                    },
                    onUserNotFound = {
                        viewModelScope.launch {
                            _events.emit(
                                EmailVerifiedEvent.ShowSnackBar("User profile not found")
                            )
                        }
                    }
                )

            }catch(e: Exception){
                _events.emit(
                    EmailVerifiedEvent.ShowSnackBar("Unable to fetch user role")
                )
            }

        }
    }



}
