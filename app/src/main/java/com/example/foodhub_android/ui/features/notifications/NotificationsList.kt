package com.example.foodhub_android.ui.features.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.Notification
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.ui.navigation.OrderDetails
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.ui.navigation.RiderOrderDetails
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NotificationsList(navController: NavController, viewModel: NotificationsViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(Unit) {
        viewModel.getNotifications()
        viewModel.event.collectLatest {
            if (it is NotificationsViewModel.NotificationsEvent.NavigateToOrderDetail) {
                if (navController.context.packageName.endsWith(".rider")) navController.navigate(RiderOrderDetails(it.orderID))
                else navController.navigate(OrderDetails(it.orderID))
            }
        }
    }
    FoodHubPage {
        FoodHubHeader("Notifications", "Order updates and important alerts")
        when (state) {
            NotificationsViewModel.NotificationsState.Loading -> StatePane("Loading alerts", "Getting your latest updates", loading = true)
            is NotificationsViewModel.NotificationsState.Error -> {
                val expired = state.message.contains("session has expired", ignoreCase = true)
                StatePane(
                    if (expired) "Please sign in again" else "Couldn’t load alerts",
                    state.message,
                    Icons.Rounded.ErrorOutline,
                    actionLabel = if (expired) "Go to sign in" else "Retry",
                    onAction = {
                        if (expired) {
                            viewModel.clearExpiredSession()
                            navController.navigate(AuthScreen) { popUpTo(navController.graph.id) { inclusive = true } }
                        } else viewModel.getNotifications()
                    }
                )
            }
            is NotificationsViewModel.NotificationsState.Success -> {
                if (state.data.isEmpty()) StatePane("No notifications", "New order updates will appear here", Icons.Rounded.NotificationsNone)
                else LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.data, key = { it.id }) { NotificationItem(it) { viewModel.readNotification(it) } }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onRead: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onRead),
        shape = MaterialTheme.shapes.large,
        color = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = if (notification.isRead) 1.dp else 0.dp
    ) {
        Row(Modifier.padding(17.dp)) {
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Rounded.NotificationsNone, null, Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(notification.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(notification.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!notification.isRead) Badge()
        }
    }
}

@Composable
fun LoadingScreen() = StatePane("Loading", "Please wait a moment", loading = true)

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) = StatePane("Something went wrong", message, Icons.Rounded.ErrorOutline, actionLabel = "Retry", onAction = onRetry)
