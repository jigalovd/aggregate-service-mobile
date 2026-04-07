package com.aggregateservice.core.network

import com.aggregateservice.core.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * Centralized authentication management for Ktor Auth plugin.
 *
 * **Responsibilities:**
 * - Thread-safe token refresh (mutex protected to prevent concurrent refresh attempts)
 * - Access token storage via TokenStorage
 * - Logout event emission when refresh permanently fails
 *
 * **Thread Safety:** Uses Mutex to ensure only one refresh executes at a time.
 * If multiple requests receive 401 simultaneously, they are serialized via mutex.
 * The Ktor Auth plugin retries each failed request after a successful refresh.
 *
 * **Usage with Ktor Auth plugin:**
 * ```kotlin
 * install(Auth) {
 *     bearer {
 *         loadTokens { authManager.loadTokens() }
 *         refreshTokens { authManager.refreshTokens() }
 *     }
 * }
 * ```
 *
 * @param httpClientProvider Lazy provider for HttpClient (avoids circular init)
 * @param tokenStorage TokenStorage for persisting access token
 */
class AuthManager(
    private val httpClientProvider: () -> HttpClient,
    private val tokenStorage: TokenStorage,
) {
    private val httpClient: HttpClient by lazy { httpClientProvider() }
    private val refreshMutex = Mutex()

    private val _logoutEvents = MutableSharedFlow<Unit>(replay = 1)
    val logoutEvents: SharedFlow<Unit> = _logoutEvents.asSharedFlow()

    suspend fun loadTokens(): BearerTokens? {
        val token = tokenStorage.getAccessTokenSync()
        return if (token != null) {
            BearerTokens(
                accessToken = token,
                refreshToken = "",
            )
        } else {
            null
        }
    }

    /**
     * Refreshes the access token using the HTTP-only refresh cookie.
     *
     * **Concurrency:** Protected by [refreshMutex] — if multiple 401 responses arrive
     * simultaneously, only one refresh executes at a time. After a successful refresh,
     * [loadTokens] returns the new token for all subsequent requests.
     */
    suspend fun refreshTokens(): BearerTokens? = refreshMutex.withLock {
        try {
            val response = httpClient.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
            }

            when (response.status.value) {
                200 -> {
                    val tokens = response.body<RefreshTokenResponseDto>()
                    tokenStorage.saveAccessToken(tokens.accessToken)
                    BearerTokens(
                        accessToken = tokens.accessToken,
                        refreshToken = "",
                    )
                }
                else -> {
                    clearTokensAndEmitLogout()
                    null
                }
            }
        } catch (e: Exception) {
            clearTokensAndEmitLogout()
            null
        }
    }

    /**
     * Clear tokens and emit logout event.
     * Called when refresh permanently fails.
     */
    private suspend fun clearTokensAndEmitLogout() {
        tokenStorage.clearTokens()
        _logoutEvents.emit(Unit)
    }
}

/**
 * DTO for refresh token response from backend.
 */
@Serializable
data class RefreshTokenResponseDto(
    val accessToken: String,
)
