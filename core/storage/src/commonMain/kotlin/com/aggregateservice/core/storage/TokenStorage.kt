package com.aggregateservice.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Storage for authentication tokens using DataStore Preferences.
 *
 * **Features:**
 * - Type-safe access to access and refresh tokens
 * - Flow-based reactive updates
 * - Thread-safe operations
 * - Encrypted storage on Android (via DataStore)
 *
 * **Note:** Refresh token is stored in HTTP-only cookie on iOS.
 * This storage only manages the access token on all platforms.
 *
 * @see BACKEND_API_REFERENCE.md секция 3.1 "JWT токены"
 */
interface TokenStorage {
    /**
     * Get the current access token.
     *
     * @return Flow emitting the access token or null if not found
     */
    fun getAccessToken(): Flow<String?>

    /**
     * Get the current access token synchronously.
     *
     * @return Access token or null if not found
     */
    suspend fun getAccessTokenSync(): String?

    /**
     * Save the access token.
     *
     * @param token Access token to save
     */
    suspend fun saveAccessToken(token: String)

    /**
     * Clear all stored tokens (logout).
     */
    suspend fun clearTokens()
}

/**
 * Implementation of [TokenStorage] using DataStore Preferences.
 *
 * **Storage keys:**
 * - `KEY_ACCESS_TOKEN`: JWT access token (15 min expiration)
 *
 * **Note:** Refresh token is stored in HTTP-only cookie by the backend.
 * We don't need to manage it on the client side.
 *
 * @param dataStore DataStore Preferences instance
 */
class TokenStorageImpl(
    private val dataStore: DataStore<Preferences>,
) : TokenStorage {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }

    override fun getAccessToken(): Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }

    override suspend fun getAccessTokenSync(): String? =
        dataStore.data.first()[ACCESS_TOKEN_KEY]

    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
        }
    }
}

/**
 * Factory function to create [TokenStorage].
 *
 * Platform-specific implementations will handle the actual creation.
 *
 * **Usage:**
 * ```kotlin
 * // In Koin module (Android)
 * single<TokenStorage> { createTokenStorage(androidContext()) }
 *
 * // In Koin module (iOS)
 * single<TokenStorage> { createTokenStorage() }
 * ```
 */
expect fun createTokenStorage(): TokenStorage

/**
 * Extension to check if user is authenticated.
 *
 * @return true if access token exists
 */
suspend fun TokenStorage.isAuthenticated(): Boolean =
    getAccessTokenSync()?.isNotEmpty() == true

/**
 * Extension to get Authorization header value.
 *
 * @return "Bearer <token>" or null if not authenticated
 */
suspend fun TokenStorage.getAuthHeader(): String? =
    getAccessTokenSync()?.takeIf { it.isNotEmpty() }?.let { "Bearer $it" }
