package com.aggregateservice.feature.provider.dashboard.di

import co.touchlab.kermit.Logger
import com.aggregateservice.core.navigation.ProviderNavigator
import com.aggregateservice.feature.provider.dashboard.data.api.ProviderDashboardApiService
import com.aggregateservice.feature.provider.dashboard.data.repository.ProviderRepositoryImpl
import com.aggregateservice.feature.provider.dashboard.domain.repository.ProviderRepository
import com.aggregateservice.feature.provider.dashboard.navigation.ProviderNavigatorImpl
import com.aggregateservice.feature.provider.dashboard.presentation.screenmodel.ProviderDashboardScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Provider Dashboard feature DI модуль.
 *
 * Предоставляет зависимости для feature:provider-dashboard:
 * - ProviderDashboardApiService (single - shared HTTP client instance)
 * - ProviderRepository (factory - new instance per injection)
 * - ProviderDashboardScreenModel (factory - new instance per screen)
 * - ProviderNavigator (single - navigation)
 *
 * **Architecture:**
 * - ApiService as single: shared across all repository instances
 * - Repository as factory: new instance per ScreenModel (clean state)
 * - ScreenModel as factory: new instance per screen (isolated state)
 * - Navigator as single: shared across all navigation requests
 * - Follows pattern from BookingModule
 */
val providerDashboardModule =
    module {
        // API Service (single - shared across repository instances)
        single { ProviderDashboardApiService(get()) }

        // Repository (factory - new instance per ScreenModel)
        factory<ProviderRepository> {
            ProviderRepositoryImpl(
                apiService = get(),
                logger = Logger.withTag("ProviderDashboard"),
            )
        }

        // ScreenModel (factory - new instance per screen)
        factory<ProviderDashboardScreenModel> {
            ProviderDashboardScreenModel(
                repository = get(),
                logger = Logger.withTag("ProviderDashboard"),
            )
        }

        // Navigation (single - shared across all navigation requests)
        single<ProviderNavigator> { ProviderNavigatorImpl() }
    }