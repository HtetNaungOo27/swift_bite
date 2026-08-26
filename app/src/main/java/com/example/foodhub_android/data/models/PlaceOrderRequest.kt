package com.example.foodhub_android.data.models

data class PlaceOrderRequest(val addressId: String, val paymentMethod: String = "COD")
data class PlaceOrderResponse(val id: String, val message: String)
