package com.bev.ledgr

import android.app.Application
import com.bev.ledgr.di.initKoin
import org.koin.android.ext.koin.androidContext

class LedgrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@LedgrApp)
        }
    }
}