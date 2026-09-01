package com.example.foodhub_android.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

@Composable
fun NotificationPermissionPrompt(isAuthenticated: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("notification_permission_ux", Context.MODE_PRIVATE)
    }
    var showEducation by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { preferences.edit().putBoolean(EDUCATION_SHOWN, true).apply() }

    LaunchedEffect(isAuthenticated) {
        if (
            isAuthenticated &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            !preferences.getBoolean(EDUCATION_SHOWN, false)
        ) {
            // Let the home screen settle before showing a contextual request.
            delay(600)
            showEducation = true
        } else if (!isAuthenticated) {
            showEducation = false
        }
    }

    if (showEducation) {
        AlertDialog(
            onDismissRequest = {
                showEducation = false
                preferences.edit().putBoolean(EDUCATION_SHOWN, true).apply()
            },
            icon = {
                Icon(
                    Icons.Rounded.NotificationsActive,
                    contentDescription = null
                )
            },
            title = { Text("Stay updated") },
            text = {
                Text(
                    "Allow SwiftBite notifications for order progress, new jobs, " +
                        "delivery updates, and time-sensitive alerts."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEducation = false
                        preferences.edit().putBoolean(EDUCATION_SHOWN, true).apply()
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) { Text("Enable alerts") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEducation = false
                        preferences.edit().putBoolean(EDUCATION_SHOWN, true).apply()
                    }
                ) { Text("Not now") }
            }
        )
    }
}

private const val EDUCATION_SHOWN = "education_shown"
