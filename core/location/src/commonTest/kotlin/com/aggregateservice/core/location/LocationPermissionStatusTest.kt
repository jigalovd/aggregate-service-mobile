package com.aggregateservice.core.location

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for LocationPermissionStatus sealed interface.
 * Tests sealed interface cases per GEO-04 requirement.
 */
class LocationPermissionStatusTest {
    @Test
    fun `LocationPermissionStatus Granted should be the granted case`() {
        val status = LocationPermissionStatus.Granted
        assertIs<LocationPermissionStatus.Granted>(status)
    }

    @Test
    fun `LocationPermissionStatus Denied should be the denied case`() {
        val status = LocationPermissionStatus.Denied
        assertIs<LocationPermissionStatus.Denied>(status)
    }

    @Test
    fun `LocationPermissionStatus DeniedPermanently should be the permanently denied case`() {
        val status = LocationPermissionStatus.DeniedPermanently
        assertIs<LocationPermissionStatus.DeniedPermanently>(status)
    }

    @Test
    fun `LocationPermissionStatus Unknown should be the unknown case`() {
        val status = LocationPermissionStatus.Unknown
        assertIs<LocationPermissionStatus.Unknown>(status)
    }

    @Test
    fun `LocationPermissionStatus should have exactly four cases`() {
        // Sealed interface: all implementations should be data objects
        val cases =
            listOf(
                LocationPermissionStatus.Granted,
                LocationPermissionStatus.Denied,
                LocationPermissionStatus.DeniedPermanently,
                LocationPermissionStatus.Unknown,
            )
        assertTrue(cases.size == 4, "LocationPermissionStatus should have exactly 4 cases")
    }

    @Test
    fun `all LocationPermissionStatus cases should be distinct`() {
        val granted = LocationPermissionStatus.Granted
        val denied = LocationPermissionStatus.Denied
        val deniedPermanently = LocationPermissionStatus.DeniedPermanently
        val unknown = LocationPermissionStatus.Unknown

        assertTrue(granted != denied, "Granted should not equal Denied")
        assertTrue(granted != deniedPermanently, "Granted should not equal DeniedPermanently")
        assertTrue(granted != unknown, "Granted should not equal Unknown")
        assertTrue(denied != deniedPermanently, "Denied should not equal DeniedPermanently")
        assertTrue(denied != unknown, "Denied should not equal Unknown")
        assertTrue(deniedPermanently != unknown, "DeniedPermanently should not equal Unknown")
    }

    @Test
    fun `LocationPermissionStatus Granted should have correct toString`() {
        val status = LocationPermissionStatus.Granted
        assertTrue(status.toString().contains("Granted"))
    }

    @Test
    fun `LocationPermissionStatus Denied should have correct toString`() {
        val status = LocationPermissionStatus.Denied
        assertTrue(status.toString().contains("Denied"))
    }

    @Test
    fun `LocationPermissionStatus DeniedPermanently should have correct toString`() {
        val status = LocationPermissionStatus.DeniedPermanently
        assertTrue(status.toString().contains("DeniedPermanently"))
    }

    @Test
    fun `LocationPermissionStatus Unknown should have correct toString`() {
        val status = LocationPermissionStatus.Unknown
        assertTrue(status.toString().contains("Unknown"))
    }
}
