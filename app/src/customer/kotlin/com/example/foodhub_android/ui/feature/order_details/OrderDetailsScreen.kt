package com.example.foodhub_android.ui.feature.order_details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.utils.StringUtils

private val journey = listOf(
    "PENDING_ACCEPTANCE" to "Order placed",
    "ACCEPTED" to "Restaurant accepted",
    "PREPARING" to "Preparing your food",
    "READY" to "Ready for a rider",
    "ASSIGNED" to "Rider assigned",
    "OUT_FOR_DELIVERY" to "Out for delivery",
    "DELIVERED" to "Delivered"
)

@Composable
fun OrderDetailsScreen(
    navController: NavController,
    orderID: String,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(orderID) { viewModel.getOrderDetails(orderID) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.getOrderDetails(orderID) }

    FoodHubPage {
        FoodHubHeader("Order details", "Reference #${orderID.takeLast(8).uppercase()}", onBack = navController::popBackStack)
        when (state) {
            OrderDetailsViewModel.OrderDetailsState.Loading ->
                StatePane("Loading your order", "Checking its latest status", loading = true)
            is OrderDetailsViewModel.OrderDetailsState.Error ->
                StatePane("Couldn’t load order", state.message, Icons.Rounded.Refresh, actionLabel = "Try again") { viewModel.getOrderDetails(orderID) }
            is OrderDetailsViewModel.OrderDetailsState.OrderDetails -> OrderContent(state.order)
        }
    }
}

@Composable
private fun OrderContent(order: Order) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(order.restaurant.name, style = MaterialTheme.typography.titleLarge)
                            Text("${order.items.size} items", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (order.paymentMethod == "COD") "Cash on delivery" else "Paid by card", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(order.status)
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(StringUtils.formatCurrency(order.totalAmount), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item { Text("Delivery progress", style = MaterialTheme.typography.titleLarge) }
        item { OrderJourney(order.status) }
        item { Text("Items", style = MaterialTheme.typography.titleLarge) }
        items(order.items) { item ->
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(15.dp)) {
                    Text("${item.quantity}×", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(item.menuItemName, Modifier.weight(1f))
                }
            }
        }
        item {
            order.address.let { address ->
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Delivering to", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(address.addressLine1, style = MaterialTheme.typography.titleMedium)
                        Text(listOfNotNull(address.addressLine2, address.city, address.state, address.zipCode).filter { it.isNotBlank() }.joinToString(", "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        address.landmark?.takeIf { it.isNotBlank() }?.let { Text("Landmark: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        address.plusCode?.takeIf { it.isNotBlank() }?.let { Text("Plus code: $it", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
        }
        item { Spacer(Modifier.navigationBarsPadding().height(10.dp)) }
    }
}

@Composable
private fun OrderJourney(status: String) {
    val normalized = status.uppercase()
    val failure = normalized in setOf("REJECTED", "CANCELLED", "DELIVERY_FAILED")
    if (failure) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.large) {
            Text(
                normalized.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                Modifier.fillMaxWidth().padding(18.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }
    val currentIndex = journey.indexOfFirst { it.first == normalized }.coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        journey.forEachIndexed { index, (_, label) ->
            val reached = index <= currentIndex
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (reached) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(12.dp))
                Text(label, color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
