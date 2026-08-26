package com.example.foodhub_android.ui.feature.add_address

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.FoodHubPage
import com.example.foodhub_android.ui.navigation.AuthScreen
import kotlinx.coroutines.flow.collectLatest
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import androidx.core.content.ContextCompat

@Composable
fun AddAddressScreen(navController: NavController, viewModel: AddAddressViewModel = hiltViewModel()) {
    var line1 by rememberSaveable { mutableStateOf("") }
    var line2 by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var zip by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var landmark by rememberSaveable { mutableStateOf("") }
    var plusCode by rememberSaveable { mutableStateOf("") }
    var selectedPoint by remember { mutableStateOf(LatLng(16.8409, 96.1735)) }
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(selectedPoint, 13f) }
    val context = LocalContext.current
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        if (!locationGranted) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            selectedPoint = LatLng(it.latitude, it.longitude)
                            camera.move(CameraUpdateFactory.newLatLngZoom(selectedPoint, 16f))
                        }
                    }
            } catch (_: SecurityException) {
                locationGranted = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                AddAddressViewModel.AddAddressEvent.Saved -> {
                    Toast.makeText(navController.context, "Address saved", Toast.LENGTH_SHORT).show()
                    navController.previousBackStackEntry?.savedStateHandle?.set("isAddressAdded", true)
                    navController.popBackStack()
                }
                AddAddressViewModel.AddAddressEvent.SessionExpired -> navController.navigate(AuthScreen) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }

    FoodHubPage {
        FoodHubHeader("Add an address", "Where should SwiftBite deliver?", onBack = navController::popBackStack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Row(Modifier.fillMaxWidth().padding(18.dp)) {
                    Icon(Icons.Rounded.Home, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Enter the address manually. You can change it before checkout.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Pin the delivery entrance", style = MaterialTheme.typography.titleMedium)
            Text("Your blue dot shows your current position. Tap the map to place the delivery pin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    cameraPositionState = camera,
                    properties = MapProperties(isMyLocationEnabled = locationGranted),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = locationGranted, zoomControlsEnabled = false),
                    onMapClick = { selectedPoint = it }
                ) { Marker(state = MarkerState(selectedPoint), title = "Delivery entrance") }
            }
            AddressField(landmark, { landmark = it }, "Nearby landmark", false)
            AddressField(plusCode, { plusCode = it.uppercase() }, "Plus code", false)
            AddressField(line1, { line1 = it }, "Street address", true)
            AddressField(line2, { line2 = it }, "Apartment, suite, floor", false)
            AddressField(city, { city = it }, "City", true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressField(state, { state = it }, "State/Region", true, Modifier.weight(1f))
                AddressField(zip, { zip = it }, "Postal code", true, Modifier.weight(1f), KeyboardType.Number)
            }
            AddressField(country, { country = it }, "Country", true)
            if (uiState is AddAddressViewModel.AddAddressState.Error) {
                Text(uiState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { viewModel.saveAddress(line1, line2, city, state, zip, country, landmark, plusCode, selectedPoint.latitude, selectedPoint.longitude) },
                enabled = uiState !is AddAddressViewModel.AddAddressState.Saving,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (uiState is AddAddressViewModel.AddAddressState.Saving) CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Save address")
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun AddressField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    required: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (required) "$label *" else label) },
        modifier = modifier,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
