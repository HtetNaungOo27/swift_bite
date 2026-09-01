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

private val CustomerLightColors = lightColorScheme(
    primary = CustomerPrimary, onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3DA), onPrimaryContainer = Color(0xFF681C09),
    secondary = Color(0xFFD99500), onSecondary = Color(0xFF2C2000),
    secondaryContainer = Color(0xFFFFE7A8), onSecondaryContainer = Color(0xFF473600),
    background = CustomerSurfaceLight, onBackground = Color(0xFF171A21),
    surface = Color.White, onSurface = Color(0xFF171A21),
    surfaceVariant = Color(0xFFF4EFEC), onSurfaceVariant = Color(0xFF65606A),
    outline = Color(0xFFE2DCD8)
)

private val CustomerDarkColors = darkColorScheme(
    primary = CustomerPrimaryDark, onPrimary = Color(0xFF4A1004),
    primaryContainer = Color(0xFF7A2A17), onPrimaryContainer = Color(0xFFFFDBD1),
    secondary = Color(0xFFFFC857), onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF584400), onSecondaryContainer = Color(0xFFFFE08D),
    background = CustomerSurfaceDark, onBackground = Color(0xFFE8E0DD),
    surface = Color(0xFF191C22), onSurface = Color(0xFFE8E0DD),
    surfaceVariant = Color(0xFF29262A), onSurfaceVariant = Color(0xFFCEC4C0),
    outline = Color(0xFF978E89)
)

private val RestaurantLightColors = lightColorScheme(
    primary = RestaurantPrimary, onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF3FF), onPrimaryContainer = Color(0xFF073A59),
    secondary = Color(0xFF315D75), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6EAF5), onSecondaryContainer = Color(0xFF123B50),
    background = RestaurantSurfaceLight, onBackground = Color(0xFF15242F),
    surface = Color.White, onSurface = Color(0xFF15242F),
    surfaceVariant = Color(0xFFEAF3F8), onSurfaceVariant = Color(0xFF5E707B),
    outline = Color(0xFFD2E1E9)
)

private val RestaurantDarkColors = darkColorScheme(
    primary = RestaurantPrimaryDark, onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF124B68), onPrimaryContainer = Color(0xFFCAECFF),
    secondary = Color(0xFFA8D8F2), onSecondary = Color(0xFF073549),
    secondaryContainer = Color(0xFF244C61), onSecondaryContainer = Color(0xFFC9E8F8),
    background = RestaurantSurfaceDark, onBackground = Color(0xFFDDE4E8),
    surface = Color(0xFF171F24), onSurface = Color(0xFFDDE4E8),
    surfaceVariant = Color(0xFF25343C), onSurfaceVariant = Color(0xFFB9C9D1),
    outline = Color(0xFF84949C)
)

private val RiderLightColors = lightColorScheme(
    primary = RiderPrimary, onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F7EE), onPrimaryContainer = Color(0xFF063C33),
    secondary = Color(0xFFE39420), onSecondary = Color(0xFF2E1B00),
    secondaryContainer = Color(0xFFFFE2B5), onSecondaryContainer = Color(0xFF4B2D00),
    background = RiderSurfaceLight, onBackground = Color(0xFF172622),
    surface = Color.White, onSurface = Color(0xFF172622),
    surfaceVariant = Color(0xFFE8F2EF), onSurfaceVariant = Color(0xFF596A65),
    outline = Color(0xFFD1E1DC)
)

private val RiderDarkColors = darkColorScheme(
    primary = RiderPrimaryDark, onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF005142), onPrimaryContainer = Color(0xFF8FF8D8),
    secondary = Color(0xFFFFCA78), onSecondary = Color(0xFF432C00),
    secondaryContainer = Color(0xFF604100), onSecondaryContainer = Color(0xFFFFDEA5),
    background = RiderSurfaceDark, onBackground = Color(0xFFDDE5E1),
    surface = Color(0xFF18211E), onSurface = Color(0xFFDDE5E1),
    surfaceVariant = Color(0xFF25332F), onSurfaceVariant = Color(0xFFBAC9C3),
    outline = Color(0xFF84958F)
)

val SwiftBiteShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object SwiftBiteSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 16.dp
    val md = 24.dp
    val lg = 32.dp
}

@Composable
fun SwiftBiteTheme(
    flavor: SwiftBiteFlavor,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when (flavor) {
        SwiftBiteFlavor.CUSTOMER -> if (darkTheme) CustomerDarkColors else CustomerLightColors
        SwiftBiteFlavor.RESTAURANT -> if (darkTheme) RestaurantDarkColors else RestaurantLightColors
        SwiftBiteFlavor.RIDER -> if (darkTheme) RiderDarkColors else RiderLightColors
    }
    MaterialTheme(colorScheme = colors, typography = Typography, shapes = SwiftBiteShapes, content = content)
}

@Composable
fun FoodHubAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dynamic color stays disabled so SwiftBite's role identity remains predictable.
    SwiftBiteTheme(SwiftBiteFlavor.CUSTOMER, darkTheme, content)
}
