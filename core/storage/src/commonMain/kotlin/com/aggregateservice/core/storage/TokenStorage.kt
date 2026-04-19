package com.aggregateservice.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Single source of truth for auth tokens and role preference.
 * Pure read/write — no business logic.
 * Written to by Auth Ops only. Read by all contexts.
 */
interface TokenStore {
    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun saveTokens(accessToken: String, refreshToken: String)

    suspend fun clearTokens()

    suspend fun getCurrentRole(): String?

    suspend fun saveCurrentRole(role: String?)

    suspend fun clearCurrentRole()
}

class TokenStoreImpl(
    private val dataStore: DataStore<Preferences>,
) : TokenStore {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val CURRENT_ROLE_KEY = stringPreferencesKey("current_role")
    }

    override suspend fun getAccessToken(): String? =
        dataStore.data.first()[ACCESS_TOKEN_KEY]

    override suspend fun getRefreshToken(): String? =
        dataStore.data.first()[REFRESH_TOKEN_KEY]

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(CURRENT_ROLE_KEY)
        }
    }

    override suspend fun getCurrentRole(): String? =
        dataStore.data.first()[CURRENT_ROLE_KEY]

    override suspend fun saveCurrentRole(role: String?) {
        dataStore.edit { preferences ->
            if (role != null) {
                preferences[CURRENT_ROLE_KEY] = role
            } else {
                preferences.remove(CURRENT_ROLE_KEY)
            }
        }
    }

    override suspend fun clearCurrentRole() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_ROLE_KEY)
        }
    }
}

expect fun createTokenStore(): TokenStore
