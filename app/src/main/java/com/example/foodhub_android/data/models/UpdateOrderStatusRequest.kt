package com.example.foodhub_android.data.models

data class UpdateOrderStatusRequest(
    val status: String,
    val preparationMinutes: Int? = null
)
