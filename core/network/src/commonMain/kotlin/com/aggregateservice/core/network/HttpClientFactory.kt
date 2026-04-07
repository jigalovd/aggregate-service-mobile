package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TAG = "Ktor"

private val SENSITIVE_FIELD_PATTERNS = listOf(
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
        sanitized = sanitized.replace(pattern) { matchResult ->
            val field = matchResult.value.substringBefore(":").trim()
            """$field: "***""""
        }
    }
    return sanitized
}

private object SensitiveDataLogger : io.ktor.client.plugins.logging.Logger {
    override fun log(message: String) {
        println("$TAG: ${message.sanitize()}")
    }
}

/**
 * Creates configured HttpClient with all necessary plugins.
 *
 * @param engine Platform-specific HTTP engine
 * @param apiBaseUrl API base URL (with optional protocol, e.g. "https://api.example.com" or "http://localhost:8080")
 * @param apiVersion API version (default: v1)
 * @param enableLogging Enable HTTP logging for debugging
 * @param networkTimeoutMs Timeout in milliseconds (default: 30_000)
 * @param authManager AuthManager instance for automatic token refresh (optional)
 */
fun createHttpClient(
    engine: HttpClientEngine,
    apiBaseUrl: String,
    apiVersion: String = "v1",
    enableLogging: Boolean = false,
    networkTimeoutMs: Long = NetworkConstants.TIMEOUT_MS,
    authManager: AuthManager? = null,
): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            encodeDefaults = true  // Include default values in serialization
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = networkTimeoutMs
        connectTimeoutMillis = networkTimeoutMs
        socketTimeoutMillis = networkTimeoutMs
    }

    // Cookie handling for refresh token (HTTP-only cookie from backend)
    install(HttpCookies) {
        // Use default engine storage for cookies
    }

    if (enableLogging) {
        install(Logging) {
            level = LogLevel.ALL
            logger = SensitiveDataLogger
        }
    }

    // Install Auth plugin if AuthManager provided
    authManager?.let { manager ->
        install(Auth) {
            bearer {
                loadTokens {
                    manager.loadTokens()
                }
                refreshTokens {
                    manager.refreshTokens()
                }
                // Don't apply auth to public endpoints to prevent deadlock:
                // when loadTokens() returns null, Auth calls refreshTokens(),
                // which makes an HTTP request that would trigger Auth again → mutex deadlock
                sendWithoutRequest { request ->
                    val path = request.url.pathSegments.joinToString("/")
                    path.startsWith("api/v1/auth/provider") ||
                        path.startsWith("api/v1/auth/refresh") ||
                        path.startsWith("api/v1/auth/logout")
                }
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
