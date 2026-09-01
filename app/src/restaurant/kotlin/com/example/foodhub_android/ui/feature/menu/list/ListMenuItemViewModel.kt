package com.example.foodhub_android.ui.feature.menu.list

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhub_android.data.FoodApi
import com.example.foodhub_android.data.FoodHubSession
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.data.models.UpdateMenuItemRequest
import com.example.foodhub_android.data.remote.ApiResponse
import com.example.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDateTime

@HiltViewModel
class ListMenuItemViewModel @Inject constructor(
    val foodApi: FoodApi,
    val session: FoodHubSession,
    @ApplicationContext private val context: Context
) :
    ViewModel() {

    private val _listMenuItemState = MutableStateFlow<ListMenuItemState>(ListMenuItemState.Loading)
    val listMenuItemState = _listMenuItemState.asStateFlow()

    private val _menuItemEvent = MutableSharedFlow<MenuItemEvent>()
    val menuItemEvent = _menuItemEvent.asSharedFlow()
    private val _editorState = MutableStateFlow(MenuEditorState())
    val editorState = _editorState.asStateFlow()

    init {
        getListItem()
    }

    private fun getListItem() {
        viewModelScope.launch {
            _listMenuItemState.value = ListMenuItemState.Loading
            val response = safeApiCall { foodApi.getOwnerMenu() }
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

    fun openEditor(item: FoodItem) {
        _editorState.value = MenuEditorState(
            item = item,
            name = item.name,
            description = item.description,
            price = item.price.toString(), inventory = item.inventoryQuantity.toString(), tags = item.dietaryTags.joinToString(", ")
            ,modifierName = item.modifierGroups.firstOrNull()?.name.orEmpty(), modifierOptions = item.modifierGroups.firstOrNull()?.options?.joinToString(", ") { "${it.name}:${it.additionalPrice}" }.orEmpty(), modifierRequired = item.modifierGroups.firstOrNull()?.required == true
        )
    }

    fun closeEditor() { _editorState.value = MenuEditorState() }
    fun startEditing() { _editorState.value = _editorState.value.copy(editing = true, confirmingDelete = false) }
    fun cancelEditing() {
        val item = _editorState.value.item ?: return closeEditor()
        _editorState.value = MenuEditorState(item, item.name, item.description, item.price.toString(), inventory = item.inventoryQuantity.toString(), tags = item.dietaryTags.joinToString(", "))
    }
    fun changeName(value: String) { _editorState.value = _editorState.value.copy(name = value) }
    fun changeDescription(value: String) { _editorState.value = _editorState.value.copy(description = value) }
    fun changePrice(value: String) {
        if (value.isEmpty() || value.matches(Regex("\\d*(\\.\\d{0,2})?"))) _editorState.value = _editorState.value.copy(price = value)
    }
    fun changeInventory(value: String) { if (value.all(Char::isDigit)) _editorState.value = _editorState.value.copy(inventory = value) }
    fun changeTags(value: String) { _editorState.value = _editorState.value.copy(tags = value) }
    fun changeModifierName(value: String) { _editorState.value = _editorState.value.copy(modifierName = value) }
    fun changeModifierOptions(value: String) { _editorState.value = _editorState.value.copy(modifierOptions = value) }
    fun changeModifierRequired(value: Boolean) { _editorState.value = _editorState.value.copy(modifierRequired = value) }
    fun changeImage(value: Uri?) { _editorState.value = _editorState.value.copy(replacementImage = value) }
    fun confirmDelete() { _editorState.value = _editorState.value.copy(confirmingDelete = true) }
    fun cancelDelete() { _editorState.value = _editorState.value.copy(confirmingDelete = false) }
    fun saveEditor() {
        val state = _editorState.value
        val item = state.item ?: return
        val options = state.modifierOptions.split(',').mapNotNull { raw -> val parts=raw.trim().split(':'); parts.firstOrNull()?.takeIf(String::isNotBlank)?.let { com.example.foodhub_android.data.models.MenuModifierOption(it, parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0) } }
        val groups = if (state.modifierName.isBlank() || options.isEmpty()) emptyList() else listOf(com.example.foodhub_android.data.models.MenuModifierGroup(state.modifierName.trim(), state.modifierRequired, 1, options))
        updateItem(item.id, state.name, state.description, state.price, state.replacementImage, state.inventory.toIntOrNull(), state.tags.split(',').map { it.trim().uppercase() }.filter(String::isNotBlank), groups)
    }
    fun deleteSelected() { _editorState.value.item?.let { deleteItem(it.id) } }

    fun setAvailability(available: Boolean, unavailableUntil: LocalDateTime? = null) {
        val item = _editorState.value.item ?: return
        viewModelScope.launch {
            _editorState.value = _editorState.value.copy(saving = true)
            val request = UpdateMenuItemRequest(
                name = item.name,
                description = item.description,
                price = item.price,
                isAvailable = available,
                unavailableUntil = unavailableUntil?.toString()
            )
            when (val response = safeApiCall { foodApi.updateRestaurantMenuItem(item.id, request) }) {
                is ApiResponse.Success -> {
                    closeEditor()
                    getListItem()
                    _menuItemEvent.emit(MenuItemEvent.EditSaved)
                }
                is ApiResponse.Error -> {
                    _editorState.value = _editorState.value.copy(saving = false)
                    _menuItemEvent.emit(MenuItemEvent.EditFailed(response.message ?: "Couldn’t update availability"))
                }
                is ApiResponse.Exception -> {
                    _editorState.value = _editorState.value.copy(saving = false)
                    _menuItemEvent.emit(MenuItemEvent.EditFailed("Couldn’t connect to the server"))
                }
            }
        }
    }

    fun updateItem(itemId: String, name: String, description: String, priceText: String, replacementImage: Uri? = null, inventory: Int? = null, tags: List<String>? = null, modifiers: List<com.example.foodhub_android.data.models.MenuModifierGroup>? = null) {
        val price = priceText.toDoubleOrNull()
        if (name.isBlank() || description.isBlank() || price == null || price <= 0) {
            viewModelScope.launch { _menuItemEvent.emit(MenuItemEvent.EditFailed("Enter a valid name, description, and price")) }
            return
        }
        viewModelScope.launch {
            _editorState.value = _editorState.value.copy(saving = true)
            _menuItemEvent.emit(MenuItemEvent.EditSaving(true))
            val uploadedImageUrl = replacementImage?.let { uploadImage(it) }
            if (replacementImage != null && uploadedImageUrl == null) {
                _menuItemEvent.emit(MenuItemEvent.EditFailed("Couldn’t upload the replacement image"))
                _editorState.value = _editorState.value.copy(saving = false)
                _menuItemEvent.emit(MenuItemEvent.EditSaving(false))
                return@launch
            }
            when (val response = safeApiCall {
                foodApi.updateRestaurantMenuItem(itemId, UpdateMenuItemRequest(name.trim(), description.trim(), price, uploadedImageUrl, inventoryQuantity = inventory, dietaryTags = tags, modifierGroups = modifiers))
            }) {
                is ApiResponse.Success -> {
                    getListItem()
                    closeEditor()
                    _menuItemEvent.emit(MenuItemEvent.EditSaved)
                }
                is ApiResponse.Error -> _menuItemEvent.emit(MenuItemEvent.EditFailed(response.message ?: "Couldn’t update item"))
                is ApiResponse.Exception -> _menuItemEvent.emit(MenuItemEvent.EditFailed("Couldn’t connect to the server"))
            }
            _menuItemEvent.emit(MenuItemEvent.EditSaving(false))
            _editorState.value = _editorState.value.copy(saving = false)
        }
    }

    fun deleteItem(itemId: String) = viewModelScope.launch {
        _editorState.value = _editorState.value.copy(saving = true)
        _menuItemEvent.emit(MenuItemEvent.EditSaving(true))
        when (val response = safeApiCall { foodApi.deleteRestaurantMenuItem(itemId) }) {
            is ApiResponse.Success -> {
                getListItem()
                closeEditor()
                _menuItemEvent.emit(MenuItemEvent.ItemDeleted)
            }
            is ApiResponse.Error -> _menuItemEvent.emit(MenuItemEvent.EditFailed(response.message ?: "Couldn’t delete item"))
            is ApiResponse.Exception -> _menuItemEvent.emit(MenuItemEvent.EditFailed("Couldn’t connect to the server"))
        }
        _menuItemEvent.emit(MenuItemEvent.EditSaving(false))
        _editorState.value = _editorState.value.copy(saving = false)
    }

    private suspend fun uploadImage(uri: Uri): String? {
        val file = File.createTempFile("menu-edit-", ".jpg", context.cacheDir)
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

    sealed class MenuItemEvent {
        object AddNewMenuItem : MenuItemEvent()
        object EditSaved : MenuItemEvent()
        object ItemDeleted : MenuItemEvent()
        data class EditSaving(val saving: Boolean) : MenuItemEvent()
        data class EditFailed(val message: String) : MenuItemEvent()
    }

    sealed class ListMenuItemState {
        object Loading : ListMenuItemState()
        data class Success(val data: List<FoodItem>) : ListMenuItemState()
        data class Error(val message: String) : ListMenuItemState()
    }

    data class MenuEditorState(
        val item: FoodItem? = null,
        val name: String = "",
        val description: String = "",
        val price: String = "",
        val inventory: String = "100",
        val tags: String = "",
        val modifierName: String = "",
        val modifierOptions: String = "",
        val modifierRequired: Boolean = false,
        val replacementImage: Uri? = null,
        val editing: Boolean = false,
        val confirmingDelete: Boolean = false,
        val saving: Boolean = false
    )
}
