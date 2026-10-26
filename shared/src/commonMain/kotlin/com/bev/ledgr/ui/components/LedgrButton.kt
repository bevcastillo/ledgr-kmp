package com.bev.ledgr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.bev.ledgr.ui.theme.SquircleShape
import com.bev.ledgr.ui.theme.primaryDark
import com.bev.ledgr.ui.theme.primaryLight

@Composable
fun SaveButton(text: String? = "Save", onClick: () -> Unit, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = primaryDark
    val pressedColor = lerp(baseColor, Color.Black, 0.15f)

    val containerColor by animateColorAsState(
        targetValue = if (isPressed) pressedColor else baseColor,
        label = "buttonColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .heightIn(min = 50.dp),
        enabled = enabled,
        shape = SquircleShape(cornerRadius = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = primaryDark
        )
    ) {
        Text(
            text = "$text",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun DeleteButton(text: String? = "Delete", onClick: () -> Unit, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = primaryLight
    val pressedColor = lerp(baseColor, Color.Black, 0.5f)

    val containerColor by animateColorAsState(
        targetValue = if (isPressed) pressedColor else baseColor,
        label = "buttonColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .heightIn(min = 50.dp),
        enabled = enabled,
        shape = SquircleShape(cornerRadius = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = primaryLight
        )
    ) {
        Text(
            text = "$text",
            style = MaterialTheme.typography.labelLarge,
            color = primaryDark,
            modifier = Modifier.padding(4.dp)
        )
    }
}
