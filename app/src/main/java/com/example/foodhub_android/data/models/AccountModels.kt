package com.example.foodhub_android.data.models

data class AccountProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val vehicleType: String? = null,
    val vehiclePlate: String? = null
)

data class UpdateAccountRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val vehicleType: String? = null,
    val vehiclePlate: String? = null
)

data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
