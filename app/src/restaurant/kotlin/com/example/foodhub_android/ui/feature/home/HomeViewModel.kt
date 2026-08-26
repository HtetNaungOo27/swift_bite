package com.example.foodhub_android.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.Restaurant
import com.example.foodhub_android.data.models.RestaurantStatistics
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val foodApi: FoodApi, val session: FoodHubSession) :
    ViewModel() {

    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _statistics = MutableStateFlow<StatisticsState>(StatisticsState.Loading)
    val statistics = _statistics.asStateFlow()

    init {
        getRestaurantProfile()
        getStatistics()
    }

    fun getRestaurantProfile() {
        viewModelScope.launch {
            _uiState.value = HomeScreenState.Loading
            val response = safeApiCall { foodApi.getRestaurantProfile() }
            when (response) {
                is ApiResponse.Success -> {
                    _uiState.value = HomeScreenState.Success(response.data)
                    session.storeRestaurantId(response.data.id)
                }

                is ApiResponse.Error -> {
                    _uiState.value = HomeScreenState.Failed
                }

                is ApiResponse.Exception -> {
                    _uiState.value = HomeScreenState.Failed
                }
            }
        }
    }

    fun retry() {
        getRestaurantProfile()
        getStatistics()
    }

    fun logout() = session.clear()

    fun getStatistics() {
        viewModelScope.launch {
            _statistics.value = StatisticsState.Loading
            when (val response = safeApiCall { foodApi.getRestaurantStatistics() }) {
                is ApiResponse.Success -> _statistics.value = StatisticsState.Success(response.data)
                is ApiResponse.Error -> _statistics.value = StatisticsState.Error(response.message ?: "Unable to load analytics")
                is ApiResponse.Exception -> _statistics.value = StatisticsState.Error("Analytics are temporarily unavailable")
            }
        }
    }

    sealed class HomeScreenState {
        object Loading : HomeScreenState()
        object Failed : HomeScreenState()
        data class Success(val data: Restaurant) : HomeScreenState()
    }

    sealed interface StatisticsState {
        data object Loading : StatisticsState
        data class Success(val data: RestaurantStatistics) : StatisticsState
        data class Error(val message: String) : StatisticsState
    }

}
