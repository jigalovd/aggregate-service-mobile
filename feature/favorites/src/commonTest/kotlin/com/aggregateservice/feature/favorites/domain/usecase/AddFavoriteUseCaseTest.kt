package com.aggregateservice.feature.favorites.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for AddFavoriteUseCase.
 */
class AddFavoriteUseCaseTest {

    private lateinit var addFavoriteUseCase: AddFavoriteUseCase
    private lateinit var mockRepository: MockFavoritesRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockFavoritesRepository()
        addFavoriteUseCase = AddFavoriteUseCase(mockRepository)
    }

    @Test
    fun `should add favorite successfully`() = runTest {
        // Arrange
        val providerId = "provider-123"
        mockRepository.addFavoriteResult = Result.success(Unit)

        // Act
        val result = addFavoriteUseCase(providerId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(providerId, mockRepository.lastAddedProviderId)
    }

    @Test
    fun `should return error when add fails`() = runTest {
        // Arrange
        val providerId = "provider-123"
        val expectedError = RuntimeException("Already in favorites")
        mockRepository.addFavoriteResult = Result.failure(expectedError)

        // Act
        val result = addFavoriteUseCase(providerId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `should call repository addFavorite with correct providerId`() = runTest {
        // Arrange
        val providerId = "provider-456"
        mockRepository.addFavoriteResult = Result.success(Unit)

        // Act
        addFavoriteUseCase(providerId)

        // Assert
        assertEquals(1, mockRepository.addFavoriteCallCount)
        assertEquals(providerId, mockRepository.lastAddedProviderId)
    }
}
