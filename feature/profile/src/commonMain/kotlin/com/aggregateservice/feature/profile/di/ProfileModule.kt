package com.aggregateservice.feature.profile.di

import com.aggregateservice.feature.profile.data.api.ProfileApiService
import com.aggregateservice.feature.profile.data.repository.ProfileRepositoryImpl
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository
import com.aggregateservice.feature.profile.domain.usecase.GetProfileUseCase
import com.aggregateservice.feature.profile.domain.usecase.UpdateProfileUseCase
import com.aggregateservice.feature.profile.presentation.screenmodel.ProfileScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Profile feature DI module.
 *
 * Provides dependencies for feature:profile:
 * - API Service
 * - Repository (implementation)
 * - UseCases (domain layer)
 * - ScreenModels (presentation layer)
 */
val profileModule =
    module {
        // API Service (Ktor Auth Plugin handles auth automatically)
        single { ProfileApiService(get()) }

        // Repository
        single<ProfileRepository> {
            ProfileRepositoryImpl(apiService = get())
        }

        // UseCases (Domain layer)
        factoryOf(::GetProfileUseCase)
        factoryOf(::UpdateProfileUseCase)

        // ScreenModels (Presentation layer)
        factoryOf(::ProfileScreenModel)
    }
