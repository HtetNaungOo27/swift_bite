package com.example.foodhub_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.foodhub_android.ui.motion.bounceClick

@Composable
fun SwiftBiteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp).bounceClick(interaction),
        enabled = enabled,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        leadingIcon?.let {
            Icon(it, contentDescription = null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ExpandableCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = { onExpandedChange(!expanded) },
        modifier = modifier.fillMaxWidth().animateContentSize(spring()),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            header()
            AnimatedVisibility(expanded) { Column(content = content) }
        }
    }
}

enum class OrderStatus {
    PENDING, ACCEPTED, PREPARING, READY, ASSIGNED, OUT_FOR_DELIVERY, DELIVERED,
    CANCELLED, REJECTED, DELIVERY_FAILED, UNKNOWN;

    companion object {
        fun from(raw: String): OrderStatus = entries.firstOrNull {
            it.name == raw.trim().uppercase().replace(' ', '_')
        } ?: UNKNOWN
    }
}

@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val success = Color(0xFF16865F)
    val warning = Color(0xFFC77900)
    val info = Color(0xFF2979B8)
    val error = MaterialTheme.colorScheme.error
    val (icon, color) = when (status) {
        OrderStatus.PENDING -> Icons.Rounded.Schedule to warning
        OrderStatus.ACCEPTED -> Icons.Rounded.ThumbUp to info
        OrderStatus.PREPARING -> Icons.Rounded.LocalFireDepartment to warning
        OrderStatus.READY -> Icons.Rounded.ShoppingBag to success
        OrderStatus.ASSIGNED -> Icons.Rounded.AssignmentInd to info
        OrderStatus.OUT_FOR_DELIVERY -> Icons.Rounded.TwoWheeler to MaterialTheme.colorScheme.primary
        OrderStatus.DELIVERED -> Icons.Rounded.CheckCircle to success
        OrderStatus.CANCELLED -> Icons.Rounded.Cancel to error
        OrderStatus.REJECTED, OrderStatus.DELIVERY_FAILED -> Icons.Rounded.ErrorOutline to error
        OrderStatus.UNKNOWN -> Icons.Rounded.Schedule to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(modifier, color = color.copy(alpha = 0.14f), contentColor = color, shape = CircleShape) {
        Row(
            Modifier.heightIn(min = 32.dp).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                status.name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    icon: ImageVector,
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(icon, null, Modifier.padding(20.dp).size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        action()
    }
}
