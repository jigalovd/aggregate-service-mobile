package com.aggregateservice.feature.catalog.presentation.screenmodel

import com.aggregateservice.core.common.model.Location
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.WorkingHours
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for ProviderWithDistance distance formatting (UI-05 requirement).
 */
class ProviderWithDistanceTest {
    @Test
    fun `from formats distance under 1km as meters`() {
        val userLocation =
            Location(
                latitude = 32.0853,
                longitude = 34.7818,
                address = "User",
                city = "Tel Aviv",
            )

        // Provider ~87m away
        val provider = createProviderAt(32.0860, 34.7820)

        val result = ProviderWithDistance.from(provider, userLocation)

        assertTrue(result.formattedDistance.endsWith(" m"), "Expected meters suffix, got: ${result.formattedDistance}")
        val distanceValue =
            result.formattedDistance
                .dropLast(2)
                .trim()
                .toInt()
        assertTrue(distanceValue in 50..150, "Expected ~50-150m, got: ${result.formattedDistance}")
        assertEquals(0.087, result.distanceKm!!, 0.01)
    }

    @Test
    fun `from formats distance over 1km as kilometers with one decimal`() {
        val userLocation =
            Location(
                latitude = 32.0853,
                longitude = 34.7818,
                address = "User",
                city = "Tel Aviv",
            )

        // Provider ~15km away
        val provider = createProviderAt(32.1953, 34.8818)

        val result = ProviderWithDistance.from(provider, userLocation)

        assertTrue(result.formattedDistance.endsWith(" km"), "Expected km suffix, got: ${result.formattedDistance}")
        assertTrue(result.formattedDistance.contains("15"), "Expected ~15km, got: ${result.formattedDistance}")
        assertEquals(15.0, result.distanceKm!!, 1.0)
    }

    @Test
    fun `from returns empty string when userLocation is null`() {
        val provider = createProviderAt(32.0860, 34.7820)

        val result = ProviderWithDistance.from(provider, null)

        assertEquals("", result.formattedDistance)
        assertEquals(null, result.distanceKm)
    }

    @Test
    fun `from preserves provider data`() {
        val userLocation =
            Location(
                latitude = 32.0853,
                longitude = 34.7818,
                address = "User",
                city = "Tel Aviv",
            )
        val provider = createProviderAt(32.0860, 34.7820)

        val result = ProviderWithDistance.from(provider, userLocation)

        assertEquals(provider, result.provider)
        assertEquals("Test Salon", result.provider.businessName)
    }

    @Test
    fun `from formats exactly 1km as kilometers`() {
        val userLocation =
            Location(
                latitude = 0.0,
                longitude = 0.0,
                address = "User",
                city = "Origin",
            )

        // Provider exactly 1km away (0.009 degrees ~ 1km at equator)
        val provider = createProviderAt(0.009, 0.0)

        val result = ProviderWithDistance.from(provider, userLocation)

        // At exactly 1km, should format as km (since condition is < 1.0, not <= 1.0)
        assertTrue(result.formattedDistance.endsWith(" km"), "Expected km suffix at exactly 1km, got: ${result.formattedDistance}")
    }

    @Test
    fun `from formats just under 1km as meters`() {
        val userLocation =
            Location(
                latitude = 32.0853,
                longitude = 34.7818,
                address = "User",
                city = "Tel Aviv",
            )

        // Provider ~999m away
        val provider = createProviderAt(32.0844, 34.7818)

        val result = ProviderWithDistance.from(provider, userLocation)

        assertTrue(result.formattedDistance.endsWith(" m"), "Expected meters at ~999m, got: ${result.formattedDistance}")
    }

    private fun createProviderAt(lat: Double, lon: Double): Provider {
        return Provider(
            id = "provider-test",
            userId = "user-test",
            businessName = "Test Salon",
            description = "Test",
            rating = 4.5,
            reviewCount = 100,
            location =
                Location(
                    latitude = lat,
                    longitude = lon,
                    address = "Test Address",
                    city = "Tel Aviv",
                ),
            workingHours = WorkingHours(),
            isVerified = true,
            createdAt = Instant.fromEpochMilliseconds(0),
            categories = emptyList(),
        )
    }
}
