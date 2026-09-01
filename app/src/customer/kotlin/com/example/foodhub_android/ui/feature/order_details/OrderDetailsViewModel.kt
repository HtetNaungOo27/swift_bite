package com.example.foodhub_android.ui.feature.order_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.R
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.data.models.CreateOrderIssueRequest
import com.example.foodhub_android.data.models.OrderIssue
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
class OrderDetailsViewModel @Inject constructor(
    private val foodApi: FoodApi
) : ViewModel(){

    private val _state = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Loading)
    val state get() = _state.asStateFlow()

    private val _event = MutableSharedFlow<OrderDetailsEvent>()
    val event get() = _event.asSharedFlow()
    private val _cancelling = MutableStateFlow(false)
    val cancelling = _cancelling.asStateFlow()
    private val _issue = MutableStateFlow<OrderIssue?>(null)
    val issue = _issue.asStateFlow()
    private val _submittingIssue = MutableStateFlow(false)
    val submittingIssue = _submittingIssue.asStateFlow()

    fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            _state.value = OrderDetailsState.Loading
            val result = safeApiCall { foodApi.getOrderDetails(orderId) }

            when (result) {
                is ApiResponse.Success -> {
                    _state.value = OrderDetailsState.OrderDetails(result.data)
                    loadIssue(orderId)
                }

                is ApiResponse.Error -> {
                    _state.value = OrderDetailsState.Error(result.message ?: "Unknown Error")
                }

                is ApiResponse.Exception -> {
                    _state.value = OrderDetailsState.Error(result.exception.message ?: "An error occurred.")
                }
            }
        }
    }

    private fun loadIssue(orderId: String) = viewModelScope.launch {
        when (val result = safeApiCall { foodApi.getOrderIssue(orderId) }) {
            is ApiResponse.Success -> _issue.value = result.data.issue
            else -> Unit
        }
    }

    fun submitIssue(orderId: String, type: String, description: String) {
        if (_submittingIssue.value) return
        viewModelScope.launch {
            _submittingIssue.value = true
            when (val result = safeApiCall {
                foodApi.createOrderIssue(orderId, CreateOrderIssueRequest(type, description))
            }) {
                is ApiResponse.Success -> {
                    _issue.value = result.data
                    _event.emit(OrderDetailsEvent.Message("Support request submitted"))
                }
                is ApiResponse.Error -> _event.emit(OrderDetailsEvent.Message(result.message ?: "Couldn’t submit the request"))
                is ApiResponse.Exception -> _event.emit(OrderDetailsEvent.Message("Check your connection and try again"))
            }
            _submittingIssue.value = false
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _event.emit(OrderDetailsEvent.NavigateBack)
        }
    }

    fun cancelOrder(orderId: String) {
        if (_cancelling.value) return
        viewModelScope.launch {
            _cancelling.value = true
            when (val result = safeApiCall { foodApi.cancelOrder(orderId) }) {
                is ApiResponse.Success -> _state.value = OrderDetailsState.OrderDetails(result.data)
                is ApiResponse.Error -> _event.emit(OrderDetailsEvent.Message(result.message ?: "This order can no longer be cancelled"))
                is ApiResponse.Exception -> _event.emit(OrderDetailsEvent.Message("Couldn’t cancel the order. Please try again."))
            }
            _cancelling.value = false
        }
    }

    fun reorder(orderId: String) {
        viewModelScope.launch {
            when (val result = safeApiCall { foodApi.reorder(orderId) }) {
                is ApiResponse.Success -> _event.emit(OrderDetailsEvent.ReorderReady)
                is ApiResponse.Error -> _event.emit(OrderDetailsEvent.Message(result.message ?: "Unable to reorder these items"))
                is ApiResponse.Exception -> _event.emit(OrderDetailsEvent.Message("Couldn’t connect to the server"))
            }
        }
    }

    fun getImage(order: Order): Int {
        when(order.status) {
            "Delivered" -> return R.drawable.ic_delivered
            "Preparing" -> return R.drawable.ic_preparing
            "On the way" -> return R.drawable.picked_by_rider_icon
            else -> return R.drawable.ic_pending
        }
    }

    sealed class OrderDetailsEvent {
        object NavigateBack : OrderDetailsEvent()
        data class Message(val text: String) : OrderDetailsEvent()
        data object ReorderReady : OrderDetailsEvent()
    }

    sealed class OrderDetailsState {
        object Loading : OrderDetailsState()
        data class OrderDetails(val order: Order) : OrderDetailsState()
        data class Error(val message: String) : OrderDetailsState()
    }
}
