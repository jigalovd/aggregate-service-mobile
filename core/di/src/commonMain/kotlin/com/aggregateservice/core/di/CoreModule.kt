package com.aggregateservice.core.di

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single<AppConfig> { Config.instance }

    single<HttpClient> {
        val config = get<AppConfig>()
        createHttpClient(
            engine = get(),
            apiBaseUrl = config.apiBaseUrl,
            apiVersion = config.apiVersion,
            enableLogging = config.enableLogging,
        )
    }
}
