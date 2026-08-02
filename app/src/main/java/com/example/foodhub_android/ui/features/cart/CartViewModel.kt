package com.example.foodhub_android.ui.features.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.models.CartItem
import com.example.foodhub_android.data.models.CartResponse
import com.example.foodhub_android.data.models.UpdateCartItemRequest
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
class CartViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<CartEvent>()
    val event = _event.asSharedFlow()
     private val CartResponse: CartResponse? = null

    init {
        getCart()
    }
    fun getCart() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res = safeApiCall { foodApi.getCart() }
            when (res) {
                is ApiResponse.Success -> {
                    CartResponse = res.data
                    _uiState.value = CartUiState.Success(res.data)
                }
                is ApiResponse.Error -> {
                    _uiState.value = CartUiState.Error(res.message)
                }
                else -> {
                    _uiState.value = CartUiState.Error("An error occured")
                }
            }
        }
    }

    fun inCrementQuantity(cartItem: CartItem, quantity: Int) {
        if(CartItem.quantity == 5) {
            return
        }
        updateItemQuantity(cartItem, cartItem.quantity + 1)
    }

    fun decrementQuantity(cartItem: CartItem, quantity: Int){
        if(CartItem.quantity == 1) {
            return
        }
        updateItemQuantity(cartItem, cartItem.quantity - 1)
    }
    private fun updateItemQuantity(cartItem: CartItem, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res =
                safeApiCall { foodApi.updateCart(UpdateCartItemRequest(cartItem.id, quantity)) }
            when (res) {
                is ApiResponse.Success -> {

                    getCart()
                }

                is ApiResponse.Error -> {
                    CartResponse?. let {

                        _uiState.value = CartEvent.Success(CartResponse!!)
                    }
                    _event.emit(CartEvent.onQuantityUpdateError)
                }


            }
                else -> {
                    _event.emit(CartEvent.onQuantityUpdateError)
                }
            }

        }

    }
    fun removeItem(cartItem: CartItem){

    }

    fun checkout(){

    }

    sealed class CartUiState{
        object Nothing : CartUiState()
        object Loading : CartUiState()
        data class Success(val data: CartResponse) : CartUiState()
        data class Error(val message: String) : CartUiState()
    }
    sealed class CartEvent{
        object showErrorDialog : CartEvent()
        object OnCheckout : CartEvent()
        object ItemRemoveError: CartEvent()

    }
}