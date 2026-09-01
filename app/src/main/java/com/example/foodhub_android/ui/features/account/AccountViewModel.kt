package com.example.foodhub_android.ui.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.AccountProfile
import com.example.foodhub_android.data.models.ChangePasswordRequest
import com.example.foodhub_android.data.models.UpdateAccountRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(private val api: FoodApi) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = State.Loading
        when (val result = safeApiCall { api.getAccount() }) {
            is ApiResponse.Success -> _state.value = State.Ready(result.data)
            is ApiResponse.Error -> _state.value = State.Error(result.message ?: "Unable to load your account")
            is ApiResponse.Exception -> _state.value = State.Error("Check your connection and try again")
        }
    }

    fun save(request: UpdateAccountRequest) = viewModelScope.launch {
        if (_saving.value) return@launch
        _saving.value = true
        when (val result = safeApiCall { api.updateAccount(request) }) {
            is ApiResponse.Success -> { _state.value = State.Ready(result.data); _events.emit("Account updated") }
            is ApiResponse.Error -> _events.emit(result.message ?: "Account update failed")
            is ApiResponse.Exception -> _events.emit("Check your connection and try again")
        }
        _saving.value = false
    }

    fun changePassword(current: String, replacement: String, confirm: String) = viewModelScope.launch {
        if (replacement != confirm) return@launch _events.emit("New passwords do not match")
        if (_saving.value) return@launch
        _saving.value = true
        when (val result = safeApiCall { api.changePassword(ChangePasswordRequest(current, replacement)) }) {
            is ApiResponse.Success -> _events.emit("Password changed")
            is ApiResponse.Error -> _events.emit(result.message ?: "Password change failed")
            is ApiResponse.Exception -> _events.emit("Check your connection and try again")
        }
        _saving.value = false
    }

    sealed interface State {
        data object Loading : State
        data class Ready(val profile: AccountProfile) : State
        data class Error(val message: String) : State
    }
}
