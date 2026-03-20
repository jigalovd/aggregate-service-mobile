package com.aggregateservice.core.di

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Core DI модуль.
 *
 * Предоставляет базовые зависимости для всего приложения:
 * - Config
 * - HttpClient
 *
 * **Note:** TokenStorage предоставляется в platform-specific модулях:
 * - Android: androidCoreModule
 * - iOS: (будет добавлен позже)
 */
val coreModule: Module = module {
    // Config
    single<AppConfig> { Config.instance }

    // HttpClient с базовой конфигурацией
    single<HttpClient> {
        HttpClient(get()) {
            // JSON сериализация
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            // Логирование (только в debug)
            val config = get<AppConfig>()
            if (config.enableLogging) {
                install(Logging) {
                    level = LogLevel.ALL
                    logger = object : Logger {
                        override fun log(message: String) {
                            println("Ktor: $message")
                        }
                    }
                }
            }

            // Default request configuration
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = config.apiBaseUrl
                    parameters.append("api_version", config.apiVersion)
                }
            }
        }
    }
}
