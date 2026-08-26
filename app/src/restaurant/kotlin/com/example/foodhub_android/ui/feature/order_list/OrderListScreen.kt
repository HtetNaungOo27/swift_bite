package com.example.foodhub_android.ui.feature.order_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.Order
import com.example.foodhub_android.ui.features.notifications.ErrorScreen
import com.example.foodhub_android.ui.features.notifications.LoadingScreen
import com.example.foodhub_android.ui.navigation.OrderDetails
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.StatusPill
import com.example.foodhub_android.ui.components.StatePane
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReceiptLong
import kotlinx.coroutines.launch

@Composable
fun OrderListScreen(
    navController: NavController,
    viewModel: OrdersListViewModel = hiltViewModel()
) {
    val statuses = viewModel.getOrderTypes()
    val pagerState = rememberPagerState(pageCount = { statuses.size })
    val scope = rememberCoroutineScope()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(pagerState.currentPage) {
        viewModel.getOrdersByType(statuses[pagerState.currentPage])
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FoodHubHeader("Orders", "Track every stage from new to delivered")
        PrimaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
            statuses.forEachIndexed { index, status ->
                Tab(
                    selected = index == pagerState.currentPage,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(status.readableStatus()) }
                )
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
            when (state) {
                OrdersListViewModel.OrdersScreenState.Loading -> LoadingScreen()
                OrdersListViewModel.OrdersScreenState.Failed -> ErrorScreen("Failed to load orders") {
                    viewModel.getOrdersByType(statuses[pagerState.currentPage])
                }
                is OrdersListViewModel.OrdersScreenState.Success -> {
                    if (state.data.isEmpty()) {
                        StatePane("The kitchen is quiet", "Take a breath before the next rush. ${statuses[pagerState.currentPage].readableStatus()} orders will appear here.", Icons.Rounded.ReceiptLong)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data, key = { it.id }) { order ->
                                OrderListItem(order) {
                                    navController.navigate(OrderDetails(order.id))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderListItem(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order.id.takeLast(8)}", fontWeight = FontWeight.Bold)
                StatusPill(order.status)
            }
            Text(order.address.addressLine1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$${"%.2f".format(order.totalAmount)}", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun String.readableStatus(): String =
    lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
