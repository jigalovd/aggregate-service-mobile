package com.aggregateservice.androidApp

import android.app.Application
import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.di.androidCoreModule
import com.aggregateservice.core.di.coreModule
import com.aggregateservice.core.i18n.di.i18nModule
import com.aggregateservice.core.logging.initLogging
import com.aggregateservice.core.logging.loggingModule
import com.aggregateservice.feature.auth.di.authModule
import com.aggregateservice.feature.booking.di.bookingModule
import com.aggregateservice.feature.catalog.di.catalogModule
import com.aggregateservice.feature.favorites.di.favoritesModule
import com.aggregateservice.feature.profile.di.profileModule
import com.aggregateservice.feature.reviews.di.reviewsModule
import com.aggregateservice.feature.services.di.servicesModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = createConfig()
        Config.initialize(config)
        initLogging(this, config.enableLogging)

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
            modules(
                loggingModule,
                androidCoreModule,
                coreModule,
                i18nModule,
                authModule,
                catalogModule,
                bookingModule,
                servicesModule,
                profileModule,
                favoritesModule,
                reviewsModule,
            )
        }
    }

    private fun createConfig(): AppConfig =
        AppConfig(
            apiBaseUrl =
                BuildConfig.API_BASE_URL.takeIf { it.isNotEmpty() }
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
}
