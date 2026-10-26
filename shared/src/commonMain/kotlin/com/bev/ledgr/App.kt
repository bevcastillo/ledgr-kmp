package com.bev.ledgr

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bev.ledgr.ui.LedgrNavHost
import com.bev.ledgr.ui.theme.ledgrTypography

@Composable
@Preview
fun App() {
    MaterialTheme(
        typography = ledgrTypography()
    ) {
        LedgrNavHost()
    }
}
