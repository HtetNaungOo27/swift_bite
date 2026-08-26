package com.example.foodhub_android.ui.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.models.RiderWallet
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiderWalletViewModel @Inject constructor(private val api: FoodApi) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()
    init { refresh() }
    fun refresh() = viewModelScope.launch {
        _state.value = State.Loading
        _state.value = when (val result = safeApiCall { api.getRiderWallet() }) {
            is ApiResponse.Success -> State.Success(result.data)
            is ApiResponse.Error -> State.Error(result.message ?: "Unable to load wallet")
            is ApiResponse.Exception -> State.Error("Couldn’t connect to SwiftBite")
        }
    }
    fun settle() = viewModelScope.launch {
        val current = (_state.value as? State.Success)?.wallet ?: return@launch
        _state.value = State.Settling(current)
        when (safeApiCall { api.settleRiderWallet() }) {
            is ApiResponse.Success -> refresh()
            else -> _state.value = State.Error("Settlement failed. Your balance was not changed.")
        }
    }
    sealed interface State {
        data object Loading : State
        data class Success(val wallet: RiderWallet) : State
        data class Settling(val wallet: RiderWallet) : State
        data class Error(val message: String) : State
    }
}
