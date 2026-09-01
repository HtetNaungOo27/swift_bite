package com.example.foodhub_android.data.models

data class Order(
    val address: Address,
    val createdAt: String,
    val id: String,
    val items: List<OrderItem> = emptyList(),
    val paymentStatus: String,
    val restaurant: Restaurant,
    val restaurantId: String,
    val riderId: String? = null,
    val riderName: String? = null,
    val status: String,
    val stripePaymentIntentId: String,
    val totalAmount: Double,
    val specialInstructions: String? = null,
    val riderInstructions: String? = null,
    val preparationMinutes: Int? = null,
    val deliveryOtp: String? = null,
    val rejectionReason: String? = null,
    val fulfillmentType: String = "DELIVERY",
    val scheduledFor: String? = null,
    val updatedAt: String,
    val userId: String,
    val paymentMethod: String = "CARD",
    val codCollected: Boolean = false
)

data class OrderActionRequest(val action: String, val reason: String? = null)
