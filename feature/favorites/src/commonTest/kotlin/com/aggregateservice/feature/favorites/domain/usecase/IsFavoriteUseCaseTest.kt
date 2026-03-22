package com.aggregateservice.feature.favorites.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for IsFavoriteUseCase.
 */
class IsFavoriteUseCaseTest {

    private lateinit var isFavoriteUseCase: IsFavoriteUseCase
    private lateinit var mockRepository: MockFavoritesRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockFavoritesRepository()
        isFavoriteUseCase = IsFavoriteUseCase(mockRepository)
    }

    @Test
    fun `should return true when provider is favorite`() = runTest {
        // Arrange
        val providerId = "provider-123"
        mockRepository.isFavoriteResult = Result.success(true)

        // Act
        val result = isFavoriteUseCase(providerId)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `should return false when provider is not favorite`() = runTest {
        // Arrange
        val providerId = "provider-456"
        mockRepository.isFavoriteResult = Result.success(false)

        // Act
        val result = isFavoriteUseCase(providerId)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!! == false)
    }

    @Test
    fun `should return error when check fails`() = runTest {
        // Arrange
        val providerId = "provider-123"
        val expectedError = RuntimeException("Network error")
        mockRepository.isFavoriteResult = Result.failure(expectedError)

        // Act
        val result = isFavoriteUseCase(providerId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `should call repository isFavorite with correct providerId`() = runTest {
        // Arrange
        val providerId = "provider-789"
        mockRepository.isFavoriteResult = Result.success(false)

        // Act
        isFavoriteUseCase(providerId)

        // Assert
        assertEquals(1, mockRepository.isFavoriteCallCount)
        assertEquals(providerId, mockRepository.lastCheckedProviderId)
    }
}
