package com.example.foodhub_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RestaurantSkyBlue = RestaurantPrimary
val RestaurantDarkBlue = Color(0xFF123B5D)
val RestaurantLightBlue = Color(0xFFDDF3FF)

@Composable
fun RestaurantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = SwiftBiteTheme(SwiftBiteFlavor.RESTAURANT, darkTheme, content)
