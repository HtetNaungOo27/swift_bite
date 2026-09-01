package com.example.foodhub_android.data.models

data class AddToCartRequest(
    val restaurantId : String,
    val menuItemId : String,
    val quantity: Int,
    val selectedModifiers: List<SelectedModifier> = emptyList()
)
data class SelectedModifier(val group: String, val option: String)
