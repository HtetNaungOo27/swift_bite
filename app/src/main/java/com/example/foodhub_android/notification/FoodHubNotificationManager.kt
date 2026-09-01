package com.example.foodhub_android.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.foodhub_android.R
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
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
    private val session: FoodHubSession,
    @ApplicationContext private val context: Context
) {
    private val manager = NotificationManagerCompat.from(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        createChannels()
        if (session.getToken() == null) return
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
        notificationId: Int,
        type: String
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification skipped because permission was not granted")
            return
        }
        val channelId = when (type) {
            "NEW_ORDER", "NEW_DELIVERY" -> ACTION_CHANNEL_ID
            "OUT_FOR_DELIVERY" -> MILESTONE_CHANNEL_ID
            else -> QUIET_CHANNEL_ID
        }
        val priority = when (channelId) {
            ACTION_CHANNEL_ID -> NotificationCompat.PRIORITY_HIGH
            MILESTONE_CHANNEL_ID -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(notificationId, notification)
    }

    private fun createChannels() {
        val actionChannel = NotificationChannelCompat.Builder(
            ACTION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        )
            .setName("New jobs and orders")
            .setDescription("Alerts that need an immediate response")
            .build()
        val milestoneChannel = NotificationChannelCompat.Builder(
            MILESTONE_CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .setName("Delivery milestones")
            .setDescription("Important changes while an order is on its way")
            .build()
        val quietChannel = NotificationChannelCompat.Builder(
            QUIET_CHANNEL_ID,
            NotificationManager.IMPORTANCE_LOW
        )
            .setName("Order updates")
            .setDescription("Quiet progress updates shown without sound")
            .setSound(null, null)
            .build()
        manager.createNotificationChannel(actionChannel)
        manager.createNotificationChannel(milestoneChannel)
        manager.createNotificationChannel(quietChannel)
    }

    private companion object {
        const val TAG = "SwiftBiteNotifications"
        const val ACTION_CHANNEL_ID = "order_actions_v2"
        const val MILESTONE_CHANNEL_ID = "delivery_milestones_v2"
        const val QUIET_CHANNEL_ID = "order_updates_quiet_v2"
    }
}
