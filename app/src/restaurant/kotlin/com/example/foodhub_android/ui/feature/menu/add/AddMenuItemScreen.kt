package com.example.foodhub_android.ui.feature.menu.add

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.ui.FoodHubTextField
import com.example.foodhub_android.ui.navigation.ImagePicker
import kotlinx.coroutines.flow.collectLatest
import com.example.foodhub_android.ui.components.FoodHubHeader

@Composable
fun AddMenuItemScreen(
    navController: NavController,
    viewModel: AddMenuItemViewModel = hiltViewModel()
) {
    val name = viewModel.name.collectAsStateWithLifecycle()
    val description = viewModel.description.collectAsStateWithLifecycle()
    val price = viewModel.price.collectAsStateWithLifecycle()
    val uiState = viewModel.addMenuItemState.collectAsStateWithLifecycle()
    val selectedImage = viewModel.imageUrl.collectAsStateWithLifecycle()
    val imageUri = navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow<Uri?>("imageUri", null)?.collectAsStateWithLifecycle()

    LaunchedEffect(imageUri?.value) {
        imageUri?.value?.let(viewModel::onImageUrlChange)
    }
    LaunchedEffect(Unit) {
        viewModel.addMenuItemEvent.collectLatest { event ->
            when (event) {
                AddMenuItemViewModel.AddMenuItemEvent.GoBack -> {
                    Toast.makeText(navController.context, "Menu item added", Toast.LENGTH_SHORT).show()
                    navController.previousBackStackEntry?.savedStateHandle?.set("added", true)
                    navController.popBackStack()
                }
                AddMenuItemViewModel.AddMenuItemEvent.AddNewImage -> navController.navigate(ImagePicker)
                is AddMenuItemViewModel.AddMenuItemEvent.ShowErrorMessage ->
                    Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val loading = uiState.value is AddMenuItemViewModel.AddMenuItemState.Loading
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FoodHubHeader("Add menu item", "Create a new dish", onBack = navController::popBackStack)
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AsyncImage(
            model = selectedImage.value,
            contentDescription = "Menu item image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !loading, onClick = viewModel::onImageClicked)
        )
        if (selectedImage.value == null) {
            Text("Tap the image area to choose a photo", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FoodHubTextField(name.value, viewModel::onNameChange, Modifier.fillMaxWidth(), label = { Text("Name") })
        FoodHubTextField(description.value, viewModel::onDescriptionChange, Modifier.fillMaxWidth(), label = { Text("Description") })
        FoodHubTextField(
            price.value,
            { value -> if (value.isEmpty() || value.matches(Regex("\\d*(\\.\\d{0,2})?"))) viewModel.onPriceChange(value) },
            Modifier.fillMaxWidth(),
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        if (uiState.value is AddMenuItemViewModel.AddMenuItemState.Error) {
            Text((uiState.value as AddMenuItemViewModel.AddMenuItemState.Error).message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = viewModel::addMenuItem,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            else Text("Add menu item")
        }
        }
    }
}
