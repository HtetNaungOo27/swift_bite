package com.example.foodhub_android.ui.feature.orders

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.MapProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.RiderDelivery
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.ui.navigation.RiderOrderDetails
import kotlinx.coroutines.flow.collectLatest
import com.example.foodhub_android.utils.StringUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun RiderOrdersScreen(navController: NavController, viewModel: RiderOrdersViewModel = hiltViewModel()) {
    FoodHubPage {
        FoodHubHeader("Active deliveries", "Orders you’ve accepted")
        when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
            RiderOrdersViewModel.State.Loading -> ListSkeleton()
            is RiderOrdersViewModel.State.Error -> StatePane("Couldn’t load deliveries", state.message, Icons.Rounded.Refresh, actionLabel = "Retry", onAction = viewModel::refresh)
            is RiderOrdersViewModel.State.Success -> {
                if (state.orders.isEmpty()) StatePane(
                    "Ready for your next route",
                    "Accept a nearby job and it’ll appear here.",
                    Icons.Rounded.AssignmentTurnedIn,
                    actionLabel = "Find available jobs",
                    onAction = { navController.navigate(com.example.foodhub_android.ui.navigation.Home) { launchSingleTop = true } }
                )
                else LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.orders, key = { it.orderId }) { order ->
                        ElevatedCard(
                            onClick = { navController.navigate(RiderOrderDetails(order.orderId)) { launchSingleTop = true } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("#${order.orderId.takeLast(8)}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                                        StatusChip(OrderStatus.from(order.status))
                                    }
                                    Text(order.restaurant.name, style = MaterialTheme.typography.titleLarge)
                                    Text("${order.customer.addressLine1}, ${order.customer.city}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Rounded.ChevronRight, "Open delivery details")
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
        RiderOrderDetailsViewModel.State.Loading -> FoodHubPage { FoodHubHeader("Delivery details", onBack = navController::popBackStack); DetailSkeleton() }
        RiderOrderDetailsViewModel.State.Completed -> FoodHubPage { FoodHubHeader("Delivery details", onBack = navController::popBackStack); StatePane("Delivery complete", "Great work—this order is finished", Icons.Rounded.AssignmentTurnedIn) }
        is RiderOrderDetailsViewModel.State.Error -> FoodHubPage { FoodHubHeader("Delivery details", onBack = navController::popBackStack); StatePane("Couldn’t load delivery", state.message, Icons.Rounded.Refresh, actionLabel = "Retry", onAction = viewModel::refresh) }
        is RiderOrderDetailsViewModel.State.Success -> DeliveryDetails(state.order, false, viewModel, navController::popBackStack)
        is RiderOrderDetailsViewModel.State.Updating -> DeliveryDetails(state.order, true, viewModel, navController::popBackStack)
    }
}

@Composable
private fun DeliveryDetails(order: RiderDelivery, updating: Boolean, viewModel: RiderOrderDetailsViewModel, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var cashReceivedText by rememberSaveable(order.orderId) { mutableStateOf("") }
    var deliveryOtp by rememberSaveable(order.orderId) { mutableStateOf("") }
    val isCod = order.paymentMethod.equals("COD", ignoreCase = true)
    val amountDueMmk = order.totalAmount * 1000.0
    val cashReceivedMmk = cashReceivedText.toDoubleOrNull()
    val cashIsValid = cashReceivedMmk != null && cashReceivedMmk >= amountDueMmk
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location ->
                    location?.let { currentLocation = LatLng(it.latitude, it.longitude) }
                }
            } catch (_: SecurityException) {
                locationGranted = false
            }
        }
    }
    FoodHubPage {
        FoodHubHeader("Delivery details", "Order #${order.orderId.takeLast(8)}", onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (order.hasValidRoute()) {
                item {
                    DeliveryRoutePreview(order, locationGranted)
                    if (!locationGranted) {
                        TextButton(onClick = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }) { Text("Enable current location for accurate navigation") }
                    }
                }
            }
            item {
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Current status", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            StatusChip(OrderStatus.from(order.status))
                        }
                        HorizontalDivider()
                        AddressBlock("Pickup", order.restaurant.name, order.restaurant.address)
                        AddressBlock("Drop-off", "Customer", "${order.customer.addressLine1}, ${order.customer.city}")
                        order.customer.landmark?.takeIf { it.isNotBlank() }?.let { Text("Landmark: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        order.customer.plusCode?.takeIf { it.isNotBlank() }?.let { Text("Plus code: $it", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
                        order.riderInstructions?.takeIf { it.isNotBlank() }?.let {
                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.medium) {
                                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text("Customer delivery note", style = MaterialTheme.typography.labelLarge)
                                    Text(it, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                            }
                        }
                        order.preparationMinutes?.let { Text("Restaurant estimate: $it minutes", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        PaymentStatusCard(
                            isCod = isCod,
                            paymentStatus = order.paymentStatus,
                            amountDue = order.totalAmount,
                            canCollect = order.status == "OUT_FOR_DELIVERY",
                            cashReceivedText = cashReceivedText,
                            onCashReceivedChange = { cashReceivedText = it }
                        )
                        if (isCod && order.status == "ASSIGNED") {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "Cash entry unlocks after pickup. Do not collect payment at the restaurant.",
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        if (order.status == "OUT_FOR_DELIVERY") {
                            OutlinedTextField(
                                value = deliveryOtp,
                                onValueChange = { value -> if (value.length <= 4 && value.all(Char::isDigit)) deliveryOtp = value },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Customer delivery code") },
                                supportingText = { Text("Ask the customer for the 4-digit code") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }
            }
            item { Text("Order summary", style = MaterialTheme.typography.titleLarge) }
            items(order.items, key = { it.id }) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("${it.quantity}×", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){Text(it.name);if(it.selectedModifiers.isNotEmpty())Text(it.selectedModifiers.joinToString(" · "){choice->"${choice.group}: ${choice.option}"},style=MaterialTheme.typography.bodySmall)}; Text(StringUtils.formatCurrency(it.price))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Your earning", style = MaterialTheme.typography.titleMedium)
                    Text(StringUtils.formatCurrency(order.estimatedEarning), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
        DeliveryActionBar(
            order = order,
            updating = updating,
            viewModel = viewModel,
            openNavigation = {
                val destination = if (order.status == "ASSIGNED") {
                    LatLng(order.restaurant.latitude, order.restaurant.longitude)
                } else {
                    LatLng(order.customer.latitude, order.customer.longitude)
                }
                val directionsUrl = buildString {
                    append("https://www.google.com/maps/dir/?api=1")
                    currentLocation?.let { append("&origin=${it.latitude},${it.longitude}") }
                    append("&destination=${destination.latitude},${destination.longitude}")
                    append("&travelmode=driving&dir_action=navigate")
                }
                val navigationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directionsUrl)).apply {
                    setPackage("com.google.android.apps.maps")
                }
                runCatching { context.startActivity(navigationIntent) }
                    .onFailure {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:${destination.latitude},${destination.longitude}?q=${destination.latitude},${destination.longitude}")
                            )
                        )
                    }
            },
            completeDelivery = {
                if (isCod) viewModel.markDelivered(deliveryOtp, cashReceivedMmk!! / 1000.0)
                else viewModel.markDelivered(deliveryOtp)
            },
            codReady = deliveryOtp.length == 4 && (!isCod || cashIsValid)
        )
    }
}

@Composable
private fun DeliveryActionBar(
    order: RiderDelivery,
    updating: Boolean,
    viewModel: RiderOrderDetailsViewModel,
    openNavigation: () -> Unit,
    completeDelivery: () -> Unit,
    codReady: Boolean
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (order.hasValidRoute() && order.status in setOf("ASSIGNED", "OUT_FOR_DELIVERY")) {
                OutlinedButton(
                    onClick = openNavigation,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) {
                    Icon(Icons.Rounded.Navigation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (order.status == "ASSIGNED") "Navigate to pickup" else "Navigate to customer")
                }
            }
            when (order.status) {
                "ASSIGNED" -> PrimaryActionButton(
                    text = if (updating) "Updating…" else "Picked up — start delivery",
                    onClick = viewModel::markPickedUp,
                    enabled = !updating,
                    leadingIcon = Icons.Rounded.Route
                )
                "OUT_FOR_DELIVERY" -> {
                    PrimaryActionButton(
                        text = if (order.paymentMethod == "COD") "Confirm cash and complete" else "Mark as delivered",
                        onClick = completeDelivery,
                        enabled = !updating && codReady,
                        leadingIcon = if (order.paymentMethod == "COD") Icons.Rounded.Payments else Icons.Rounded.AssignmentTurnedIn
                    )
                    OutlinedButton(onClick = viewModel::markFailed, enabled = !updating, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Report delivery issue") }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusCard(
    isCod: Boolean,
    paymentStatus: String?,
    amountDue: Double,
    canCollect: Boolean,
    cashReceivedText: String,
    onCashReceivedChange: (String) -> Unit
) {
    val isPaid = paymentStatus?.equals("PAID", ignoreCase = true) ?: !isCod
    val amountDueMmk = amountDue * 1000.0
    val received = cashReceivedText.toDoubleOrNull()
    val shortBy = received?.let { (amountDueMmk - it).coerceAtLeast(0.0) }
    val change = received?.let { (it - amountDueMmk).coerceAtLeast(0.0) }
    val statusColor = if (isPaid) Color(0xFF16865F) else MaterialTheme.colorScheme.error

    Surface(
        color = statusColor.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Payments, null, tint = statusColor)
                Spacer(Modifier.width(8.dp))
                Text(if (isCod) "Cash on Delivery" else "Paid by card", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Surface(color = statusColor, contentColor = Color.White, shape = MaterialTheme.shapes.small) {
                    Text(if (isPaid) "PAID" else "UNPAID", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            if (isCod && !isPaid) {
                Text("Amount to collect", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(StringUtils.formatCurrency(amountDue), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = statusColor)
                if (canCollect) {
                    OutlinedTextField(
                        value = cashReceivedText,
                        onValueChange = { value -> if (value.isEmpty() || value.all(Char::isDigit)) onCashReceivedChange(value) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cash received (MMK)") },
                        placeholder = { Text(formatMmkAmount(amountDueMmk)) },
                        leadingIcon = { Icon(Icons.Rounded.Payments, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = received != null && received < amountDueMmk,
                        supportingText = {
                            Text(when {
                                received == null -> "Enter the exact cash handed to you"
                                received < amountDueMmk -> "Still due: ${formatMmkAmount(shortBy ?: 0.0)}"
                                else -> "Change to return: ${formatMmkAmount(change ?: 0.0)}"
                            })
                        }
                    )
                }
            } else {
                Text("Nothing to collect from the customer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CashCollectionDialog(
    amountDue: Double,
    saving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var receivedText by remember { mutableStateOf("") }
    val received = receivedText.toDoubleOrNull()
    val amountDueMmk = amountDue * 1000.0
    val change = received?.minus(amountDueMmk)
    val valid = received != null && received >= amountDueMmk
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Payments, contentDescription = null) },
        title = { Text("Confirm cash collection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ask the customer for exactly:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(StringUtils.formatCurrency(amountDue), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = receivedText,
                    onValueChange = { value -> if (value.isEmpty() || value.matches(Regex("\\d*(\\.\\d{0,2})?"))) receivedText = value },
                    label = { Text("Cash received (MMK)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = received != null && received < amountDueMmk,
                    supportingText = {
                        Text(
                            when {
                                received == null -> "Enter the amount handed to you"
                                received < amountDueMmk -> "Received amount is short by ${formatMmkAmount(amountDueMmk - received)}"
                                else -> "Return change: ${formatMmkAmount(change ?: 0.0)}"
                            }
                        )
                    }
                )
                Text("Do not complete the delivery until payment is received.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(received!!) }, enabled = valid && !saving) { Text("Cash received") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } }
    )
}

private fun formatMmkAmount(value: Double): String =
    "${java.text.NumberFormat.getNumberInstance(java.util.Locale("my", "MM")).format(value)} Ks"

@Composable
private fun DeliveryRoutePreview(order: RiderDelivery, locationGranted: Boolean) {
    val pickup = LatLng(order.restaurant.latitude, order.restaurant.longitude)
    val dropOff = LatLng(order.customer.latitude, order.customer.longitude)
    val midpoint = LatLng(
        (pickup.latitude + dropOff.latitude) / 2,
        (pickup.longitude + dropOff.longitude) / 2
    )
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(midpoint, routeZoom(pickup, dropOff))
    }
    val pickupMarker = rememberUpdatedMarkerState(pickup)
    val dropOffMarker = rememberUpdatedMarkerState(dropOff)
    val routeColor = MaterialTheme.colorScheme.primary

    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Route, contentDescription = null, tint = routeColor)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Route snapshot", style = MaterialTheme.typography.titleMedium)
                    Text("Pickup to customer • Open Maps for live directions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            GoogleMap(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                cameraPositionState = camera,
                properties = MapProperties(isMyLocationEnabled = locationGranted),
                uiSettings = com.google.maps.android.compose.MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = locationGranted
                )
            ) {
                Marker(pickupMarker, title = "Pickup", snippet = order.restaurant.name)
                Marker(dropOffMarker, title = "Customer")
                Polyline(points = listOf(pickup, dropOff), color = routeColor, width = 9f)
            }
        }
    }
}

private fun RiderDelivery.hasValidRoute(): Boolean =
    restaurant.latitude in -90.0..90.0 && restaurant.longitude in -180.0..180.0 &&
        customer.latitude in -90.0..90.0 && customer.longitude in -180.0..180.0 &&
        !(restaurant.latitude == 0.0 && restaurant.longitude == 0.0) &&
        !(customer.latitude == 0.0 && customer.longitude == 0.0)

private fun routeZoom(first: LatLng, second: LatLng): Float {
    val spread = maxOf(
        kotlin.math.abs(first.latitude - second.latitude),
        kotlin.math.abs(first.longitude - second.longitude)
    )
    return when {
        spread < .01 -> 14f
        spread < .04 -> 12.5f
        spread < .12 -> 11f
        else -> 9.5f
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
