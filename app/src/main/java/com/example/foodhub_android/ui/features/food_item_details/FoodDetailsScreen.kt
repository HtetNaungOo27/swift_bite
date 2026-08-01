package com.example.foodhub_android.ui.features.food_item_details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhub_android.R
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.ui.features.restaurant_details.RestaurantDetailHeader
import com.example.foodhub_android.ui.features.restaurant_details.RestaurantDetails

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodDetailsScreen(
    navController: NavController,
    foodItem: FoodItem,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val count = remember {
        mutableStateOf(0)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        RestaurantDetailHeader(
            imageUrl = foodItem.imageUrl,
            restaurantID = foodItem.id,
            onBackButton = { /*todo*/ },
            onFavoriteButton = { /*todo*/ } )
        RestaurantDetails(
            title = foodItem.name,
            description = foodItem.description,
            restaurantID = foodItem.id
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$${foodItem.price}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.add),
                    contentDescription = null,
                    modifier = Modifier.size(91.dp).clickable{count.value++ } )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "${count.value}",
                    style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(10.dp))
                Image(painter = painterResource(id = R.drawable.minus),
                    contentDescription = null,
                    modifier = Modifier.size(91.dp).clickable{if(count.value>0) count.value-- })
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { /*todo*/ }, modifier = Modifier.padding(8.dp), contentPadding = PaddingValues(0.dp)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(32.dp)),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cart),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = "Add to Cart".uppercase(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}