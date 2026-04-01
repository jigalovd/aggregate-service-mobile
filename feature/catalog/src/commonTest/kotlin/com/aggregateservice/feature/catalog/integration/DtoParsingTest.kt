package com.aggregateservice.feature.catalog.integration

import com.aggregateservice.feature.catalog.data.dto.CategoriesResponseDto
import com.aggregateservice.feature.catalog.data.dto.response.ProviderDetailsResponseDto
import com.aggregateservice.feature.catalog.data.dto.response.ProviderSearchResponseDto
import com.aggregateservice.feature.catalog.data.dto.response.ServiceListResponseDto
import com.aggregateservice.feature.favorites.data.dto.FavoritesListResponseDto
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
 * parse correctly into mobile DTOs. This catches DTO mismatches early
 * and prevents API contract drift.
 *
 * Fixture files are stored in: src/commonTest/resources/fixtures/
 */
class DtoParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Helper to load fixture JSON from resources.
     * Uses classpath resource loading.
     */
    private fun loadFixture(fixtureName: String): String {
        val classLoader = this::class.java.classLoader
        val resource = classLoader.getResourceAsStream("fixtures/$fixtureName")
            ?: throw IllegalArgumentException("Fixture not found: $fixtureName")
        return resource.bufferedReader().use { it.readText() }
    }

    // ========== Categories Endpoint Tests ==========

    @Test
    fun `categories_response parses correctly`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoriesResponseDto>(jsonString)

        assertNotNull(dto.categories)
        assertEquals(3, dto.categories.size)
        assertEquals(3, dto.total)
    }

    @Test
    fun `category dto fields populate correctly with i18n name`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoriesResponseDto>(jsonString)

        val firstCategory = dto.categories[0]
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", firstCategory.id)
        assertNotNull(firstCategory.name)
        // name is Map<String, String> per CategoryDto
        assertTrue(firstCategory.name.containsKey("ru"))
        assertTrue(firstCategory.name.containsKey("he"))
        assertTrue(firstCategory.name.containsKey("en"))
        assertEquals("Стрижка и укладка", firstCategory.name["ru"])
        assertEquals("תספורת ועיצוב", firstCategory.name["he"])
        assertEquals("Haircut and Styling", firstCategory.name["en"])
        assertEquals("https://cdn.example.com/icons/haircut.svg", firstCategory.icon)
        assertTrue(firstCategory.isActive)
        assertEquals(1, firstCategory.sortOrder)
        assertNull(firstCategory.parentId)
    }

    @Test
    fun `category with parent_id parses as subcategory`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoriesResponseDto>(jsonString)

        val subcategory = dto.categories[2]
        assertEquals("c3d4e5f6-a7b8-9012-cdef-345678901234", subcategory.id)
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", subcategory.parentId)
        assertFalse(subcategory.parentId == null)
    }

    @Test
    fun `category icon_url field maps to icon property`() {
        val jsonString = loadFixture("categories_response.json")
        val dto = json.decodeFromString<CategoriesResponseDto>(jsonString)

        // icon_url from backend maps to icon property via @SerialName("icon_url")
        assertEquals("https://cdn.example.com/icons/haircut.svg", dto.categories[0].icon)
        assertEquals("https://cdn.example.com/icons/nails.svg", dto.categories[1].icon)
    }

    // ========== Provider Search Endpoint Tests ==========

    @Test
    fun `provider_search_response parses correctly`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderSearchResponseDto>(jsonString)

        assertNotNull(dto.providers)
        assertEquals(2, dto.providers.size)
        assertEquals(2, dto.total)
        assertEquals(20, dto.limit)
        assertEquals(0, dto.offset)
    }

    @Test
    fun `provider dto fields map correctly via SerialName annotations`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderSearchResponseDto>(jsonString)

        val provider = dto.providers[0]
        // Backend display_name maps to businessName
        assertEquals("Beauty Studio Elite", provider.businessName)
        // Backend rating_cached maps to rating
        assertEquals(4.8, provider.rating)
        // Backend reviews_count maps to reviewCount
        assertEquals(127, provider.reviewCount)
        // Backend avatar_url maps to logoUrl
        assertEquals("https://cdn.example.com/avatars/provider1.jpg", provider.logoUrl)
        // Backend user_id maps to userId
        assertEquals("u1a2b3c4-d5e6-7890-abcd-ef1234567890", provider.userId)
        assertTrue(provider.isVerified)
        assertTrue(provider.isActive)
    }

    @Test
    fun `provider location parses correctly`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderSearchResponseDto>(jsonString)

        val provider = dto.providers[0]
        assertNotNull(provider.location)
        assertEquals(32.0853, provider.location!!.lat)
        assertEquals(34.7818, provider.location!!.lon)
    }

    @Test
    fun `provider with default address city parses correctly`() {
        val jsonString = loadFixture("provider_search_response.json")
        val dto = json.decodeFromString<ProviderSearchResponseDto>(jsonString)

        val provider = dto.providers[0]
        // address is not in backend response but ProviderDto has default ""
        assertEquals("", provider.address)
        assertEquals("", provider.city)
    }

    // ========== Provider Details Endpoint Tests ==========

    @Test
    fun `provider_detail_response parses correctly`() {
        val jsonString = loadFixture("provider_detail_response.json")
        val dto = json.decodeFromString<ProviderDetailsResponseDto>(jsonString)

        assertNotNull(dto.data)
    }

    @Test
    fun `provider detail dto includes working hours`() {
        val jsonString = loadFixture("provider_detail_response.json")
        val dto = json.decodeFromString<ProviderDetailsResponseDto>(jsonString)

        val provider = dto.data
        assertNotNull(provider.workingHours)
        assertNotNull(provider.workingHours!!.monday)
        assertEquals("09:00", provider.workingHours.monday!!.openTime)
        assertEquals("18:00", provider.workingHours.monday!!.closeTime)
        assertEquals("13:00", provider.workingHours.monday!!.breakStart)
        assertEquals("14:00", provider.workingHours.monday!!.breakEnd)
    }

    @Test
    fun `provider detail with categories parses correctly`() {
        val jsonString = loadFixture("provider_detail_response.json")
        val dto = json.decodeFromString<ProviderDetailsResponseDto>(jsonString)

        val provider = dto.data
        assertNotNull(provider.categories)
        assertEquals(1, provider.categories.size)
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", provider.categories[0].id)
    }

    @Test
    fun `provider detail photos array parses correctly`() {
        val jsonString = loadFixture("provider_detail_response.json")
        val dto = json.decodeFromString<ProviderDetailsResponseDto>(jsonString)

        val provider = dto.data
        assertTrue(provider.photos.isNotEmpty())
        assertEquals(3, provider.photos.size)
    }

    // ========== Favorites Endpoint Tests ==========

    @Test
    fun `favorites_response parses correctly`() {
        val jsonString = loadFixture("favorites_response.json")
        val dto = json.decodeFromString<FavoritesListResponseDto>(jsonString)

        assertNotNull(dto.favorites)
        assertEquals(1, dto.favorites.size)
        assertEquals(1, dto.total)
        assertEquals(20, dto.limit)
        assertEquals(0, dto.offset)
    }

    @Test
    fun `favorite dto fields populate correctly`() {
        val jsonString = loadFixture("favorites_response.json")
        val dto = json.decodeFromString<FavoritesListResponseDto>(jsonString)

        val favorite = dto.favorites[0]
        assertEquals("f1a2b3c4-d5e6-7890-abcd-ef1234567890", favorite.id)
        assertEquals("u1a2b3c4-d5e6-7890-abcd-ef1234567890", favorite.userId)
        assertEquals("p1a2b3c4-d5e6-7890-abcd-ef1234567890", favorite.providerId)
        assertNotNull(favorite.provider)
    }

    @Test
    fun `favorite provider nested dto fields populate correctly`() {
        val jsonString = loadFixture("favorites_response.json")
        val dto = json.decodeFromString<FavoritesListResponseDto>(jsonString)

        // FavoriteDto has its own nested ProviderDto with specific fields
        val provider = dto.favorites[0].provider
        assertEquals("p1a2b3c4-d5e6-7890-abcd-ef1234567890", provider.id)
        assertEquals("u1a2b3c4-d5e6-7890-abcd-ef1234567890", provider.userId)
        assertEquals("Beauty Studio Elite", provider.displayName)
        assertEquals("https://cdn.example.com/avatars/provider1.jpg", provider.avatarUrl)
        assertEquals(4.8, provider.ratingCached)
        assertEquals(127, provider.reviewsCount)
        assertEquals("123 Herzl Street, Tel Aviv", provider.address)
        assertEquals("Professional hair and beauty services with 10 years of experience", provider.bio)
        assertTrue(provider.isVerified)
        assertTrue(provider.isActive)
    }

    // ========== Service List Endpoint Tests ==========

    @Test
    fun `service_list_response parses correctly`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<ServiceListResponseDto>(jsonString)

        assertNotNull(dto.services)
        assertEquals(3, dto.services.size)
        assertEquals(3, dto.total)
    }

    @Test
    fun `service dto fields parse correctly with i18n title`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<ServiceListResponseDto>(jsonString)

        val service = dto.services[0]
        assertEquals("s1a2b3c4-d5e6-7890-abcd-ef1234567890", service.id)
        // Backend title is I18nStringSchema (Map<String, String>)
        assertTrue(service.titleMap.containsKey("ru"))
        assertTrue(service.titleMap.containsKey("he"))
        assertTrue(service.titleMap.containsKey("en"))
        // Backend base_price is int (cents), maps to priceInCents
        assertEquals(15000, service.priceInCents)
        // Backend duration_minutes maps to durationMinutes
        assertEquals(60, service.durationMinutes)
        assertTrue(service.isActive)
    }

    @Test
    fun `service name extraction uses i18n priority`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<ServiceListResponseDto>(jsonString)

        val service = dto.services[0]
        // ServiceDto.name getter uses priority: ru -> he -> en
        assertEquals("Women's Haircut", service.name)
    }

    @Test
    fun `service description extraction uses i18n priority`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<ServiceListResponseDto>(jsonString)

        val service = dto.services[0]
        assertNotNull(service.description)
        // Description also uses i18n priority
        assertEquals("Professional women's haircut including wash, cut, and styling", service.description)
    }

    @Test
    fun `multiple services parse correctly`() {
        val jsonString = loadFixture("service_list_response.json")
        val dto = json.decodeFromString<ServiceListResponseDto>(jsonString)

        assertEquals("Women's Haircut", dto.services[0].name)
        assertEquals("Men's Haircut", dto.services[1].name)
        assertEquals("Hair Coloring", dto.services[2].name)

        // Different prices in cents
        assertEquals(15000, dto.services[0].priceInCents)
        assertEquals(8000, dto.services[1].priceInCents)
        assertEquals(35000, dto.services[2].priceInCents)
    }
}