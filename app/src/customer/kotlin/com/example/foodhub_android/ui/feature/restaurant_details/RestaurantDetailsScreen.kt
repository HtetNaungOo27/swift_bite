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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.foodhub_android.data.models.FoodItem
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
    }
    val uiState = viewModel.uiState.collectAsState()
    LazyVerticalGrid(GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            RestaurantDetailHeader(
                imageUrl = imageUrl,
                restaurantID = restaurantID,
                onBackButton = { navController.popBackStack() },
                onFavoriteButton = {},
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            RestaurantDetails(
                title = name,
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed ut purus eget sapien fermentum aliquam. Nam sollicitudin interdum risus.",
                restaurantID = restaurantID,
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
}

@Composable
fun RestaurantDetails(title: String, description: String, restaurantID: String) {
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
                text = "4.5",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "(30+)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.size(8.dp))
            TextButton(onClick = {/*TODO*/ }) {
                Text(
                    text = "View All Reviews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
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

@Composable
fun RestaurantDetailHeader(

    imageUrl: String,
    onBackButton: () -> Unit,
    onFavoriteButton: () -> Unit,
    restaurantID: String
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
            Image(painter = painterResource(id = R.drawable.favorite), contentDescription = null)
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
            .background(Color.White)
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
                    .background(Color.White)
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
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4.5", style = MaterialTheme.typography.titleSmall, maxLines = 1
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "(21)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
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
