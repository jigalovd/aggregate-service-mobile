package com.aggregateservice.feature.auth.di

import com.aggregateservice.core.navigation.AuthStateProvider
import com.aggregateservice.feature.auth.AuthStateProviderImpl
import com.aggregateservice.feature.auth.data.repository.AuthRepositoryImpl
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.feature.auth.domain.usecase.LoginUseCase
import com.aggregateservice.feature.auth.domain.usecase.LogoutUseCase
import com.aggregateservice.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.aggregateservice.feature.auth.presentation.screenmodel.LoginScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Auth feature DI модуль.
 *
 * Предоставляет зависимости для feature:auth:
 * - Repository (implementation)
 * - UseCases
 * - ScreenModels
 * - AuthStateProvider (for cross-feature access via core:navigation)
 */
val authModule = module {
    // Repository
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get(),
            tokenStorage = get(),
        )
    }

    // UseCases
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::ObserveAuthStateUseCase)

    // ScreenModels
    factoryOf(::LoginScreenModel)

    // AuthStateProvider - abstraction for cross-feature auth access
    single<AuthStateProvider> { AuthStateProviderImpl(get()) }
}
