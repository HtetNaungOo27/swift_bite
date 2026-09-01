package com.example.foodhub_android.data.models

data class Restaurant(
    val address: String,
    val categoryId: String,
    val createdAt: String,
    val distance: Double,
    val id: String,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val ownerId: String,
    val isOpen: Boolean = true,
    val isBusy: Boolean = false,
    val opensAt: String = "08:00",
    val closesAt: String = "22:00",
    val deliveryRadiusKm: Double = 10.0,
    val minimumOrderAmount: Double = 0.0,
    val weeklyHours: List<RestaurantHours> = emptyList(),
    val phone: String? = null,
    val cuisine: String? = null,
    val deliveryFee: Double = 1.5
)

data class RestaurantHours(
    val dayOfWeek: Int,
    val opensAt: String = "08:00",
    val closesAt: String = "22:00",
    val isClosed: Boolean = false
)

data class UpdateRestaurantHoursRequest(val hours: List<RestaurantHours>)
