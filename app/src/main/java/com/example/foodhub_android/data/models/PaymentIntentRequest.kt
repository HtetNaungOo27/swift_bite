package com.example.foodhub_android.data.models

data class PaymentIntentRequest(
    val addressId: String,
    val idempotencyKey: String,
    val fulfillmentType: String = "DELIVERY",
    val scheduledFor: String? = null
)
