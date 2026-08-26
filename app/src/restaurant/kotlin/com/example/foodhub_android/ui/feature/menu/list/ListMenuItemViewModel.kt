package com.example.foodhub_android.ui.feature.menu.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.data.models.UpdateMenuItemRequest
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
class ListMenuItemViewModel @Inject constructor(val foodApi: FoodApi, val session: FoodHubSession) :
    ViewModel() {

    private val _listMenuItemState = MutableStateFlow<ListMenuItemState>(ListMenuItemState.Loading)
    val listMenuItemState = _listMenuItemState.asStateFlow()

    private val _menuItemEvent = MutableSharedFlow<MenuItemEvent>()
    val menuItemEvent = _menuItemEvent.asSharedFlow()

    init {
        getListItem()
    }

    private fun getListItem() {
        viewModelScope.launch {
            _listMenuItemState.value = ListMenuItemState.Loading
            val restaurantID = session.getRestaurantId()
            if (restaurantID.isNullOrBlank()) {
                _listMenuItemState.value = ListMenuItemState.Error("Restaurant profile is not available yet")
                return@launch
            }
            val response = safeApiCall { foodApi.getRestaurantMenu(restaurantID) }
            when (response) {
                is ApiResponse.Success -> {
                    _listMenuItemState.value = ListMenuItemState.Success(response.data.foodItems)
                }

                is ApiResponse.Error -> {
                    _listMenuItemState.value = ListMenuItemState.Error(response.message.orEmpty())
                }

                is ApiResponse.Exception -> {
                    _listMenuItemState.value = ListMenuItemState.Error("An error occurred")
                }
            }
        }
    }

    fun retry() {
        getListItem()
    }

    fun onAddItemClicked() {
        viewModelScope.launch {
            _menuItemEvent.emit(MenuItemEvent.AddNewMenuItem)
        }
    }

    fun updateItem(itemId: String, name: String, description: String, priceText: String) {
        val price = priceText.toDoubleOrNull()
        if (name.isBlank() || description.isBlank() || price == null || price <= 0) {
            viewModelScope.launch { _menuItemEvent.emit(MenuItemEvent.EditFailed("Enter a valid name, description, and price")) }
            return
        }
        viewModelScope.launch {
            _menuItemEvent.emit(MenuItemEvent.EditSaving(true))
            when (val response = safeApiCall {
                foodApi.updateRestaurantMenuItem(itemId, UpdateMenuItemRequest(name.trim(), description.trim(), price))
            }) {
                is ApiResponse.Success -> {
                    getListItem()
                    _menuItemEvent.emit(MenuItemEvent.EditSaved)
                }
                is ApiResponse.Error -> _menuItemEvent.emit(MenuItemEvent.EditFailed(response.message ?: "Couldn’t update item"))
                is ApiResponse.Exception -> _menuItemEvent.emit(MenuItemEvent.EditFailed("Couldn’t connect to the server"))
            }
            _menuItemEvent.emit(MenuItemEvent.EditSaving(false))
        }
    }

    sealed class MenuItemEvent {
        object AddNewMenuItem : MenuItemEvent()
        object EditSaved : MenuItemEvent()
        data class EditSaving(val saving: Boolean) : MenuItemEvent()
        data class EditFailed(val message: String) : MenuItemEvent()
    }

    sealed class ListMenuItemState {
        object Loading : ListMenuItemState()
        data class Success(val data: List<FoodItem>) : ListMenuItemState()
        data class Error(val message: String) : ListMenuItemState()
    }
}
