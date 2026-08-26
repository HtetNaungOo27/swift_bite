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

    sealed interface State {
        data object Loading : State
        data class Success(val profile: CustomerProfile) : State
        data class Error(val message: String) : State
    }
}
