package com.example.foodhub_android.ui.features.auth.login

import android.content.Context
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.BaseAuthViewModel
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.SignInRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import com.example.foodhub_android.notification.FoodHubNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SignInViewModel @Inject constructor(
    override val foodApi: FoodApi,
    private val session: FoodHubSession,
    private val notificationManager: FoodHubNotificationManager
) : BaseAuthViewModel(foodApi) {

    private val _uiState = MutableStateFlow<SignInEvent>(SignInEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SignInNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()
    private val _validation = MutableStateFlow(SignInValidation())
    val validation = _validation.asStateFlow()
    private val _resetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val resetState = _resetState.asStateFlow()

    fun onEmailChange(email: String){
        _email.value = email
        _validation.value = _validation.value.copy(email = null)
    }

    fun onPasswordChange(password: String) {
        _password.value = password
        _validation.value = _validation.value.copy(password = null)
    }

    fun requestPasswordReset(email: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _resetState.value = PasswordResetState.Error("Enter a valid email address")
            return
        }
        viewModelScope.launch {
            _resetState.value = PasswordResetState.Loading
            when (val response = safeApiCall { foodApi.requestPasswordReset(mapOf("email" to email.trim())) }) {
                is ApiResponse.Success -> _resetState.value = PasswordResetState.CodeSent(response.data["debugCode"])
                is ApiResponse.Error -> _resetState.value = PasswordResetState.Error(response.message ?: "Unable to request a reset code")
                is ApiResponse.Exception -> _resetState.value = PasswordResetState.Error("Couldn’t connect to the server")
            }
        }
    }

    fun confirmPasswordReset(email: String, code: String, newPassword: String) {
        if (code.length != 6 || newPassword.length < 8) {
            _resetState.value = PasswordResetState.Error("Enter the 6-digit code and a password of at least 8 characters")
            return
        }
        viewModelScope.launch {
            _resetState.value = PasswordResetState.Loading
            when (val response = safeApiCall {
                foodApi.resetPassword(mapOf("email" to email.trim(), "code" to code, "newPassword" to newPassword))
            }) {
                is ApiResponse.Success -> _resetState.value = PasswordResetState.Success
                is ApiResponse.Error -> _resetState.value = PasswordResetState.Error(response.message ?: "The code is invalid or expired")
                is ApiResponse.Exception -> _resetState.value = PasswordResetState.Error("Couldn’t connect to the server")
            }
        }
    }

    fun clearPasswordReset() { _resetState.value = PasswordResetState.Idle }

    fun onSignInClick() {
        val errors = SignInValidation(
            email = when {
                email.value.isBlank() -> "Email is required"
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value.trim()).matches() -> "Enter a valid email address"
                else -> null
            },
            password = if (password.value.isBlank()) "Password is required" else null
        )
        _validation.value = errors
        if (errors.email != null || errors.password != null) return
        viewModelScope.launch {
            _uiState.value = SignInEvent.Loading
            try {
                when (val response = safeApiCall {
                    foodApi.signIn(SignInRequest(
                        email = email.value,
                        password = password.value
                    ))
                }) {
                    is ApiResponse.Success -> {
                        session.storeToken(response.data.token)
                        notificationManager.initialize()
                        _uiState.value = SignInEvent.Success
                        _navigationEvent.emit(SignInNavigationEvent.NavigateToHome(session.consumePendingOrderId()))
                    }
                    else -> _uiState.value = SignInEvent.Error
                }
            }catch (e: Exception){
                e.printStackTrace()
                _uiState.value = SignInEvent.Error
            }

        }
    }

//    fun onGoogleSignInClicked(context: Context){
//        viewModelScope.launch {
//            _uiState.value = SignInEvent.Loading
//            val response = GoogleAuthUiProvider.signIn(
//                context,
//                CredentialManager.create(context)
//            )
//
//            if(response != null){
//                _uiState.value = SignInEvent.Success
//                _navigationEvent.emit(SignInNavigationEvent.NavigateToHome)
//            } else {
//                _uiState.value = SignInEvent.Error
//            }
//        }
//    }
    fun onSignUpClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SignInNavigationEvent.NavigateToSignUp)
        }

    }

    override fun loading() {
        _uiState.value = SignInEvent.Loading
    }

    override fun onGoogleError(msg: String) {
        _uiState.value = SignInEvent.Error
    }

    override suspend fun onSocialLoginSuccess(token: String) {
        session.storeToken(token)
        notificationManager.initialize()
        _uiState.value = SignInEvent.Success
        _navigationEvent.emit(SignInNavigationEvent.NavigateToHome(session.consumePendingOrderId()))
    }


    sealed class SignInNavigationEvent{
        object NavigateToSignUp : SignInNavigationEvent()
        data class NavigateToHome(val pendingOrderId: String? = null) : SignInNavigationEvent()
    }
    sealed class SignInEvent{
        object Nothing: SignInEvent()
        object Success: SignInEvent()
        object Error: SignInEvent()
        object Loading: SignInEvent()
    }
    data class SignInValidation(val email: String? = null, val password: String? = null)
    sealed interface PasswordResetState {
        data object Idle : PasswordResetState
        data object Loading : PasswordResetState
        data class CodeSent(val debugCode: String?) : PasswordResetState
        data object Success : PasswordResetState
        data class Error(val message: String) : PasswordResetState
    }
}
