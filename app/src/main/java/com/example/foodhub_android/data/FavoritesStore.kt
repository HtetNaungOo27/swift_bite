package com.example.foodhub_android.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("foodhub_favorites", Context.MODE_PRIVATE)

    fun contains(foodItemId: String): Boolean = preferences.getBoolean(foodItemId, false)

    fun toggle(foodItemId: String): Boolean {
        val favorite = !contains(foodItemId)
        preferences.edit().putBoolean(foodItemId, favorite).apply()
        return favorite
    }
}
