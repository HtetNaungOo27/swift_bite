package com.example.foodhub_android.ui.feature.menu.list

import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.ui.features.common.FoodItemView
import com.example.foodhub_android.ui.features.notifications.ErrorScreen
import com.example.foodhub_android.ui.features.notifications.LoadingScreen
import com.example.foodhub_android.ui.navigation.AddMenu
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.StatePane
import com.example.foodhub_android.ui.components.ShimmerBlock
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlinx.coroutines.flow.collectLatest
import coil3.compose.AsyncImage
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.utils.StringUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ListMenuItemsScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: ListMenuItemViewModel = hiltViewModel()
) {
    val uiState = viewModel.listMenuItemState.collectAsStateWithLifecycle()
    val editor by viewModel.editorState.collectAsStateWithLifecycle()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.changeImage(it)
    }
    val itemAdded = navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow("added", false)?.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.menuItemEvent.collectLatest { event ->
            when (event) {
                ListMenuItemViewModel.MenuItemEvent.AddNewMenuItem -> {
                    navController.currentBackStackEntry?.savedStateHandle?.set("added", false)
                    navController.navigate(AddMenu)
                }
                ListMenuItemViewModel.MenuItemEvent.EditSaved -> {
                    Toast.makeText(navController.context, "Menu item updated", Toast.LENGTH_SHORT).show()
                }
                ListMenuItemViewModel.MenuItemEvent.ItemDeleted -> {
                    Toast.makeText(navController.context, "Menu item deleted", Toast.LENGTH_SHORT).show()
                }
                is ListMenuItemViewModel.MenuItemEvent.EditFailed ->
                    Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
                is ListMenuItemViewModel.MenuItemEvent.EditSaving -> Unit
            }
        }
    }
    LaunchedEffect(itemAdded?.value) {
        if (itemAdded?.value == true) {
            navController.currentBackStackEntry?.savedStateHandle?.set("added", false)
            viewModel.retry()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            FoodHubHeader("Your menu", "Keep dishes, prices, and photos up to date")
        when (val state = uiState.value) {
            ListMenuItemViewModel.ListMenuItemState.Loading -> MenuGridShimmer()
            is ListMenuItemViewModel.ListMenuItemState.Error -> ErrorScreen(state.message, viewModel::retry)
            is ListMenuItemViewModel.ListMenuItemState.Success -> {
                if (state.data.isEmpty()) {
                    StatePane("Your menu is empty", "Add your first dish to start taking orders", Icons.Rounded.RestaurantMenu, actionLabel = "Add menu item", onAction = viewModel::onAddItemClicked)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.data, key = { it.id }) { item ->
                            Box {
                                FoodItemView(item, animatedVisibilityScope) {
                                    viewModel.openEditor(it)
                                }
                                val paused = !item.isAvailable || item.unavailableUntil != null
                                if (paused) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            if (!item.isAvailable) "Unavailable" else "Paused",
                                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        FloatingActionButton(
            onClick = viewModel::onAddItemClicked,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Rounded.Add, "Add menu item")
        }
    }

    editor.item?.let { item ->
        AlertDialog(
            onDismissRequest = { if (!editor.saving) viewModel.closeEditor() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (editor.editing) "Edit menu item" else item.name, modifier = Modifier.weight(1f))
                    if (!editor.editing) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(Icons.Rounded.Edit, "Edit ${item.name}")
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(
                        model = editor.replacementImage ?: item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxWidth().height(190.dp),
                        contentScale = ContentScale.Crop
                    )
                    if (editor.editing) {
                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            enabled = !editor.saving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, null)
                            Text(if (editor.replacementImage == null) " Replace photo" else " Choose another photo")
                        }
                        OutlinedTextField(
                            value = editor.name,
                            onValueChange = viewModel::changeName,
                            label = { Text("Item name") },
                            singleLine = true,
                            enabled = !editor.saving,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editor.description,
                            onValueChange = viewModel::changeDescription,
                            label = { Text("Description") },
                            minLines = 2,
                            enabled = !editor.saving,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editor.price,
                            onValueChange = viewModel::changePrice,
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            enabled = !editor.saving,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(editor.inventory, viewModel::changeInventory, label = { Text("Inventory quantity") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(editor.tags, viewModel::changeTags, label = { Text("Dietary tags") }, supportingText = { Text("Comma-separated: VEGAN, HALAL, SPICY, GLUTEN_FREE") }, modifier = Modifier.fillMaxWidth())
                        HorizontalDivider()
                        Text("Modifier group", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(editor.modifierName, viewModel::changeModifierName, label = { Text("Group name") }, placeholder = { Text("Size") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(editor.modifierOptions, viewModel::changeModifierOptions, label = { Text("Options") }, supportingText = { Text("Format: Regular:0, Large:1.5") }, modifier = Modifier.fillMaxWidth())
                        Row(verticalAlignment = Alignment.CenterVertically) { Text("Required", Modifier.weight(1f)); Switch(editor.modifierRequired, viewModel::changeModifierRequired) }
                    } else {
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            StringUtils.formatCurrency(item.price),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("${item.inventoryQuantity} portions in stock", style = MaterialTheme.typography.labelLarge)
                        if (item.dietaryTags.isNotEmpty()) Text(item.dietaryTags.joinToString(" · ") { it.replace('_', ' ') }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        val pauseUntil = item.unavailableUntil?.let {
                            runCatching { LocalDateTime.parse(it).format(DateTimeFormatter.ofPattern("d MMM, h:mm a")) }.getOrNull()
                        }
                        Text(
                            when {
                                !item.isAvailable -> "Hidden until you make it available"
                                pauseUntil != null -> "Sold out until $pauseUntil"
                                else -> "Available on your customer menu"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!item.isAvailable || pauseUntil != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider()
                        Text("Availability", style = MaterialTheme.typography.titleSmall)
                        if (!item.isAvailable || item.unavailableUntil != null) {
                            Button(
                                onClick = { viewModel.setAvailability(true) },
                                enabled = !editor.saving,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Make available now") }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.setAvailability(true, LocalDateTime.now().plusHours(2)) },
                                enabled = !editor.saving,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Sold out for 2 hours") }
                            OutlinedButton(
                                onClick = { viewModel.setAvailability(true, LocalDateTime.now().plusDays(1).withHour(8).withMinute(0)) },
                                enabled = !editor.saving,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Sold out until tomorrow") }
                            TextButton(
                                onClick = { viewModel.setAvailability(false) },
                                enabled = !editor.saving,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Hide until manually restored") }
                        }
                    }
                }
            },
            confirmButton = {
                if (editor.editing) {
                    Button(
                        onClick = viewModel::saveEditor,
                        enabled = !editor.saving
                    ) {
                        if (editor.saving) CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
                        else Text("Save changes")
                    }
                } else {
                    Button(onClick = viewModel::startEditing) {
                        Icon(Icons.Rounded.Edit, null)
                        Text(" Edit item")
                    }
                }
            },
            dismissButton = {
                Row {
                    if (editor.editing) {
                        IconButton(onClick = viewModel::confirmDelete, enabled = !editor.saving) {
                            Icon(Icons.Rounded.Delete, "Delete ${item.name}", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(
                        onClick = { if (editor.editing) viewModel.cancelEditing() else viewModel.closeEditor() },
                        enabled = !editor.saving
                    ) { Text(if (editor.editing) "Cancel" else "Close") }
                }
            }
        )

        if (editor.confirmingDelete) {
            AlertDialog(
                onDismissRequest = { if (!editor.saving) viewModel.cancelDelete() },
                icon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete ${item.name}?") },
                text = { Text("This permanently removes the item from your customer menu. This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = viewModel::deleteSelected,
                        enabled = !editor.saving,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (editor.saving) "Deleting…" else "Delete")
                    }
                },
                dismissButton = { TextButton(onClick = viewModel::cancelDelete, enabled = !editor.saving) { Text("Keep item") } }
            )
        }
    }
}

@Composable
private fun MenuGridShimmer() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(6) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBlock(Modifier.fillMaxWidth().height(132.dp))
                ShimmerBlock(Modifier.fillMaxWidth(.75f).height(18.dp))
                ShimmerBlock(Modifier.fillMaxWidth(.42f).height(14.dp))
            }
        }
    }
}
