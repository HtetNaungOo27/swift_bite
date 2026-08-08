package com.example.foodhub_android.ui.features.add_address

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun AddAddressScreen(navController: NavController, viewModel: AddAddressViewModel= hiltViewModel()) {


    Column {
        val cameraState = rememberCameraPositionState()
    }

}