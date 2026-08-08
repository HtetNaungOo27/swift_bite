package com.example.foodhub_android.ui.features.add_address

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun AddAddressScreen(navController: NavController, viewModel: AddAddressViewModel= hiltViewModel()) {


    Column {
        val cameraState = rememberCameraPositionState()
        cameraState.position = CameraPosition.fromLatLngZoom(LatLng(40.9971,29.1007),13f)

        GoogleMap(
            cameraPositionState = cameraState,
            modifier = Modifier.fillMaxSize()
        )
    }

}