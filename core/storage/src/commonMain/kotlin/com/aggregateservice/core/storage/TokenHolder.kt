package com.aggregateservice.core.storage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Single source of truth for access token.
 *
 * Synchronizes in-memory volatile storage with persistent DataStore storage.
 * All operations (initFromStorage, set, clear, get) use Mutex for thread-safety.
 * This pattern is KMP-compatible and works on iOS (Kotlin/Native).
 *
 * @param tokenStorage TokenStorage instance for persistence
 */
class TokenHolder(private val tokenStorage: TokenStorage) {

    private val mutex = Mutex()
    private var _token: String? = null

    /**
     * Initialize from persistent storage on cold start.
     * Loads token from DataStore into memory.
     */
    suspend fun initFromStorage() {
        mutex.withLock {
            _token = tokenStorage.getAccessTokenSync()
        }
    }

    /**
     * Set new access token.
     * Updates both memory AND persistent storage atomically.
     *
     * @param token New access token
     */
    suspend fun set(token: String) {
        mutex.withLock {
            _token = token
            tokenStorage.saveAccessToken(token)
        }
    }

    /**
     * Clear access token.
     * Clears both memory AND persistent storage atomically.
     */
    suspend fun clear() {
        mutex.withLock {
            _token = null
            tokenStorage.clearTokens()
        }
    }

    /**
     * Get current access token.
     * Returns token via mutex-protected read for consistency.
     *
     * @return Current token or null if not set
     */
    suspend fun get(): String? = mutex.withLock { _token }
}