package com.example.foodhub_android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _event = Channel<HomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun navigateToOrderDetail(orderID: String) {
        viewModelScope.launch {
            _event.send(HomeEvent.NavigateToOrderDetail(orderID))
        }
    }

    sealed class HomeEvent {
        data class NavigateToOrderDetail(val orderID: String) : HomeEvent()
    }
}
