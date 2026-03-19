package com.aggregateservice.core.network

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

private const val TIMEOUT_MS = 30_000L

expect val httpClientEngine: HttpClientEngine

fun createHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    enableLogging: Boolean = false,
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
            requestTimeoutMillis = TIMEOUT_MS
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
        }
        if (enableLogging) {
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            // Implement your logging logic here
                            println("AggregateService Log: $message")
                        }
                    }
                level = LogLevel.ALL
            }
        }
        // Дефолтный URL и заголовки для всех запросов
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }
