package com.example.foodhub_android.ui.feature.order_details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.ui.features.notifications.ErrorScreen
import com.example.foodhub_android.ui.features.notifications.LoadingScreen
import kotlinx.coroutines.flow.collectLatest
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.FoodHubPage
import com.example.foodhub_android.ui.components.StatePane
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun OrderDetailsScreen(
    orderID: String,
    navController: NavController,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val updating = viewModel.updating.collectAsStateWithLifecycle().value

    LaunchedEffect(orderID) { viewModel.getOrderDetails(orderID) }
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                OrderDetailsViewModel.OrderDetailsEvent.NavigateBack -> navController.popBackStack()
                is OrderDetailsViewModel.OrderDetailsEvent.ShowPopUp ->
                    Toast.makeText(navController.context, event.msg, Toast.LENGTH_SHORT).show()
                null -> Unit
            }
        }
    }

    FoodHubPage {
        FoodHubHeader("Order details", "Order #${orderID.takeLast(8)}", onBack = navController::popBackStack)
        when (state) {
        OrderDetailsViewModel.OrderDetailsUiState.Loading -> StatePane("Loading order", "Getting the latest details", loading = true)
        OrderDetailsViewModel.OrderDetailsUiState.Error -> StatePane("Couldn’t load order", "Unable to load order", Icons.Rounded.Refresh, actionLabel = "Retry") {
            viewModel.getOrderDetails(orderID)
        }
        is OrderDetailsViewModel.OrderDetailsUiState.Success -> {
            val order = state.order
            val preparationMinutes = remember(order.id) { mutableIntStateOf(25) }
            var rejecting by remember(order.id) { mutableStateOf(false) }
            var rejectionReason by remember(order.id) { mutableStateOf("") }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("#${order.id}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(order.status.readableStatus(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(
                        when {
                            order.fulfillmentType == "PICKUP" -> "Customer pickup — hand over only after confirming the order reference"
                            !order.riderName.isNullOrBlank() -> "Assigned rider: ${order.riderName}"
                            order.status.uppercase() == "READY" -> "Waiting for a rider to accept this delivery"
                            else -> "SwiftBite delivery"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (order.status.uppercase() == "ACCEPTED" || order.status.uppercase() == "PREPARING") {
                        PreparationCountdown(order.updatedAt, order.preparationMinutes)
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Delivery address", fontWeight = FontWeight.Bold)
                            Text(order.address.addressLine1)
                            order.address.addressLine2?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            Text(
                                listOf(order.address.city, order.address.state, order.address.zipCode)
                                    .filter { it.isNotBlank() }.joinToString(", "),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            order.address.landmark?.takeIf { it.isNotBlank() }?.let {
                                Text("Landmark: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            order.address.plusCode?.takeIf { it.isNotBlank() }?.let {
                                Text("Plus code: $it", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Total: ${com.example.foodhub_android.utils.StringUtils.formatCurrency(order.totalAmount)}")
                            Text(if (order.paymentMethod == "COD") "Payment: Cash on delivery" else "Payment: Card")
                        }
                    }
                }
                item { Text("Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                order.specialInstructions?.takeIf { it.isNotBlank() }?.let { instructions ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Preparation instructions", fontWeight = FontWeight.Bold)
                                Text(instructions, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
                items(order.items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.menuItemName.orEmpty())
                            if(item.selectedModifiers.isNotEmpty()) Text(item.selectedModifiers.joinToString(" · ") { "${it.group}: ${it.option}" }, style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("×${item.quantity}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    val nextStatuses = viewModel.nextStatuses(order)
                    if (nextStatuses.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Update status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            nextStatuses.forEach { status ->
                                if (status == "ACCEPTED") {
                                    Text("Estimated preparation time", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(15, 25, 40).forEach { minutes ->
                                            FilterChip(
                                                selected = preparationMinutes.intValue == minutes,
                                                onClick = { preparationMinutes.intValue = minutes },
                                                label = { Text("$minutes min") }
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatus(
                                            orderID,
                                            status,
                                            if (status == "ACCEPTED") preparationMinutes.intValue else null
                                        )
                                    },
                                    enabled = !updating,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (updating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.height(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Mark as ${status.readableStatus()}")
                                    }
                                }
                            }
                            if (order.status.uppercase() == "PENDING_ACCEPTANCE") {
                                OutlinedButton(
                                    onClick = { rejecting = true },
                                    enabled = !updating,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Reject order") }
                            }
                        }
                    } else {
                        Text("No further status updates available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (rejecting) {
                val commonReasons = listOf("Item unavailable", "Kitchen is at capacity", "Closing soon", "Unable to fulfil this order")
                AlertDialog(
                    onDismissRequest = { if (!updating) rejecting = false },
                    title = { Text("Why are you rejecting this order?") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("The customer will see this reason.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            commonReasons.forEach { reason ->
                                FilterChip(
                                    selected = rejectionReason == reason,
                                    onClick = { rejectionReason = reason },
                                    label = { Text(reason) }
                                )
                            }
                            OutlinedTextField(
                                value = rejectionReason,
                                onValueChange = { rejectionReason = it.take(500) },
                                label = { Text("Reason") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.rejectOrder(orderID, rejectionReason); rejecting = false },
                            enabled = rejectionReason.isNotBlank() && !updating
                        ) { Text("Confirm rejection") }
                    },
                    dismissButton = { TextButton(onClick = { rejecting = false }) { Text("Keep order") } }
                )
            }
        }
        }
    }
}

@Composable
private fun PreparationCountdown(updatedAt: String, preparationMinutes: Int?) {
    if (preparationMinutes == null) return
    var remaining by remember(updatedAt, preparationMinutes) { mutableIntStateOf(preparationMinutes) }
    LaunchedEffect(updatedAt, preparationMinutes) {
        while (true) {
            val started = runCatching { LocalDateTime.parse(updatedAt.replace(' ', 'T')) }.getOrNull()
            remaining = if (started == null) preparationMinutes else
                (preparationMinutes - Duration.between(started, LocalDateTime.now()).toMinutes().toInt()).coerceAtLeast(0)
            delay(30_000)
        }
    }
    Text(
        if (remaining > 0) "$remaining min preparation time remaining" else "Preparation time reached — update the order",
        style = MaterialTheme.typography.bodySmall,
        color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
    )
}

private fun String.readableStatus(): String =
    lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
