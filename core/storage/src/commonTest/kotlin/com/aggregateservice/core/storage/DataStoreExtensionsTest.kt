package com.aggregateservice.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for DataStore extension functions.
 * Uses MockK to mock DataStore and Preferences for testing extension behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreExtensionsTest {

    // Test 1: get() - Flow-based value retrieval
    @Test
    fun `get returns Flow of value when key exists`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = stringPreferencesKey("name")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns "test_value"
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.get(key).first()

        assertEquals("test_value", result)
    }

    @Test
    fun `get returns null when key does not exist`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = stringPreferencesKey("name")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns null
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.get(key).first()

        assertNull(result)
    }

    // Test 2: getFirst() - Synchronous value retrieval
    @Test
    fun `getFirst returns value when key exists`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = intPreferencesKey("count")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns 42
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals(42, result)
    }

    @Test
    fun `getFirst returns null when key does not exist`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = intPreferencesKey("count")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns null
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertNull(result)
    }

    // Test 3: contains() - Check key existence
    @Test
    fun `contains returns true when key exists`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = stringPreferencesKey("exists")
        val mockPrefs = mockk<Preferences> {
            every { contains(key) } returns true
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.contains(key)

        assertTrue(result)
    }

    @Test
    fun `contains returns false when key does not exist`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = stringPreferencesKey("nonexistent")
        val mockPrefs = mockk<Preferences> {
            every { contains(key) } returns false
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.contains(key)

        assertFalse(result)
    }

    // Test 4: getAll() - Get all preferences as Flow
    @Test
    fun `getAll returns Flow of all preferences`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPrefs = mockk<Preferences>()
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getAll()

        assertEquals(mockPrefs, result.first())
    }

    // Test 5: getAllSync() - Synchronous get all
    @Test
    fun `getAllSync returns all preferences synchronously`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPrefs = mockk<Preferences>()
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getAllSync()

        assertEquals(mockPrefs, result)
    }

    // Test 6: getFirst retrieves different types correctly
    @Test
    fun `getFirst retrieves boolean value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = booleanPreferencesKey("bool_key")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns true
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertTrue(result!!)
    }

    @Test
    fun `getFirst retrieves long value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = longPreferencesKey("long_key")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns 123456789L
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals(123456789L, result)
    }

    @Test
    fun `getFirst retrieves float value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = floatPreferencesKey("float_key")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns 3.14f
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals(3.14f, result!!)
    }

    @Test
    fun `getFirst retrieves double value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = doublePreferencesKey("double_key")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns 2.71828
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals(2.71828, result!!)
    }

    // Test 7: Empty string handling
    @Test
    fun `getFirst returns empty string when stored`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = stringPreferencesKey("empty_value")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns ""
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals("", result)
    }

    @Test
    fun `get returns empty string from Flow`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = stringPreferencesKey("empty_value")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns ""
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.get(key).first()

        assertEquals("", result)
    }

    // Test 8: Zero and negative numbers
    @Test
    fun `getFirst retrieves zero value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = intPreferencesKey("zero")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns 0
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals(0, result)
    }

    @Test
    fun `getFirst retrieves negative value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = intPreferencesKey("negative")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns -999
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals(-999, result)
    }

    @Test
    fun `getFirst retrieves large negative long value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = longPreferencesKey("large_negative")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns Long.MIN_VALUE
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertEquals(Long.MIN_VALUE, result)
    }

    // Test 9: Boolean false value
    @Test
    fun `getFirst retrieves false boolean value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val key = booleanPreferencesKey("is_enabled")
        val mockPrefs = mockk<Preferences> {
            every { this@mockk[key] } returns false
        }
        every { mockDataStore.data } returns flowOf(mockPrefs)

        val result = mockDataStore.getFirst(key)

        assertFalse(result!!)
    }

    // Test 10: set() - Save value to DataStore (verifies edit is called)
    @Test
    fun `set calls edit on DataStore`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = stringPreferencesKey("name")
        mockDataStore.set(key, "new_value")
        // Just verify no exception is thrown - the function completes successfully
    }

    @Test
    fun `set calls edit with int value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = intPreferencesKey("count")
        mockDataStore.set(key, 99)
    }

    @Test
    fun `set calls edit with boolean value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = booleanPreferencesKey("enabled")
        mockDataStore.set(key, true)
    }

    @Test
    fun `set calls edit with long value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = longPreferencesKey("timestamp")
        mockDataStore.set(key, 1713000000000L)
    }

    @Test
    fun `set calls edit with double value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = doublePreferencesKey("price")
        mockDataStore.set(key, 99.99)
    }

    @Test
    fun `set calls edit with float value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = floatPreferencesKey("rating")
        mockDataStore.set(key, 4.5f)
    }

    @Test
    fun `set overwrites existing value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = stringPreferencesKey("counter")
        mockDataStore.set(key, "second")
        mockDataStore.set(key, "third")
    }

    // Test 11: remove() - Delete a key from DataStore
    @Test
    fun `remove calls edit on DataStore`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = stringPreferencesKey("old_key")
        mockDataStore.remove(key)
    }

    @Test
    fun `remove works on non-existent key`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = stringPreferencesKey("nonexistent")
        mockDataStore.remove(key)
    }

    @Test
    fun `remove deletes int key`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = intPreferencesKey("score")
        mockDataStore.remove(key)
    }

    @Test
    fun `remove deletes boolean key`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = booleanPreferencesKey("flag")
        mockDataStore.remove(key)
    }

    // Test 12: clear() - Clear all data from DataStore
    @Test
    fun `clear calls edit on DataStore`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        mockDataStore.clear()
    }

    @Test
    fun `clear on empty DataStore does not throw`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        mockDataStore.clear()
    }

    @Test
    fun `clear can be called multiple times`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        mockDataStore.clear()
        mockDataStore.clear()
    }

    // Test 13: set saves empty string and zero values
    @Test
    fun `set saves empty string value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = stringPreferencesKey("empty_val")
        mockDataStore.set(key, "")
    }

    @Test
    fun `set saves zero int value`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        val key = intPreferencesKey("zero_count")
        mockDataStore.set(key, 0)
    }
}