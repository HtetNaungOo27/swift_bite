package com.example.foodhub_android.ui.features.auth.login

import android.os.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.SignInRequest
import com.example.foodhub_android.data.models.SignUpRequest
import com.example.foodhub_android.ui.features.auth.signup.SignUpViewModel.SignupNavigationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInEvent>(SignInEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SignInNavigationEvent?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun onEmailChange(email: String){
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onNavigationDone() {
        _navigationEvent.value = null
    }

    fun onSignInClick() {
        viewModelScope.launch {
            _uiState.value = SignInEvent.Loading
            try {
                val response = foodApi.signIn(
                    SignInRequest(
                        email = email.value,
                        password = password.value
                    )
                )
                if(response.token.isNotEmpty()){
                    _uiState.value = SignInEvent.Success
                    _navigationEvent.emit(SignInNavigationEvent.NavigateToHome)
                }
            }catch (e: Exception){
                e.printStackTrace()
                _uiState.value = SignInEvent.Error
            }

        }
    }

    fun onSignUpClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SignInNavigationEvent.NavigateToSignUp)
        }

    }


    sealed class SignInNavigationEvent{
        object NavigateToSignUp : SignInNavigationEvent()
        object NavigateToHome : SignInNavigationEvent()
    }
    sealed class SignInEvent{
        object Nothing: SignInEvent()
        object Success: SignInEvent()
        object Error: SignInEvent()
        object Loading: SignInEvent()
    }
}