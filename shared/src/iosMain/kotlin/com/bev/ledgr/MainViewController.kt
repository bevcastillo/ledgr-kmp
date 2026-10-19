package com.bev.ledgr

import androidx.compose.ui.window.ComposeUIViewController
import com.bev.ledgr.di.initKoin

@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { App() }
