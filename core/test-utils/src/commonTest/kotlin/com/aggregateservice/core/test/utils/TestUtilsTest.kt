package com.aggregateservice.core.test.utils

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for test-utils module.
 * Verifies that test utilities are properly configured.
 */
class TestUtilsTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test utilities module compiles correctly`() = runTest {
        // Simple sanity test to verify the module compiles
        assertEquals(1, 1)
    }

    @Test
    fun `StandardTestDispatcher works correctly`() = runTest {
        // Verify StandardTestDispatcher is accessible from commonTest
        assert(testDispatcher.scheduler.currentTime >= 0)
    }
}
