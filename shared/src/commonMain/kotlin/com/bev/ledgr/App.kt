package com.bev.ledgr

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.bev.ledgr.ui.ExpenseListScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        ExpenseListScreen()
    }
}