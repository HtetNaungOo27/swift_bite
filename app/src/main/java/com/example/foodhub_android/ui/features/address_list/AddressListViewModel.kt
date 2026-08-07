package com.example.foodhub_android.ui.features.address_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.Address
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressListViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {


    private val _state = MutableStateFlow<AddressState>(AddressState.Loading)
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<AddressEvent?>()
    val event = _event.asSharedFlow()

    fun getAddress() {
        viewModelScope.launch {
            val result = safeApiCall { foodApi.getUserAddresses() }
            when(result){
                is com.example.foodhub_android.data.remote.ApiResponse.Success -> {
                    _state.value = AddressState.Success(result.data)
                }
                is com.example.foodhub_android.data.remote.ApiResponse.Error -> {
                    _state.value = AddressState.Error(result.message.orEmpty())
                }
                else -> {
                    _state.value = AddressState.Error("An error occurred")
                }
            }

        }
    }
    sealed class AddressState {
        object Loading : AddressState()
        data class Success(val data: List<Address>) : AddressState()
        data class Error(val message: String) : AddressState()
    }
    sealed class AddressEvent {
        data class NavigateToAddressDetails(val address: Address) : AddressEvent()
        object NavigateToAddAddress : AddressEvent()
    }
}