package com.example.foodhub_android.data.models

data class Address(
val id: String? = null,
val userId: String? = null,
val addressLine1: String,
val addressLine2: String? = null,
val city: String,
val state: String,
val zipCode: String,
val country: String,
val latitude: Double? = null,
val longitude: Double? = null
)
