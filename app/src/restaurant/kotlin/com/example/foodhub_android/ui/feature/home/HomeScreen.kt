package com.example.foodhub_android.ui.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.foodhub_android.ui.navigation.AppSettings
import com.example.foodhub_android.data.models.RestaurantStatistics
import com.example.foodhub_android.utils.StringUtils
import com.example.foodhub_android.data.models.RestaurantHours

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val statistics = viewModel.statistics.collectAsStateWithLifecycle().value
    val profileUpdate = viewModel.profileUpdate.collectAsStateWithLifecycle().value
    var editingProfile by remember { mutableStateOf(false) }
    var editingHours by remember { mutableStateOf(false) }
    when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
        HomeViewModel.HomeScreenState.Loading -> FoodHubPage { FoodHubHeader("Restaurant dashboard", "Preparing today’s overview"); DetailSkeleton() }
        HomeViewModel.HomeScreenState.Failed -> ErrorScreen("Failed to load restaurant profile", viewModel::retry)
        is HomeViewModel.HomeScreenState.Success -> {
            val restaurant = state.data
            FoodHubPage {
                FoodHubHeader(
                    "Restaurant dashboard",
                    "Here’s how your restaurant looks today",
                    action = {
                        AccountActionsMenu(
                            onSettings = { navController.navigate(AppSettings) },
                            onSignOut = {
                                viewModel.logout()
                                navController.navigate(AuthScreen) { popUpTo(navController.graph.id) { inclusive = true } }
                            }
                        )
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
                            Surface(
                                modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                                shape = MaterialTheme.shapes.large,
                                color = if (restaurant.isOpen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(Modifier.padding(start = 12.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (restaurant.isOpen) "Open" else "Closed", style = MaterialTheme.typography.labelMedium)
                                    Switch(checked = restaurant.isOpen, onCheckedChange = viewModel::setRestaurantOpen)
                                }
                            }
                            IconButton(
                                onClick = { editingProfile = true },
                                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                            ) {
                                Surface(shape = MaterialTheme.shapes.medium, color = androidx.compose.ui.graphics.Color.Black.copy(.45f)) {
                                    Icon(Icons.Rounded.Edit, "Edit restaurant profile", Modifier.padding(9.dp), tint = androidx.compose.ui.graphics.Color.White)
                                }
                            }
                        }
                    }
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Busy mode", style = MaterialTheme.typography.titleMedium)
                                    Text("Pause new orders while the kitchen catches up", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(checked = restaurant.isBusy, onCheckedChange = viewModel::setRestaurantBusy)
                            }
                            HorizontalDivider()
                            Text("Hours: ${restaurant.opensAt}–${restaurant.closesAt}")
                            Text("Delivery area: ${restaurant.deliveryRadiusKm.toInt()} km · Minimum ${StringUtils.formatCurrency(restaurant.minimumOrderAmount)}")
                            Text("Delivery fee: ${StringUtils.formatCurrency(restaurant.deliveryFee)}")
                            restaurant.phone?.takeIf(String::isNotBlank)?.let { Text("Phone: $it") }
                            restaurant.cuisine?.takeIf(String::isNotBlank)?.let { Text("Cuisine: $it") }
                            OutlinedButton(onClick = { editingHours = true }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Rounded.Schedule, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Set weekly hours")
                            }
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
            if (editingProfile) {
                EditRestaurantDialog(
                    restaurant = restaurant,
                    state = profileUpdate,
                    onDismiss = { editingProfile = false; viewModel.clearProfileUpdate() },
                    onSave = viewModel::updateProfile
                )
            }
            if (editingHours) {
                WeeklyHoursDialog(
                    initial = restaurant.weeklyHours.ifEmpty {
                        (1..7).map { RestaurantHours(it, restaurant.opensAt, restaurant.closesAt) }
                    },
                    state = profileUpdate,
                    onDismiss = { editingHours = false; viewModel.clearProfileUpdate() },
                    onSave = viewModel::updateWeeklyHours
                )
            }
        }
    }
}

@Composable
private fun WeeklyHoursDialog(
    initial: List<RestaurantHours>,
    state: HomeViewModel.ProfileUpdateState,
    onDismiss: () -> Unit,
    onSave: (List<RestaurantHours>) -> Unit
) {
    var hours by remember(initial) { mutableStateOf(initial.sortedBy { it.dayOfWeek }) }
    val names = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    if (state is HomeViewModel.ProfileUpdateState.Saved) {
        androidx.compose.runtime.LaunchedEffect(state) { onDismiss() }
    }
    val valid = hours.all {
        it.opensAt.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")) &&
            it.closesAt.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")) &&
            (it.isClosed || it.opensAt != it.closesAt)
    }
    AlertDialog(
        onDismissRequest = { if (state !is HomeViewModel.ProfileUpdateState.Saving) onDismiss() },
        icon = { Icon(Icons.Rounded.Schedule, null) },
        title = { Text("Weekly opening hours") },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Customers can order only during these hours.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                hours.forEachIndexed { index, day ->
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(names[index], Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                Text(if (day.isClosed) "Closed" else "Open", style = MaterialTheme.typography.labelMedium)
                                Switch(
                                    checked = !day.isClosed,
                                    onCheckedChange = { open -> hours = hours.toMutableList().also { it[index] = day.copy(isClosed = !open) } }
                                )
                            }
                            if (!day.isClosed) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        day.opensAt,
                                        { value -> hours = hours.toMutableList().also { it[index] = day.copy(opensAt = value.take(5)) } },
                                        label = { Text("Opens") }, singleLine = true, modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        day.closesAt,
                                        { value -> hours = hours.toMutableList().also { it[index] = day.copy(closesAt = value.take(5)) } },
                                        label = { Text("Closes") }, singleLine = true, modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
                if (state is HomeViewModel.ProfileUpdateState.Error) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(hours) }, enabled = valid && state !is HomeViewModel.ProfileUpdateState.Saving) {
                if (state is HomeViewModel.ProfileUpdateState.Saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Save hours")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = state !is HomeViewModel.ProfileUpdateState.Saving) { Text("Cancel") } }
    )
}

@Composable
private fun EditRestaurantDialog(
    restaurant: com.example.foodhub_android.data.models.Restaurant,
    state: HomeViewModel.ProfileUpdateState,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?, String, String, Double, Double, String, String, Double) -> Unit
) {
    var name by remember(restaurant.id) { mutableStateOf(restaurant.name) }
    var address by remember(restaurant.id) { mutableStateOf(restaurant.address) }
    var replacementImage by remember(restaurant.id) { mutableStateOf<Uri?>(null) }
    var opensAt by remember(restaurant.id) { mutableStateOf(restaurant.opensAt) }
    var closesAt by remember(restaurant.id) { mutableStateOf(restaurant.closesAt) }
    var radius by remember(restaurant.id) { mutableStateOf(restaurant.deliveryRadiusKm.toString()) }
    var minimum by remember(restaurant.id) { mutableStateOf((restaurant.minimumOrderAmount * 1000).toInt().toString()) }
    var phone by remember(restaurant.id) { mutableStateOf(restaurant.phone.orEmpty()) }
    var cuisine by remember(restaurant.id) { mutableStateOf(restaurant.cuisine.orEmpty()) }
    var deliveryFee by remember(restaurant.id) { mutableStateOf((restaurant.deliveryFee * 1000).toInt().toString()) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        replacementImage = it
    }
    if (state is HomeViewModel.ProfileUpdateState.Saved) {
        androidx.compose.runtime.LaunchedEffect(state) { onDismiss() }
    }
    AlertDialog(
        onDismissRequest = { if (state !is HomeViewModel.ProfileUpdateState.Saving) onDismiss() },
        icon = { Icon(Icons.Rounded.Storefront, null) },
        title = { Text("Edit restaurant profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AsyncImage(
                    model = replacementImage ?: restaurant.imageUrl,
                    contentDescription = "Restaurant cover preview",
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = state !is HomeViewModel.ProfileUpdateState.Saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (replacementImage == null) "Change cover photo" else "Choose another photo")
                }
                OutlinedTextField(name, { name = it }, label = { Text("Restaurant name") }, singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, minLines = 2)
                OutlinedTextField(phone, { phone = it.take(40) }, label = { Text("Restaurant phone") }, singleLine = true)
                OutlinedTextField(cuisine, { cuisine = it.take(120) }, label = { Text("Cuisine type") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(opensAt, { opensAt = it.take(5) }, label = { Text("Opens") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(closesAt, { closesAt = it.take(5) }, label = { Text("Closes") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(radius, { radius = it.filter(Char::isDigit).take(2) }, label = { Text("Delivery radius (km)") }, singleLine = true)
                OutlinedTextField(minimum, { minimum = it.filter(Char::isDigit).take(8) }, label = { Text("Minimum order (MMK)") }, singleLine = true)
                OutlinedTextField(deliveryFee, { deliveryFee = it.filter(Char::isDigit).take(8) }, label = { Text("Delivery fee (MMK)") }, singleLine = true)
                if (state is HomeViewModel.ProfileUpdateState.Error) {
                    Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name, address, replacementImage, opensAt, closesAt,
                        radius.toDoubleOrNull() ?: 10.0,
                        (minimum.toDoubleOrNull() ?: 0.0) / 1000.0,
                        phone, cuisine,
                        (deliveryFee.toDoubleOrNull() ?: 0.0) / 1000.0
                    )
                },
                enabled = state !is HomeViewModel.ProfileUpdateState.Saving && opensAt.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")) && closesAt.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))
            ) {
                if (state is HomeViewModel.ProfileUpdateState.Saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = state !is HomeViewModel.ProfileUpdateState.Saving) { Text("Cancel") } }
    )
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
