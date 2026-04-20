package com.aggregateservice.feature.provider.bookings.di

import com.aggregateservice.feature.provider.bookings.data.api.ProviderBookingsApiService
import com.aggregateservice.feature.provider.bookings.data.repository.ProviderBookingRepositoryImpl
import com.aggregateservice.feature.provider.bookings.domain.repository.ProviderBookingRepository
import com.aggregateservice.feature.provider.bookings.navigation.ProviderBookingsNavigator
import com.aggregateservice.feature.provider.bookings.navigation.ProviderBookingsNavigatorImpl
import com.aggregateservice.feature.provider.bookings.presentation.screenmodel.ProviderBookingsScreenModel
import co.touchlab.kermit.Logger
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Provider Bookings feature DI модуль.
 *
 * Предоставляет зависимости для feature:provider-bookings:
 * - ProviderBookingsApiService (single - shared HTTP client instance)
 * - ProviderBookingRepository (factory - new instance per ScreenModel)
 * - ProviderBookingsScreenModel (factory - new instance per screen)
 * - ProviderBookingsNavigator (single - navigation)
 *
 * **Architecture:**
 * - ApiService as single: shared across all repository instances
 * - Repository as factory: new instance per ScreenModel (clean state)
 * - ScreenModel as factory: new instance per screen (isolated state)
 * - Navigator as single: shared across all navigation requests
 * - Follows pattern from ProviderDashboardModule
 */
val providerBookingsModule =
    module {
        // API Service (single - shared across repository instances)
        single { ProviderBookingsApiService(get()) }

        // Repository (factory - new instance per ScreenModel)
        factory<ProviderBookingRepository> {
            ProviderBookingRepositoryImpl(
                apiService = get(),
                logger = Logger.withTag("ProviderBookings"),
            )
        }

        // ScreenModel (factory - new instance per screen)
        factory<ProviderBookingsScreenModel> {
            ProviderBookingsScreenModel(
                repository = get(),
                logger = Logger.withTag("ProviderBookings"),
            )
        }

        // Navigation (single - shared across all navigation requests)
        single<ProviderBookingsNavigator> { ProviderBookingsNavigatorImpl() }
    }
