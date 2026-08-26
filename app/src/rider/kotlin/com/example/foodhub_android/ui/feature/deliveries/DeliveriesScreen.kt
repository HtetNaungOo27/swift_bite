package com.example.foodhub_android.ui.feature.deliveries

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.AvailableDelivery
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.FoodHubPage
import com.example.foodhub_android.ui.components.StatePane
import com.example.foodhub_android.ui.components.StatusPill
import com.example.foodhub_android.ui.navigation.RiderOrderDetails
import com.example.foodhub_android.ui.navigation.AuthScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DeliveriesScreen(navController: NavController, viewModel: DeliveriesViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DeliveriesViewModel.DeliveryEvent.Message -> Toast.makeText(navController.context, event.text, Toast.LENGTH_SHORT).show()
                is DeliveriesViewModel.DeliveryEvent.OpenOrder -> navController.navigate(RiderOrderDetails(event.orderId))
            }
        }
    }
    FoodHubPage {
        FoodHubHeader(
            title = "Available jobs",
            subtitle = "Choose a delivery that works for you",
            action = {
                Row {
                    FilledTonalIconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Rounded.Refresh, "Refresh")
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate(AuthScreen) { popUpTo(navController.graph.id) { inclusive = true } }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, "Sign out")
                    }
                }
            }
        )
        when (state) {
            DeliveriesViewModel.DeliveriesState.Loading -> StatePane("Finding nearby jobs", "Looking for ready orders around you", loading = true)
            is DeliveriesViewModel.DeliveriesState.Error -> StatePane("Couldn’t load jobs", state.message, Icons.Rounded.Refresh, actionLabel = "Try again", onAction = viewModel::refresh)
            is DeliveriesViewModel.DeliveriesState.Success -> {
                if (state.deliveries.isEmpty()) StatePane("No deliveries nearby", "Stay tuned—new jobs can arrive any moment.", Icons.Rounded.TwoWheeler, actionLabel = "Refresh", onAction = viewModel::refresh)
                else LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.TwoWheeler, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text("${state.deliveries.size} jobs nearby", style = MaterialTheme.typography.titleMedium)
                                    Text("Accept one to start earning", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    items(state.deliveries, key = { it.orderId }) { delivery ->
                        DeliveryCard(delivery, delivery.orderId in state.busyOrders, viewModel::accept, viewModel::reject)
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DeliveryCard(item: AvailableDelivery, busy: Boolean, accept: (String) -> Unit, reject: (String) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.restaurantName, style = MaterialTheme.typography.titleLarge)
                    Text("Order #${item.orderId.takeLast(8)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
                    Text("$${"%.2f".format(item.estimatedEarning)}", Modifier.padding(horizontal = 14.dp, vertical = 9.dp), fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .55f))
            LocationLine("Pickup", item.restaurantAddress)
            LocationLine("Drop-off", item.customerAddress)
            Text("${"%.1f".format(item.estimatedDistance)} km estimated", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            if (item.paymentMethod == "COD") StatusPill("COD • Collect $${"%.2f".format(item.orderAmount)}")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { reject(item.orderId) }, enabled = !busy, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = MaterialTheme.shapes.medium) { Text("Pass") }
                Button(onClick = { accept(item.orderId) }, enabled = !busy, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = MaterialTheme.shapes.medium) { Text(if (busy) "Accepting…" else "Accept job") }
            }
        }
    }
}

@Composable
private fun LocationLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
