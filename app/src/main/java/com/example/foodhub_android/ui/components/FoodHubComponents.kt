package com.example.foodhub_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.foodhub_android.ui.motion.shimmerEffect

@Composable
fun FoodHubPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        content = content
    )
}

@Composable
fun FoodHubHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        action?.invoke()
    }
}

/** Keeps destructive and infrequent account actions out of the primary task area. */
@Composable
fun AccountActionsMenu(
    onSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                onClick = { expanded = false; onSettings() }
            )
            DropdownMenuItem(
                text = { Text("Sign out") },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                onClick = { expanded = false; onSignOut() }
            )
        }
    }
}

@Composable
fun SectionHeading(title: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (actionLabel != null) TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier) {
    val normalized = text.uppercase()
    val container = when {
        normalized.contains("DELIVERED") || normalized.contains("SUCCESS") -> MaterialTheme.colorScheme.primaryContainer
        normalized.contains("FAILED") || normalized.contains("REJECT") -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val content = when {
        normalized.contains("FAILED") || normalized.contains("REJECT") -> MaterialTheme.colorScheme.onErrorContainer
        normalized.contains("DELIVERED") || normalized.contains("SUCCESS") -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val icon = when {
        normalized.contains("PENDING") -> Icons.Rounded.Schedule
        normalized.contains("PREPAR") -> Icons.Rounded.LocalFireDepartment
        normalized == "READY" -> Icons.Rounded.ShoppingBag
        normalized.contains("ASSIGNED") -> Icons.Rounded.PersonPinCircle
        normalized.contains("OUT_FOR_DELIVERY") -> Icons.Rounded.TwoWheeler
        normalized.contains("DELIVERED") || normalized.contains("SUCCESS") -> Icons.Rounded.CheckCircle
        normalized.contains("FAILED") || normalized.contains("REJECT") || normalized.contains("CANCEL") -> Icons.Rounded.ErrorOutline
        normalized.contains("ACCEPT") -> Icons.Rounded.ThumbUp
        normalized.contains("COD") -> Icons.Rounded.Payments
        else -> Icons.Rounded.Info
    }
    Surface(modifier, color = container, contentColor = content, shape = CircleShape) {
        Row(Modifier.padding(horizontal = 11.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                text.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ShimmerBlock(modifier: Modifier = Modifier) {
    Box(modifier.clip(MaterialTheme.shapes.medium).shimmerEffect())
}

@Composable
fun ListSkeleton(rows: Int = 4, showTabs: Boolean = false) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showTabs) ShimmerBlock(Modifier.fillMaxWidth().height(44.dp))
        repeat(rows) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ShimmerBlock(Modifier.size(58.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        ShimmerBlock(Modifier.fillMaxWidth(.7f).height(17.dp))
                        ShimmerBlock(Modifier.fillMaxWidth(.48f).height(13.dp))
                        ShimmerBlock(Modifier.fillMaxWidth(.86f).height(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSkeleton() {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ShimmerBlock(Modifier.fillMaxWidth().height(210.dp))
        ShimmerBlock(Modifier.fillMaxWidth(.72f).height(24.dp))
        ShimmerBlock(Modifier.fillMaxWidth().height(15.dp))
        ShimmerBlock(Modifier.fillMaxWidth(.88f).height(15.dp))
        ShimmerBlock(Modifier.fillMaxWidth().height(92.dp))
        ShimmerBlock(Modifier.fillMaxWidth().height(56.dp))
    }
}

@Composable
fun NetworkNotice(message: String, onRetry: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WifiOff, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(10.dp))
            Text(message, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
fun StatePane(
    title: String,
    message: String,
    icon: ImageVector? = null,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerBlock(Modifier.fillMaxWidth().height(96.dp))
                ShimmerBlock(Modifier.fillMaxWidth(.72f).height(20.dp))
                ShimmerBlock(Modifier.fillMaxWidth().height(14.dp))
            }
        }
        else if (icon != null) Icon(icon, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
