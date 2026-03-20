package com.aggregateservice.core.di

import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.core.storage.createTokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific DI модуль.
 *
 * Содержит зависимости, которые требуют Android Context.
 */
val androidCoreModule = module {
    // TokenStorage с Context
    single<TokenStorage> { createTokenStorage(androidContext()) }
}
