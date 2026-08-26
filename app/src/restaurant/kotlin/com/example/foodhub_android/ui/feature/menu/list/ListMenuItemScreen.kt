package com.example.foodhub_android.ui.feature.menu.list

import android.widget.Toast
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlinx.coroutines.flow.collectLatest
import coil3.compose.AsyncImage
import com.example.foodhub_android.data.models.FoodItem
import com.example.foodhub_android.utils.StringUtils

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ListMenuItemsScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: ListMenuItemViewModel = hiltViewModel()
) {
    val uiState = viewModel.listMenuItemState.collectAsStateWithLifecycle()
    var selectedItem by remember { mutableStateOf<FoodItem?>(null) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var editSaving by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
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
                    selectedItem = null
                    Toast.makeText(navController.context, "Menu item updated", Toast.LENGTH_SHORT).show()
                }
                is ListMenuItemViewModel.MenuItemEvent.EditFailed ->
                    Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
                is ListMenuItemViewModel.MenuItemEvent.EditSaving -> editSaving = event.saving
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
                    StatePane("Your menu is empty", "Add your first dish to start taking orders", Icons.Rounded.RestaurantMenu)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.data, key = { it.id }) { item ->
                            FoodItemView(item, animatedVisibilityScope) {
                                selectedItem = it
                                editName = it.name
                                editDescription = it.description
                                editPrice = it.price.toString()
                                editMode = false
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

    selectedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (editMode) "Edit menu item" else item.name, modifier = Modifier.weight(1f))
                    if (!editMode) {
                        IconButton(onClick = { editMode = true }) {
                            Icon(Icons.Rounded.Edit, "Edit ${item.name}")
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxWidth().height(190.dp),
                        contentScale = ContentScale.Crop
                    )
                    if (editMode) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Item name") },
                            singleLine = true,
                            enabled = !editSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Description") },
                            minLines = 2,
                            enabled = !editSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPrice,
                            onValueChange = { value -> if (value.isEmpty() || value.matches(Regex("\\d*(\\.\\d{0,2})?"))) editPrice = value },
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            enabled = !editSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            StringUtils.formatCurrency(item.price),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Visible on your customer menu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                if (editMode) {
                    Button(
                        onClick = { viewModel.updateItem(item.id, editName, editDescription, editPrice) },
                        enabled = !editSaving
                    ) {
                        if (editSaving) CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
                        else Text("Save changes")
                    }
                } else {
                    Button(onClick = { editMode = true }) {
                        Icon(Icons.Rounded.Edit, null)
                        Text(" Edit item")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (editMode) editMode = false else selectedItem = null },
                    enabled = !editSaving
                ) { Text(if (editMode) "Cancel" else "Close") }
            }
        )
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
