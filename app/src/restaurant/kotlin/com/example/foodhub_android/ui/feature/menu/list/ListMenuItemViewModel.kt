package com.example.foodhub_android.ui.feature.menu.list

import android.view.MenuItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.FoodItem
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
            val restaurantID = session.getRestaurantId() ?: ""
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

    sealed class MenuItemEvent {
        object AddNewMenuItem : MenuItemEvent()
    }

    sealed class ListMenuItemState {
        object Loading : ListMenuItemState()
        data class Success(val data: List<FoodItem>) : ListMenuItemState()
        data class Error(val message: String) : ListMenuItemState()
    }
}
