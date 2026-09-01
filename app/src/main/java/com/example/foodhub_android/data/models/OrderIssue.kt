package com.example.foodhub_android.data.models

data class CreateOrderIssueRequest(val type: String, val description: String)

data class OrderIssue(
    val id: String,
    val orderId: String,
    val type: String,
    val description: String,
    val status: String,
    val resolution: String? = null,
    val createdAt: String
)

data class OrderIssueResponse(val issue: OrderIssue? = null)
