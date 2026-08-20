package com.example.foodhub_android.data.models

data class Order(
    val address: Address,
    val createdAt: String,
    val id: String,
    val orderItems: List<OrderItem>,
    val paymentStatus: String,
    val restaurant: Restaurant,
    val restaurantId: String,
    val riderId: Any,
    val status: String,
    val stripePaymentIntentId: String,
    val totalAmount: Double,
    val updatedAt: String,
    val userId: String
)
