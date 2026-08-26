package com.example.foodhub_android.ui.feature.deliveries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.AvailableDelivery
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
class DeliveriesViewModel @Inject constructor(private val foodApi: FoodApi, private val session: FoodHubSession) : ViewModel() {
    private val _state = MutableStateFlow<DeliveriesState>(DeliveriesState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<DeliveryEvent>()
    val events = _events.asSharedFlow()

    private val busyOrders = mutableSetOf<String>()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = DeliveriesState.Loading
        when (val result = safeApiCall { foodApi.getAvailableDeliveries() }) {
            is ApiResponse.Success -> _state.value = DeliveriesState.Success(result.data.data, emptySet())
            is ApiResponse.Error -> _state.value = DeliveriesState.Error(result.message ?: "Unable to load deliveries")
            is ApiResponse.Exception -> _state.value = DeliveriesState.Error(result.exception.message ?: "Network error")
        }
    }

    fun logout() = session.clear()

    fun accept(orderId: String) = update(orderId, true)
    fun reject(orderId: String) = update(orderId, false)

    private fun update(orderId: String, accept: Boolean) = viewModelScope.launch {
        if (!busyOrders.add(orderId)) return@launch
        updateBusyState()
        val result = safeApiCall {
            if (accept) foodApi.acceptDelivery(orderId) else foodApi.rejectDelivery(orderId)
        }
        busyOrders.remove(orderId)
        when (result) {
            is ApiResponse.Success -> {
                _events.emit(DeliveryEvent.Message(if (accept) "Delivery accepted" else "Delivery rejected"))
                if (accept) _events.emit(DeliveryEvent.OpenOrder(orderId)) else refresh()
            }
            is ApiResponse.Error -> {
                updateBusyState()
                _events.emit(DeliveryEvent.Message(result.message ?: "Request failed"))
            }
            is ApiResponse.Exception -> {
                updateBusyState()
                _events.emit(DeliveryEvent.Message(result.exception.message ?: "Network error"))
            }
        }
    }

    private fun updateBusyState() {
        val current = _state.value
        if (current is DeliveriesState.Success) _state.value = current.copy(busyOrders = busyOrders.toSet())
    }

    sealed interface DeliveriesState {
        data object Loading : DeliveriesState
        data class Success(val deliveries: List<AvailableDelivery>, val busyOrders: Set<String>) : DeliveriesState
        data class Error(val message: String) : DeliveriesState
    }

    sealed interface DeliveryEvent {
        data class Message(val text: String) : DeliveryEvent
        data class OpenOrder(val orderId: String) : DeliveryEvent
    }
}
