package com.example.foodhub_android.ui.motion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

/** Visual press feedback. Pass the same interaction source to the clickable component. */
@Composable
fun Modifier.bounceClick(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 650f),
        label = "tactileScale"
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
    val transition = rememberInfiniteTransition(label = "structuralShimmer")
    val x by transition.animateFloat(
        initialValue = -900f,
        targetValue = 1_800f,
        animationSpec = infiniteRepeatable(tween(1_250, easing = FastOutSlowInEasing)),
        label = "shimmerSweep"
    )
    background(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(x, 0f),
            end = Offset(x + 700f, 500f)
        )
    )
}
