package com.example.foodhub_android.ui.feature.address_list

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

    init {
        getAddress()
    }

    fun getAddress() {
        viewModelScope.launch {
            _state.value = AddressState.Loading
            val result = safeApiCall{ foodApi.getUserAddress() }
            when(result){
                is com.example.foodhub_android.data.remote.ApiResponse.Success -> {
                    _state.value = AddressState.Success(result.data.addresses)
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

    fun onAddAddressClicked(){
        viewModelScope.launch {
            _event.emit(AddressEvent.NavigateToAddAddress)
        }
    }

    fun onAddressSelected(address: Address){
        viewModelScope.launch {
            _event.emit(AddressEvent.NavigateBack(address))
        }
    }

    fun deleteAddress(address: Address) {
        val id = address.id ?: return
        viewModelScope.launch {
            when (val result = safeApiCall { foodApi.deleteAddress(id) }) {
                is com.example.foodhub_android.data.remote.ApiResponse.Success -> getAddress()
                is com.example.foodhub_android.data.remote.ApiResponse.Error ->
                    _event.emit(AddressEvent.Message(result.message ?: "Unable to delete this address"))
                is com.example.foodhub_android.data.remote.ApiResponse.Exception ->
                    _event.emit(AddressEvent.Message("Couldn’t connect to the server"))
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
        object NavigateToEditAddress : AddressEvent()
        data class NavigateBack(val address: Address) : AddressEvent()
        data class Message(val text: String) : AddressEvent()
    }
}
