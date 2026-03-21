package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.feature.catalog.data.dto.CategoryDto
import com.aggregateservice.feature.catalog.data.dto.DayScheduleDto
import com.aggregateservice.feature.catalog.data.dto.ProviderDto
import com.aggregateservice.feature.catalog.data.dto.WorkingHoursDto
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertFalse

class ProviderMapperTest {

    // ========== Basic Mapping Tests ==========

    @Test
    fun `should map ProviderDto to Provider with all fields`() {
        val dto = createTestProviderDto()
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("provider-123", provider.id)
        assertEquals("user-456", provider.userId)
        assertEquals("Test Salon", provider.businessName)
        assertEquals("A beautiful salon", provider.description)
        assertEquals("https://example.com/logo.png", provider.logoUrl)
        assertEquals(2, provider.photos.size)
        assertEquals(4.8, provider.rating)
        assertEquals(150, provider.reviewCount)
        assertTrue(provider.isVerified)
        assertTrue(provider.isActive)
        assertEquals(25, provider.servicesCount)
    }

    @Test
    fun `should map location correctly`() {
        val dto = createTestProviderDto()
        val provider = ProviderMapper.toDomain(dto)

        assertEquals(32.0853, provider.location.latitude)
        assertEquals(34.7818, provider.location.longitude)
        assertEquals("Dizengoff 100", provider.location.address)
        assertEquals("Tel Aviv", provider.location.city)
        assertEquals("12345", provider.location.postalCode)
        assertEquals("Israel", provider.location.country)
    }

    @Test
    fun `should map working hours correctly`() {
        val dto = createTestProviderDto(
            workingHours = WorkingHoursDto(
                monday = DayScheduleDto(openTime = "09:00", closeTime = "18:00"),
                tuesday = DayScheduleDto(openTime = "10:00", closeTime = "19:00"),
                friday = DayScheduleDto(
                    openTime = "09:00",
                    closeTime = "14:00",
                    breakStart = "12:00",
                    breakEnd = "12:30",
                ),
            ),
        )
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("09:00", provider.workingHours.monday?.openTime)
        assertEquals("18:00", provider.workingHours.monday?.closeTime)
        assertNull(provider.workingHours.monday?.breakStart)

        assertEquals("10:00", provider.workingHours.tuesday?.openTime)
        assertEquals("19:00", provider.workingHours.tuesday?.closeTime)

        assertEquals("09:00", provider.workingHours.friday?.openTime)
        assertEquals("14:00", provider.workingHours.friday?.closeTime)
        assertEquals("12:00", provider.workingHours.friday?.breakStart)
        assertEquals("12:30", provider.workingHours.friday?.breakEnd)
    }

    @Test
    fun `should map empty working hours to default WorkingHours`() {
        val dto = createTestProviderDto(workingHours = null)
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.workingHours.monday)
        assertNull(provider.workingHours.tuesday)
    }

    @Test
    fun `should map categories correctly`() {
        val dto = createTestProviderDto(
            categories = listOf(
                CategoryDto(id = "cat-1", name = "Haircut", sortOrder = 1),
                CategoryDto(id = "cat-2", name = "Manicure", parentId = "cat-parent", sortOrder = 2),
            ),
        )
        val provider = ProviderMapper.toDomain(dto)

        assertEquals(2, provider.categories.size)
        assertEquals("cat-1", provider.categories[0].id)
        assertEquals("Haircut", provider.categories[0].name)
        assertTrue(provider.categories[0].isRootCategory)

        assertEquals("cat-2", provider.categories[1].id)
        assertEquals("Manicure", provider.categories[1].name)
        assertEquals("cat-parent", provider.categories[1].parentId)
        assertFalse(provider.categories[1].isRootCategory)
    }

    @Test
    fun `should map empty categories list`() {
        val dto = createTestProviderDto(categories = emptyList())
        val provider = ProviderMapper.toDomain(dto)

        assertTrue(provider.categories.isEmpty())
    }

    // ========== Nullable Fields Tests ==========

    @Test
    fun `should handle null description`() {
        val dto = createTestProviderDto(description = null)
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.description)
    }

    @Test
    fun `should handle null logoUrl`() {
        val dto = createTestProviderDto(logoUrl = null)
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.logoUrl)
    }

    @Test
    fun `should handle null postalCode`() {
        val dto = createTestProviderDto(postalCode = null)
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.location.postalCode)
    }

    @Test
    fun `should handle null country`() {
        val dto = createTestProviderDto(country = null)
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.location.country)
    }

    // ========== Default Values Tests ==========

    @Test
    fun `should use default values for optional fields`() {
        val dto = ProviderDto(
            id = "provider-1",
            userId = "user-1",
            businessName = "Test",
            latitude = 32.0,
            longitude = 34.0,
            address = "Address",
            city = "City",
            createdAt = Instant.DISTANT_PAST,
        )
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.description)
        assertNull(provider.logoUrl)
        assertTrue(provider.photos.isEmpty())
        assertEquals(0.0, provider.rating)
        assertEquals(0, provider.reviewCount)
        assertFalse(provider.isVerified)
        assertTrue(provider.isActive)
        assertEquals(0, provider.servicesCount)
        assertTrue(provider.categories.isEmpty())
    }

    // ========== Photos Mapping Tests ==========

    @Test
    fun `should map photos list correctly`() {
        val photos = listOf("photo1.jpg", "photo2.jpg", "photo3.jpg")
        val dto = createTestProviderDto(photos = photos)
        val provider = ProviderMapper.toDomain(dto)

        assertEquals(3, provider.photos.size)
        assertEquals("photo1.jpg", provider.photos[0])
        assertEquals("photo2.jpg", provider.photos[1])
        assertEquals("photo3.jpg", provider.photos[2])
    }

    @Test
    fun `should map empty photos list`() {
        val dto = createTestProviderDto(photos = emptyList())
        val provider = ProviderMapper.toDomain(dto)

        assertTrue(provider.photos.isEmpty())
    }

    // ========== Computed Properties Tests ==========

    @Test
    fun `provider should have formattedRating computed property`() {
        val dto = createTestProviderDto(rating = 4.567)
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("4.6", provider.formattedRating)
    }

    @Test
    fun `provider should have primaryPhotoUrl from photos list`() {
        val dto = createTestProviderDto(
            photos = listOf("primary.jpg", "secondary.jpg"),
            logoUrl = "logo.png",
        )
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("primary.jpg", provider.primaryPhotoUrl)
    }

    @Test
    fun `provider should use logoUrl as primaryPhotoUrl when photos is empty`() {
        val dto = createTestProviderDto(
            photos = emptyList(),
            logoUrl = "logo.png",
        )
        val provider = ProviderMapper.toDomain(dto)

        assertEquals("logo.png", provider.primaryPhotoUrl)
    }

    @Test
    fun `provider should have null primaryPhotoUrl when no photos or logo`() {
        val dto = createTestProviderDto(
            photos = emptyList(),
            logoUrl = null,
        )
        val provider = ProviderMapper.toDomain(dto)

        assertNull(provider.primaryPhotoUrl)
    }

    // ========== Helper Methods ==========

    private fun createTestProviderDto(
        id: String = "provider-123",
        userId: String = "user-456",
        businessName: String = "Test Salon",
        description: String? = "A beautiful salon",
        logoUrl: String? = "https://example.com/logo.png",
        photos: List<String> = listOf("photo1.jpg", "photo2.jpg"),
        rating: Double = 4.8,
        reviewCount: Int = 150,
        latitude: Double = 32.0853,
        longitude: Double = 34.7818,
        address: String = "Dizengoff 100",
        city: String = "Tel Aviv",
        postalCode: String? = "12345",
        country: String? = "Israel",
        isVerified: Boolean = true,
        isActive: Boolean = true,
        createdAt: Instant = Instant.DISTANT_PAST,
        categories: List<CategoryDto> = listOf(
            CategoryDto(id = "cat-1", name = "Haircut"),
        ),
        servicesCount: Int = 25,
        workingHours: WorkingHoursDto? = WorkingHoursDto(
            monday = DayScheduleDto(openTime = "09:00", closeTime = "18:00"),
        ),
    ): ProviderDto {
        return ProviderDto(
            id = id,
            userId = userId,
            businessName = businessName,
            description = description,
            logoUrl = logoUrl,
            photos = photos,
            rating = rating,
            reviewCount = reviewCount,
            latitude = latitude,
            longitude = longitude,
            address = address,
            city = city,
            postalCode = postalCode,
            country = country,
            isVerified = isVerified,
            isActive = isActive,
            createdAt = createdAt,
            categories = categories,
            servicesCount = servicesCount,
            workingHours = workingHours,
        )
    }
}
