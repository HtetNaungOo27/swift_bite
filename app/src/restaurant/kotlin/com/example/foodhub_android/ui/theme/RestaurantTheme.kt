package com.example.foodhub_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val RestaurantSkyBlue = Color(0xFF2D9CDB)
val RestaurantDarkBlue = Color(0xFF123B5D)
val RestaurantLightBlue = Color(0xFFDDF3FF)

private val RestaurantLightColors = lightColorScheme(
    primary = RestaurantSkyBlue,
    onPrimary = Color.White,
    primaryContainer = RestaurantLightBlue,
    onPrimaryContainer = RestaurantDarkBlue,
    secondary = RestaurantDarkBlue,
    background = Color(0xFFF5FAFD),
    onBackground = Color(0xFF15242F),
    surface = Color.White,
    onSurface = Color(0xFF15242F),
    surfaceVariant = Color(0xFFEAF3F8),
    onSurfaceVariant = Color(0xFF667782),
    outline = Color(0xFFDCE7ED)
)

private val RestaurantDarkColors = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    onPrimary = Color(0xFF003548),
    primaryContainer = RestaurantDarkBlue,
    onPrimaryContainer = RestaurantLightBlue,
    secondary = RestaurantSkyBlue
)

@Composable
fun RestaurantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) RestaurantDarkColors else RestaurantLightColors,
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
