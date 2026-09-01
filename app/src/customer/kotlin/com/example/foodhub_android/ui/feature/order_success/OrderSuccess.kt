package com.example.foodhub_android.ui.feature.order_success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhub_android.ui.navigation.Home
import com.example.foodhub_android.ui.navigation.OrderDetails
import com.example.foodhub_android.ui.navigation.OrderSuccess as OrderSuccessRoute

@Composable
fun OrderSuccess(orderID: String, navController: NavController) {
    val pulse by rememberInfiniteTransition(label = "successPulse").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "successScale"
    )
    val goHome = {
        if (!navController.popBackStack(route = Home, inclusive = false)) {
            navController.navigate(Home) { launchSingleTop = true }
        }
        Unit
    }
    BackHandler(onBack = goHome)
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(.8f))
            Surface(Modifier.size(112.dp).scale(pulse), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Rounded.Check, "Order confirmed", Modifier.padding(28.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(26.dp))
            Text("Your order is confirmed!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(
                "The restaurant received your order. We’ll notify you as it moves through each stage.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(13.dp))
                    Column {
                        Text("Order reference", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("#${orderID.takeLast(8).uppercase()}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = {
                navController.navigate(OrderDetails(orderID)) {
                    popUpTo<OrderSuccessRoute> { inclusive = true }
                    launchSingleTop = true
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Track order status")
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = goHome, modifier = Modifier.fillMaxWidth()) { Text("Back to home") }
        }
    }
}
