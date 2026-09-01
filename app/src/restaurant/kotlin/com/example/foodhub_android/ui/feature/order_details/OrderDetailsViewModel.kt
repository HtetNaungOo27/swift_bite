package com.example.foodhub_android.ui.feature.order_details

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import com.example.foodhub_android.utils.OrdersUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {

    val listOfStatus = OrdersUtils.OrderStatus.entries.map { it.name }

    private val _uiState = MutableStateFlow<OrderDetailsUiState>(OrderDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<OrderDetailsEvent?>()
    val event = _event.asSharedFlow()
    private val _updating = MutableStateFlow(false)
    val updating = _updating.asStateFlow()
    var order: Order? = null

    fun getOrderDetails(orderID: String) {
        viewModelScope.launch {
            _uiState.value = OrderDetailsUiState.Loading
            val result = safeApiCall { foodApi.getRestaurantOrderDetails(orderID) }
            when (result) {
                is ApiResponse.Success -> {
                    _uiState.value = OrderDetailsUiState.Success(result.data)
                    order = result.data
                }

                is ApiResponse.Error -> {
                    _uiState.value = OrderDetailsUiState.Error
                }

                else -> {
                    _uiState.value = OrderDetailsUiState.Error
                }
            }
        }
    }

    fun updateOrderStatus(orderID: String, status: String, preparationMinutes: Int? = null) {
        viewModelScope.launch {
            _updating.value = true
            val result =
                safeApiCall {
                    foodApi.updateOrderStatus(
                        orderID,
                        com.example.foodhub_android.data.models.UpdateOrderStatusRequest(status, preparationMinutes)
                    )
                }
            when (result) {
                is ApiResponse.Success -> {
                    _event.emit(OrderDetailsEvent.ShowPopUp("Order Status updated"))
                    getOrderDetails(orderID)
                }

                else -> {
                    _event.emit(OrderDetailsEvent.ShowPopUp("Order Status update failed"))
                }
            }
            _updating.value = false
        }
    }

    fun rejectOrder(orderID: String, reason: String) {
        if (reason.isBlank()) {
            viewModelScope.launch { _event.emit(OrderDetailsEvent.ShowPopUp("Choose or enter a rejection reason")) }
            return
        }
        viewModelScope.launch {
            _updating.value = true
            when (val result = safeApiCall {
                foodApi.performRestaurantOrderAction(
                    orderID,
                    com.example.foodhub_android.data.models.OrderActionRequest("REJECT", reason.trim())
                )
            }) {
                is ApiResponse.Success -> {
                    _event.emit(OrderDetailsEvent.ShowPopUp("Order rejected"))
                    getOrderDetails(orderID)
                }
                is ApiResponse.Error -> _event.emit(OrderDetailsEvent.ShowPopUp(result.message ?: "Couldn’t reject order"))
                is ApiResponse.Exception -> _event.emit(OrderDetailsEvent.ShowPopUp("Couldn’t connect to the server"))
            }
            _updating.value = false
        }
    }

    fun nextStatuses(order: Order): List<String> = when (order.status.uppercase()) {
        OrdersUtils.OrderStatus.PENDING_ACCEPTANCE.name -> listOf(OrdersUtils.OrderStatus.ACCEPTED.name)
        OrdersUtils.OrderStatus.ACCEPTED.name -> listOf(OrdersUtils.OrderStatus.PREPARING.name)
        OrdersUtils.OrderStatus.PREPARING.name -> listOf(OrdersUtils.OrderStatus.READY.name)
        OrdersUtils.OrderStatus.READY.name -> if (order.fulfillmentType == "PICKUP") listOf("DELIVERED") else emptyList()
        else -> emptyList()
    }

    sealed class OrderDetailsUiState {
        object Loading : OrderDetailsUiState()
        data class Success(val order: Order) : OrderDetailsUiState()
        object Error : OrderDetailsUiState()
    }

    sealed class OrderDetailsEvent {
        object NavigateBack : OrderDetailsEvent()
        data class ShowPopUp(val msg: String) : OrderDetailsEvent()
    }
}
