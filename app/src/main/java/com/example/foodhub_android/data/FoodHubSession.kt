package com.example.foodhub_android.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject

class FoodHubSession(context: Context) {
    private val preferences = context.getSharedPreferences("foodhub", Context.MODE_PRIVATE)

    fun storeToken(token: String) {
        preferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        val token = preferences.getString("token", null) ?: return null
        if (isExpired(token)) {
            preferences.edit().remove("token").apply()
            return null
        }
        return token
    }

    fun storeRestaurantId(restaurantId: String) {
        preferences.edit().putString("restaurantId", restaurantId).apply()
    }

    fun getRestaurantId(): String? = preferences.getString("restaurantId", null)

    private fun isExpired(token: String): Boolean = try {
        val payload = token.split('.')[1]
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        val expiresAtMillis = JSONObject(json).getLong("exp") * 1_000L
        System.currentTimeMillis() >= expiresAtMillis
    } catch (_: Exception) {
        true
    }
}
