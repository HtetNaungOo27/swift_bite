package com.example.foodhub_android.data

import android.content.Context

class FoodHubSession(context: Context) {
    private val preferences = context.getSharedPreferences("foodhub", Context.MODE_PRIVATE)

    fun storeToken(token: String) {
        preferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? = preferences.getString("token", null)
}
