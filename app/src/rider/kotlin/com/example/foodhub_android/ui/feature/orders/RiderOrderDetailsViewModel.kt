package com.example.foodhub_android.ui.feature.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.DeliveryStatusUpdate
import com.example.foodhub_android.data.models.RiderDelivery
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import com.example.foodhub_android.ui.navigation.RiderOrderDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiderOrderDetailsViewModel @Inject constructor(
    private val foodApi: FoodApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val orderId = savedStateHandle.toRoute<RiderOrderDetails>().orderID
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = State.Loading
        when (val result = safeApiCall { foodApi.getActiveDeliveries() }) {
            is ApiResponse.Success -> {
                val order = result.data.data.firstOrNull { it.orderId == orderId }
                _state.value = if (order == null) State.Error("Delivery is no longer active") else State.Success(order)
            }
            is ApiResponse.Error -> _state.value = State.Error(result.message ?: "Unable to load delivery")
            is ApiResponse.Exception -> _state.value = State.Error(result.exception.message ?: "Network error")
        }
    }

    fun markPickedUp() = update("PICKED_UP")
    fun markDelivered() = update("DELIVERED")
    fun markFailed() = update("FAILED", "Delivery could not be completed")

    private fun update(status: String, reason: String? = null) = viewModelScope.launch {
        val current = (_state.value as? State.Success)?.order ?: return@launch
        if (!isTransitionAllowed(current.status, status)) return@launch
        _state.value = State.Updating(current)
        when (val result = safeApiCall { foodApi.updateDeliveryStatus(orderId, DeliveryStatusUpdate(status, reason)) }) {
            is ApiResponse.Success -> {
                _events.emit("Delivery status updated")
                if (status == "DELIVERED" || status == "FAILED") {
                    _state.value = State.Completed
                }
                else refresh()
            }
            is ApiResponse.Error -> _state.value = State.Error(result.message ?: "Status update failed")
            is ApiResponse.Exception -> _state.value = State.Error(result.exception.message ?: "Network error")
        }
    }

    private fun isTransitionAllowed(current: String, requested: String) = when (current) {
        "ASSIGNED" -> requested == "PICKED_UP"
        "OUT_FOR_DELIVERY" -> requested == "DELIVERED" || requested == "FAILED"
        else -> false
    }

    sealed interface State {
        data object Loading : State
        data class Success(val order: RiderDelivery) : State
        data class Updating(val order: RiderDelivery) : State
        data object Completed : State
        data class Error(val message: String) : State
    }
}
