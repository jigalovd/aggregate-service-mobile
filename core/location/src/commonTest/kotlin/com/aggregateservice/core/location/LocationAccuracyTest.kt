package com.aggregateservice.core.location

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for LocationAccuracy enum.
 * Tests enum values per GEO-03 requirement.
 */
class LocationAccuracyTest {
    @Test
    fun `LocationAccuracy should have HIGH value`() {
        val high = LocationAccuracy.HIGH
        assertEquals(LocationAccuracy.HIGH, high)
    }

    @Test
    fun `LocationAccuracy should have MEDIUM value`() {
        val medium = LocationAccuracy.MEDIUM
        assertEquals(LocationAccuracy.MEDIUM, medium)
    }

    @Test
    fun `LocationAccuracy should have LOW value`() {
        val low = LocationAccuracy.LOW
        assertEquals(LocationAccuracy.LOW, low)
    }

    @Test
    fun `LocationAccuracy should have exactly three values`() {
        val values = LocationAccuracy.entries
        assertEquals(3, values.size)
    }

    @Test
    fun `LocationAccuracy entries should be in order HIGH, MEDIUM, LOW`() {
        val values = LocationAccuracy.entries
        assertEquals(LocationAccuracy.HIGH, values[0])
        assertEquals(LocationAccuracy.MEDIUM, values[1])
        assertEquals(LocationAccuracy.LOW, values[2])
    }

    @Test
    fun `LocationAccuracy name should match enum name`() {
        assertEquals("HIGH", LocationAccuracy.HIGH.name)
        assertEquals("MEDIUM", LocationAccuracy.MEDIUM.name)
        assertEquals("LOW", LocationAccuracy.LOW.name)
    }

    @Test
    fun `LocationAccuracy ordinal should be correct`() {
        assertEquals(0, LocationAccuracy.HIGH.ordinal)
        assertEquals(1, LocationAccuracy.MEDIUM.ordinal)
        assertEquals(2, LocationAccuracy.LOW.ordinal)
    }
}
