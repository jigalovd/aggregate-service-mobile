package com.aggregateservice.core.auth.impl.token

import com.aggregateservice.core.storage.TokenStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenManagerImplTest {
    private lateinit var tokenStorage: TokenStorage
    private lateinit var tokenManager: TokenManagerImpl

    @BeforeTest
    fun setup() {
        tokenStorage = mockk(relaxed = true)
        tokenManager = TokenManagerImpl(tokenStorage)
    }

    @Test
    fun `getAccessToken returns null initially`() = runTest {
        assertNull(tokenManager.getAccessToken())
    }

    @Test
    fun `setTokens saves token and updates flow`() = runTest {
        tokenManager.setTokens("test-token")
        assertEquals("test-token", tokenManager.getAccessToken())
        assertEquals("test-token", tokenManager.observeToken().first())
    }

    @Test
    fun `clearTokens removes token and updates flow`() = runTest {
        tokenManager.setTokens("test-token")
        tokenManager.clearTokens()
        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.observeToken().first())
    }

    @Test
    fun `observeToken emits token updates`() = runTest {
        tokenManager.observeToken().test {
            // Initial emission is null
            assertNull(awaitItem())
            tokenManager.setTokens("token-1")
            assertEquals("token-1", awaitItem())
            tokenManager.setTokens("token-2")
            assertEquals("token-2", awaitItem())
            tokenManager.clearTokens()
            assertNull(awaitItem())
        }
    }

    @Test
    fun `setTokens persists to storage`() = runTest {
        tokenManager.setTokens("test-token")
        coVerify { tokenStorage.saveAccessToken("test-token") }
    }

    @Test
    fun `clearTokens clears storage`() = runTest {
        tokenManager.clearTokens()
        coVerify { tokenStorage.clearTokens() }
    }

    @Test
    fun `initFromStorage loads token from storage`() = runTest {
        coEvery { tokenStorage.getAccessTokenSync() } returns "stored-token"
        tokenManager.initFromStorage()
        assertEquals("stored-token", tokenManager.getAccessToken())
        assertEquals("stored-token", tokenManager.observeToken().first())
    }

    @Test
    fun `initFromStorage with null storage sets null`() = runTest {
        coEvery { tokenStorage.getAccessTokenSync() } returns null
        tokenManager.initFromStorage()
        assertNull(tokenManager.getAccessToken())
    }
}
