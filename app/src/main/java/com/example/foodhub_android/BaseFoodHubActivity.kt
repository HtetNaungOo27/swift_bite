package com.example.foodhub_android

import android.content.Intent
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.example.foodhub_android.notification.FoodHubNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFoodHubActivity : ComponentActivity() {
    val viewModel by viewModels<HomeViewModel>()
    @Inject lateinit var foodHubNotificationManager: FoodHubNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foodHubNotificationManager.initialize()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent, viewModel)
    }

    protected fun processIntent(intent: Intent, viewModel: HomeViewModel) {
        if (intent.hasExtra(ORDER_ID)) {
            intent.getStringExtra(ORDER_ID)?.let(viewModel::navigateToOrderDetail)
            intent.removeExtra(ORDER_ID)
        }
    }

    private companion object {
        const val ORDER_ID = "orderId"
        const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }
}
