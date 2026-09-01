package com.example.foodhub_android.ui.features.common

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.foodhub_android.R
import com.example.foodhub_android.data.models.FoodItem


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodItemView(
    footItem: FoodItem,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (FoodItem) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .width(162.dp)
            .height(224.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Gray.copy(alpha = 0.2f),
                spotColor = Color.Gray.copy(alpha = 0.2f)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick.invoke(footItem) }
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(147.dp)
        ) {
            AsyncImage(
                model = footItem.imageUrl, contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "image/${footItem.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = com.example.foodhub_android.utils.StringUtils.formatCurrency(footItem.price),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .align(Alignment.TopStart)
            )
        }

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = footItem.name, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "title/${footItem.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
            Text(
                text = "${footItem.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
