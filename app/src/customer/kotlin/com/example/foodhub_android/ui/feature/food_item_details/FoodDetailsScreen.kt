package com.example.foodhub_android.ui.feature.food_item_details

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.R
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.ui.feature.restaurant_details.RestaurantDetailHeader
import com.example.foodhub_android.ui.feature.restaurant_details.RestaurantDetails
import com.example.foodhub_android.ui.navigation.Cart
import kotlinx.coroutines.flow.collectLatest

@OptIn(
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class)

@Composable
fun SharedTransitionScope.FoodDetailsScreen(
    navController: NavController,
    foodItem: FoodItem,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onItemAddedToCart:()->Unit,
    viewModel: FoodDetailsViewModel = hiltViewModel()

) {
    val showSuccessDialog = remember {
        mutableStateOf(false)
    }
    val showErrorDialog = remember {
        mutableStateOf(false)
    }
    val count = viewModel.quantity.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite = viewModel.isFavorite.collectAsStateWithLifecycle()
    val isLoading = remember {
        mutableStateOf(false)
    }
    val selectedModifiers = remember(foodItem.id) { mutableStateMapOf<String, String>() }

    when (uiState.value) {
        FoodDetailsViewModel.FoodDetailsUiState.Loading -> {
            isLoading.value =true
        }
        else -> {
            isLoading.value = false
        }
    }
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest {
            when(it) {
                is FoodDetailsViewModel.FoodDetailsEvent.onAddToCart -> {
                   showSuccessDialog.value = true
                    onItemAddedToCart()

                }
                is FoodDetailsViewModel.FoodDetailsEvent.showErrorDialog -> {
                    showErrorDialog.value = true

                }
                is FoodDetailsViewModel.FoodDetailsEvent.goToCart -> {
                    navController.navigate(Cart)

                }
            }
        }
    }
    LaunchedEffect(foodItem.id) { viewModel.loadFavorite(foodItem.id) }
    Column(modifier = Modifier.fillMaxSize()) {
        RestaurantDetailHeader(
            imageUrl = foodItem.imageUrl,
            restaurantID = foodItem.id,
            onBackButton = {
                navController.popBackStack()
            },
            onFavoriteButton = { viewModel.toggleFavorite(foodItem.id) },
            isFavorite = isFavorite.value
        )
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
            Text(text = com.example.foodhub_android.utils.StringUtils.formatCurrency(foodItem.price),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            FoodItemCounter(
                onCounterIncrement = {
                    viewModel.incrementQuantity()
                },
                onCounterDecrement = {
                    viewModel.decrementQuantity()
                },
                count = count.value
            )
        }
        foodItem.modifierGroups.forEach { group ->
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(group.name + if (group.required) " · Required" else " · Optional", style = MaterialTheme.typography.titleSmall)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(group.options) { option ->
                        androidx.compose.material3.FilterChip(
                            selected = selectedModifiers[group.name] == option.name,
                            onClick = { selectedModifiers[group.name] = option.name },
                            label = { Text(option.name + if (option.additionalPrice > 0) " +${com.example.foodhub_android.utils.StringUtils.formatCurrency(option.additionalPrice)}" else "") }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                viewModel.addToCart(
                    restaurantId = foodItem.restaurantId,
                    foodItemId = foodItem.id,
                    selectedModifiers = selectedModifiers.map { com.example.foodhub_android.data.models.SelectedModifier(it.key, it.value) }
                )
            },
            enabled = !isLoading.value && foodItem.modifierGroups.filter { it.required }.all { selectedModifiers.containsKey(it.name) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            // Show Icon and Text when NOT loading
            AnimatedVisibility(visible = !isLoading.value) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.cart),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Cart".uppercase(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // Show Spinner when LOADING
            AnimatedVisibility(visible = isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
    if (showSuccessDialog.value) {
        ModalBottomSheet(
            onDismissRequest = { showSuccessDialog.value = false } ,
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ){
                Text(
                    text = "OrderItem added to cart",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.size(16.dp))

                Button(onClick = {
                    showErrorDialog.value = false
                    viewModel.goToCart()
                }, modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()){
                    Text(text = "Go to Cart")
                }
                Button(onClick = {
                    showSuccessDialog.value = false
                }, modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()){
                    Text(text = "OK")
                }
            }
        }
    }
    if (showErrorDialog.value) {
        ModalBottomSheet(
            onDismissRequest = {
                showErrorDialog.value = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = (
                            uiState.value as? FoodDetailsViewModel.FoodDetailsUiState.Error
                            )?.message ?: "Failed to add to cart"
                )

                Spacer(modifier = Modifier.size(24.dp))

                Button(
                    onClick = {
                        showErrorDialog.value = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }

                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FoodItemCounter(onCounterIncrement:() -> Unit, onCounterDecrement: () -> Unit, count: Int){
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onCounterDecrement, enabled = count > 1, modifier = Modifier.size(48.dp)) {
            Image(painterResource(R.drawable.minus), "Decrease quantity", Modifier.size(28.dp))
        }
        Text(text = "${count}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = onCounterIncrement, enabled = count < 5, modifier = Modifier.size(48.dp)) {
            Image(painterResource(R.drawable.add), "Increase quantity", Modifier.size(28.dp))
        }
    }
}
