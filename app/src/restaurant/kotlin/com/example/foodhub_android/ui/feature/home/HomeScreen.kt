package com.example.foodhub_android.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.ui.features.notifications.ErrorScreen
import com.example.foodhub_android.ui.features.notifications.LoadingScreen
import com.example.foodhub_android.ui.navigation.MenuList
import com.example.foodhub_android.ui.navigation.OrderList
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.data.models.RestaurantStatistics
import com.example.foodhub_android.utils.StringUtils

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val statistics = viewModel.statistics.collectAsStateWithLifecycle().value
    when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
        HomeViewModel.HomeScreenState.Loading -> LoadingScreen()
        HomeViewModel.HomeScreenState.Failed -> ErrorScreen("Failed to load restaurant profile", viewModel::retry)
        is HomeViewModel.HomeScreenState.Success -> {
            val restaurant = state.data
            FoodHubPage {
                FoodHubHeader(
                    "Good day 👋",
                    "Here’s how your restaurant looks today",
                    action = {
                        IconButton(onClick = {
                            viewModel.logout()
                            navController.navigate(AuthScreen) { popUpTo(navController.graph.id) { inclusive = true } }
                        }) { Icon(Icons.AutoMirrored.Rounded.Logout, "Sign out") }
                    }
                )
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                        Box {
                            AsyncImage(restaurant.imageUrl, restaurant.name, Modifier.fillMaxWidth().height(230.dp), contentScale = ContentScale.Crop)
                            Box(Modifier.matchParentSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(.72f)))))
                            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                                Text(restaurant.name, style = MaterialTheme.typography.headlineMedium, color = androidx.compose.ui.graphics.Color.White)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, null, Modifier.size(17.dp), tint = androidx.compose.ui.graphics.Color.White)
                                    Text(" ${restaurant.address}", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.White.copy(.88f))
                                }
                            }
                            StatusPill("Profile active", Modifier.align(Alignment.TopEnd).padding(14.dp))
                        }
                    }
                    Text("Quick actions", style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickAction("Manage orders", "Review and update", Icons.Rounded.ReceiptLong, Modifier.weight(1f)) { navController.navigate(OrderList) }
                        QuickAction("Edit menu", "Items and pricing", Icons.Rounded.RestaurantMenu, Modifier.weight(1f)) { navController.navigate(MenuList) }
                    }
                    when (statistics) {
                        HomeViewModel.StatisticsState.Loading -> AnalyticsLoading()
                        is HomeViewModel.StatisticsState.Error -> AnalyticsError(statistics.message, viewModel::getStatistics)
                        is HomeViewModel.StatisticsState.Success -> AnalyticsSection(statistics.data)
                    }
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.TipsAndUpdates, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(13.dp))
                            Column {
                                Text("Keep your menu fresh", style = MaterialTheme.typography.titleMedium)
                                Text("Great photos and clear descriptions help customers decide faster.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.navigationBarsPadding().height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSection(stats: RestaurantStatistics) {
    Text("Business snapshot", style = MaterialTheme.typography.titleLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard("Completed", stats.totalOrders.toString(), Icons.Rounded.DoneAll, Modifier.weight(1f))
        MetricCard("Revenue", StringUtils.formatCurrency(stats.totalRevenue), Icons.Rounded.Payments, Modifier.weight(1f))
        MetricCard("Avg. order", StringUtils.formatCurrency(stats.averageOrderValue), Icons.Rounded.Receipt, Modifier.weight(1f))
    }
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Orders by stage", style = MaterialTheme.typography.titleMedium)
            if (stats.ordersByStatus.isEmpty()) {
                Text("Order activity will appear after your first order.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                StatusBarChart(stats.ordersByStatus)
                stats.ordersByStatus.entries.sortedByDescending { it.value }.take(4).forEach { (status, count) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(status.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Text(count.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Top selling items", style = MaterialTheme.typography.titleMedium)
            if (stats.popularItems.isEmpty()) Text("Your best sellers will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            stats.popularItems.take(3).forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("${index + 1}", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("${item.totalOrders} sold", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, null, Modifier.size(21.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusBarChart(values: Map<String, Int>) {
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val data = values.values.sortedDescending().take(7)
    val maximum = (data.maxOrNull() ?: 1).coerceAtLeast(1)
    Canvas(Modifier.fillMaxWidth().height(100.dp)) {
        val gap = 8.dp.toPx()
        val width = (size.width - gap * (data.size - 1).coerceAtLeast(0)) / data.size.coerceAtLeast(1)
        data.forEachIndexed { index, count ->
            val left = index * (width + gap)
            drawRoundRect(trackColor, Offset(left, 0f), Size(width, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 3))
            val height = size.height * count / maximum
            drawRoundRect(barColor, Offset(left, size.height - height), Size(width, height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 3))
        }
    }
}

@Composable
private fun AnalyticsLoading() {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp)); Text("Preparing your business snapshot…")
        }
    }
}

@Composable
private fun AnalyticsError(message: String, retry: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.errorContainer) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = retry) { Text("Retry") }
        }
    }
}

@Composable
private fun QuickAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(18.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Icon(icon, null, Modifier.padding(11.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp)); Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
