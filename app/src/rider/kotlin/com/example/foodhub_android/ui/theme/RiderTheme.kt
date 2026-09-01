package com.example.foodhub_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RiderTeal = RiderPrimary
val RiderNavy = Color(0xFF12312D)

@Composable
fun RiderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = SwiftBiteTheme(SwiftBiteFlavor.RIDER, darkTheme, content)
