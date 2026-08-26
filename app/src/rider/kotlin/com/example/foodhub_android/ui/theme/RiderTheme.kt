package com.example.foodhub_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val RiderTeal = Color(0xFF13A88A)
val RiderNavy = Color(0xFF12312D)

private val RiderLightColors = lightColorScheme(
    primary = RiderTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F7EE),
    onPrimaryContainer = RiderNavy,
    secondary = Color(0xFFFFB84D),
    background = Color(0xFFF5FAF8),
    onBackground = Color(0xFF172622),
    surface = Color.White,
    onSurface = Color(0xFF172622),
    surfaceVariant = Color(0xFFE8F2EF),
    onSurfaceVariant = Color(0xFF65736F),
    outline = Color(0xFFD8E5E1)
)

private val RiderDarkColors = darkColorScheme(
    primary = Color(0xFF6DE1C5),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF005142),
    secondary = Color(0xFFFFCA78),
    background = Color(0xFF101815),
    surface = Color(0xFF18211E)
)

@Composable
fun RiderTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) RiderDarkColors else RiderLightColors,
        typography = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(10.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(36.dp)
        ),
        content = content
    )
}
