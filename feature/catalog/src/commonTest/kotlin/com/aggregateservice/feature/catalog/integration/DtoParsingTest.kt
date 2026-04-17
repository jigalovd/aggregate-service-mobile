package com.aggregateservice.feature.catalog.integration

import com.aggregateservice.core.api.models.CategoryListResponse
import com.aggregateservice.core.api.models.ProviderDetailResponse
import com.aggregateservice.core.api.models.ProviderListResponse
import com.aggregateservice.core.api.models.PublicProviderServicesResponse
import com.aggregateservice.feature.catalog.data.mapper.localized
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for API DTO parsing.
 *
 * These tests verify that real API response structures (stored as fixtures)
 * parse correctly into generated DTOs from :core:api-models. This catches
 * DTO mismatches early and prevents API contract drift.
 *
 * Note: provider_detail_response.json uses a {"data": {...}} wrapper that
 * doesn't match the generated ProviderDetailResponse. Those tests still use
 * the legacy ProviderDetailsResponseDto until the fixture is updated.
 *
 * Fixture files are stored in: src/commonTest/resources/fixtures/
 */
class DtoParsingTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    /**
     * Helper to load fixture JSON from resources.
     * Uses file-based loading since classpath resource loading
     * doesn't work reliably for Android unit tests.
     */
    private fun loadFixture(fixtureName: String): String {
        // Try multiple possible resource paths for Android unit test compatibility
        val possiblePaths =
            listOf(
                "src/commonTest/resources/fixtures/$fixtureName",
                "fixtures/$fixtureName",
            )

        for (path in possiblePaths) {
            val file = java.io.File(path)
            if (file.exists()) {
                return file.readText()
            }
        }

        // Fallback: try classpath loading
        val classLoader = this::class.java.classLoader ?: ClassLoader.getSystemClassLoader()
        val resourcePath = "fixtures/$fixtureName"
        val resource =
            classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Fixture not found: $fixtureName (searched: $possiblePaths)")
        return resource.bufferedReader().use { it.readText() }
    }

    // ========== Categories Endpoint Tests ==========

    @Test
    fun `categories_response parses correctly`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoryListResponse>(jsonString)

        assertNotNull(dto.categories)
        assertEquals(3, dto.categories.size)
        assertEquals(3, dto.total)
    }

    @Test
    fun `category dto fields populate correctly with i18n name`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoryListResponse>(jsonString)

        val firstCategory = dto.categories[0]
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", firstCategory.id)
        assertNotNull(firstCategory.name)
        // name is I18nStringSchema with ru, he, en properties
        assertEquals("Стрижка и укладка", firstCategory.name.ru)
        assertEquals("תספורת ועיצוב", firstCategory.name.he)
        assertEquals("Haircut and Styling", firstCategory.name.en)
        assertEquals("https://cdn.example.com/icons/haircut.svg", firstCategory.iconUrl)
        assertTrue(firstCategory.isActive ?: true)
        assertEquals(1, firstCategory.sortOrder)
        assertNull(firstCategory.parentId)
    }

    @Test
    fun `category with parent_id parses as subcategory`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoryListResponse>(jsonString)

        val subcategory = dto.categories[2]
        assertEquals("c3d4e5f6-a7b8-9012-cdef-345678901234", subcategory.id)
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", subcategory.parentId)
        assertFalse(subcategory.parentId == null)
    }

    @Test
    fun `category icon_url field maps to iconUrl property`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoryListResponse>(jsonString)

        // icon_url from backend maps to iconUrl via @SerialName("icon_url")
        assertEquals("https://cdn.example.com/icons/haircut.svg", dto.categories[0].iconUrl)
        assertEquals("https://cdn.example.com/icons/nails.svg", dto.categories[1].iconUrl)
    }

    // ========== Provider Search Endpoint Tests ==========

    @Test
    fun `provider_search_response parses correctly`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderListResponse>(jsonString)

        assertNotNull(dto.providers)
        assertEquals(2, dto.providers.size)
        assertEquals(2, dto.total)
        assertEquals(20, dto.limit)
        assertEquals(0, dto.offset)
    }

    @Test
    fun `provider dto fields map correctly via SerialName annotations`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderListResponse>(jsonString)

        val provider = dto.providers[0]
        // Backend display_name maps to displayName
        assertEquals("Beauty Studio Elite", provider.displayName)
        // Backend rating_cached maps to ratingCached
        assertEquals(4.8, provider.ratingCached)
        // Backend reviews_count maps to reviewsCount
        assertEquals(127, provider.reviewsCount)
        // Backend avatar_url maps to avatarUrl
        assertEquals("https://cdn.example.com/avatars/provider1.jpg", provider.avatarUrl)
        // Backend user_id maps to userId
        assertEquals("u1a2b3c4-d5e6-7890-abcd-ef1234567890", provider.userId)
        assertTrue(provider.isVerified ?: false)
        assertTrue(provider.isActive ?: true)
    }

    @Test
    fun `provider location parses correctly`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderListResponse>(jsonString)

        val provider = dto.providers[0]
        assertNotNull(provider.location)
        assertEquals(32.0853, provider.location!!.lat)
        assertEquals(34.7818, provider.location!!.lon)
    }

    @Test
    fun `provider with default address parses correctly`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderListResponse>(jsonString)

        val provider = dto.providers[0]
        // address is present in fixture
        assertEquals("123 Herzl Street, Tel Aviv", provider.address)
    }

    // ========== Provider Details Endpoint Tests ==========

    @Test
    fun `provider_detail_response parses correctly`() {
        val jsonString = loadFixture("provider_detail_response.json")
        val dto = json.decodeFromString<ProviderDetailResponse>(jsonString)

        assertNotNull(dto.provider)
    }

    // ========== Service List Endpoint Tests ==========

    @Test
    fun `service_list_response parses correctly`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<PublicProviderServicesResponse>(jsonString)

        assertNotNull(dto.services)
        assertEquals(3, dto.services.size)
        assertEquals(3, dto.total)
    }

    @Test
    fun `service dto fields parse correctly with i18n title`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<PublicProviderServicesResponse>(jsonString)

        val service = dto.services[0]
        assertEquals("s1a2b3c4-d5e6-7890-abcd-ef1234567890", service.id)
        // Backend title is I18nStringSchema
        assertEquals("Женская стрижка", service.title.ru)
        assertEquals("תספורת נשים", service.title.he)
        assertEquals("Women's Haircut", service.title.en)
        // Backend base_price is int (cents)
        assertEquals(15000, service.basePrice)
        // Backend duration_minutes maps to durationMinutes
        assertEquals(60, service.durationMinutes)
        assertTrue(service.isActive)
    }

    @Test
    fun `service name extraction uses i18n priority`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<PublicProviderServicesResponse>(jsonString)

        val service = dto.services[0]
        // localized() uses priority: ru -> he -> en
        // Fixture has Russian, so it returns Russian
        assertEquals("Женская стрижка", service.title.localized())
    }

    @Test
    fun `service description extraction uses i18n priority`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<PublicProviderServicesResponse>(jsonString)

        val service = dto.services[0]
        assertNotNull(service.description)
        // Description uses i18n priority ru -> he -> en, returns Russian
        assertEquals(
            "Профессиональная женская стрижка включая мытье, стрижку и укладку",
            service.description?.localized(),
        )
    }

    @Test
    fun `multiple services parse correctly`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<PublicProviderServicesResponse>(jsonString)

        // localized() uses priority ru -> he -> en
        assertEquals("Женская стрижка", dto.services[0].title.localized())
        assertEquals("Мужская стрижка", dto.services[1].title.localized())
        assertEquals("Покраска волос", dto.services[2].title.localized())

        // Different prices in cents
        assertEquals(15000, dto.services[0].basePrice)
        assertEquals(8000, dto.services[1].basePrice)
        assertEquals(35000, dto.services[2].basePrice)
    }

    // ========== Provider Services Endpoint Tests ==========

    @Test
    fun `provider services response parses with services wrapper`() {
        val jsonString = loadFixture("provider_services_response.json")
        val dto = json.decodeFromString<PublicProviderServicesResponse>(jsonString)

        assertNotNull(dto.services)
        assertEquals(2, dto.services.size)
        assertEquals(2, dto.total)
        assertEquals("s1a2b3c4-d5e6-7890-abcd-ef1234567890", dto.services[0].id)
    }
}
