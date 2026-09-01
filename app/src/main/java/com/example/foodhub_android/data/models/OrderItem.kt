package com.example.foodhub_android.data.models

data class OrderItem(
    val id: String,
    val menuItemId: String,
    val menuItemName: String,
    val orderId: String,
    val quantity: Int,
    val selectedModifiers: List<SelectedModifier> = emptyList()
)
