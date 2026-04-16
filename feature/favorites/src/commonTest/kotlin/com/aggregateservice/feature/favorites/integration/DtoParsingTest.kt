package com.aggregateservice.feature.favorites.integration

import com.aggregateservice.feature.favorites.data.dto.FavoriteCheckResponseDto
import com.aggregateservice.feature.favorites.data.dto.FavoritesListResponseDto
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for favorites API DTO parsing.
 *
 * Verifies that real API response structures (stored as fixtures)
 * parse correctly into favorites DTOs, catching @SerialName drift early.
 *
 * Fixture files are stored in: src/commonTest/resources/fixtures/
 */
class DtoParsingTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    private fun loadFixture(fixtureName: String): String {
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

        val classLoader = this::class.java.classLoader ?: ClassLoader.getSystemClassLoader()
        val resourcePath = "fixtures/$fixtureName"
        val resource =
            classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Fixture not found: $fixtureName (searched: $possiblePaths)")
        return resource.bufferedReader().use { it.readText() }
    }

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

    @Test
    fun `favorite check response parses correctly when not favorite`() {
        val jsonString = loadFixture("favorite_check_response.json")
        val dto = json.decodeFromString<FavoriteCheckResponseDto>(jsonString)

        assertFalse(dto.isFavorite)
        assertNull(dto.favoriteId)
    }

    @Test
    fun `favorite check response parses correctly when favorite`() {
        val jsonString = """{"is_favorite": true, "favorite_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"}"""
        val dto = json.decodeFromString<FavoriteCheckResponseDto>(jsonString)

        assertTrue(dto.isFavorite)
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", dto.favoriteId)
    }
}
