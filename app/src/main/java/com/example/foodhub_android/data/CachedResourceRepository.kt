package com.example.foodhub_android.data

import com.example.foodhub_android.data.local.RoomCacheStore
import com.example.foodhub_android.data.models.AvailableDelivery
import com.example.foodhub_android.data.models.Notification
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CachedResourceRepository @Inject constructor(
    private val api: FoodApi,
    private val session: FoodHubSession,
    private val store: RoomCacheStore
) {
    private val gson = Gson()

    fun notifications(): Flow<NotificationCache?> = observe(NOTIFICATIONS, NotificationCache::class.java)
    fun orders(): Flow<OrderCache?> = observe(ORDERS, OrderCache::class.java)
    fun availableDeliveries(): Flow<DeliveryCache?> = observe(DELIVERIES, DeliveryCache::class.java)

    suspend fun refreshNotifications(): RefreshResult {
        val requestScope = session.cacheScopeKey()
        return when (val result = safeApiCall { api.getNotifications() }) {
            is ApiResponse.Success -> save(
                requestScope,
                NOTIFICATIONS,
                NotificationCache(result.data.notifications, result.data.unreadCount)
            )
            is ApiResponse.Error -> RefreshResult.Failure(result.message ?: "Unable to load notifications")
            is ApiResponse.Exception -> RefreshResult.Failure("We couldn’t connect. Check your internet and try again.")
        }
    }

    suspend fun markNotificationRead(id: String): RefreshResult =
        when (val result = safeApiCall { api.readNotification(id) }) {
            is ApiResponse.Success -> refreshNotifications()
            is ApiResponse.Error -> RefreshResult.Failure(result.message ?: "Unable to update notification")
            is ApiResponse.Exception -> RefreshResult.Failure("We couldn’t connect. Check your internet and try again.")
        }

    suspend fun refreshOrders(): RefreshResult {
        val requestScope = session.cacheScopeKey()
        return when (val result = safeApiCall { api.getOrders() }) {
        is ApiResponse.Success -> save(requestScope, ORDERS, OrderCache(result.data.orders))
        is ApiResponse.Error -> RefreshResult.Failure(result.message ?: "Unable to load orders")
        is ApiResponse.Exception -> RefreshResult.Failure("We couldn’t connect. Check your internet and try again.")
        }
    }

    suspend fun refreshAvailableDeliveries(): RefreshResult {
        val requestScope = session.cacheScopeKey()
        return when (val result = safeApiCall { api.getAvailableDeliveries() }) {
            is ApiResponse.Success -> save(requestScope, DELIVERIES, DeliveryCache(result.data.data))
            is ApiResponse.Error -> RefreshResult.Failure(result.message ?: "Unable to refresh deliveries")
            is ApiResponse.Exception -> RefreshResult.Failure("We couldn’t connect. Check your internet and try again.")
        }
    }

    private fun <T> observe(resource: String, type: Class<T>): Flow<T?> =
        session.cacheScope.flatMapLatest { scope ->
            store.observe(scope, resource)
        }.map { payload -> payload?.let { runCatching { gson.fromJson(it, type) }.getOrNull() } }

    private suspend fun save(scope: String, resource: String, value: Any): RefreshResult {
        if (scope != session.cacheScopeKey()) return RefreshResult.Failure("Account changed during refresh. Please retry.")
        store.write(scope, resource, gson.toJson(value))
        return RefreshResult.Success
    }

    data class NotificationCache(val items: List<Notification>, val unreadCount: Int)
    data class OrderCache(val items: List<Order>)
    data class DeliveryCache(val items: List<AvailableDelivery>)

    sealed interface RefreshResult {
        data object Success : RefreshResult
        data class Failure(val message: String) : RefreshResult
    }

    private companion object {
        const val NOTIFICATIONS = "notifications"
        const val ORDERS = "customer-orders"
        const val DELIVERIES = "rider-available-deliveries"
    }
}
