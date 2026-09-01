package com.example.foodhub_android.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.CustomerProfile
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val foodApi: FoodApi,
    private val session: FoodHubSession
) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = State.Loading
        _state.value = when (val result = safeApiCall { foodApi.getCustomerProfile() }) {
            is ApiResponse.Success -> State.Success(result.data)
            is ApiResponse.Error -> State.Error(result.message ?: "Unable to load your profile")
            is ApiResponse.Exception -> State.Error("We couldn’t connect. Check the backend and try again.")
        }
    }

    fun logout() = session.clear()

    fun updateName(name: String) = viewModelScope.launch {
        val normalized = name.trim()
        if (normalized.length !in 2..80) {
            _updateState.value = UpdateState.Error("Name must contain 2 to 80 characters")
            return@launch
        }
        _updateState.value = UpdateState.Saving
        when (val result = safeApiCall { foodApi.updateCustomerProfile(mapOf("name" to normalized)) }) {
            is ApiResponse.Success -> { _updateState.value = UpdateState.Saved; refresh() }
            is ApiResponse.Error -> _updateState.value = UpdateState.Error(result.message ?: "Unable to update profile")
            is ApiResponse.Exception -> _updateState.value = UpdateState.Error("Couldn’t connect to the server")
        }
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()
    fun clearUpdateState() { _updateState.value = UpdateState.Idle }

    sealed interface State {
        data object Loading : State
        data class Success(val profile: CustomerProfile) : State
        data class Error(val message: String) : State
    }
    sealed interface UpdateState {
        data object Idle : UpdateState
        data object Saving : UpdateState
        data object Saved : UpdateState
        data class Error(val message: String) : UpdateState
    }
}
