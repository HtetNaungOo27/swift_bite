package com.example.foodhub_android.ui.feature.order_details

import android.os.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.R
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.Order
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
class OrderDetailsViewModel @Inject constructor(private val foodApi: FoodApi) : ViewModel(){

    private val _state = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Loading)
    val state get() = _state.asStateFlow()

    private val _event = MutableSharedFlow<OrderDetailsEvent>()
    val event get() = _event.asSharedFlow()

    fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            _state.value = OrderDetailsState.Loading
            val result = safeApiCall { foodApi.getOrderDetails(orderId) }

            when (result) {
                is ApiResponse.Success -> {
                    _state.value = OrderDetailsState.OrderDetails(result.data)
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

    fun navigateBack() {
        viewModelScope.launch {
            _event.emit(OrderDetailsEvent.NavigateBack)
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
    }

    sealed class OrderDetailsState {
        object Loading : OrderDetailsState()
        data class OrderDetails(val order: Order) : OrderDetailsState()
        data class Error(val message: String) : OrderDetailsState()
    }
}
