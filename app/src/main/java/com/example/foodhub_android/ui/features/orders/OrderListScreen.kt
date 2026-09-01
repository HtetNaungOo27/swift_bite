package com.example.foodhub_android.ui.features.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.ui.navigation.OrderDetails
import com.example.foodhub_android.utils.StringUtils
import kotlinx.coroutines.flow.collectLatest

private val finishedStatuses = setOf("DELIVERED", "DELIVERY_FAILED", "REJECTED", "CANCELLED")

@Composable
fun OrderListScreen(navController: NavController, viewModel: OrderListViewModel = hiltViewModel()) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.getOrders() }
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is OrderListViewModel.OrderListEvent.NavigateToOrderDetailScreen ->
                    navController.navigate(OrderDetails(event.order.id))
                OrderListViewModel.OrderListEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    FoodHubPage {
        FoodHubHeader("Your orders", "Follow every order from kitchen to doorstep")
        PrimaryTabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(horizontal = 20.dp)) {
            listOf("Active", "History").forEachIndexed { index, label ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(label) })
            }
        }
        when (val value = state) {
            OrderListViewModel.OrderListState.Loading ->
                ListSkeleton(showTabs = false)
            is OrderListViewModel.OrderListState.Error ->
                StatePane("Couldn’t load orders", value.message, Icons.Rounded.Refresh, actionLabel = "Try again", onAction = viewModel::getOrders)
            is OrderListViewModel.OrderListState.OrderList -> {
                val orders = value.orderList.filter {
                    if (selectedTab == 0) it.status.uppercase() !in finishedStatuses
                    else it.status.uppercase() in finishedStatuses
                }
                if (orders.isEmpty()) {
                    StatePane(
                        if (selectedTab == 0) "No active orders" else "No order history",
                        if (selectedTab == 0) "Your next order will appear here." else "Completed and cancelled orders will appear here.",
                        Icons.Rounded.ReceiptLong,
                        actionLabel = if (selectedTab == 0) "Browse restaurants" else "Refresh",
                        onAction = {
                            if (selectedTab == 0) navController.navigate(com.example.foodhub_android.ui.navigation.Home) { launchSingleTop = true }
                            else viewModel.getOrders()
                        }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        value.refreshError?.let { message -> item { NetworkNotice(message, viewModel::getOrders) } }
                        items(orders, key = { it.id }) { order -> OrderListItem(order) { viewModel.navigateToDetails(order) } }
                        item { Spacer(Modifier.navigationBarsPadding().height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderListItem(order: Order, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = order.restaurant.imageUrl,
                    contentDescription = "${order.restaurant.name} restaurant",
                    modifier = Modifier.size(58.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(order.restaurant.name, style = MaterialTheme.typography.titleMedium)
                    Text("Order #${order.id.takeLast(8).uppercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = "Open order details")
            }
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(order.status, Modifier.weight(1f, fill = false))
                Spacer(Modifier.weight(1f))
                Text("${order.items.size} items", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text(StringUtils.formatCurrency(order.totalAmount), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OrderDetailsText(order: Order) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(order.restaurant.name, style = MaterialTheme.typography.titleLarge)
        Text("${order.items.size} items • ${StringUtils.formatCurrency(order.totalAmount)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
