package com.example.foodhub_android.data.models

data class RiderWallet(
    val completedDeliveries: Int,
    val deliveryEarnings: Double,
    val cashCollected: Double,
    val amountToSettle: Double,
    val lastSettlementMessage: String
)
