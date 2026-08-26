package com.example.foodhub_android.ui.feature.restaurant_details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.data.models.ReviewSummary
import com.example.foodhub_android.ui.navigation.FoodDetails


@Composable
fun SharedTransitionScope.RestaurantDetailScreen(
    navController: NavController,
    name: String,
    imageUrl: String,
    restaurantID: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: RestaurantViewModel = hiltViewModel(),
) {
    LaunchedEffect(restaurantID) {
        viewModel.getFoodItem((restaurantID))
        viewModel.loadFavorite(restaurantID)
        viewModel.getReviews(restaurantID)
    }
    val uiState = viewModel.uiState.collectAsState()
    val isFavorite = viewModel.isFavorite.collectAsState()
    val reviews = viewModel.reviews.collectAsState()
    val savingReview = viewModel.reviewSaving.collectAsState()
    var showReviews by remember { mutableStateOf(false) }
    LazyVerticalGrid(GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            RestaurantDetailHeader(
                imageUrl = imageUrl,
                restaurantID = restaurantID,
                onBackButton = { navController.popBackStack() },
                onFavoriteButton = { viewModel.toggleFavorite(restaurantID) },
                isFavorite = isFavorite.value,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            RestaurantDetails(
                title = name,
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed ut purus eget sapien fermentum aliquam. Nam sollicitudin interdum risus.",
                restaurantID = restaurantID,
                reviewSummary = reviews.value,
                onViewReviews = { showReviews = true },
                )
        }
        when(uiState.value){
            is RestaurantViewModel.RestaurantEvent.Loading ->  {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Loading")
                    }

                }
            }
            is RestaurantViewModel.RestaurantEvent.Success -> {
                val foodItems =
                    (uiState.value as RestaurantViewModel.RestaurantEvent.Success).foodItems
                if (foodItems.isNotEmpty()) {
                    items(foodItems) { foodItem ->
                        FoodItemView(foodItem = foodItem,
                            animatedVisibilityScope = animatedVisibilityScope) {
                            navController.navigate(
                                FoodDetails(foodItem)
                            )
                        }
                    }
                }
             else{
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(text = "No Food Items")

                    }

                }
            }
            is RestaurantViewModel.RestaurantEvent.Error ->{
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(text = "Error")
                }
            }

            RestaurantViewModel.RestaurantEvent.Nothing -> {}
        }
    }
    if (showReviews) {
        ReviewSheet(
            summary = reviews.value,
            saving = savingReview.value,
            onDismiss = { showReviews = false },
            onSubmit = { rating, comment -> viewModel.saveReview(restaurantID, rating, comment) }
        )
    }
}

@Composable
fun RestaurantDetails(
    title: String,
    description: String,
    restaurantID: String,
    reviewSummary: ReviewSummary? = null,
    onViewReviews: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.size(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = reviewSummary?.let { "%.1f".format(it.averageRating) } ?: "New",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = reviewSummary?.let { "(${it.reviewCount})" } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            if (reviewSummary != null) {
                Spacer(modifier = Modifier.size(8.dp))
                TextButton(onClick = onViewReviews) { Text("View Reviews") }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewSheet(
    summary: ReviewSummary,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxWidth().height(520.dp).padding(horizontal = 20.dp)
        ) {
            item {
                Column {
                    Text("Restaurant Reviews", style = MaterialTheme.typography.headlineSmall)
                    Text("${"%.1f".format(summary.averageRating)} from ${summary.reviewCount} reviews")
                    Row {
                        (1..5).forEach { value ->
                            IconButton(onClick = { rating = value }) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "$value stars",
                                    tint = if (value <= rating) Color(0xFFFFB300) else Color.LightGray
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your review") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSubmit(rating, comment); comment = "" },
                        enabled = !saving && comment.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) { Text(if (saving) "Saving…" else "Submit Review") }
                }
            }
            if (summary.reviews.isEmpty()) {
                item { Text("No reviews yet. Be the first to review this restaurant.") }
            } else {
                items(summary.reviews, key = { it.id }) { review ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                        Text(review.userName, style = MaterialTheme.typography.titleMedium)
                        Text("★".repeat(review.rating), color = Color(0xFFFFB300))
                        Text(review.comment)
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantDetailHeader(

    imageUrl: String,
    onBackButton: () -> Unit,
    onFavoriteButton: () -> Unit,
    restaurantID: String,
    isFavorite: Boolean = false
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = imageUrl, contentDescription = null, modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ),
            contentScale = ContentScale.Crop

        )
        IconButton(
            onClick = onBackButton, modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
        ) {
            Image(painter = painterResource(id = R.drawable.back), contentDescription = null)
        }
        IconButton(
            onClick = onFavoriteButton, modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) Color(0xFFFE724C) else Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    .padding(10.dp)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodItemView(foodItem: FoodItem,animatedVisibilityScope: AnimatedVisibilityScope, onClick: (FoodItem) -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(162.dp)
            .height(216.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Gray.copy(alpha = 0.8f),
                spotColor = Color.Gray.copy(alpha = 0.8f)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable{ onClick.invoke(foodItem) }
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = foodItem.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(147.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "image/${foodItem.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                contentScale = ContentScale.FillWidth,
            )
            Text(
                text = "$${foodItem.price}", style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.TopStart)
            )
            Image(
                painter = painterResource(id = R.drawable.favorite),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .align(Alignment.TopEnd)
            )
        }


        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = foodItem.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = foodItem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "title/${foodItem.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
        }
    }
}
