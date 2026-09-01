package com.example.foodhub_android.ui.feature.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.data.models.Category
import com.example.foodhub_android.data.models.Restaurant
import com.example.foodhub_android.ui.components.StatePane
import com.example.foodhub_android.ui.components.ShimmerBlock
import com.example.foodhub_android.ui.navigation.RestaurantDetails
import com.example.foodhub_android.utils.StringUtils
import kotlinx.coroutines.flow.collectLatest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val locationLabel by viewModel.locationLabel.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) loadDeviceLocation(context, viewModel)
        else viewModel.useYangonFallback("Yangon · using saved service area")
    }
    LaunchedEffect(Unit) {
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            loadDeviceLocation(context, viewModel)
        }
        viewModel.navigationEvent.collectLatest {
            if (it is HomeViewModel.HomeScreenNavigationEvent.NavigateToDetail) {
                navController.navigate(RestaurantDetails(it.id, it.name, it.imageUrl, it.isOpen))
            }
        }
    }
    val state = viewModel.uiState.collectAsState().value
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (state) {
            HomeViewModel.HomeScreenState.Loading -> RestaurantFeedShimmer()
            HomeViewModel.HomeScreenState.Empty -> StatePane(
                "Restaurants are temporarily unavailable",
                "Your service area is set to Yangon. Retry after confirming the backend is running.",
                Icons.Rounded.Restaurant,
                actionLabel = "Reload Yangon restaurants",
                onAction = viewModel::useYangonFallback
            )
            HomeViewModel.HomeScreenState.Success -> {
                val filtered = viewModel.restaurants.filter {
                    (query.isBlank() || it.name.contains(query, ignoreCase = true)) &&
                        (selectedCategory == null || it.categoryId == selectedCategory)
                }
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    item {
                        Column(
                            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer)
                                .statusBarsPadding().padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Good food, delivered", style = MaterialTheme.typography.headlineMedium)
                                    Text("Discover Yangon favourites", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                                    Text("SB", Modifier.padding(13.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(18.dp))
                            Surface(
                                onClick = {
                                    val permitted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (permitted) loadDeviceLocation(context, viewModel)
                                    else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = .78f)
                            ) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(9.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Delivering to", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(locationLabel, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text("Change", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search restaurants or dishes") },
                                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                                singleLine = true,
                                shape = MaterialTheme.shapes.large,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                    item {
                        Text("What are you craving?", Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp), style = MaterialTheme.typography.titleLarge)
                        CategoryList(viewModel.categories, selectedCategory) { selectedCategory = if (selectedCategory == it.id) null else it.id }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Popular nearby", style = MaterialTheme.typography.titleLarge)
                                Text("Top picks around you", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            AssistChip(onClick = { query = "" }, label = { Text("${filtered.size} places") })
                        }
                    }
                    if (filtered.isEmpty()) item { StatePane("No matches", "Try a different search", Icons.Rounded.Search) }
                    else items(filtered, key = { it.id }) { restaurant ->
                        RestaurantCard(restaurant) { viewModel.onRestaurantSelected(it) }
                    }
                }
            }
        }
    }
}

private fun loadDeviceLocation(context: android.content.Context, viewModel: HomeViewModel) {
    val permitted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!permitted) return
    try {
        val cancellation = CancellationTokenSource()
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
            .addOnSuccessListener { location ->
                if (location != null) viewModel.updateLocation(location.latitude, location.longitude)
                else viewModel.useYangonFallback("Yangon · current location unavailable")
            }
            .addOnFailureListener { viewModel.useYangonFallback("Yangon · current location unavailable") }
    } catch (_: SecurityException) {
        viewModel.useYangonFallback("Yangon · location permission unavailable")
    }
}

@Composable
private fun RestaurantFeedShimmer() {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), userScrollEnabled = false) {
        item {
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer)
                    .statusBarsPadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ShimmerBlock(Modifier.width(230.dp).height(32.dp))
                ShimmerBlock(Modifier.width(170.dp).height(16.dp))
                ShimmerBlock(Modifier.fillMaxWidth().height(56.dp))
            }
        }
        item { ShimmerBlock(Modifier.padding(20.dp).fillMaxWidth().height(82.dp)) }
        items(3) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 7.dp)) {
                ShimmerBlock(Modifier.fillMaxWidth().height(190.dp))
                Spacer(Modifier.height(10.dp))
                ShimmerBlock(Modifier.fillMaxWidth(.62f).height(22.dp))
                Spacer(Modifier.height(8.dp))
                ShimmerBlock(Modifier.fillMaxWidth(.4f).height(15.dp))
            }
        }
    }
}

@Composable
private fun CategoryList(categories: List<Category>, selected: String?, onSelect: (Category) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(categories, key = { it.id }) { category ->
            val active = category.id == selected
            Surface(
                modifier = Modifier.width(86.dp).clickable { onSelect(category) },
                shape = MaterialTheme.shapes.large,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                tonalElevation = if (active) 0.dp else 2.dp
            ) {
                Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(category.imageUrl, null, Modifier.size(52.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(7.dp))
                    Text(category.name, style = MaterialTheme.typography.labelSmall, maxLines = 1, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun RestaurantCard(restaurant: Restaurant, onClick: (Restaurant) -> Unit) {
    ElevatedCard(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp).clickable(enabled = restaurant.isOpen) { onClick(restaurant) },
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            Box {
                AsyncImage(restaurant.imageUrl, restaurant.name, Modifier.fillMaxWidth().height(172.dp), contentScale = ContentScale.Crop)
                if (!restaurant.isOpen) {
                    Box(Modifier.matchParentSize().background(androidx.compose.ui.graphics.Color.Black.copy(.55f)), contentAlignment = Alignment.Center) {
                        Text("Currently closed", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Column(Modifier.padding(17.dp)) {
                Text(restaurant.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                restaurant.cuisine?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(" 20–30 min", style = MaterialTheme.typography.bodySmall)
                    if (restaurant.distance > 0) {
                        Spacer(Modifier.width(14.dp))
                        Icon(Icons.Rounded.LocationOn, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(" ${"%.1f".format(restaurant.distance)} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Payments, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (restaurant.deliveryFee <= 0.0) " Free delivery" else " ${StringUtils.formatCurrency(restaurant.deliveryFee)} delivery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (restaurant.minimumOrderAmount > 0) Text(
                        "  ·  Min ${StringUtils.formatCurrency(restaurant.minimumOrderAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
