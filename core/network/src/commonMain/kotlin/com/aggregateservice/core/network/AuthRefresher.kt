package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.post
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mutex-protected token refresh with concurrent detection.
 *
 * Ensures only one refresh executes at a time. If multiple 401 responses
 * arrive simultaneously, captures tokenBeforeRefresh BEFORE acquiring mutex
 * to detect if another coroutine already refreshed while waiting.
 *
 * @param httpClient HttpClient for making refresh API calls
 * @param tokenHolder TokenHolder for storing new tokens after refresh
 */
class AuthRefresher(
    private val httpClient: HttpClient,
    private val tokenHolder: TokenHolder,
) {

    private val refreshMutex = Mutex()

    /**
     * Tracks the token value at the moment a 401 was detected.
     * Used to detect if another coroutine already refreshed while
     * this one was waiting for the mutex.
     *
     * Must be captured BEFORE calling refresh() and BEFORE acquiring mutex.
     */
    private var tokenBeforeRefresh: String? = null

    /**
     * Capture current token state before attempting refresh.
     * Called by BearerTokenPlugin BEFORE calling refresh().
     *
     * This snapshot is used to detect concurrent refresh:
     * - If token changed between captureTokenState() and mutex acquisition,
     *   another coroutine already refreshed successfully.
     */
    suspend fun captureTokenState() {
        tokenBeforeRefresh = tokenHolder.get()
    }

    /**
     * Refresh the access token using HTTP-only refresh cookie.
     *
     * Protected by mutex - only one refresh executes at a time.
     * Before performing refresh, checks if token already changed
     * (another coroutine refreshed while this one waited for mutex).
     *
     * @return New access token if refresh successful, null otherwise
     */
    suspend fun refresh(): String? = refreshMutex.withLock {
        val currentToken = tokenHolder.get()

        // If token changed since we captured the 401,
        // another coroutine already refreshed successfully.
        // Return current token without making another API call.
        if (currentToken != null && currentToken != tokenBeforeRefresh) {
            return currentToken
        }

        try {
            // Note: /auth/refresh is in excluded paths (no Authorization header)
            // HttpCookies plugin sends the refresh_token cookie automatically
            val response = httpClient.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
            }

            if (response.status.value == 200) {
                val tokens = response.body<RefreshTokenResponseDto>()
                tokenHolder.set(tokens.accessToken)
                tokens.accessToken
            } else {
                tokenHolder.clear()
                null
            }
        } catch (e: Exception) {
            tokenHolder.clear()
            null
        }
    }
}

