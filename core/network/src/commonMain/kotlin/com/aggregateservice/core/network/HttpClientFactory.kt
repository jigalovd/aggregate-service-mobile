package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private val SENSITIVE_FIELD_PATTERNS =
    listOf(
        Regex(""""password"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
        Regex(""""currentPassword"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
        Regex(""""newPassword"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
        Regex(""""confirmPassword"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
        Regex(""""token"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
        Regex(""""refreshToken"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
        Regex(""""accessToken"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
        Regex(""""Authorization"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE),
    )

internal fun String.sanitize(): String {
    var sanitized = this
    SENSITIVE_FIELD_PATTERNS.forEach { pattern ->
        sanitized =
            sanitized.replace(pattern) { matchResult ->
                val field = matchResult.value.substringBefore(":").trim()
                """$field: "***"""" 
            }
    }
    return sanitized
}

fun createHttpClient(
    engine: HttpClientEngine,
    apiBaseUrl: String,
    apiVersion: String = "v1",
    enableLogging: Boolean = false,
    networkTimeoutMs: Long = NetworkConstants.TIMEOUT_MS,
    loadTokens: (suspend () -> BearerTokens?)? = null,
    refreshTokens: (suspend () -> BearerTokens?)? = null,
): HttpClient =
    HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                    encodeDefaults = true
                },
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = networkTimeoutMs
            connectTimeoutMillis = networkTimeoutMs
            socketTimeoutMillis = networkTimeoutMs
        }

        install(HttpCookies) {}

        if (enableLogging) {
            install(Logging) {
                logger = PlatformHttpLogger("Http").logger
                level = LogLevel.HEADERS
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }

        loadTokens?.let { loadFn ->
            install(Auth) {
                bearer {
                    loadTokens { loadFn() }
                    refreshTokens { refreshTokens?.invoke() }
                }
            }
        }

        defaultRequest {
            val baseUrl = apiBaseUrl.trimEnd('/')
            val isHttps = baseUrl.startsWith("https://")
            val host = baseUrl.removePrefix("http://").removePrefix("https://")
            url {
                protocol = if (isHttps) URLProtocol.HTTPS else URLProtocol.HTTP
                this.host = host
            }
        }
    }