package com.bev.ledgr

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bev.ledgr.ui.LedgrNavHost

@Composable
@Preview
fun App() {
    MaterialTheme {
        LedgrNavHost()
    }
}