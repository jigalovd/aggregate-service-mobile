package com.aggregateservice.core.network

import co.touchlab.kermit.Logger
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

private fun String.sanitize(): String {
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

private class SanitizingKermitLogger(
    private val delegate: Logger,
) : io.ktor.client.plugins.logging.Logger {
    override fun log(message: String) {
        delegate.d { message.sanitize() }
    }
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
                logger = SanitizingKermitLogger(Logger.withTag("Http"))
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
                parameters.append("api_version", apiVersion)
            }
        }
    }
