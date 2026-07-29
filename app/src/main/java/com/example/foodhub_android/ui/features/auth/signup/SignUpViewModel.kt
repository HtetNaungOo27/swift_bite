package com.example.foodhub_android.ui.features.auth.signup

import android.content.Context
import android.os.Message
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.BaseAuthViewModel
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.SignUpRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    override val foodApi: FoodApi,
    private val session: FoodHubSession
) : BaseAuthViewModel(foodApi) {

    private val _uiState = MutableStateFlow<SignUpEvent>(SignUpEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SignupNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    fun onEmailChange(email: String){
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onNameChange(name: String) {
        _name.value = name
    }

    fun onSignUpClick() {
        viewModelScope.launch {
            _uiState.value = SignUpEvent.Loading
            try {
                when (val response = safeApiCall {
                    foodApi.signUp(SignUpRequest(
                        name = name.value,
                        email = email.value,
                        password = password.value
                    ))
                }) {
                    is ApiResponse.Success -> {
                        session.storeToken(response.data.token)
                        _uiState.value = SignUpEvent.Success
                        _navigationEvent.emit(SignupNavigationEvent.NavigateToHome)
                    }
                    else -> _uiState.value = SignUpEvent.Error
                }
            }catch (e: Exception){
                e.printStackTrace()
                _uiState.value = SignUpEvent.Error
            }

        }
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SignupNavigationEvent.NavigateToLogin)
        }

    }

    override fun loading() {
        _uiState.value = SignUpEvent.Loading
    }

    override fun onGoogleError(msg: String) {
        _uiState.value = SignUpEvent.Error
    }

    override suspend fun onSocialLoginSuccess(token: String) {
        session.storeToken(token)
        _uiState.value = SignUpEvent.Success
        _navigationEvent.emit(SignupNavigationEvent.NavigateToHome)
    }
    sealed class SignupNavigationEvent{
        object NavigateToLogin : SignupNavigationEvent()
        object NavigateToHome : SignupNavigationEvent()
    }
    sealed class SignUpEvent{
        object Nothing: SignUpEvent()
        object Success: SignUpEvent()
        object Error: SignUpEvent()
        object Loading: SignUpEvent()
    }
}
