package com.example.foodhub_android.ui.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.CachedResourceRepository
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: CachedResourceRepository,
    private val session: FoodHubSession
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<NotificationsEvent>()
    val event = _event.asSharedFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    init {
        viewModelScope.launch {
            repository.notifications().collectLatest { cached ->
                if (cached == null) {
                    _unreadCount.value = 0
                    _state.value = NotificationsState.Loading
                    return@collectLatest
                }
                _unreadCount.value = cached.unreadCount
                val previous = _state.value as? NotificationsState.Success
                _state.value = NotificationsState.Success(
                    data = cached.items,
                    refreshError = previous?.refreshError,
                    isRefreshing = previous?.isRefreshing == true
                )
            }
        }
        viewModelScope.launch {
            session.cacheScope.collectLatest {
                if (session.getToken() != null) getNotifications()
            }
        }
    }

    fun navigateToOrderDetail(orderID: String) {
        viewModelScope.launch {
            _event.emit(NotificationsEvent.NavigateToOrderDetail(orderID))
        }
    }

    fun readNotification(notification: Notification) {
        viewModelScope.launch {
            notification.orderId?.let { navigateToOrderDetail(it) }
            repository.markNotificationRead(notification.id)
        }
    }

    fun clearExpiredSession() = session.clear()

    fun getNotifications() {
        viewModelScope.launch {
            val current = _state.value as? NotificationsState.Success
            _state.value = current?.copy(isRefreshing = true, refreshError = null)
                ?: NotificationsState.Loading
            when (val result = repository.refreshNotifications()) {
                CachedResourceRepository.RefreshResult.Success -> {
                    (_state.value as? NotificationsState.Success)?.let {
                        _state.value = it.copy(isRefreshing = false, refreshError = null)
                    }
                }
                is CachedResourceRepository.RefreshResult.Failure -> {
                    val cached = _state.value as? NotificationsState.Success
                    _state.value = cached?.copy(isRefreshing = false, refreshError = result.message)
                        ?: NotificationsState.Error(result.message)
                }
            }
        }
    }

    sealed class NotificationsEvent {
        data class NavigateToOrderDetail(val orderID: String) : NotificationsEvent()
    }

    sealed class NotificationsState {
        object Loading : NotificationsState()
        data class Success(
            val data: List<Notification>,
            val refreshError: String? = null,
            val isRefreshing: Boolean = false
        ) : NotificationsState()
        data class Error(val message: String) : NotificationsState()
    }
}
