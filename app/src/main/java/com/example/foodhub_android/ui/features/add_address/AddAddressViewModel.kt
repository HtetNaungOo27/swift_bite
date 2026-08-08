package com.example.foodhub_android.ui.features.add_address

import androidx.lifecycle.ViewModel
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.Address
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddAddressViewModel @Inject constructor(val foodApi: FoodApi): ViewModel() {

    private val _uiState = MutableStateFlow<AddAddressState>(AddAddressState.Loading)
    val uiState = _uiState.asStateFlow()

    fun reverseGeocode(lat: Double, lon: Double) {

    }

    fun addAddress(address : Address){

    }

    sealed class AddAddressEvent {
        object NavigateToAddressDetails : AddAddressEvent()
    }
    sealed class AddAddressState {
        object Loading : AddAddressState()
        object Success : AddAddressState()
        data class Error(val message: String) : AddAddressState()
    }
}