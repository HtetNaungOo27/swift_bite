package com.example.foodhub_android.ui.feature.home

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.data.models.Category
import com.example.foodhub_android.data.models.Restaurant
import com.example.foodhub_android.ui.components.StatePane
import com.example.foodhub_android.ui.components.ShimmerBlock
import com.example.foodhub_android.ui.navigation.RestaurantDetails
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest {
            if (it is HomeViewModel.HomeScreenNavigationEvent.NavigateToDetail) {
                navController.navigate(RestaurantDetails(it.id, it.name, it.imageUrl))
            }
        }
    }
    val state = viewModel.uiState.collectAsState().value
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (state) {
            HomeViewModel.HomeScreenState.Loading -> RestaurantFeedShimmer()
            HomeViewModel.HomeScreenState.Empty -> StatePane("Nothing nearby yet", "Try changing your location", Icons.Rounded.Restaurant)
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.LocationOn, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(" Near your saved address", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                                    Text("SB", Modifier.padding(13.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(18.dp))
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
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp).clickable { onClick(restaurant) },
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            Box {
                AsyncImage(restaurant.imageUrl, restaurant.name, Modifier.fillMaxWidth().height(190.dp), contentScale = ContentScale.Crop)
            }
            Column(Modifier.padding(17.dp)) {
                Text(restaurant.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(" 20–30 min", style = MaterialTheme.typography.bodySmall)
                    if (restaurant.distance > 0) {
                        Spacer(Modifier.width(14.dp))
                        Icon(Icons.Rounded.LocationOn, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(" ${"%.1f".format(restaurant.distance)} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
