package com.example.foodhub_android.ui.features.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.foodhub_android.ui.components.FoodHubHeader
import com.example.foodhub_android.ui.components.FoodHubPage
import com.example.foodhub_android.ui.theme.ThemeMode
import com.example.foodhub_android.ui.theme.ThemePreferences
import com.example.foodhub_android.ui.navigation.Account
import com.example.foodhub_android.ui.navigation.Payouts
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val themeMode by ThemePreferences.mode.collectAsStateWithLifecycle()
    var showThemePicker by remember { mutableStateOf(false) }
    val notificationsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val locationAllowed = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    FoodHubPage {
        FoodHubHeader("Settings", "Permissions, privacy, and app preferences", onBack = navController::popBackStack)
        Column(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Account", style = MaterialTheme.typography.titleLarge)
            SettingCard(
                title = "Profile and security",
                description = "Update your personal details and password",
                icon = Icons.Rounded.ManageAccounts,
                status = "Manage"
            ) { navController.navigate(Account) }
            if (context.packageName.endsWith(".restaurant") || context.packageName.endsWith(".rider")) {
                SettingCard("Bank and payouts", "Manage payout account and transfer history", Icons.Rounded.AccountBalance, "Manage") {
                    navController.navigate(Payouts)
                }
            }
            Text("Permissions", style = MaterialTheme.typography.titleLarge)
            SettingCard(
                title = "Notifications",
                description = if (notificationsAllowed) "Order and delivery alerts are enabled" else "Alerts are currently disabled",
                icon = Icons.Rounded.Notifications,
                status = if (notificationsAllowed) "Enabled" else "Off"
            ) {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else appDetailsIntent(context.packageName)
                context.startActivity(intent)
            }
            SettingCard(
                title = "Location",
                description = if (locationAllowed) "Used for delivery pins and rider navigation" else "Enable for accurate routes and addresses",
                icon = Icons.Rounded.LocationOn,
                status = if (locationAllowed) "Enabled" else "Off"
            ) { context.startActivity(appDetailsIntent(context.packageName)) }

            Text("Preferences", style = MaterialTheme.typography.titleLarge)
            SettingCard(
                title = "Appearance",
                description = "Choose how SwiftBite looks on this device",
                icon = Icons.Rounded.DarkMode,
                status = themeMode.label
            ) { showThemePicker = true }
            SettingCard(
                title = "Privacy and storage",
                description = "Account data is isolated locally and cleared when you sign out",
                icon = Icons.Rounded.Security,
                status = "Protected"
            ) { context.startActivity(appDetailsIntent(context.packageName)) }

            Spacer(Modifier.weight(1f))
            Text("SwiftBite", style = MaterialTheme.typography.titleMedium)
            Text("Customer, restaurant, and rider delivery platform", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.navigationBarsPadding().height(12.dp))
        }
    }

    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            icon = { Icon(Icons.Rounded.DarkMode, contentDescription = null) },
            title = { Text("Choose appearance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeMode.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == option,
                                onClick = {
                                    ThemePreferences.setMode(context, option)
                                    showThemePicker = false
                                }
                            )
                            Text(option.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    status: String,
    onClick: () -> Unit
) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(onClick = onClick, label = { Text(status) })
        }
    }
}

private fun appDetailsIntent(packageName: String) =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
