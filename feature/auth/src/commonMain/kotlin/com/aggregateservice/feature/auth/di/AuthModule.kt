package com.aggregateservice.feature.auth.di

import com.aggregateservice.core.auth.contract.AuthNavigator
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.InitializeAuthUseCase
import com.aggregateservice.core.auth.contract.LogoutUseCase
import com.aggregateservice.core.auth.contract.ObserveAuthStateUseCase
import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.contract.SignInUseCase
import com.aggregateservice.core.auth.impl.AuthStateProviderImpl
import com.aggregateservice.core.auth.impl.network.createAuthLambdas
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.repository.AuthRepositoryImpl
import com.aggregateservice.core.auth.impl.state.AuthStateMachine
import com.aggregateservice.core.auth.impl.token.TokenManager
import com.aggregateservice.core.auth.impl.token.TokenManagerImpl
import com.aggregateservice.core.auth.impl.usecase.InitializeAuthUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.LogoutUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.ObserveAuthStateUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.RefreshTokenUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.SignInUseCaseImpl
import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.firebase.AuthProviderApi
import com.aggregateservice.core.network.createHttpClient
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.feature.auth.AuthNavigatorImpl
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    // Core:auth-impl components
    single<TokenManager> { TokenManagerImpl(get<TokenStorage>()) }
    single<AuthRepository> { AuthRepositoryImpl(get<HttpClient>()) }
    single { AuthStateMachine(get(), get()) }

    // HttpClient with auth lambdas (replaces CoreModule's HttpClient)
    single<HttpClient> {
        val config = get<AppConfig>()
        val tokenManager = get<TokenManagerImpl>()
        val refreshTokenUseCase = get<RefreshTokenUseCase>()
        val authLambdas = createAuthLambdas(tokenManager, refreshTokenUseCase)
        createHttpClient(
            engine = get(),
            apiBaseUrl = config.apiBaseUrl,
            apiVersion = config.apiVersion,
            enableLogging = config.enableLogging,
            loadTokens = authLambdas.loadTokens,
            refreshTokens = authLambdas.refreshTokens,
        )
    }

    // UseCases
    singleOf(::InitializeAuthUseCaseImpl) bind InitializeAuthUseCase::class
    single<LogoutUseCase> {
        LogoutUseCaseImpl(
            authStateMachine = get(),
            platformSignOut = { get<AuthProviderApi>().signOut() },
            repository = get(),
        )
    }
    singleOf(::ObserveAuthStateUseCaseImpl) bind ObserveAuthStateUseCase::class
    singleOf(::SignInUseCaseImpl) bind SignInUseCase::class
    singleOf(::RefreshTokenUseCaseImpl) bind RefreshTokenUseCase::class

    // AuthStateProvider
    singleOf(::AuthStateProviderImpl) bind AuthStateProvider::class

    // Firebase Auth
    single { AuthProviderApi() }

    // AuthNavigator (presentation)
    singleOf(::AuthNavigatorImpl) bind AuthNavigator::class
}
