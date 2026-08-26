package com.example.foodhub_android.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val country: String,
    val id: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val state: String,
    val userId: String? = null,
    val zipCode: String,
    val landmark: String? = null,
    val plusCode: String? = null
) : Parcelable
