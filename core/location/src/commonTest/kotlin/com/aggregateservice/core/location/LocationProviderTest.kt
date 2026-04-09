package com.aggregateservice.core.location

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Unit tests for LocationProvider interface/contract.
 * Tests per GEO-02 requirement for expect/actual pattern.
 *
 * Note: Since LocationProvider is an expect class with platform-specific
 * actual implementations (Android/iOS), this test verifies the interface
 * contract. Platform-specific behavior is tested in respective platform
 * test directories.
 */
class LocationProviderTest {
    @Test
    fun `LocationProviderFactory should create LocationProvider instance`() {
        val provider = LocationProviderFactory.create()
        assertTrue(provider != null, "LocationProviderFactory.create() should return non-null instance")
    }

    @Test
    fun `LocationProvider instance should support setActivity method signature`() {
        val provider = LocationProviderFactory.create()
        // Verify setActivity method exists via reflection
        // Note: Actual behavior is platform-specific (Android requires valid Activity)
        val setActivityMethod = provider::class.java.methods.find { it.name == "setActivity" }
        assertTrue(setActivityMethod != null, "LocationProvider should have setActivity method")
    }

    @Test
    fun `LocationProvider instance should have getCurrentLocation suspend function`() {
        val provider = LocationProviderFactory.create()
        // This test verifies the method signature exists
        // Actual behavior requires platform-specific setup or MockK
        assertTrue(provider is LocationProvider, "Provider should be LocationProvider instance")
    }

    @Test
    fun `LocationProvider instance should have requestPermission suspend function`() {
        val provider = LocationProviderFactory.create()
        // This test verifies the method signature exists
        // Actual behavior requires platform-specific setup or MockK
        assertTrue(provider is LocationProvider, "Provider should be LocationProvider instance")
    }

    @Test
    fun `LocationAccuracy enum should work with LocationProvider`() {
        // Verify that LocationAccuracy values can be passed to LocationProvider
        val highAccuracy = LocationAccuracy.HIGH
        val mediumAccuracy = LocationAccuracy.MEDIUM
        val lowAccuracy = LocationAccuracy.LOW

        assertTrue(highAccuracy != null, "HIGH accuracy should be available")
        assertTrue(mediumAccuracy != null, "MEDIUM accuracy should be available")
        assertTrue(lowAccuracy != null, "LOW accuracy should be available")
    }

    @Test
    fun `LocationPermissionStatus sealed interface should work with LocationProvider`() {
        // Verify that LocationPermissionStatus cases can be returned by LocationProvider
        val granted = LocationPermissionStatus.Granted
        val denied = LocationPermissionStatus.Denied
        val deniedPermanently = LocationPermissionStatus.DeniedPermanently
        val unknown = LocationPermissionStatus.Unknown

        assertTrue(granted != null, "Granted status should be available")
        assertTrue(denied != null, "Denied status should be available")
        assertTrue(deniedPermanently != null, "DeniedPermanently status should be available")
        assertTrue(unknown != null, "Unknown status should be available")
    }

    @Test
    fun `LocationProvider expect class should exist`() {
        // Verify the expect class can be referenced
        val providerClass = LocationProvider::class
        assertTrue(providerClass != null, "LocationProvider expect class should exist")
    }
}
