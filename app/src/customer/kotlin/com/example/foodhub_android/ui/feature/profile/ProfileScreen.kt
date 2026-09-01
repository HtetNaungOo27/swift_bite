package com.example.foodhub_android.ui.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.foodhub_android.data.models.CustomerProfile
import com.example.foodhub_android.ui.components.*
import com.example.foodhub_android.ui.navigation.AddressList
import com.example.foodhub_android.ui.navigation.AuthScreen
import com.example.foodhub_android.ui.navigation.OrderList
import com.example.foodhub_android.ui.navigation.AppSettings
import com.example.foodhub_android.utils.StringUtils

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    var editing by remember { mutableStateOf(false) }
    val updateState = viewModel.updateState.collectAsStateWithLifecycle().value
    FoodHubPage {
        FoodHubHeader("Profile", "Your SwiftBite account and rewards")
        when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
            ProfileViewModel.State.Loading -> StatePane("Loading your profile", "Gathering your SwiftBite details", loading = true)
            is ProfileViewModel.State.Error -> StatePane("Couldn’t load profile", state.message, Icons.Rounded.Refresh, actionLabel = "Try again", onAction = viewModel::refresh)
            is ProfileViewModel.State.Success -> ProfileContent(
                state.profile,
                navController,
                onEdit = { editing = true },
                logout = {
                    viewModel.logout()
                    navController.navigate(AuthScreen) { popUpTo(navController.graph.id) { inclusive = true } }
                }
            )
        }
    }
    val profile = (viewModel.state.collectAsStateWithLifecycle().value as? ProfileViewModel.State.Success)?.profile
    if (editing && profile != null) {
        var name by remember(profile.id) { mutableStateOf(profile.name) }
        AlertDialog(
            onDismissRequest = { if (updateState !is ProfileViewModel.UpdateState.Saving) { editing = false; viewModel.clearUpdateState() } },
            icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
            title = { Text("Edit profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it.take(80) }, label = { Text("Full name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Your email is used to sign in and cannot be changed here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (updateState is ProfileViewModel.UpdateState.Error) Text(updateState.message, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.updateName(name) }, enabled = updateState !is ProfileViewModel.UpdateState.Saving) {
                    if (updateState is ProfileViewModel.UpdateState.Saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Save")
                }
            },
            dismissButton = { TextButton(onClick = { editing = false; viewModel.clearUpdateState() }) { Text("Cancel") } }
        )
        LaunchedEffect(updateState) {
            if (updateState is ProfileViewModel.UpdateState.Saved) { editing = false; viewModel.clearUpdateState() }
        }
    }
}

@Composable
private fun ProfileContent(profile: CustomerProfile, navController: NavController, onEdit: () -> Unit, logout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(72.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(initials(profile.name), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(profile.email, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Member since ${profile.memberSince.take(10)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit profile")
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraLarge) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Rounded.Stars, null, Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Bite Points", style = MaterialTheme.typography.labelLarge)
                    Text(profile.bitePoints.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Earn 1 point for every completed dollar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileMetric("Orders", profile.completedOrders.toString(), Modifier.weight(1f))
            ProfileMetric("Spent", StringUtils.formatCurrency(profile.totalSpent), Modifier.weight(1f))
            ProfileMetric("Addresses", profile.savedAddresses.toString(), Modifier.weight(1f))
        }

        Text("Account", style = MaterialTheme.typography.titleLarge)
        ProfileAction("My orders", "Active and previous orders", Icons.Rounded.ReceiptLong) { navController.navigate(OrderList) }
        ProfileAction("Delivery addresses", "Manage saved addresses", Icons.Rounded.LocationOn) { navController.navigate(AddressList) }
        ProfileAction("App settings", "Permissions, privacy, and appearance", Icons.Rounded.Settings) { navController.navigate(AppSettings) }
        OutlinedButton(onClick = logout, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
            Icon(Icons.AutoMirrored.Rounded.Logout, null); Spacer(Modifier.width(9.dp)); Text("Sign out")
        }
        Spacer(Modifier.navigationBarsPadding().height(12.dp))
    }
}

@Composable
private fun ProfileMetric(label: String, value: String, modifier: Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(13.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) {
    ElevatedCard(onClick = action, shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Rounded.ChevronRight, null)
        }
    }
}

private fun initials(name: String) = name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { "SB" }
