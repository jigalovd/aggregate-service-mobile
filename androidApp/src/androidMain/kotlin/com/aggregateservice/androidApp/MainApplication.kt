package com.aggregateservice.androidApp

import android.app.Application
import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import org.koin.core.context.startKoin

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
            environment = BuildConfig.BUILD_TYPE,
            languageCode = "ru",
            isDebug = BuildConfig.DEBUG,
            enableLogging = BuildConfig.DEBUG,
            networkTimeoutMs = 30000L,
            apiVersion = "v1",
        )

        Config.initialize(config)
    }

    private fun initializeKoin() {
        startKoin {
            // Feature modules will be added here when ready
            // modules(authModule, catalogModule, etc.)
        }
    }
}
