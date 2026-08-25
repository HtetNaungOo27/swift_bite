package com.example.foodhub_android.data.models

data class NotificationListResponse(
    val notifications: List<Notification>,
    val unreadCount: Int
)
