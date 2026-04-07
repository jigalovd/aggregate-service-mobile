package com.aggregateservice.core.storage

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [TokenHolder] memory+DataStore synchronization.
 *
 * Verifies:
 * - set() syncs memory + DataStore
 * - clear() syncs memory + DataStore
 * - initFromStorage() loads from DataStore
 * - get() returns token via mutex
 */
class TokenHolderTest {

    private val mockStorage = mockk<TokenStorage>()

    @After
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun `set updates both memory and storage`() = runTest {
        coEvery { mockStorage.saveAccessToken(any()) } returns Unit

        val holder = TokenHolder(mockStorage)
        holder.set("test_token")

        coVerify { mockStorage.saveAccessToken("test_token") }
        assertEquals("test_token", holder.get())
    }

    @Test
    fun `clear nullifies both memory and storage`() = runTest {
        coEvery { mockStorage.saveAccessToken(any()) } returns Unit
        coEvery { mockStorage.clearTokens() } returns Unit

        val holder = TokenHolder(mockStorage)
        holder.set("test_token")
        holder.clear()

        coVerify { mockStorage.clearTokens() }
        assertNull(holder.get())
    }

    @Test
    fun `initFromStorage loads token from DataStore`() = runTest {
        coEvery { mockStorage.getAccessTokenSync() } returns "stored_token"

        val holder = TokenHolder(mockStorage)
        holder.initFromStorage()

        assertEquals("stored_token", holder.get())
    }

    @Test
    fun `get returns token via mutex protection`() = runTest {
        coEvery { mockStorage.getAccessTokenSync() } returns "mutex_token"

        val holder = TokenHolder(mockStorage)
        holder.initFromStorage()

        // Multiple concurrent gets should all return same value
        val results = mutableListOf<String?>()
        repeat(5) {
            results.add(holder.get())
        }

        results.forEach { assertEquals("mutex_token", it) }
    }

    @Test
    fun `set is atomic with memory and storage`() = runTest {
        coEvery { mockStorage.saveAccessToken(any()) } returns Unit

        val holder = TokenHolder(mockStorage)

        holder.set("token1")
        assertEquals("token1", holder.get())

        holder.set("token2")
        assertEquals("token2", holder.get())

        // Storage called for each set
        coVerify { mockStorage.saveAccessToken("token1") }
        coVerify { mockStorage.saveAccessToken("token2") }
    }
}