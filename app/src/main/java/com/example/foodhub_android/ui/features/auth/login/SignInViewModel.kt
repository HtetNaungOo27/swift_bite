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
    private val session: FoodHubSession
) : BaseAuthViewModel(foodApi) {

    private val _uiState = MutableStateFlow<SignInEvent>(SignInEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SignInNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

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

    fun onSignInClick() {
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
                        _uiState.value = SignInEvent.Success
                        _navigationEvent.emit(SignInNavigationEvent.NavigateToHome)
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
        _uiState.value = SignInEvent.Success
        _navigationEvent.emit(SignInNavigationEvent.NavigateToHome)
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
