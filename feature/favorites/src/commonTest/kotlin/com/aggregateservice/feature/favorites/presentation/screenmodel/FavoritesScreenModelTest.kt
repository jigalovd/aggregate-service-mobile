package com.aggregateservice.feature.favorites.presentation.screenmodel

import com.aggregateservice.feature.favorites.domain.model.Favorite
import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository
import com.aggregateservice.feature.favorites.domain.usecase.GetFavoritesUseCase
import com.aggregateservice.feature.favorites.domain.usecase.RemoveFavoriteUseCase
import com.aggregateservice.feature.favorites.presentation.model.FavoritesUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for FavoritesScreenModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockFavoritesRepositoryForScreenModel
    private lateinit var getFavoritesUseCase: GetFavoritesUseCase
    private lateinit var removeFavoriteUseCase: RemoveFavoriteUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockFavoritesRepositoryForScreenModel()
        getFavoritesUseCase = GetFavoritesUseCase(mockRepository)
        removeFavoriteUseCase = RemoveFavoriteUseCase(mockRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() {
        // Arrange
        mockRepository.getFavoritesResult = Result.success(emptyList())

        // Act
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )

        // Assert
        assertTrue(screenModel.uiState.value.isLoading)
    }

    @Test
    fun `loadFavorites should update state with favorites`() = runTest {
        // Arrange
        val expectedFavorites = listOf(createTestFavorite())
        mockRepository.getFavoritesResult = Result.success(expectedFavorites)

        // Act
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(expectedFavorites, state.favorites)
        assertTrue(state.hasFavorites)
    }

    @Test
    fun `loadFavorites should set error state when loading fails`() = runTest {
        // Arrange
        val expectedError = RuntimeException("Network error")
        mockRepository.getFavoritesResult = Result.failure(expectedError)

        // Act
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.error != null)
    }

    @Test
    fun `loadFavorites should show empty state when no favorites`() = runTest {
        // Arrange
        mockRepository.getFavoritesResult = Result.success(emptyList())

        // Act
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasFavorites)
        assertEquals(0, state.favoritesCount)
    }

    @Test
    fun `confirmRemove should set favoriteToRemove`() = runTest {
        // Arrange
        val favorite = createTestFavorite()
        mockRepository.getFavoritesResult = Result.success(listOf(favorite))
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        screenModel.confirmRemove(favorite)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertTrue(state.showRemoveDialog)
        assertEquals(favorite, state.favoriteToRemove)
    }

    @Test
    fun `dismissRemoveDialog should clear favoriteToRemove`() = runTest {
        // Arrange
        val favorite = createTestFavorite()
        mockRepository.getFavoritesResult = Result.success(listOf(favorite))
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()
        screenModel.confirmRemove(favorite)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        screenModel.dismissRemoveDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertFalse(state.showRemoveDialog)
        assertNull(state.favoriteToRemove)
    }

    @Test
    fun `removeFavorite should remove from list on success`() = runTest {
        // Arrange
        val favorite1 = createTestFavorite(providerId = "provider-1")
        val favorite2 = createTestFavorite(providerId = "provider-2")
        mockRepository.getFavoritesResult = Result.success(listOf(favorite1, favorite2))
        mockRepository.removeFavoriteResult = Result.success(Unit)
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()
        screenModel.confirmRemove(favorite1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        screenModel.removeFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertEquals(1, state.favorites.size)
        assertEquals("provider-2", state.favorites.first().providerId)
        assertFalse(state.showRemoveDialog)
    }

    @Test
    fun `removeFavorite should set error on failure`() = runTest {
        // Arrange
        val favorite = createTestFavorite()
        mockRepository.getFavoritesResult = Result.success(listOf(favorite))
        mockRepository.removeFavoriteResult = Result.failure(RuntimeException("Remove failed"))
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()
        screenModel.confirmRemove(favorite)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        screenModel.removeFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertTrue(state.error != null)
        assertFalse(state.showRemoveDialog)
        assertEquals(1, state.favorites.size) // Favorite not removed
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Arrange
        val expectedError = RuntimeException("Network error")
        mockRepository.getFavoritesResult = Result.failure(expectedError)
        val screenModel = FavoritesScreenModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
        )
        screenModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        screenModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertNull(screenModel.uiState.value.error)
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
 * Mock implementation of FavoritesRepository for ScreenModel testing.
 */
class MockFavoritesRepositoryForScreenModel : FavoritesRepository {
    var getFavoritesResult: Result<List<Favorite>> = Result.success(emptyList())
    var removeFavoriteResult: Result<Unit> = Result.success(Unit)

    override suspend fun getFavorites(): Result<List<Favorite>> {
        return getFavoritesResult
    }

    override suspend fun addFavorite(providerId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun removeFavorite(providerId: String): Result<Unit> {
        return removeFavoriteResult
    }

    override suspend fun isFavorite(providerId: String): Result<Boolean> {
        return Result.success(false)
    }
}
