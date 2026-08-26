package com.example.foodhub_android.ui.feature.orders

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
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
import com.example.foodhub_android.data.models.RiderDelivery
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.ui.navigation.RiderOrderDetails
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RiderOrdersScreen(navController: NavController, viewModel: RiderOrdersViewModel = hiltViewModel()) {
    FoodHubPage {
        FoodHubHeader("Active deliveries", "Orders you’ve accepted")
        when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
            RiderOrdersViewModel.State.Loading -> StatePane("Loading deliveries", "Getting your active orders", loading = true)
            is RiderOrdersViewModel.State.Error -> StatePane("Couldn’t load deliveries", state.message, Icons.Rounded.Refresh, actionLabel = "Retry", onAction = viewModel::refresh)
            is RiderOrdersViewModel.State.Success -> {
                if (state.orders.isEmpty()) StatePane("Ready for your next route", "Accept a nearby job and it’ll appear here.", Icons.Rounded.AssignmentTurnedIn)
                else LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(state.orders, key = { it.orderId }) { order ->
                        ElevatedCard(
                            Modifier.fillMaxWidth().clickable { navController.navigate(RiderOrderDetails(order.orderId)) },
                            shape = MaterialTheme.shapes.large
                        ) {
                            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("#${order.orderId.takeLast(8)}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                                        StatusPill(order.status)
                                    }
                                    Text(order.restaurant.name, style = MaterialTheme.typography.titleLarge)
                                    Text("${order.customer.addressLine1}, ${order.customer.city}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Rounded.ChevronRight, null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiderOrderDetailsScreen(navController: NavController, viewModel: RiderOrderDetailsViewModel = hiltViewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) { viewModel.events.collectLatest { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        RiderOrderDetailsViewModel.State.Loading -> FoodHubPage { FoodHubHeader("Delivery details", onBack = navController::popBackStack); StatePane("Loading delivery", "Getting the latest order details", loading = true) }
        RiderOrderDetailsViewModel.State.Completed -> FoodHubPage { FoodHubHeader("Delivery details", onBack = navController::popBackStack); StatePane("Delivery complete", "Great work—this order is finished", Icons.Rounded.AssignmentTurnedIn) }
        is RiderOrderDetailsViewModel.State.Error -> FoodHubPage { FoodHubHeader("Delivery details", onBack = navController::popBackStack); StatePane("Couldn’t load delivery", state.message, Icons.Rounded.Refresh, actionLabel = "Retry", onAction = viewModel::refresh) }
        is RiderOrderDetailsViewModel.State.Success -> DeliveryDetails(state.order, false, viewModel, navController::popBackStack)
        is RiderOrderDetailsViewModel.State.Updating -> DeliveryDetails(state.order, true, viewModel, navController::popBackStack)
    }
}

@Composable
private fun DeliveryDetails(order: RiderDelivery, updating: Boolean, viewModel: RiderOrderDetailsViewModel, onBack: () -> Unit) {
    FoodHubPage {
        FoodHubHeader("Delivery details", "Order #${order.orderId.takeLast(8)}", onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Current status", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            StatusPill(order.status)
                        }
                        HorizontalDivider()
                        AddressBlock("Pickup", order.restaurant.name, order.restaurant.address)
                        AddressBlock("Drop-off", "Customer", "${order.customer.addressLine1}, ${order.customer.city}")
                        order.customer.landmark?.takeIf { it.isNotBlank() }?.let { Text("Landmark: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        order.customer.plusCode?.takeIf { it.isNotBlank() }?.let { Text("Plus code: $it", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
                        if (order.paymentMethod == "COD") {
                            StatusPill("COD • Collect $${"%.2f".format(order.totalAmount)}")
                        }
                    }
                }
            }
            item { Text("Order summary", style = MaterialTheme.typography.titleLarge) }
            items(order.items, key = { it.id }) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("${it.quantity}×", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp)); Text(it.name, Modifier.weight(1f)); Text("$${"%.2f".format(it.price)}")
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Your earning", style = MaterialTheme.typography.titleMedium)
                    Text("$${"%.2f".format(order.estimatedEarning)}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
        DeliveryActionBar(order.status, updating, viewModel)
    }
}

@Composable
private fun DeliveryActionBar(status: String, updating: Boolean, viewModel: RiderOrderDetailsViewModel) {
    Surface(tonalElevation = 8.dp, shadowElevation = 10.dp) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (status) {
                "ASSIGNED" -> Button(
                    onClick = viewModel::markPickedUp,
                    enabled = !updating,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
                ) { Text(if (updating) "Updating…" else "Picked up — start delivery") }
                "OUT_FOR_DELIVERY" -> {
                    Button(onClick = viewModel::markDelivered, enabled = !updating, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) { Text("Mark as delivered") }
                    OutlinedButton(onClick = viewModel::markFailed, enabled = !updating, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Report delivery issue") }
                }
            }
        }
    }
}

@Composable
private fun AddressBlock(label: String, title: String, address: String) {
    Row {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Rounded.LocationOn, null, Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(title, style = MaterialTheme.typography.titleMedium); Text(address, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
