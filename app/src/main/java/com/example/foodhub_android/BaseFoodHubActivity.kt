package com.example.foodhub_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.foodhub_android.notification.FoodHubNotificationManager
import com.example.foodhub_android.data.FoodHubSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFoodHubActivity : ComponentActivity() {
    val viewModel by viewModels<HomeViewModel>()
    @Inject lateinit var foodHubNotificationManager: FoodHubNotificationManager
    @Inject lateinit var foodHubSession: FoodHubSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foodHubNotificationManager.initialize()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent, viewModel)
    }

    protected fun processIntent(intent: Intent, viewModel: HomeViewModel) {
        if (intent.hasExtra(ORDER_ID)) {
            intent.getStringExtra(ORDER_ID)?.let { orderId ->
                if (foodHubSession.getToken() == null) foodHubSession.storePendingOrderId(orderId)
                else viewModel.navigateToOrderDetail(orderId)
            }
            intent.removeExtra(ORDER_ID)
        }
    }

    private companion object {
        const val ORDER_ID = "orderId"
    }
}
