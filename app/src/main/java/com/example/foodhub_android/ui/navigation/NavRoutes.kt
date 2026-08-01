package com.example.foodhub_android.ui.navigation

import com.example.foodhub_android.data.models.FoodItem
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object SignUp

@Serializable
object AuthScreen
@Serializable
object Home

@Serializable
data class RestaurantDetails(
    val restaurantId: String,
    val restaurantName : String,
    val restaurantImageUrl: String,
)

@Serializable
data class FoodDetails(val foodItem: FoodItem)