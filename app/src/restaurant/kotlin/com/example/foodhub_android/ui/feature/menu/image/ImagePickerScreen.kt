package com.example.foodhub_android.ui.feature.menu.image

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.foodhub_android.ui.components.FoodHubHeader

@Composable
fun ImagePickerScreen(navController: NavController) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null && selectedImageUri == null) navController.popBackStack()
        else if (uri != null) selectedImageUri = uri
    }

    LaunchedEffect(Unit) { picker.launch("image/*") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FoodHubHeader("Choose menu image", "Preview before using it", onBack = navController::popBackStack)
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AsyncImage(
            model = selectedImageUri,
            contentDescription = "Selected menu image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(360.dp)
        )
        OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("Choose another image")
        }
        Button(
            enabled = selectedImageUri != null,
            onClick = {
                navController.previousBackStackEntry?.savedStateHandle?.set("imageUri", selectedImageUri)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Use this image") }
        }
    }
}
