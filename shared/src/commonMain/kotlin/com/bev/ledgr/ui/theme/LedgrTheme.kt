package com.bev.ledgr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LedgrLightColors = lightColorScheme(
    primary = primaryColor,
    onPrimary = Color.White,
    secondaryContainer = primaryColor.copy(alpha = 0.20f),
    onSecondaryContainer = primaryDark
)

@Composable
fun LedgrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LedgrLightColors,
        typography = ledgrTypography(),
        content = content
    )
}
