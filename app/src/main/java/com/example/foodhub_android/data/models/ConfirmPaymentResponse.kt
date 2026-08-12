package com.example.foodhub_android.data.models

data class ConfirmPaymentResponse(
    val clientSecret: String,
    val message: String,
    val orderId: String,
    val orderStatus: String,
    val requiresAction: Boolean,
    val status: String
)