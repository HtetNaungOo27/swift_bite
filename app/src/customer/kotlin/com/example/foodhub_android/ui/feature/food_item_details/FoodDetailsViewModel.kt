package com.example.foodhub_android.ui.feature.food_item_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FavoritesStore
import com.example.foodhub_android.data.models.AddToCartRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FoodDetailsViewModel @Inject constructor(
    val foodApi: FoodApi,
    private val favoritesStore: FavoritesStore
) : ViewModel(){

    private val _uiState = MutableStateFlow<FoodDetailsUiState>(FoodDetailsUiState.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<FoodDetailsEvent>()
    val event = _event.asSharedFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity =_quantity.asStateFlow()
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    fun loadFavorite(foodItemId: String) {
        _isFavorite.value = favoritesStore.contains("food:$foodItemId")
        viewModelScope.launch {
            when (val result = safeApiCall { foodApi.getFavorites() }) {
                is ApiResponse.Success -> {
                    val serverValue = foodItemId in result.data.ids
                    _isFavorite.value = serverValue
                    if (favoritesStore.contains("food:$foodItemId") != serverValue) favoritesStore.toggle("food:$foodItemId")
                }
                else -> Unit
            }
        }
    }

    fun toggleFavorite(foodItemId: String) {
        val desired = favoritesStore.toggle("food:$foodItemId")
        _isFavorite.value = desired
        viewModelScope.launch {
            when (safeApiCall { foodApi.setFavorite(foodItemId, mapOf("favorite" to desired)) }) {
                is ApiResponse.Success -> Unit
                else -> {
                    favoritesStore.toggle("food:$foodItemId")
                    _isFavorite.value = !desired
                    _event.emit(FoodDetailsEvent.showErrorDialog("Couldn’t sync this favorite. Try again."))
                }
            }
        }
    }

    fun incrementQuantity() {
        if(quantity.value == 5) {
            return
        }
        _quantity.value += 1
    }

    fun decrementQuantity() {
        if(quantity.value <= 1) {
            return
        }
        _quantity.value -= 1
    }

    fun addToCart(restaurantId:String, foodItemId: String, selectedModifiers: List<com.example.foodhub_android.data.models.SelectedModifier> = emptyList()) {
        viewModelScope.launch {
            _uiState.value = FoodDetailsUiState.Loading
            val response = safeApiCall {
                foodApi.addToCart(
                    AddToCartRequest(
                        restaurantId = restaurantId,
                        menuItemId = foodItemId,
                        quantity = quantity.value,
                        selectedModifiers = selectedModifiers
                    )
                )
            }
            when(response){
                is ApiResponse.Success -> {
                    _uiState.value = FoodDetailsUiState.Success
                    _event.emit(FoodDetailsEvent.onAddToCart)
                }
                is ApiResponse.Error -> {
                    _uiState.value = FoodDetailsUiState.Error(response.message.orEmpty())
                    _event.emit(FoodDetailsEvent.showErrorDialog(response.message?: "Failed to add item to cart."))
                }
                else-> {
                    _uiState.value = FoodDetailsUiState.Error("Unknown Error")
                    _event.emit(FoodDetailsEvent.showErrorDialog("Unknown error"))
                }
            }
        }
    }

    fun goToCart() {
        viewModelScope.launch {
            _event.emit(FoodDetailsEvent.goToCart)
        }
    }

    sealed class FoodDetailsUiState {
        object Nothing : FoodDetailsUiState()
        object Loading : FoodDetailsUiState()
        object Success : FoodDetailsUiState()
        data class Error(val message: String) : FoodDetailsUiState()
    }
    sealed class FoodDetailsEvent {
        data class showErrorDialog(val message: String) : FoodDetailsEvent()
        object onAddToCart : FoodDetailsEvent()
        object goToCart : FoodDetailsEvent()
    }
}
