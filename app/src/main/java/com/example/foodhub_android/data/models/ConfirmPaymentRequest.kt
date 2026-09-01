package com.example.foodhub_android.data.models

data class ConfirmPaymentRequest(
    val paymentIntentId : String,
    val addressId : String,
    val specialInstructions: String? = null,
    val riderInstructions: String? = null,
    val fulfillmentType: String = "DELIVERY",
    val scheduledFor: String? = null
)
