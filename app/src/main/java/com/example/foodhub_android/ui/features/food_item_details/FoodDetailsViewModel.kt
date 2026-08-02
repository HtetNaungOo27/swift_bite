package com.example.foodhub_android.ui.features.food_item_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.AddToCartRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FoodDetailsViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel(){

    private val _uiState = MutableStateFlow<FoodDetailsUiState>(FoodDetailsUiState.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _event =MutableStateFlow<FoodDetailsEvent>()
    val event = _event.asStateFlow()

    private val _quantity = MutableStateFlow<Int>(0)
    val quantity =_quantity.asStateFlow()

    fun incrementQuantity() {
        if(quantity.value == 5) {
            return
        }
        _quantity.value += 1
    }

    fun decrementQuantity() {
        if(quantity.value == 1) {
            return
        }
        _quantity.value -= 1
    }

    fun addToCart(restaurant:String, foodItemId: String) {
        viewModelScope.launch {
            _uiState.value = FoodDetailsUiState.Loading
            val response = safeApiCall {
                foodApi.addToCart(
                    AddToCartRequest(
                        restaurantId = restaurantId,
                        menuItemId = foodItemId,
                        quantity = quantity.value
                    )
                )
            }
            when(response){
                is ApiResponse.Success -> {
                    _uiState.value = FoodDetailsUiState.Success
                    _event.emit(FoodDetailsEvent.OnAddToCart)
                }
                is ApiResponse.Error -> {
                    _uiState.value = FoodDetailsUiState.Error(response.message)
                    _event.emit(FoodDetailsEvent.showErrorDialog(response.message))
                }
                else {
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