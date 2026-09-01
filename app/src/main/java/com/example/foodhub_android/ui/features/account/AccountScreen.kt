package com.example.foodhub_android.ui.features.account

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.UpdateAccountRequest
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.FoodHubPage
import com.example.foodhub_android.ui.components.StatePane
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AccountScreen(navController: NavController, viewModel: AccountViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var passwordDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.events.collectLatest { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    FoodHubPage {
        FoodHubHeader("Your account", "Personal details and security", onBack = navController::popBackStack)
        when (val value = state) {
            AccountViewModel.State.Loading -> CircularProgressIndicator(Modifier.padding(32.dp))
            is AccountViewModel.State.Error -> StatePane(
                "Couldn’t load account",
                value.message,
                Icons.Rounded.Person,
                actionLabel = "Retry",
                onAction = viewModel::refresh
            )
            is AccountViewModel.State.Ready -> AccountForm(value.profile, saving, viewModel::save) { passwordDialog = true }
        }
    }
    if (passwordDialog) PasswordDialog(saving, { passwordDialog = false }) { current, next, confirm ->
        viewModel.changePassword(current, next, confirm)
        passwordDialog = false
    }
}

@Composable
private fun AccountForm(profile: com.example.foodhub_android.data.models.AccountProfile, saving: Boolean, onSave: (UpdateAccountRequest) -> Unit, onPassword: () -> Unit) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var phone by remember(profile) { mutableStateOf(profile.phone.orEmpty()) }
    var vehicle by remember(profile) { mutableStateOf(profile.vehicleType.orEmpty()) }
    var plate by remember(profile) { mutableStateOf(profile.vehiclePlate.orEmpty()) }
    val rider = profile.role.equals("RIDER", true)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(name, { name = it.take(80) }, label = { Text("Full name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it.take(120) }, label = { Text("Email address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it.take(40) }, label = { Text("Phone number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (rider) {
            Text("Delivery vehicle", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(vehicle, { vehicle = it.take(40) }, label = { Text("Vehicle type") }, placeholder = { Text("Motorbike") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(plate, { plate = it.take(40) }, label = { Text("Vehicle plate") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        Button(onClick = { onSave(UpdateAccountRequest(name, email, phone, vehicle, plate)) }, enabled = !saving && name.trim().length >= 2 && email.contains('@'), modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(if (saving) "Saving…" else "Save account")
        }
        OutlinedButton(onClick = onPassword, enabled = !saving, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Rounded.Lock, null); Spacer(Modifier.width(8.dp)); Text("Change password")
        }
        Text("Changing your email affects your next sign-in.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PasswordDialog(saving: Boolean, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var current by remember { mutableStateOf("") }; var next by remember { mutableStateOf("") }; var confirm by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Change password") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(current, { current = it }, label = { Text("Current password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
            OutlinedTextField(next, { next = it }, label = { Text("New password") }, supportingText = { Text("At least 8 characters") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
            OutlinedTextField(confirm, { confirm = it }, label = { Text("Confirm new password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        }
    }, confirmButton = { Button(onClick = { onSave(current, next, confirm) }, enabled = !saving && current.isNotBlank() && next.length >= 8 && next == confirm) { Text("Change") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
