package com.example.foodhub_android.ui.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.Notification
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
class NotificationsViewModel @Inject constructor(
    private val foodApi: FoodApi,
    private val session: FoodHubSession
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<NotificationsEvent>()
    val event = _event.asSharedFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    init {
        getNotifications()
    }

    fun navigateToOrderDetail(orderID: String) {
        viewModelScope.launch {
            _event.emit(NotificationsEvent.NavigateToOrderDetail(orderID))
        }
    }

    fun readNotification(notification: Notification) {
        viewModelScope.launch {
            notification.orderId?.let { navigateToOrderDetail(it) }
            val response = safeApiCall { foodApi.readNotification(notification.id) }
            if (response is ApiResponse.Success) {
                getNotifications()
            }
        }
    }

    fun clearExpiredSession() = session.clear()

    fun getNotifications() {
        viewModelScope.launch {
            _state.value = NotificationsState.Loading
            val response = safeApiCall { foodApi.getNotifications() }
            if (response is ApiResponse.Success) {
                _unreadCount.value = response.data.unreadCount
                _state.value = NotificationsState.Success(response.data.notifications)
            } else if (response is ApiResponse.Error) {
                _state.value = NotificationsState.Error(response.message ?: "Unable to load notifications")
            } else {
                _state.value = NotificationsState.Error("We couldn’t connect. Check your internet and try again.")
            }
        }
    }

    sealed class NotificationsEvent {
        data class NavigateToOrderDetail(val orderID: String) : NotificationsEvent()
    }

    sealed class NotificationsState {
        object Loading : NotificationsState()
        data class Success(val data: List<Notification>) : NotificationsState()
        data class Error(val message: String) : NotificationsState()
    }
}
