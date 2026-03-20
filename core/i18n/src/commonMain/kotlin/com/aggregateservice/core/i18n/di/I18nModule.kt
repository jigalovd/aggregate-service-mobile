package com.aggregateservice.core.i18n.di

import com.aggregateservice.core.i18n.AppLocale
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.createDefaultI18nProvider
import org.koin.dsl.module

/**
 * Koin module for i18n (internationalization) functionality.
 *
 * Provides:
 * - [I18nProvider] for accessing localized strings
 */
val i18nModule = module {
    single<I18nProvider> {
        // Default to system locale or Russian as fallback
        val initialLocale = AppLocale.RU // Can be changed based on system settings
        createDefaultI18nProvider(initialLocale)
    }
}
