package com.aggregateservice.app

import android.app.Application
import android.content.Context
import com.aggregateservice.app.di.startKoin
import com.aggregateservice.app.di.appModule
import org.koin.android.ext.koinAndroidContext

class BeautyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(androidContext, listOf(appModule))
    }
}
