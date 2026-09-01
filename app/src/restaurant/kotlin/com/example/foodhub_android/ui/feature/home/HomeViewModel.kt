package com.example.foodhub_android.ui.feature.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.Restaurant
import com.example.foodhub_android.data.models.RestaurantStatistics
import com.example.foodhub_android.data.models.UpdateRestaurantRequest
import com.example.foodhub_android.data.models.RestaurantHours
import com.example.foodhub_android.data.models.UpdateRestaurantHoursRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@HiltViewModel
class HomeViewModel @Inject constructor(
    val foodApi: FoodApi,
    val session: FoodHubSession,
    @ApplicationContext private val context: Context
) :
    ViewModel() {

    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _statistics = MutableStateFlow<StatisticsState>(StatisticsState.Loading)
    val statistics = _statistics.asStateFlow()
    private val _profileUpdate = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdate = _profileUpdate.asStateFlow()

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

    fun updateProfile(
        name: String,
        address: String,
        replacementImage: Uri?,
        opensAt: String,
        closesAt: String,
        deliveryRadiusKm: Double,
        minimumOrderAmount: Double,
        phone: String,
        cuisine: String,
        deliveryFee: Double
    ) {
        if (name.isBlank() || address.isBlank()) {
            _profileUpdate.value = ProfileUpdateState.Error("Restaurant name and address are required")
            return
        }
        viewModelScope.launch {
            _profileUpdate.value = ProfileUpdateState.Saving
            val uploadedImageUrl = replacementImage?.let { uploadImage(it) }
            if (replacementImage != null && uploadedImageUrl == null) {
                _profileUpdate.value = ProfileUpdateState.Error("Couldn’t upload the new cover photo")
                return@launch
            }
            when (val response = safeApiCall {
                foodApi.updateRestaurantProfile(
                    UpdateRestaurantRequest(
                        name = name.trim(), address = address.trim(), imageUrl = uploadedImageUrl,
                        opensAt = opensAt, closesAt = closesAt,
                        deliveryRadiusKm = deliveryRadiusKm,
                        minimumOrderAmount = minimumOrderAmount,
                        phone = phone.trim(), cuisine = cuisine.trim(), deliveryFee = deliveryFee
                    )
                )
            }) {
                is ApiResponse.Success -> {
                    _profileUpdate.value = ProfileUpdateState.Saved
                    getRestaurantProfile()
                }
                is ApiResponse.Error -> _profileUpdate.value = ProfileUpdateState.Error(response.message ?: "Unable to update profile")
                is ApiResponse.Exception -> _profileUpdate.value = ProfileUpdateState.Error("Couldn’t connect to the server")
            }
        }
    }

    fun clearProfileUpdate() { _profileUpdate.value = ProfileUpdateState.Idle }

    fun updateWeeklyHours(hours: List<RestaurantHours>) {
        viewModelScope.launch {
            _profileUpdate.value = ProfileUpdateState.Saving
            when (val response = safeApiCall { foodApi.updateRestaurantHours(UpdateRestaurantHoursRequest(hours)) }) {
                is ApiResponse.Success -> {
                    _profileUpdate.value = ProfileUpdateState.Saved
                    getRestaurantProfile()
                }
                is ApiResponse.Error -> _profileUpdate.value = ProfileUpdateState.Error(response.message ?: "Unable to save weekly hours")
                is ApiResponse.Exception -> _profileUpdate.value = ProfileUpdateState.Error("Couldn’t connect to the server")
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String? {
        val file = File.createTempFile("restaurant-cover-", ".jpg", context.cacheDir)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use(input::copyTo)
            } ?: return null
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, body)
            when (val result = safeApiCall { foodApi.uploadImage(part) }) {
                is ApiResponse.Success -> result.data.url
                else -> null
            }
        } finally {
            file.delete()
        }
    }

    fun setRestaurantOpen(open: Boolean) {
        val current = (_uiState.value as? HomeScreenState.Success)?.data ?: return
        _uiState.value = HomeScreenState.Success(current.copy(isOpen = open))
        viewModelScope.launch {
            when (val response = safeApiCall { foodApi.updateRestaurantProfile(UpdateRestaurantRequest(isOpen = open)) }) {
                is ApiResponse.Success -> getRestaurantProfile()
                else -> _uiState.value = HomeScreenState.Success(current)
            }
        }
    }

    fun setRestaurantBusy(busy: Boolean) {
        val current = (_uiState.value as? HomeScreenState.Success)?.data ?: return
        _uiState.value = HomeScreenState.Success(current.copy(isBusy = busy))
        viewModelScope.launch {
            when (safeApiCall { foodApi.updateRestaurantProfile(UpdateRestaurantRequest(isBusy = busy)) }) {
                is ApiResponse.Success -> getRestaurantProfile()
                else -> _uiState.value = HomeScreenState.Success(current)
            }
        }
    }

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

    sealed interface ProfileUpdateState {
        data object Idle : ProfileUpdateState
        data object Saving : ProfileUpdateState
        data object Saved : ProfileUpdateState
        data class Error(val message: String) : ProfileUpdateState
    }

}
