package com.example.foodhub_android.ui.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.CachedResourceRepository
import com.example.foodhub_android.data.models.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val repository: CachedResourceRepository
) : ViewModel(){

    private val _state = MutableStateFlow<OrderListState>(OrderListState.Loading)
    val state get() = _state.asStateFlow()

    private val _event = MutableSharedFlow<OrderListEvent>()
    val event get() = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.orders().collectLatest { cached ->
                if (cached == null) {
                    _state.value = OrderListState.Loading
                    return@collectLatest
                }
                val previous = _state.value as? OrderListState.OrderList
                _state.value = OrderListState.OrderList(
                    orderList = cached.items,
                    refreshError = previous?.refreshError,
                    isRefreshing = previous?.isRefreshing == true
                )
            }
        }
        getOrders()
    }

    fun navigateToDetails(order: Order) {
        viewModelScope.launch {
            _event.emit(OrderListEvent.NavigateToOrderDetailScreen(order))
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _event.emit(OrderListEvent.NavigateBack)
        }
    }

    fun getOrders() {
        viewModelScope.launch {
            val current = _state.value as? OrderListState.OrderList
            _state.value = current?.copy(isRefreshing = true, refreshError = null)
                ?: OrderListState.Loading
            when (val result = repository.refreshOrders()) {
                CachedResourceRepository.RefreshResult.Success ->
                    (_state.value as? OrderListState.OrderList)?.let {
                        _state.value = it.copy(isRefreshing = false, refreshError = null)
                    }
                is CachedResourceRepository.RefreshResult.Failure -> {
                    val cached = _state.value as? OrderListState.OrderList
                    _state.value = cached?.copy(isRefreshing = false, refreshError = result.message)
                        ?: OrderListState.Error(result.message)
                }
            }
        }
    }

    sealed class OrderListEvent {
        data class NavigateToOrderDetailScreen(val order: Order) : OrderListEvent()
        object NavigateBack : OrderListEvent()
    }

    sealed class OrderListState {
        object Loading : OrderListState()
        data class OrderList(
            val orderList: List<Order>,
            val refreshError: String? = null,
            val isRefreshing: Boolean = false
        ) : OrderListState()
        data class Error(val message: String) : OrderListState()
    }

}
