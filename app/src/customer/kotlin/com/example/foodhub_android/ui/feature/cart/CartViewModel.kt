package com.example.foodhub_android.ui.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.Address
import com.example.foodhub_android.data.models.CartItem
import com.example.foodhub_android.data.models.CartResponse
import com.example.foodhub_android.data.models.ConfirmPaymentRequest
import com.example.foodhub_android.data.models.PaymentIntentRequest
import com.example.foodhub_android.data.models.PaymentIntentResponse
import com.example.foodhub_android.data.models.UpdateCartItemRequest
import com.example.foodhub_android.data.models.PlaceOrderRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID
import java.time.LocalDateTime

@HiltViewModel
class CartViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {

    var errorTitle: String = ""
    var errorMessage: String = ""
    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<CartEvent>()
    val event = _event.asSharedFlow()
    private var cartResponse: CartResponse? = null
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount = _cartItemCount.asStateFlow()
    private var paymentIntent: PaymentIntentResponse? = null
    private val address = MutableStateFlow<Address?>(null)
    private var codCheckoutKey = UUID.randomUUID().toString()
    private var cardCheckoutKey = UUID.randomUUID().toString()
    val selectedAddress = address.asStateFlow()
    private val _specialInstructions = MutableStateFlow("")
    val specialInstructions = _specialInstructions.asStateFlow()
    private val _riderInstructions = MutableStateFlow("")
    val riderInstructions = _riderInstructions.asStateFlow()
    private val _fulfillmentType = MutableStateFlow("DELIVERY")
    val fulfillmentType = _fulfillmentType.asStateFlow()
    private val _scheduledFor = MutableStateFlow<String?>(null)
    val scheduledFor = _scheduledFor.asStateFlow()
    init {
        getCart()
        loadDefaultAddress()
    }

    private fun loadDefaultAddress() {
        viewModelScope.launch {
            if (address.value != null) return@launch
            when (val result = safeApiCall { foodApi.getUserAddress() }) {
                is ApiResponse.Success -> address.value = result.data.addresses.firstOrNull()
                else -> Unit
            }
        }
    }
    fun getCart() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res = safeApiCall { foodApi.getCart() }
            when (res) {
                is ApiResponse.Success -> {
                    cartResponse = res.data
                    _cartItemCount.value = res.data.items.size
                    _uiState.value = CartUiState.Success(res.data)
                }
                is ApiResponse.Error -> {
                    _uiState.value = CartUiState.Error(res.message.orEmpty())
                }
                else -> {
                    _uiState.value = CartUiState.Error("We couldn’t load your cart. Check your connection and try again.")
                }
            }
        }
    }

    fun incrementQuantity(cartItem: CartItem) {
        if (cartItem.quantity >= 5) {
            return
        }
        updateItemQuantity(cartItem, cartItem.quantity + 1)
    }

    fun decrementQuantity(cartItem: CartItem) {
        if (cartItem.quantity <= 1) {
            return
        }
        updateItemQuantity(cartItem, cartItem.quantity - 1)
    }
    private fun updateItemQuantity(cartItem: CartItem, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res =
                safeApiCall {
                    foodApi.updateCart(
                        UpdateCartItemRequest(cartItemId = cartItem.id, quantity = quantity)
                    )
                }
            when (res) {
                is ApiResponse.Success -> {
                    getCart()
                }
                is ApiResponse.Error -> {
                    errorTitle = "Couldn’t update quantity"
                    errorMessage = res.message ?: "Your quantity wasn’t changed. Please try again."
                    cartResponse?.let {
                        _uiState.value = CartUiState.Success(it)
                    }
                    _event.emit(CartEvent.onQuantityUpdateError)
                }
                else -> {
                    cartResponse?.let {
                        _uiState.value = CartUiState.Success(it)
                    }
                    errorTitle = "Cannot Update Quantity."
                    errorMessage = "An error occurred while updating the quantity of the item."
                    _event.emit(CartEvent.onQuantityUpdateError)
                }
            }
        }
    }

    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res = safeApiCall { foodApi.deleteCartItem(cartItem.id) }
            when (res) {
                is ApiResponse.Success -> {
                    getCart()
                }
                is ApiResponse.Error -> {
                    errorTitle = "Couldn’t remove item"
                    errorMessage = res.message ?: "The item is still in your cart. Please try again."
                    _event.emit(CartEvent.onItemRemoveError)
                    getCart()
                }
                else -> {
                    cartResponse?.let {
                        _uiState.value = CartUiState.Success(cartResponse!!)
                    }
                    errorTitle = "Cannot Delete."
                    errorMessage = "An error occurred while removing the item from the cart."
                    _event.emit(CartEvent.onItemRemoveError)
                }
            }
        }
    }


    fun checkout() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val paymentDetails = safeApiCall {
                foodApi.getPaymentIntent(PaymentIntentRequest(address.value!!.id!!, cardCheckoutKey, _fulfillmentType.value, _scheduledFor.value))
            }

            when (paymentDetails) {
                is ApiResponse.Success -> {
                    paymentIntent = paymentDetails.data
                    _event.emit(CartEvent.OnInitiatePayment(paymentDetails.data ))
                    _uiState.value = CartUiState.Success(cartResponse!!)

                }
                else -> {
                    errorTitle = "Cannot Checkout."
                    errorMessage = "An error occurred while checking out."
                    _event.emit(CartEvent.showErrorDialog)
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }

            }
        }
    }

    fun checkoutWithCash() {
        val selected = address.value ?: return
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            when (val response = safeApiCall {
                foodApi.placeOrder(
                    PlaceOrderRequest(
                        selected.id!!, "COD", codCheckoutKey, _specialInstructions.value, _riderInstructions.value,
                        _fulfillmentType.value, _scheduledFor.value
                    )
                )
            }) {
                is ApiResponse.Success -> {
                    codCheckoutKey = UUID.randomUUID().toString()
                    _event.emit(CartEvent.OrderSuccess(response.data.id))
                    getCart()
                }
                else -> {
                    errorTitle = "Couldn’t place COD order"
                    errorMessage = "Please try again. No payment was taken."
                    _event.emit(CartEvent.showErrorDialog)
                    _uiState.value = cartResponse?.let { CartUiState.Success(it) } ?: CartUiState.Error(errorMessage)
                }
            }
        }
    }

    fun onAddressClicked() {
        viewModelScope.launch {
            _event.emit(CartEvent.onAddressClicked)
        }
    }

    fun onAddressSelected(it: Address) {
        address.value = it

    }

    fun onSpecialInstructionsChanged(value: String) {
        _specialInstructions.value = value.take(500)
    }

    fun onRiderInstructionsChanged(value: String) {
        _riderInstructions.value = value.take(500)
    }

    fun setFulfillmentType(value: String) {
        _fulfillmentType.value = value
        if (value == "PICKUP") loadDefaultAddress()
    }
    fun scheduleNow() { _scheduledFor.value = null }
    fun scheduleInOneHour() { _scheduledFor.value = LocalDateTime.now().plusHours(1).withSecond(0).withNano(0).toString() }
    fun scheduleTomorrowLunch() { _scheduledFor.value = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0).toString() }



    fun onPaymentFailed() {
        errorTitle = "Payment Failed"
        errorMessage = "An error occurred while processing your payment."
        viewModelScope.launch {
            _event.emit(CartEvent.showErrorDialog)
        }
    }

    fun onPaymentSuccess() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val response =
                safeApiCall { foodApi.verifyPurchase(
                    ConfirmPaymentRequest(
                        paymentIntent!!.paymentIntentId,
                        address.value!!.id!!,
                        _specialInstructions.value,
                        _riderInstructions.value,
                        _fulfillmentType.value,
                        _scheduledFor.value
                    ), paymentIntent!!.paymentIntentId
                    )
                }
            when(response){
                is ApiResponse.Success -> {
                    cardCheckoutKey = UUID.randomUUID().toString()
                    _event.emit(CartEvent.OrderSuccess(response.data.orderId))
                    _uiState.value = CartUiState.Success(cartResponse!!)
                    getCart()
                }
                else -> {
                    errorTitle = "Payment Failed"
                    errorMessage = "An error occurred while processing your payment."
                    _event.emit(CartEvent.showErrorDialog)
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }
            }
        }
    }

    sealed class CartUiState {
        object Nothing : CartUiState()
        object Loading : CartUiState()
        data class Success(val data: CartResponse) : CartUiState()
        data class Error(val message: String) : CartUiState()
    }

    sealed class CartEvent {
        object showErrorDialog : CartEvent()
        data class OrderSuccess(val orderId: String) : CartEvent()
        object OnCheckout : CartEvent()
        data class OnInitiatePayment(val data: PaymentIntentResponse) : CartEvent()

        object onQuantityUpdateError : CartEvent()
        object onItemRemoveError : CartEvent()
        object onAddressClicked  : CartEvent()
    }
}
