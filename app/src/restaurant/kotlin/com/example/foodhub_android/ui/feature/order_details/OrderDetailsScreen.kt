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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("#${order.id}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(order.status.readableStatus(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                            Text("Total: $${"%.2f".format(order.totalAmount)}")
                            Text(if (order.paymentMethod == "COD") "Payment: Cash on delivery" else "Payment: Card")
                        }
                    }
                }
                item { Text("Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
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
                            Text("×${item.quantity}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    val nextStatuses = viewModel.nextStatuses(order.status)
                    if (nextStatuses.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Update status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            nextStatuses.forEach { status ->
                                Button(
                                    onClick = { viewModel.updateOrderStatus(orderID, status) },
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
                        }
                    } else {
                        Text("No further status updates available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        }
    }
}

private fun String.readableStatus(): String =
    lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
