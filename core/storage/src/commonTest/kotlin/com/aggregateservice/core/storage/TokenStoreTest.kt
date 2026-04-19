package com.aggregateservice.core.storage

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [TokenStore] implementations.
 * Uses FakeTokenStore to test TokenStore behavior without DataStore dependency.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TokenStoreTest {

    @Test
    fun `getAccessToken returns null when no token saved`() = runTest {
        val tokenStore = FakeTokenStore()

        val result = tokenStore.getAccessToken()

        assertNull(result)
    }

    @Test
    fun `getRefreshToken returns null when no token saved`() = runTest {
        val tokenStore = FakeTokenStore()

        val result = tokenStore.getRefreshToken()

        assertNull(result)
    }

    @Test
    fun `saveTokens stores both access and refresh tokens`() = runTest {
        val tokenStore = FakeTokenStore()

        tokenStore.saveTokens("access_token_value", "refresh_token_value")

        assertEquals("access_token_value", tokenStore.getAccessToken())
        assertEquals("refresh_token_value", tokenStore.getRefreshToken())
    }

    @Test
    fun `clearTokens removes both access and refresh tokens`() = runTest {
        val tokenStore = FakeTokenStore()
        tokenStore.saveTokens("access_token_value", "refresh_token_value")

        tokenStore.clearTokens()

        assertNull(tokenStore.getAccessToken())
        assertNull(tokenStore.getRefreshToken())
    }

    @Test
    fun `saveTokens overwrites existing tokens`() = runTest {
        val tokenStore = FakeTokenStore()
        tokenStore.saveTokens("old_access", "old_refresh")

        tokenStore.saveTokens("new_access", "new_refresh")

        assertEquals("new_access", tokenStore.getAccessToken())
        assertEquals("new_refresh", tokenStore.getRefreshToken())
    }

    @Test
    fun `clearTokens works on empty store`() = runTest {
        val tokenStore = FakeTokenStore()

        // Should not throw
        tokenStore.clearTokens()

        assertNull(tokenStore.getAccessToken())
        assertNull(tokenStore.getRefreshToken())
    }

    @Test
    fun `saveTokens with empty strings stores empty strings`() = runTest {
        val tokenStore = FakeTokenStore()

        tokenStore.saveTokens("", "")

        assertEquals("", tokenStore.getAccessToken())
        assertEquals("", tokenStore.getRefreshToken())
    }
}

/**
 * Fake implementation of TokenStore for testing.
 * Provides in-memory token storage without requiring DataStore.
 */
class FakeTokenStore : TokenStore {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var currentRole: String? = null

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
        currentRole = null
    }

    override suspend fun getCurrentRole(): String? = currentRole

    override suspend fun saveCurrentRole(role: String?) {
        currentRole = role
    }

    override suspend fun clearCurrentRole() {
        currentRole = null
    }
}
