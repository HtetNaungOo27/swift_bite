package com.example.foodhub_android.notification

import android.app.PendingIntent
import android.content.Intent
import com.example.foodhub_android.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FoodHubMessagingService : FirebaseMessagingService() {
    @Inject lateinit var notificationManager: FoodHubNotificationManager

    override fun onNewToken(token: String) {
        notificationManager.updateToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val orderId = message.data[ORDER_ID]
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            orderId?.let { putExtra(ORDER_ID, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            orderId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationManager.showOrderNotification(
            title = message.notification?.title ?: message.data["title"] ?: "SwiftBite",
            message = message.notification?.body ?: message.data["message"] ?: "You have an order update",
            pendingIntent = pendingIntent,
            notificationId = orderId?.hashCode() ?: message.messageId?.hashCode() ?: 1,
            type = message.data["type"].orEmpty()
        )
    }

    companion object {
        const val ORDER_ID = "orderId"
    }
}
