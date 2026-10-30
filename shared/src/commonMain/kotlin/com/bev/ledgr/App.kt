package com.bev.ledgr

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bev.ledgr.ui.LedgrNavHost
import com.bev.ledgr.ui.theme.LedgrTheme

@Composable
@Preview
fun App() {
    LedgrTheme {
        LedgrNavHost()
    }
}
