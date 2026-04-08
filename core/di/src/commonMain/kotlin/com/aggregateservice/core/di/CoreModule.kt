package com.aggregateservice.core.di

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.network.AuthEventBus
import com.aggregateservice.core.network.AuthManager
import com.aggregateservice.core.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single<AppConfig> { Config.instance }

    single<AuthEventBus> { AuthEventBus() }

    single { AuthManager(
        httpClientProvider = { get<HttpClient>() },
        tokenStorage = get(),
        authEventBus = get(),
    ) }

    single<HttpClient> {
        val config = get<AppConfig>()
        val authMgr = get<AuthManager>()
        createHttpClient(
            engine = get(),
            apiBaseUrl = config.apiBaseUrl,
            apiVersion = config.apiVersion,
            enableLogging = config.enableLogging,
            loadTokens = { authMgr.loadTokens() },
            refreshTokens = { authMgr.refreshTokens() },
        )
    }
}
