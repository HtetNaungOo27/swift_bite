package com.example.foodhub_android.ui.features.order_failure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun OrderFailed(){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Order Failed", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "We could not process your payment. Please try again",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}