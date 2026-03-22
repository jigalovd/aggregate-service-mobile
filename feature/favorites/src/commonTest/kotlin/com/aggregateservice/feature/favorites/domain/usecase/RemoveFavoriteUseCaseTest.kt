package com.aggregateservice.feature.favorites.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for RemoveFavoriteUseCase.
 */
class RemoveFavoriteUseCaseTest {

    private lateinit var removeFavoriteUseCase: RemoveFavoriteUseCase
    private lateinit var mockRepository: MockFavoritesRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockFavoritesRepository()
        removeFavoriteUseCase = RemoveFavoriteUseCase(mockRepository)
    }

    @Test
    fun `should remove favorite successfully`() = runTest {
        // Arrange
        val providerId = "provider-123"
        mockRepository.removeFavoriteResult = Result.success(Unit)

        // Act
        val result = removeFavoriteUseCase(providerId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(providerId, mockRepository.lastRemovedProviderId)
    }

    @Test
    fun `should return error when remove fails`() = runTest {
        // Arrange
        val providerId = "provider-123"
        val expectedError = RuntimeException("Not in favorites")
        mockRepository.removeFavoriteResult = Result.failure(expectedError)

        // Act
        val result = removeFavoriteUseCase(providerId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `should call repository removeFavorite with correct providerId`() = runTest {
        // Arrange
        val providerId = "provider-789"
        mockRepository.removeFavoriteResult = Result.success(Unit)

        // Act
        removeFavoriteUseCase(providerId)

        // Assert
        assertEquals(1, mockRepository.removeFavoriteCallCount)
        assertEquals(providerId, mockRepository.lastRemovedProviderId)
    }
}
