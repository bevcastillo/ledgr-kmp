package com.bev.ledgr

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
