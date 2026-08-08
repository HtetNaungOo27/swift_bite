package com.example.foodhub_android.data.models

data class Address(
    val addressLine1: String,
    val addressLine2: Any,
    val city: String,
    val country: String,
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val state: String,
    val userId: String,
    val zipCode: String
)