package com.bev.ledgr

import androidx.compose.ui.window.ComposeUIViewController
import com.bev.ledgr.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { App() }