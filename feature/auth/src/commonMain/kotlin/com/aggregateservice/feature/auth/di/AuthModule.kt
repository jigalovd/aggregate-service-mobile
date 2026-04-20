package com.aggregateservice.feature.auth.di

import com.aggregateservice.core.auth.contract.AuthGate
import com.aggregateservice.core.auth.contract.AuthNavigator
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.InitializeAuthUseCase
import com.aggregateservice.core.auth.contract.LogoutUseCase
import com.aggregateservice.core.auth.contract.ObserveAuthStateUseCase
import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.contract.SignInUseCase
import com.aggregateservice.core.auth.contract.SwitchRoleUseCase
import com.aggregateservice.core.auth.impl.AuthStateProviderImpl
import com.aggregateservice.core.auth.impl.gate.AuthGateImpl
import com.aggregateservice.core.auth.impl.gate.AuthPromptPresenter
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.repository.AuthRepositoryImpl
import com.aggregateservice.core.auth.impl.state.AuthStateMachine
import com.aggregateservice.core.auth.impl.usecase.InitializeAuthUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.LogoutUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.ObserveAuthStateUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.RefreshTokenUseCaseImpl
import com.aggregateservice.core.auth.impl.usecase.SignInUseCaseImpl
import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.firebase.AuthProviderApi
import com.aggregateservice.core.network.createHttpClient
import com.aggregateservice.core.storage.TokenStore
import com.aggregateservice.feature.auth.AuthNavigatorImpl
import com.aggregateservice.feature.auth.presentation.FirebaseAuthPromptPresenter
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule =
    module {
        // Core:auth-impl components
        single { AuthStateMachine(get<TokenStore>(), get(), get()) }
        single<AuthRepository> {
            AuthRepositoryImpl(
                httpClient = get(),
                authClient = get(named("unauth")),
                tokenStore = get(),
            )
        }

        // UnauthClient — no Bearer plugin.
        // Used for Guest requests (catalog) and Auth Ops (verify, refresh).
        // Shares same config as main client but without auth callbacks.
        single<HttpClient>(named("unauth")) {
            val config = get<AppConfig>()
            createHttpClient(
                engine = get(),
                apiBaseUrl = config.apiBaseUrl,
                apiVersion = config.apiVersion,
                enableLogging = config.enableLogging,
                loadTokens = null,
                refreshTokens = null,
            )
        }

        // MainClient — with Bearer auth plugin.
        // Used for all authenticated API calls.
        // refreshTokens lambda uses unauth client → no deadlock.
        single<HttpClient> {
            val koin = getKoin()
            val config = get<AppConfig>()
            val tokenStore = get<TokenStore>()
            createHttpClient(
                engine = get(),
                apiBaseUrl = config.apiBaseUrl,
                apiVersion = config.apiVersion,
                enableLogging = config.enableLogging,
                loadTokens = {
                    tokenStore.getAccessToken()?.let {
                        io.ktor.client.plugins.auth.providers
                            .BearerTokens(accessToken = it, refreshToken = "")
                    }
                },
                refreshTokens = {
                    val useCase: RefreshTokenUseCase = koin.get()
                    useCase().getOrNull()?.let {
                        io.ktor.client.plugins.auth.providers
                            .BearerTokens(accessToken = it, refreshToken = "")
                    }
                },
            )
        }

        // UseCases
        singleOf(::InitializeAuthUseCaseImpl) bind InitializeAuthUseCase::class
        single<LogoutUseCase> {
            LogoutUseCaseImpl(
                authStateMachine = get(),
                platformSignOut = { get<AuthProviderApi>().signOut() },
                repository = get(),
                tokenStore = get(),
            )
        }
        singleOf(::ObserveAuthStateUseCaseImpl) bind ObserveAuthStateUseCase::class
        singleOf(::SignInUseCaseImpl) bind SignInUseCase::class
        single<RefreshTokenUseCase> {
            RefreshTokenUseCaseImpl(
                tokenStore = get(),
                repository = get(),
                onRefreshFailed = { get<AuthStateMachine>().emitGuest() },
            )
        }
        single<SwitchRoleUseCase> {
            com.aggregateservice.core.auth.impl.usecase.SwitchRoleUseCaseImpl(
                authStateMachine = get(),
                repository = get(),
            )
        }

        // AuthGate
        single<AuthGate> {
            AuthGateImpl(
                authStateProvider = get(),
                signInUseCase = get(),
                authPromptPresenter = get(),
            )
        }
        single<AuthPromptPresenter> {
            FirebaseAuthPromptPresenter(
                authProviderApi = get(),
            )
        }

        // AuthStateProvider
        singleOf(::AuthStateProviderImpl) bind AuthStateProvider::class

        // Firebase Auth
        single { AuthProviderApi() }

        // AuthNavigator (presentation)
        singleOf(::AuthNavigatorImpl) bind AuthNavigator::class
    }
