package com.bev.ledgr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bev.ledgr.ui.theme.primaryColor

data class FabMenuItem(val label: String, val onClick: () -> Unit)

@Composable
fun ExpandableFabMenu(
    items: List<FabMenuItem>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEachIndexed { index, item ->
            val delay = (items.lastIndex - index) * 40

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(150, delayMillis = delay)) +
                    slideInVertically(tween(150, delayMillis = delay)) { it / 2 } +
                    scaleIn(tween(150, delayMillis = delay), initialScale = 0.8f),
                exit = fadeOut(tween(100)) +
                    slideOutVertically(tween(100)) { it / 2 } +
                    scaleOut(tween(100), targetScale = 0.8f)

            ) {
                Surface(
                    onClick = {
                        onExpandedChange(false)
                        item.onClick()
                    },
                    shape = MaterialTheme.shapes.small,
                    color = containerColor,
                    contentColor = contentColor,
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        val rotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            label = "fabRotation"
        )

        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = primaryColor,
            contentColor = Color.White
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}
