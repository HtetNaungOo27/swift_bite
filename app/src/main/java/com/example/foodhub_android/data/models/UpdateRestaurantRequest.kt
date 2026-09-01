package com.example.foodhub_android.data.models

data class UpdateRestaurantRequest(
    val name: String? = null,
    val address: String? = null,
    val imageUrl: String? = null,
    val isOpen: Boolean? = null,
    val isBusy: Boolean? = null,
    val opensAt: String? = null,
    val closesAt: String? = null,
    val deliveryRadiusKm: Double? = null,
    val minimumOrderAmount: Double? = null,
    val phone: String? = null,
    val cuisine: String? = null,
    val deliveryFee: Double? = null
)
