package com.aggregateservice.androidApp

import android.app.Application
import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.di.androidCoreModule
import com.aggregateservice.core.di.coreModule
import com.aggregateservice.feature.auth.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Main Application класс для Android.
 *
 * **Responsibilities:**
 * - Инициализация Config
 * - Инициализация Koin DI
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeConfig()
        initializeKoin()
    }

    private fun initializeConfig() {
        val config = AppConfig(
            apiBaseUrl = BuildConfig.API_BASE_URL.takeIf { it.isNotEmpty() }
                ?: "https://api.dev.aggregateservice.com",
            apiKey = BuildConfig.API_KEY.takeIf { it.isNotEmpty() } ?: "",
            environmentCode = BuildConfig.BUILD_TYPE,
            languageCode = "ru",
            isDebug = BuildConfig.DEBUG,
            enableLogging = BuildConfig.DEBUG,
            networkTimeoutMs = 30000L,
            apiVersion = "v1",
            passwordMinLength = 12,
            passwordMaxLength = 128,
        )

        Config.initialize(config)
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
            modules(
                // Core modules
                androidCoreModule,
                coreModule,
                // Feature modules
                authModule,
                // Add more feature modules here as they are implemented
                // catalogModule,
                // bookingModule,
                // profileModule,
            )
        }
    }
}
