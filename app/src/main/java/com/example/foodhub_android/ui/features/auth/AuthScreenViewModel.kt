package com.example.foodhub_android.ui.features.auth

import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.BaseAuthViewModel
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    override val foodApi: FoodApi,
    private val session: FoodHubSession
) : BaseAuthViewModel(foodApi) {

    private val _uiState = MutableStateFlow<AuthEvent>(AuthEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SignInNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    override fun loading() {
        _uiState.value = AuthEvent.Loading
    }

    override fun onGoogleError(msg: String) {
        _uiState.value = AuthEvent.Error
    }

    override suspend fun onSocialLoginSuccess(token: String) {
        session.storeToken(token)
        _uiState.value = AuthEvent.Success
        _navigationEvent.emit(SignInNavigationEvent.NavigateToHome)
    }


    sealed class SignInNavigationEvent{
        object NavigateToSignUp : SignInNavigationEvent()
        object NavigateToHome : SignInNavigationEvent()
    }
    sealed class AuthEvent{
        object Nothing: AuthEvent()
        object Success: AuthEvent()
        object Error: AuthEvent()
        object Loading: AuthEvent()
    }
}
