package com.aggregateservice.feature.auth.di

import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.core.firebase.FirebaseAuthApiFactory
import com.aggregateservice.core.navigation.AuthStateProvider
import com.aggregateservice.core.navigation.AuthNavigator
import com.aggregateservice.feature.auth.AuthStateProviderImpl
import com.aggregateservice.feature.auth.AuthNavigatorImpl
import com.aggregateservice.feature.auth.data.repository.AuthRepositoryImpl
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.feature.auth.domain.usecase.InitializeAuthUseCase
import com.aggregateservice.feature.auth.domain.usecase.LogoutUseCase
import com.aggregateservice.feature.auth.domain.usecase.ObserveAuthStateUseCase
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

    // Firebase Auth API (platform-specific implementation)
    single<FirebaseAuthApi> { FirebaseAuthApiFactory.create() }

    // UseCases
    singleOf(::InitializeAuthUseCase)
    singleOf(::LogoutUseCase)
    singleOf(::ObserveAuthStateUseCase)

    // AuthStateProvider - abstraction for cross-feature auth access
    single<AuthStateProvider> { AuthStateProviderImpl(get()) }

    // AuthNavigator - abstraction for cross-feature auth navigation
    single<AuthNavigator> { AuthNavigatorImpl() }
}
