package com.example.foodhub_android.ui.feature.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.R
import com.example.foodhub_android.data.models.Category
import com.example.foodhub_android.data.models.Restaurant
import com.example.foodhub_android.ui.navigation.RestaurantDetails
import com.example.foodhub_android.ui.theme.Orange
import com.example.foodhub_android.ui.theme.Typography
import kotlinx.coroutines.flow.collectLatest

@Composable

fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest {
            when (it) {
                is HomeViewModel.HomeScreenNavigationEvent.NavigateToDetail->{
                    navController.navigate(RestaurantDetails(it.id, it.name,it.imageUrl))
                }
                else -> {

                }
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        val uiState = viewModel.uiState.collectAsState()

        when(uiState.value){
            is HomeViewModel.HomeScreenState.Loading -> {
                Text(text = "Loading")
            }
            is HomeViewModel.HomeScreenState.Empty -> {
                Text(text = "Empty")
            }
            is HomeViewModel.HomeScreenState.Success -> {
                val categories = viewModel.categories
                CategoryList(categories = categories, onCategorySelected = {
//                    navController.navigate("category/${it.id}")
                })

                RestaurantList(restaurants = viewModel.restaurants, onRestaurantSelected = {
                     viewModel.onRestaurantSelected(it)
                })
            }
        }
    }

}

@Composable
fun CategoryList(categories: List<Category>, onCategorySelected:(Category) -> Unit){
    LazyRow {
        items(categories){
            CategoryItem(category = it, onCategorySelected = onCategorySelected)
        }
    }
}

@Composable
fun RestaurantList(restaurants: List<Restaurant>, onRestaurantSelected:(Restaurant) -> Unit){
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular Restaurants",
                style = Typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = {}) {
                Text(
                    text = "View All",
                    style = Typography.bodySmall
                )
            }
        }
        LazyRow {
            items(restaurants, key = { it.id }) {
                RestaurantItem(it, onRestaurantSelected)
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onCategorySelected: (Category) -> Unit){
    Column(
        modifier = Modifier.padding(8.dp)
            .clip(RoundedCornerShape(45.dp))
            .height(90.dp)
            .width(60.dp)
            .clickable{onCategorySelected(category)}
            .shadow(16.dp, RoundedCornerShape(45.dp),
                ambientColor = Color.Gray.copy(0.8f),
                spotColor = Color.Gray.copy(0.4f))
            .background(color = Color.White)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        AsyncImage(model=category.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
                .clip(CircleShape)
                .shadow(8.dp, CircleShape,
                    ambientColor = Orange,
                    spotColor = Orange),
            contentScale = ContentScale.Inside
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = category.name,
            style = TextStyle(fontSize = 10.sp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RestaurantItem(restaurant: Restaurant, onRestaurantSelected: (Restaurant) -> Unit){
    Box(
        modifier = Modifier
            .padding(8.dp)
            .width(250.dp)
            .height(229.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onRestaurantSelected(restaurant) }
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = restaurant.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Text(
                    text = restaurant.name,
                    style = Typography.titleMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    RestaurantInfo(R.drawable.ic_delivery, "Free Delivery")
                    Spacer(modifier = Modifier.width(12.dp))
                    RestaurantInfo(R.drawable.ic_time, "20-30 min")
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "4.5",
                style = Typography.titleSmall,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Image(
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                colorFilter = ColorFilter.tint(Color.Yellow)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = "(25)",
                style = Typography.bodySmall,
                color = Color.Gray
            )
        }

    }
}

@Composable
private fun RestaurantInfo(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(end = 8.dp)
                .size(12.dp)
        )
        Text(
            text = text,
            style = Typography.bodySmall,
            color = Color.Gray,
            maxLines = 1
        )
    }
}
