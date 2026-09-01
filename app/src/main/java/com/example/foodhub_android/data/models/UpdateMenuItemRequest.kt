package com.example.foodhub_android.data.models

data class UpdateMenuItemRequest(
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String? = null,
    val isAvailable: Boolean? = null,
    val unavailableUntil: String? = null,
    val inventoryQuantity: Int? = null,
    val dietaryTags: List<String>? = null,
    val modifierGroups: List<MenuModifierGroup>? = null
)
