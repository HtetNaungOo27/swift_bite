package com.example.foodhub_android.data.models

data class RestaurantStatistics(
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val popularItems: List<PopularItem> = emptyList(),
    val ordersByStatus: Map<String, Int> = emptyMap(),
    val revenueByDay: List<DailyRevenue> = emptyList()
)

data class PopularItem(
    val id: String,
    val name: String,
    val totalOrders: Int,
    val revenue: Double
)

data class DailyRevenue(
    val date: String,
    val revenue: Double,
    val orders: Int
)
