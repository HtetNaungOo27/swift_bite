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
import androidx.compose.material.icons.rounded.MyLocation
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
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import androidx.core.content.ContextCompat
import com.example.foodhub_android.utils.PlusCodeEncoder
import com.example.foodhub_android.data.models.Address

@Composable
fun AddAddressScreen(navController: NavController, viewModel: AddAddressViewModel = hiltViewModel()) {
    val editing = navController.previousBackStackEntry?.savedStateHandle?.get<Address>("editAddress")
    var line1 by rememberSaveable(editing?.id) { mutableStateOf(editing?.addressLine1.orEmpty()) }
    var line2 by rememberSaveable(editing?.id) { mutableStateOf(editing?.addressLine2.orEmpty()) }
    var city by rememberSaveable(editing?.id) { mutableStateOf(editing?.city.orEmpty()) }
    var state by rememberSaveable(editing?.id) { mutableStateOf(editing?.state.orEmpty()) }
    var zip by rememberSaveable(editing?.id) { mutableStateOf(editing?.zipCode.orEmpty()) }
    var country by rememberSaveable(editing?.id) { mutableStateOf(editing?.country.orEmpty()) }
    var landmark by rememberSaveable(editing?.id) { mutableStateOf(editing?.landmark.orEmpty()) }
    var plusCode by rememberSaveable(editing?.id) { mutableStateOf(editing?.plusCode.orEmpty()) }
    var selectedPoint by remember(editing?.id) { mutableStateOf(LatLng(editing?.latitude ?: 16.8409, editing?.longitude ?: 96.1735)) }
    var hasMyanmarLocation by remember { mutableStateOf(false) }
    val deliveryMarkerState = rememberUpdatedMarkerState(position = selectedPoint)
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
    val validation = viewModel.validation.collectAsStateWithLifecycle().value
    val geocodedAddress = viewModel.geocodedAddress.collectAsStateWithLifecycle().value

    LaunchedEffect(geocodedAddress) {
        geocodedAddress?.let { result ->
            if (line1.isBlank()) line1 = result.addressLine1
            if (line2.isBlank()) line2 = result.addressLine2.orEmpty()
            if (city.isBlank()) city = result.city
            if (state.isBlank()) state = result.state
            if (zip.isBlank()) zip = result.zipCode
            if (country.isBlank()) country = result.country
        }
    }

    LaunchedEffect(selectedPoint) {
        plusCode = PlusCodeEncoder.encode(selectedPoint.latitude, selectedPoint.longitude)
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { location ->
                        location?.takeIf { it.latitude in 9.0..29.0 && it.longitude in 92.0..102.0 }?.let {
                            hasMyanmarLocation = true
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
        FoodHubHeader(if (editing == null) "Add an address" else "Edit address", "Where should SwiftBite deliver?", onBack = navController::popBackStack)
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
            Text(
                if (hasMyanmarLocation) "Your blue dot shows your current position. Tap the map to place the delivery pin."
                else "The map starts in Yangon. Tap the map to place the delivery pin, or set a Myanmar location in the emulator controls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!locationGranted) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.large) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MyLocation, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Use your current location", style = MaterialTheme.typography.titleMedium)
                            Text("Optional—this helps position the delivery pin faster.", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }) { Text("Enable") }
                    }
                }
            }
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    cameraPositionState = camera,
                    properties = MapProperties(isMyLocationEnabled = locationGranted && hasMyanmarLocation),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = locationGranted && hasMyanmarLocation, zoomControlsEnabled = false),
                    onMapClick = { selectedPoint = it; viewModel.reverseGeocode(it.latitude, it.longitude) }
                ) { Marker(state = deliveryMarkerState, title = "Delivery entrance") }
            }
            AddressField(landmark, { landmark = it }, "Nearby landmark", false)
            AddressField(plusCode, { plusCode = it.uppercase() }, "Plus code (generated from pin)", false, error = validation.plusCode, reserveErrorSpace = true)
            AddressField(line1, { line1 = it }, "Street address", true, error = validation.line1)
            AddressField(line2, { line2 = it }, "Apartment, suite, floor", false)
            AddressField(city, { city = it }, "City", true, error = validation.city)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressField(state, { state = it }, "State/Region", true, Modifier.weight(1f), error = validation.state)
                AddressField(zip, { zip = it }, "Postal code", true, Modifier.weight(1f), KeyboardType.Number, error = validation.zipCode)
            }
            AddressField(country, { country = it }, "Country", true, error = validation.country)
            if (uiState is AddAddressViewModel.AddAddressState.Error) {
                Text(uiState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { viewModel.saveAddress(line1, line2, city, state, zip, country, landmark, plusCode, selectedPoint.latitude, selectedPoint.longitude, editing?.id) },
                enabled = uiState !is AddAddressViewModel.AddAddressState.Saving,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (uiState is AddAddressViewModel.AddAddressState.Saving) CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(if (editing == null) "Save address" else "Save changes")
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
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    reserveErrorSpace: Boolean = required
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (required) "$label *" else label) },
        modifier = modifier,
        singleLine = true,
        isError = error != null,
        supportingText = if (reserveErrorSpace) ({ Text(error ?: " ") }) else null,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
