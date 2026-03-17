package com.aggregateservice.androidApp

import android.app.Application
import com.aggregateservice.app.di.appModule
import com.aggregateservice.core.di.coreModule
import com.aggregateservice.core.di.platformCoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BeautyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BeautyApplication)
            modules(
                platformCoreModule,
                coreModule,
                appModule
            )
        }
    }
}
