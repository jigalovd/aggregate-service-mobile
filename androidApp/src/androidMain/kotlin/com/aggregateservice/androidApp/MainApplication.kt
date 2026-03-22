package com.aggregateservice.androidApp

import android.app.Application
import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.di.androidCoreModule
import com.aggregateservice.core.di.coreModule
import com.aggregateservice.core.i18n.di.i18nModule
import com.aggregateservice.feature.auth.di.authModule
import com.aggregateservice.feature.catalog.di.catalogModule
import com.aggregateservice.feature.booking.di.bookingModule
import com.aggregateservice.feature.services.di.servicesModule
import com.aggregateservice.feature.profile.di.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext

/**
 * Main Application класс для Android.
 *
 * **Responsibilities:**
 * - Инициализация Config
 * - Инициализация Koin DI
 * - Настройка Logback логирования
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeLogging()
        initializeConfig()
        initializeKoin()
    }

    private fun initializeLogging() {
        val loggerContext = LoggerFactory.getILoggerFactory() as? LoggerContext
        loggerContext?.let { context ->
            val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)

            if (BuildConfig.ENABLE_LOGGING) {
                rootLogger.level = ch.qos.logback.classic.Level.DEBUG
            } else {
                rootLogger.level = ch.qos.logback.classic.Level.ERROR
            }
        }
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
                i18nModule,
                // Feature modules
                authModule,
                catalogModule,
                bookingModule,
                servicesModule,
                profileModule,
            )
        }
    }
}
