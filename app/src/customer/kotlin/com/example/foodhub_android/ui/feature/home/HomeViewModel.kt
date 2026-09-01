package com.example.foodhub_android.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.Category
import com.example.foodhub_android.data.models.Restaurant
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val foodApi: FoodApi): ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<HomeScreenNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()
    private val _locationLabel = MutableStateFlow("Yangon")
    val locationLabel = _locationLabel.asStateFlow()

    var categories = emptyList<Category>()
    var restaurants = emptyList<Restaurant>()
    init{
        viewModelScope.launch {
            categories = getCategories()
            val savedAddress = when (val response = safeApiCall { foodApi.getUserAddress() }) {
                is ApiResponse.Success -> response.data.addresses.firstOrNull { address ->
                    address.city.contains("Yangon", true) && address.latitude != null && address.longitude != null
                }
                else -> null
            }
            val latitude = savedAddress?.latitude ?: YANGON_LATITUDE
            val longitude = savedAddress?.longitude ?: YANGON_LONGITUDE
            _locationLabel.value = savedAddress?.let { it.addressLine1.ifBlank { "Yangon" } } ?: "Yangon"
            restaurants = getPopularRestaurants(latitude, longitude)

            if( categories.isNotEmpty() && restaurants.isNotEmpty()){
                _uiState.value = HomeScreenState.Success
            }
            else {
                _uiState.value = HomeScreenState.Empty
            }
        }
    }

    suspend fun getCategories(): List<Category>{
        var list = emptyList<Category>()
        val response = safeApiCall {
            foodApi.getCategories()
        }
        when(response){
            is ApiResponse.Success -> {
                list = response.data.data
            }
            is ApiResponse.Error -> Unit
            else -> Unit
        }
        return list
    }

    suspend fun getPopularRestaurants(
        latitude: Double = YANGON_LATITUDE,
        longitude: Double = YANGON_LONGITUDE
    ): List<Restaurant>{
        var list = emptyList<Restaurant>()
        val response = safeApiCall {
            foodApi.getRestaurants(latitude, longitude)
        }
        when(response){
            is ApiResponse.Success -> {
                list = response.data.data
            }
            is ApiResponse.Error -> Unit
            else -> Unit
        }
        return list

    }

    fun updateLocation(latitude: Double, longitude: Double) {
        if (!isYangon(latitude, longitude)) {
            useYangonFallback("Yangon · device location is outside the service area")
            return
        }
        viewModelScope.launch {
            _uiState.value = HomeScreenState.Loading
            restaurants = getPopularRestaurants(latitude, longitude)
            _locationLabel.value = "Current location · Yangon"
            if (restaurants.isEmpty()) {
                restaurants = getPopularRestaurants(YANGON_LATITUDE, YANGON_LONGITUDE)
                _locationLabel.value = "Yangon"
            }
            _uiState.value = if (categories.isNotEmpty() && restaurants.isNotEmpty()) {
                HomeScreenState.Success
            } else {
                HomeScreenState.Empty
            }
        }
    }

    fun useYangonFallback(label: String = "Yangon") {
        viewModelScope.launch {
            _uiState.value = HomeScreenState.Loading
            _locationLabel.value = label
            restaurants = getPopularRestaurants(YANGON_LATITUDE, YANGON_LONGITUDE)
            _uiState.value = if (categories.isNotEmpty() && restaurants.isNotEmpty()) HomeScreenState.Success else HomeScreenState.Empty
        }
    }

    fun onRestaurantSelected(it: Restaurant) {
        viewModelScope.launch {
            _navigationEvent.emit(
                HomeScreenNavigationEvent.NavigateToDetail(
                    it.name,
                    it.imageUrl,
                    it.id,
                    it.isOpen
                )
            )
        }
    }

    sealed class HomeScreenState {
        object Loading: HomeScreenState()
        object Empty : HomeScreenState()
        object Success : HomeScreenState()
    }

    sealed class HomeScreenNavigationEvent {
        data class NavigateToDetail(val name:String,val imageUrl:String,val id:String,val isOpen:Boolean) : HomeScreenNavigationEvent()

    }

    private companion object {
        const val YANGON_LATITUDE = 16.8409
        const val YANGON_LONGITUDE = 96.1735
        fun isYangon(latitude: Double, longitude: Double) = latitude in 16.45..17.20 && longitude in 95.75..96.55
    }
}
