package com.example.foodhub_android

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.viewModels

abstract class BaseFoodHubActivity : ComponentActivity() {
    val viewModel by viewModels<HomeViewModel>()

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
    }
}
