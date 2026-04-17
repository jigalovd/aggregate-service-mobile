package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.core.api.models.LocationSchema
import com.aggregateservice.core.api.models.ProviderResponse
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProviderMapperTest {
    // ========== Basic Mapping Tests ==========

    @Test
    fun `should map ProviderResponse to Provider with all fields`() {
        val dto = createTestProviderResponse()
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("provider-123", provider.id)
        assertEquals("user-456", provider.userId)
        assertEquals("Test Salon", provider.businessName)
        assertEquals("A beautiful salon", provider.description)
        assertEquals("https://example.com/logo.png", provider.logoUrl)
        assertEquals(4.8, provider.rating)
        assertEquals(150, provider.reviewCount)
        assertTrue(provider.isVerified)
        assertTrue(provider.isActive)
    }

    @Test
    fun `should map location correctly`() {
        val dto = createTestProviderResponse()
        val provider = ProviderMapper.toDomain(dto)

        assertEquals(32.0853, provider.location.latitude)
        assertEquals(34.7818, provider.location.longitude)
        assertEquals("Dizengoff 100", provider.location.address)
    }

    @Test
    fun `should default location to zeros when null`() {
        val dto =
            createTestProviderResponse(
                location = null,
                address = null,
            )
        val provider = ProviderMapper.toDomain(dto)

        assertEquals(0.0, provider.location.latitude)
        assertEquals(0.0, provider.location.longitude)
        assertEquals("", provider.location.address)
    }

    // ========== Nullable Fields Tests ==========

    @Test
    fun `should handle null description`() {
        val dto = createTestProviderResponse(bio = null)
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.description)
    }

    @Test
    fun `should handle null avatarUrl`() {
        val dto = createTestProviderResponse(avatarUrl = null)
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.logoUrl)
    }

    // ========== Default Values Tests ==========

    @Test
    fun `should use default values for optional fields`() {
        val dto =
            ProviderResponse(
                id = "provider-1",
                userId = "user-1",
                displayName = "Test",
                ratingCached = 0.0,
                reviewsCount = 0,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.description)
        assertNull(provider.logoUrl)
        assertTrue(provider.photos.isEmpty())
        assertEquals(0.0, provider.rating)
        assertEquals(0, provider.reviewCount)
        assertTrue(provider.categories.isEmpty())
        assertEquals(0, provider.servicesCount)
    }

    // ========== Computed Properties Tests ==========

    @Test
    fun `provider should have formattedRating computed property`() {
        val dto = createTestProviderResponse(ratingCached = 4.567)
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("4.6", provider.formattedRating)
    }

    @Test
    fun `provider should use avatarUrl as logoUrl`() {
        val dto = createTestProviderResponse(avatarUrl = "avatar.png")
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("avatar.png", provider.logoUrl)
    }

    @Test
    fun `provider should have null primaryPhotoUrl when no photos or logo`() {
        val dto =
            createTestProviderResponse(
                avatarUrl = null,
            )
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.primaryPhotoUrl)
    }

    // ========== Helper Methods ==========

    private fun createTestProviderResponse(
        id: String = "provider-123",
        userId: String = "user-456",
        displayName: String = "Test Salon",
        bio: String? = "A beautiful salon",
        avatarUrl: String? = "https://example.com/logo.png",
        ratingCached: Double = 4.8,
        reviewsCount: Int = 150,
        location: LocationSchema? = LocationSchema(lat = 32.0853, lon = 34.7818),
        address: String? = "Dizengoff 100",
        isVerified: Boolean = true,
        isActive: Boolean = true,
        createdAt: Instant = Instant.DISTANT_PAST,
        updatedAt: Instant = Instant.DISTANT_PAST,
    ): ProviderResponse {
        return ProviderResponse(
            id = id,
            userId = userId,
            displayName = displayName,
            bio = bio,
            avatarUrl = avatarUrl,
            ratingCached = ratingCached,
            reviewsCount = reviewsCount,
            location = location,
            address = address,
            isVerified = isVerified,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
