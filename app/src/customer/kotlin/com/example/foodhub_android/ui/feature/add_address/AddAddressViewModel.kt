package com.example.foodhub_android.ui.feature.add_address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.Address
import com.example.foodhub_android.data.models.ReverseGeocodeRequest
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
class AddAddressViewModel @Inject constructor(
    private val foodApi: FoodApi,
    private val session: FoodHubSession
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddAddressState>(AddAddressState.Ready)
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<AddAddressEvent>()
    val event = _event.asSharedFlow()
    private val _validation = MutableStateFlow(AddressValidation())
    val validation = _validation.asStateFlow()
    private val _geocodedAddress = MutableStateFlow<Address?>(null)
    val geocodedAddress = _geocodedAddress.asStateFlow()

    fun reverseGeocode(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            when (val result = safeApiCall { foodApi.reverseGeocode(ReverseGeocodeRequest(latitude, longitude)) }) {
                is ApiResponse.Success -> _geocodedAddress.value = result.data
                else -> Unit // Manual entry remains available when geocoding is unavailable.
            }
        }
    }

    fun saveAddress(
        line1: String,
        line2: String,
        city: String,
        state: String,
        zipCode: String,
        country: String,
        landmark: String,
        plusCode: String,
        latitude: Double,
        longitude: Double,
        existingId: String? = null
    ) {
        val errors = AddressValidation(
            line1 = if (line1.isBlank()) "Street address is required" else null,
            city = if (city.isBlank()) "City is required" else null,
            state = if (state.isBlank()) "State or region is required" else null,
            zipCode = if (zipCode.isBlank()) "Postal code is required" else null,
            country = if (country.isBlank()) "Country is required" else null,
            plusCode = if (plusCode.isNotBlank() && !plusCode.contains('+')) "Enter a valid Plus Code" else null
        )
        _validation.value = errors
        if (errors.hasErrors) return
        viewModelScope.launch {
            _uiState.value = AddAddressState.Saving
            val result = safeApiCall {
                val address = Address(
                        addressLine1 = line1.trim(),
                        addressLine2 = line2.trim().ifBlank { null },
                        city = city.trim(),
                        state = state.trim(),
                        zipCode = zipCode.trim(),
                        country = country.trim(),
                        landmark = landmark.trim().ifBlank { null },
                        plusCode = plusCode.trim().ifBlank { null },
                        latitude = latitude,
                        longitude = longitude,
                        id = existingId
                    )
                if (existingId == null) foodApi.storeAddress(address)
                else foodApi.updateAddress(existingId, address)
            }
            when (result) {
                is ApiResponse.Success -> {
                    _uiState.value = AddAddressState.Ready
                    _event.emit(AddAddressEvent.Saved)
                }
                is ApiResponse.Error -> {
                    if (result.code == 401) {
                        session.clear()
                        _event.emit(AddAddressEvent.SessionExpired)
                    } else _uiState.value = AddAddressState.Error(result.message ?: "Unable to save this address.")
                }
                is ApiResponse.Exception -> _uiState.value = AddAddressState.Error("We couldn’t connect. Check your internet and try again.")
            }
        }
    }

    sealed interface AddAddressEvent {
        data object Saved : AddAddressEvent
        data object SessionExpired : AddAddressEvent
    }
    sealed interface AddAddressState {
        data object Ready : AddAddressState
        data object Saving : AddAddressState
        data class Error(val message: String) : AddAddressState
    }
    data class AddressValidation(
        val line1: String? = null,
        val city: String? = null,
        val state: String? = null,
        val zipCode: String? = null,
        val country: String? = null,
        val plusCode: String? = null
    ) {
        val hasErrors get() = listOf(line1, city, state, zipCode, country, plusCode).any { it != null }
    }
}
