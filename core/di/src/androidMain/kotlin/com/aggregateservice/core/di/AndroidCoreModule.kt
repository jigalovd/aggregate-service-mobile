package com.aggregateservice.core.di

import com.aggregateservice.core.storage.TokenHolder
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.core.storage.createTokenStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific DI модуль.
 *
 * Содержит зависимости, которые требуют Android Context или платформенные реализации.
 */
val androidCoreModule = module {
    // TokenStorage с Context
    single<TokenStorage> { createTokenStorage(androidContext()) }

    // TokenHolder - единый источник правды для access token
    single { TokenHolder(get<TokenStorage>()) }

    // Ktor HttpClientEngine - OkHttp for Android
    single<HttpClientEngine> { OkHttp.create() }
}
