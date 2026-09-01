package com.example.foodhub_android.ui.feature.deliveries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.CachedResourceRepository
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.AvailableDelivery
import com.example.foodhub_android.data.models.RiderLocationUpdate
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveriesViewModel @Inject constructor(
    private val foodApi: FoodApi,
    private val session: FoodHubSession,
    private val repository: CachedResourceRepository
) : ViewModel() {
    private val _state = MutableStateFlow<DeliveriesState>(DeliveriesState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<DeliveryEvent>()
    val events = _events.asSharedFlow()

    private val busyOrders = mutableSetOf<String>()
    private val _available = MutableStateFlow(true)
    val available = _available.asStateFlow()
    private val _updatingAvailability = MutableStateFlow(false)
    val updatingAvailability = _updatingAvailability.asStateFlow()

    init {
        viewModelScope.launch {
            repository.availableDeliveries().collectLatest { cached ->
                if (cached == null) {
                    _state.value = DeliveriesState.Loading
                    return@collectLatest
                }
                val previous = _state.value as? DeliveriesState.Success
                _state.value = DeliveriesState.Success(
                    deliveries = cached.items,
                    busyOrders = busyOrders.toSet(),
                    refreshError = previous?.refreshError,
                    isRefreshing = previous?.isRefreshing == true
                )
            }
        }
        refresh()
        loadAvailability()
    }

    private fun loadAvailability() = viewModelScope.launch {
        when (val result = safeApiCall { foodApi.getRiderAvailability() }) {
            is ApiResponse.Success -> _available.value = result.data["available"] ?: false
            else -> Unit
        }
    }

    fun setAvailable(available: Boolean) = viewModelScope.launch {
        if (_updatingAvailability.value) return@launch
        _updatingAvailability.value = true
        when (val result = safeApiCall { foodApi.setRiderAvailability(mapOf("available" to available)) }) {
            is ApiResponse.Success -> {
                _available.value = available
                _events.emit(DeliveryEvent.Message(if (available) "You are online" else "You are offline"))
                refresh()
            }
            is ApiResponse.Error -> _events.emit(DeliveryEvent.Message(result.message ?: "Availability update failed"))
            is ApiResponse.Exception -> _events.emit(DeliveryEvent.Message("Check your connection and try again"))
        }
        _updatingAvailability.value = false
    }

    fun refresh() = viewModelScope.launch {
        val current = _state.value as? DeliveriesState.Success
        _state.value = current?.copy(isRefreshing = true, refreshError = null)
            ?: DeliveriesState.Loading
        when (val result = repository.refreshAvailableDeliveries()) {
            CachedResourceRepository.RefreshResult.Success ->
                (_state.value as? DeliveriesState.Success)?.let {
                    _state.value = it.copy(isRefreshing = false, refreshError = null)
                }
            is CachedResourceRepository.RefreshResult.Failure -> {
                val cached = _state.value as? DeliveriesState.Success
                _state.value = cached?.copy(isRefreshing = false, refreshError = result.message)
                    ?: DeliveriesState.Error(result.message)
            }
        }
    }

    fun logout() = session.clear()

    fun publishLocation(latitude: Double, longitude: Double) = viewModelScope.launch {
        when (val result = safeApiCall {
            foodApi.updateRiderLocation(RiderLocationUpdate(latitude, longitude))
        }) {
            is ApiResponse.Success -> Unit
            is ApiResponse.Error -> _events.emit(
                DeliveryEvent.Message(result.message ?: "Your location could not be updated")
            )
            is ApiResponse.Exception -> Unit // A later foreground update retries automatically.
        }
    }

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
        data class Success(
            val deliveries: List<AvailableDelivery>,
            val busyOrders: Set<String>,
            val refreshError: String? = null,
            val isRefreshing: Boolean = false
        ) : DeliveriesState
        data class Error(val message: String) : DeliveriesState
    }

    sealed interface DeliveryEvent {
        data class Message(val text: String) : DeliveryEvent
        data class OpenOrder(val orderId: String) : DeliveryEvent
    }
}
