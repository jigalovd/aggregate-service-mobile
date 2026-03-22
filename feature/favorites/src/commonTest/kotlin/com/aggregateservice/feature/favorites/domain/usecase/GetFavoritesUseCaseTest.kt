package com.aggregateservice.feature.favorites.domain.usecase

import com.aggregateservice.feature.favorites.domain.model.Favorite
import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for GetFavoritesUseCase.
 */
class GetFavoritesUseCaseTest {

    private lateinit var getFavoritesUseCase: GetFavoritesUseCase
    private lateinit var mockRepository: MockFavoritesRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockFavoritesRepository()
        getFavoritesUseCase = GetFavoritesUseCase(mockRepository)
    }

    @Test
    fun `should return favorites on successful fetch`() = runTest {
        // Arrange
        val expectedFavorites = listOf(createTestFavorite())
        mockRepository.getFavoritesResult = Result.success(expectedFavorites)

        // Act
        val result = getFavoritesUseCase()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedFavorites, result.getOrNull())
    }

    @Test
    fun `should return empty list when no favorites`() = runTest {
        // Arrange
        mockRepository.getFavoritesResult = Result.success(emptyList())

        // Act
        val result = getFavoritesUseCase()

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `should return error when repository fails`() = runTest {
        // Arrange
        val expectedError = RuntimeException("Network error")
        mockRepository.getFavoritesResult = Result.failure(expectedError)

        // Act
        val result = getFavoritesUseCase()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `should call repository getFavorites`() = runTest {
        // Arrange
        mockRepository.getFavoritesResult = Result.success(emptyList())

        // Act
        getFavoritesUseCase()

        // Assert
        assertEquals(1, mockRepository.getFavoritesCallCount)
    }

    private fun createTestFavorite(
        providerId: String = "provider-123",
        businessName: String = "Test Salon",
    ) = Favorite(
        providerId = providerId,
        businessName = businessName,
        logoUrl = "https://example.com/logo.jpg",
        rating = 4.5,
        reviewCount = 10,
        address = "123 Main St",
        addedAt = Instant.parse("2024-01-01T00:00:00Z"),
    )
}

/**
 * Mock implementation of FavoritesRepository for testing.
 */
class MockFavoritesRepository : FavoritesRepository {
    var getFavoritesResult: Result<List<Favorite>> = Result.success(emptyList())
    var addFavoriteResult: Result<Unit> = Result.success(Unit)
    var removeFavoriteResult: Result<Unit> = Result.success(Unit)
    var isFavoriteResult: Result<Boolean> = Result.success(false)

    var getFavoritesCallCount = 0
    var addFavoriteCallCount = 0
    var removeFavoriteCallCount = 0
    var isFavoriteCallCount = 0

    var lastAddedProviderId: String? = null
    var lastRemovedProviderId: String? = null
    var lastCheckedProviderId: String? = null

    override suspend fun getFavorites(): Result<List<Favorite>> {
        getFavoritesCallCount++
        return getFavoritesResult
    }

    override suspend fun addFavorite(providerId: String): Result<Unit> {
        addFavoriteCallCount++
        lastAddedProviderId = providerId
        return addFavoriteResult
    }

    override suspend fun removeFavorite(providerId: String): Result<Unit> {
        removeFavoriteCallCount++
        lastRemovedProviderId = providerId
        return removeFavoriteResult
    }

    override suspend fun isFavorite(providerId: String): Result<Boolean> {
        isFavoriteCallCount++
        lastCheckedProviderId = providerId
        return isFavoriteResult
    }
}
