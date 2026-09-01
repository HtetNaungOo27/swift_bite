package com.example.foodhub_android.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FoodItem(
    val arModelUrl: String? = null,
    val createdAt: String = "",
    val description: String,
    val id: String = "",
    val imageUrl: String,
    val name: String,
    val price: Double,
    val restaurantId: String,
    val isAvailable: Boolean = true,
    val unavailableUntil: String? = null,
    val inventoryQuantity: Int = 100,
    val dietaryTags: List<String> = emptyList(),
    val modifierGroups: List<MenuModifierGroup> = emptyList()
)

@Serializable data class MenuModifierGroup(val name: String, val required: Boolean = false, val maxSelections: Int = 1, val options: List<MenuModifierOption> = emptyList())
@Serializable data class MenuModifierOption(val name: String, val additionalPrice: Double = 0.0)
