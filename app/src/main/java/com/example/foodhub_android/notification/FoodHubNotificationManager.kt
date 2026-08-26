package com.example.foodhub_android.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.foodhub_android.R
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.FCMRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodHubNotificationManager @Inject constructor(
    private val foodApi: FoodApi,
    @ApplicationContext private val context: Context
) {
    private val manager = NotificationManagerCompat.from(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        createChannels()
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener(::updateToken)
            .addOnFailureListener { Log.e(TAG, "Unable to obtain FCM token", it) }
    }

    fun updateToken(token: String) {
        scope.launch {
            when (val response = safeApiCall { foodApi.updateToken(FCMRequest(token)) }) {
                is ApiResponse.Success -> Log.d(TAG, "FCM token registered")
                else -> Log.w(TAG, "FCM token registration failed: $response")
            }
        }
    }

    fun showOrderNotification(
        title: String,
        message: String,
        pendingIntent: PendingIntent,
        notificationId: Int
    ) {
        val notification = NotificationCompat.Builder(context, ORDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(notificationId, notification)
    }

    private fun createChannels() {
        val orderChannel = NotificationChannelCompat.Builder(
            ORDER_CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        )
            .setName("Orders")
            .setDescription("New orders and order status updates")
            .build()
        manager.createNotificationChannel(orderChannel)
    }

    private companion object {
        const val TAG = "SwiftBiteNotifications"
        const val ORDER_CHANNEL_ID = "orders"
    }
}
