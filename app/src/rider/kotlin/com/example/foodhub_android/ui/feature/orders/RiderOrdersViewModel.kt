package com.example.foodhub_android.ui.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.RiderDelivery
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiderOrdersViewModel @Inject constructor(private val foodApi: FoodApi) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()
    init { refresh() }
    fun refresh() = viewModelScope.launch {
        _state.value = State.Loading
        when (val result = safeApiCall { foodApi.getActiveDeliveries() }) {
            is ApiResponse.Success -> _state.value = State.Success(result.data.data)
            is ApiResponse.Error -> _state.value = State.Error(result.message ?: "Unable to load orders")
            is ApiResponse.Exception -> _state.value = State.Error(result.exception.message ?: "Network error")
        }
    }
    sealed interface State {
        data object Loading : State
        data class Success(val orders: List<RiderDelivery>) : State
        data class Error(val message: String) : State
    }
}
