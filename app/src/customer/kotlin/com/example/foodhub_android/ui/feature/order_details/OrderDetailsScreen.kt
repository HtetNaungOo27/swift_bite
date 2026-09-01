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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.foodhub_android.ui.navigation.Cart
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

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
    val cancelling = viewModel.cancelling.collectAsStateWithLifecycle().value
    val issue = viewModel.issue.collectAsStateWithLifecycle().value
    val submittingIssue = viewModel.submittingIssue.collectAsStateWithLifecycle().value
    val cancelDialog = remember { mutableStateOf(false) }
    val issueDialog = remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(orderID) { viewModel.getOrderDetails(orderID) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.getOrderDetails(orderID) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is OrderDetailsViewModel.OrderDetailsEvent.Message -> snackbar.showSnackbar(event.text)
                OrderDetailsViewModel.OrderDetailsEvent.ReorderReady -> navController.navigate(Cart)
                else -> Unit
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
    FoodHubPage {
        FoodHubHeader("Order details", "Reference #${orderID.takeLast(8).uppercase()}", onBack = navController::popBackStack)
        when (state) {
            OrderDetailsViewModel.OrderDetailsState.Loading ->
                StatePane("Loading your order", "Checking its latest status", loading = true)
            is OrderDetailsViewModel.OrderDetailsState.Error ->
                StatePane("Couldn’t load order", state.message, Icons.Rounded.Refresh, actionLabel = "Try again") { viewModel.getOrderDetails(orderID) }
            is OrderDetailsViewModel.OrderDetailsState.OrderDetails -> OrderContent(
                state.order,
                cancelling,
                issue,
                onCancel = { cancelDialog.value = true },
                onReportIssue = { issueDialog.value = true },
                onReorder = { viewModel.reorder(orderID) },
                onShareReceipt = { shareReceipt(context, state.order) }
            )
        }
    }
    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
    if (cancelDialog.value) AlertDialog(
        onDismissRequest = { cancelDialog.value = false },
        title = { Text("Cancel this order?") },
        text = { Text("You can only cancel before the restaurant accepts it. Card payments will be refunded.") },
        confirmButton = { TextButton(onClick = { cancelDialog.value = false; viewModel.cancelOrder(orderID) }) { Text("Cancel order") } },
        dismissButton = { TextButton(onClick = { cancelDialog.value = false }) { Text("Keep order") } }
    )
    if (issueDialog.value) OrderIssueDialog(
        submitting = submittingIssue,
        onDismiss = { issueDialog.value = false },
        onSubmit = { type, description ->
            viewModel.submitIssue(orderID, type, description)
            issueDialog.value = false
        }
    )
}

@Composable
private fun OrderContent(
    order: Order,
    cancelling: Boolean,
    issue: com.example.foodhub_android.data.models.OrderIssue?,
    onCancel: () -> Unit,
    onReportIssue: () -> Unit,
    onReorder: () -> Unit,
    onShareReceipt: () -> Unit
) {
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
                            Text(if (order.fulfillmentType == "PICKUP") "Customer pickup" else "Delivery", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            order.scheduledFor?.let { Text("Scheduled: ${it.replace('T', ' ')}", style = MaterialTheme.typography.bodySmall) }
                        }
                        StatusPill(order.status)
                    }
                    HorizontalDivider()
                    order.preparationMinutes?.let {
                        Text("Restaurant estimate: $it minutes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (order.status in setOf("ASSIGNED", "OUT_FOR_DELIVERY") && !order.deliveryOtp.isNullOrBlank()) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Delivery confirmation code", style = MaterialTheme.typography.labelLarge)
                                Text(order.deliveryOtp, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                                Text("Tell this code to the rider only when your order arrives.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(StringUtils.formatCurrency(order.totalAmount), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        if (order.status == "PENDING_ACCEPTANCE") {
            item {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !cancelling,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text(if (cancelling) "Cancelling…" else "Cancel order") }
            }
        }
        if (order.status == "REJECTED" && !order.rejectionReason.isNullOrBlank()) {
            item {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.errorContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Restaurant could not accept this order", style = MaterialTheme.typography.titleMedium)
                        Text(order.rejectionReason, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
        if (order.status in setOf("DELIVERED", "REJECTED", "CANCELLED", "DELIVERY_FAILED")) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onReorder, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Reorder") }
                    OutlinedButton(onClick = onShareReceipt, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Receipt") }
                }
            }
        }
        if (issue != null) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = if (issue.status == "OPEN") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Support request", style = MaterialTheme.typography.titleMedium)
                        Text(issue.type.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase))
                        Text(issue.status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        issue.resolution?.takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (order.paymentStatus.startsWith("REFUND")) Text("Payment: ${order.paymentStatus.replace('_', ' ').lowercase()}")
                    }
                }
            }
        } else if (order.status in setOf("DELIVERED", "DELIVERY_FAILED", "REJECTED", "CANCELLED")) {
            item {
                OutlinedButton(onClick = onReportIssue, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text("Report an order issue")
                }
            }
        }
        item { Text("Delivery progress", style = MaterialTheme.typography.titleLarge) }
        item { OrderJourney(order.status) }
        item { Text("Items", style = MaterialTheme.typography.titleLarge) }
        order.specialInstructions?.takeIf { it.isNotBlank() }?.let { instructions ->
            item {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Preparation instructions", style = MaterialTheme.typography.labelLarge)
                        Text(instructions, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
        order.riderInstructions?.takeIf { it.isNotBlank() }?.let { instructions ->
            item {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Delivery instructions", style = MaterialTheme.typography.labelLarge)
                        Text(instructions, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }
        items(order.items) { item ->
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(15.dp)) {
                    Text("${item.quantity}×", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(item.menuItemName); if(item.selectedModifiers.isNotEmpty()) Text(item.selectedModifiers.joinToString(" · ") { "${it.group}: ${it.option}" }, style=MaterialTheme.typography.bodySmall) }
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

private fun shareReceipt(context: android.content.Context, order: Order) {
    val receipt = buildString {
        appendLine("SwiftBite receipt")
        appendLine("Order #${order.id.takeLast(8).uppercase()}")
        appendLine(order.restaurant.name)
        appendLine(order.createdAt.replace('T', ' '))
        appendLine()
        order.items.forEach { appendLine("${it.quantity} × ${it.menuItemName}${if(it.selectedModifiers.isEmpty()) "" else " (${it.selectedModifiers.joinToString { choice -> "${choice.group}: ${choice.option}" }})"}") }
        appendLine()
        appendLine("Total: ${StringUtils.formatCurrency(order.totalAmount)}")
        appendLine("Payment: ${if (order.paymentMethod == "COD") "Cash on delivery" else "Card"}")
        appendLine("Fulfilment: ${if (order.fulfillmentType == "PICKUP") "Customer pickup" else "Delivery"}")
    }
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "SwiftBite receipt #${order.id.takeLast(8)}")
        putExtra(Intent.EXTRA_TEXT, receipt)
    }, "Share receipt"))
}

@Composable
private fun OrderIssueDialog(
    submitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    val types = listOf(
        "MISSING_ITEM" to "Missing item",
        "WRONG_ITEM" to "Wrong item",
        "DAMAGED_ORDER" to "Damaged order",
        "LATE_DELIVERY" to "Late delivery",
        "NEVER_ARRIVED" to "Order never arrived",
        "OTHER" to "Other"
    )
    val selected = remember { mutableStateOf(types.first().first) }
    val description = remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report an order issue") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                types.forEach { (value, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected.value == value, onClick = { selected.value = value })
                        Text(label)
                    }
                }
                OutlinedTextField(
                    value = description.value,
                    onValueChange = { description.value = it.take(2000) },
                    label = { Text("What happened?") },
                    minLines = 3,
                    supportingText = { Text("${description.value.length}/2000") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(selected.value, description.value) },
                enabled = !submitting && description.value.trim().length >= 10
            ) { Text(if (submitting) "Submitting…" else "Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
