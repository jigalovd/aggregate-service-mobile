package com.aggregateservice.core.network

import com.aggregateservice.core.config.Config
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect val httpClientEngine: HttpClientEngine

/**
 * Создаёт HTTP клиент с централизованной конфигурацией
 *
 * Использует Config для получения настроек:
 * - Base URL из конфигурации
 * - Timeout из конфигурации
 * - Logging флаг из конфигурации
 * - API ключ автоматически добавляется в заголовки
 */
fun createHttpClient(
    engine: HttpClientEngine = httpClientEngine,
): HttpClient =
    HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = Config.networkTimeoutMs
            connectTimeoutMillis = Config.networkTimeoutMs
            socketTimeoutMillis = Config.networkTimeoutMs
        }

        if (Config.enableLogging) {
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            println("AggregateService Network: $message")
                        }
                    }
                level = LogLevel.ALL
            }
        }

        // Дефолтный URL и заголовки для всех запросов
        defaultRequest {
            url(Config.apiBaseUrl)
            contentType(ContentType.Application.Json)

            // Автоматическая инъекция API ключа
            headers {
                if (Config.apiKey.isNotEmpty()) {
                    append("X-API-Key", Config.apiKey)
                }
                append("X-API-Version", Config.apiVersion)
            }
        }
    }
