package com.example.foodhub_android.data.models

data class ReviewRequest(val rating: Int, val comment: String)
data class RestaurantReview(
    val id: String,
    val userId: String,
    val userName: String,
    val restaurantId: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)
data class ReviewSummary(
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val reviews: List<RestaurantReview> = emptyList()
)
