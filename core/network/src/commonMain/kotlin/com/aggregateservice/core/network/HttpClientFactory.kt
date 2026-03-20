package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory для создания HttpClient с базовой конфигурацией.
 *
 * **Features:**
 * - JSON serialization/deserialization
 * - Logging (опционально)
 * - Default request headers
 *
 * @param engine HTTP engine (OkHttp для Android, Darwin для iOS)
 * @param config AppConfig для настроек
 * @param enableLogging Включить логирование
 */
fun createHttpClient(
    engine: HttpClientEngine,
    apiBaseUrl: String,
    apiVersion: String = "v1",
    enableLogging: Boolean = false,
): HttpClient = HttpClient(engine) {
    // JSON serialization
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            isLenient = true
        })
    }

    // Logging (опционально)
    if (enableLogging) {
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
            host = apiBaseUrl
            parameters.append("api_version", apiVersion)
        }
    }
}
