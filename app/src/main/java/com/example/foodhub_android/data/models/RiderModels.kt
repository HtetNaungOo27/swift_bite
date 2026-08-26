package com.example.foodhub_android.data.models

data class AvailableDeliveriesResponse(val data: List<AvailableDelivery>)

data class AvailableDelivery(
    val orderId: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val customerAddress: String,
    val orderAmount: Double,
    val estimatedDistance: Double,
    val estimatedEarning: Double,
    val createdAt: String,
    val paymentMethod: String = "CARD"
)

data class RiderDeliveriesResponse(val data: List<RiderDelivery>)

data class RiderDelivery(
    val orderId: String,
    val status: String,
    val restaurant: RiderRestaurant,
    val customer: RiderCustomerAddress,
    val items: List<RiderOrderItem>,
    val totalAmount: Double,
    val estimatedEarning: Double,
    val createdAt: String,
    val updatedAt: String,
    val paymentMethod: String = "CARD"
)

data class RiderRestaurant(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String
)

data class RiderCustomerAddress(
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val state: String? = null,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    val landmark: String? = null,
    val plusCode: String? = null
)

data class RiderOrderItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double
)

data class DeliveryStatusUpdate(
    val status: String,
    val reason: String? = null
)
