package com.example.foodhub_android.ui.feature.restaurant_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FavoritesStore
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.data.models.ReviewRequest
import com.example.foodhub_android.data.models.ReviewSummary
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    val foodApi: FoodApi,
    private val favoritesStore: FavoritesStore
) : ViewModel() {
    var errorMsg =""
    var errorDescription = ""
    private val _uiState = MutableStateFlow<RestaurantEvent>(RestaurantEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RestaurantNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()
    private val _reviews = MutableStateFlow(ReviewSummary())
    val reviews = _reviews.asStateFlow()
    private val _reviewSaving = MutableStateFlow(false)
    val reviewSaving = _reviewSaving.asStateFlow()

    fun loadFavorite(restaurantId: String) {
        _isFavorite.value = favoritesStore.contains("restaurant:$restaurantId")
    }

    fun toggleFavorite(restaurantId: String) {
        _isFavorite.value = favoritesStore.toggle("restaurant:$restaurantId")
    }

    fun getReviews(restaurantId: String) = viewModelScope.launch {
        val response = safeApiCall { foodApi.getRestaurantReviews(restaurantId) }
        if (response is ApiResponse.Success) _reviews.value = response.data
    }

    fun saveReview(restaurantId: String, rating: Int, comment: String) = viewModelScope.launch {
        if (rating !in 1..5 || comment.isBlank()) return@launch
        _reviewSaving.value = true
        val response = safeApiCall {
            foodApi.saveRestaurantReview(restaurantId, ReviewRequest(rating, comment.trim()))
        }
        if (response is ApiResponse.Success) {
            _reviews.value = response.data
        } else {
            _navigationEvent.emit(RestaurantNavigationEvent.ShowErrorDialog)
        }
        _reviewSaving.value = false
    }

    fun getFoodItem(id: String) {
        viewModelScope.launch {
            _uiState.value = RestaurantEvent.Loading
            try {
                val response = safeApiCall {
                    foodApi.getFoodItemForRestaurant(restaurantId = id)
                }
                when(response){
                    is ApiResponse.Success ->{
                        _uiState.value = RestaurantEvent.Success(response.data.foodItems)
                    }
                    else -> {
                        val error =(response as? ApiResponse.Error)?.code
                        when(error) {
                            401 -> {
                                errorMsg = "Unauthorized"
                                errorDescription ="You are not authorized to view this content"
                            }
                            404 -> {
                                errorMsg = "Not Found"
                                errorDescription = "The Restaurant was not found"
                            }
                            else -> {
                                errorMsg = "Error"
                                errorDescription ="An error occurred"
                            }
                        }
                        _uiState.value = RestaurantEvent.Error
                        _navigationEvent.emit(RestaurantNavigationEvent.ShowErrorDialog)
                    }
                }

            } catch (e: Exception) {
                _uiState.value = RestaurantEvent.Error
                _navigationEvent.emit(RestaurantNavigationEvent.ShowErrorDialog)
            }

        }
    }

    sealed class RestaurantNavigationEvent {
        data object GoBack : RestaurantNavigationEvent()
        data object ShowErrorDialog : RestaurantNavigationEvent()
        data class NavigateToProduceDetails(val productID: String) : RestaurantNavigationEvent()
    }

    sealed class RestaurantEvent {
        data object Nothing : RestaurantEvent()
        data class Success(val foodItems: List<FoodItem>) : RestaurantEvent()
        data object Error : RestaurantEvent()
        data object Loading : RestaurantEvent()
    }
}
