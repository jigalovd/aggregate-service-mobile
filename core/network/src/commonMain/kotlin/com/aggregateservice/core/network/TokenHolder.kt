package com.aggregateservice.core.network

/**
 * Single source of truth for access token storage.
 *
 * Provides atomic get/set/clear operations for the current access token.
 * Used by [AuthRefresher] to store new tokens after refresh and by
 * [BearerTokenPlugin] to read token for each request.
 *
 * This interface abstracts the [com.aggregateservice.core.storage.TokenHolder]
 * implementation from the storage module. The implementation is injected via Koin.
 */
interface TokenHolder {
    /**
     * Get current access token.
     * @return Current token or null if not set
     */
    suspend fun get(): String?

    /**
     * Set new access token.
     * @param token New access token to store
     */
    suspend fun set(token: String)

    /**
     * Clear stored access token.
     * Called when refresh fails or on logout.
     */
    suspend fun clear()
}
