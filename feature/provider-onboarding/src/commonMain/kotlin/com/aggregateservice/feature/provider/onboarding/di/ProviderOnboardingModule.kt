package com.aggregateservice.feature.provider.onboarding.di

import com.aggregateservice.feature.provider.onboarding.data.api.ProviderOnboardingApiService
import com.aggregateservice.feature.provider.onboarding.data.repository.ProviderOnboardingRepositoryImpl
import com.aggregateservice.feature.provider.onboarding.domain.repository.ProviderOnboardingRepository
import com.aggregateservice.feature.provider.onboarding.presentation.screenmodel.ProviderOnboardingScreenModel
import co.touchlab.kermit.Logger
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Provider Onboarding feature DI module.
 *
 * Provides dependencies for feature:provider-onboarding:
 * - ProviderOnboardingApiService (single - shared HTTP client instance)
 * - ProviderOnboardingRepository (factory - new instance per ScreenModel)
 * - ProviderOnboardingScreenModel (factory - new instance per screen)
 *
 * **Architecture:**
 * - ApiService as single: shared across all repository instances
 * - Repository as factory: new instance per ScreenModel (clean state)
 * - ScreenModel as factory: new instance per screen (isolated state)
 * - Follows pattern from ProviderBookingsModule
 */
val providerOnboardingModule =
    module {
        // API Service (single - shared across repository instances)
        single { ProviderOnboardingApiService(get()) }

        // Repository (factory - new instance per ScreenModel)
        factory<ProviderOnboardingRepository> {
            ProviderOnboardingRepositoryImpl(
                apiService = get(),
                logger = Logger.withTag("ProviderOnboarding"),
            )
        }

        // ScreenModel (factory - new instance per screen)
        factory<ProviderOnboardingScreenModel> {
            ProviderOnboardingScreenModel(
                repository = get(),
                logger = Logger.withTag("ProviderOnboarding"),
            )
        }
    }