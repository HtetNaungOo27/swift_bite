package com.example.foodhub_android.data

import android.content.Context
import android.util.Base64
import com.example.foodhub_android.data.local.RoomCacheStore
import org.json.JSONObject
import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FoodHubSession(
    context: Context,
    private val cacheStore: RoomCacheStore
) {
    private val packageName = context.packageName
    private val preferences = context.getSharedPreferences("foodhub", Context.MODE_PRIVATE)
    private val _cacheScope = MutableStateFlow(cacheScopeKey(preferences.getString("token", null)))
    val cacheScope = _cacheScope.asStateFlow()

    fun storeToken(token: String) {
        preferences.edit().putString("token", token).apply()
        _cacheScope.value = cacheScopeKey(token)
    }

    fun getToken(): String? {
        val token = preferences.getString("token", null) ?: return null
        if (isExpired(token)) {
            cacheStore.purgeScope(cacheScopeKey(token))
            preferences.edit().remove("token").apply()
            _cacheScope.value = cacheScopeKey(null)
            return null
        }
        return token
    }

    fun storeRestaurantId(restaurantId: String) {
        preferences.edit().putString("restaurantId", restaurantId).apply()
    }

    fun getRestaurantId(): String? = preferences.getString("restaurantId", null)

    fun storePendingOrderId(orderId: String) {
        preferences.edit().putString("pendingOrderId", orderId).apply()
    }

    fun consumePendingOrderId(): String? {
        val orderId = preferences.getString("pendingOrderId", null)
        preferences.edit().remove("pendingOrderId").apply()
        return orderId
    }

    fun clear() {
        getToken()?.let { cacheStore.purgeScope(cacheScopeKey(it)) }
        preferences.edit().clear().apply()
        _cacheScope.value = cacheScopeKey(null)
    }

    /** A non-reversible, flavor-aware key keeps one user's cache invisible to every other login. */
    fun cacheScopeKey(): String = cacheScopeKey(getToken())

    private fun cacheScopeKey(token: String?): String {
        val userId = token?.let(::readUserId) ?: "signed-out"
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$packageName:$userId".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun readUserId(token: String): String = try {
        val payload = token.split('.')[1]
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        JSONObject(json).optString("userId").ifBlank { "invalid-token" }
    } catch (_: Exception) {
        "invalid-token"
    }

    private fun isExpired(token: String): Boolean = try {
        val payload = token.split('.')[1]
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        val expiresAtMillis = JSONObject(json).getLong("exp") * 1_000L
        System.currentTimeMillis() >= expiresAtMillis
    } catch (_: Exception) {
        true
    }
}
