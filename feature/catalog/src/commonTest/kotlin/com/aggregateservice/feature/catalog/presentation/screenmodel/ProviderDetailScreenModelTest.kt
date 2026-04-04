package com.aggregateservice.feature.catalog.presentation.screenmodel

import com.aggregateservice.core.common.model.Location
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.DaySchedule
import com.aggregateservice.feature.catalog.domain.model.Price
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.domain.model.WorkingHours
import com.aggregateservice.feature.favorites.domain.model.Favorite
import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository
import com.aggregateservice.feature.favorites.domain.usecase.AddFavoriteUseCase
import com.aggregateservice.feature.favorites.domain.usecase.IsFavoriteUseCase
import com.aggregateservice.feature.favorites.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for ProviderDetailScreenModel using functional mocks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProviderDetailScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private var getProviderByIdBehavior: suspend (String) -> Result<Provider> =
        { Result.failure(AppError.NotFound) }

    private var getProviderServicesBehavior: suspend (String, String?) -> Result<List<Service>> =
        { _, _ -> Result.success(emptyList()) }

    private var getCategoriesBehavior: suspend (String?) -> Result<List<Category>> =
        { Result.success(emptyList()) }

    private var providerInvokeCount = 0
    private var servicesInvokeCount = 0
    private var lastCategoryId: String? = null

    private lateinit var mockFavoritesRepository: MockFavoritesRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        providerInvokeCount = 0
        servicesInvokeCount = 0
        lastCategoryId = null
        mockFavoritesRepository = MockFavoritesRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createScreenModel(): ProviderDetailScreenModel {
        val repository =
            object : com.aggregateservice.feature.catalog.domain.repository.CatalogRepository {
                override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> =
                    Result.success(SearchResult.empty())

                override suspend fun getProviderById(providerId: String): Result<Provider> {
                    providerInvokeCount++
                    return getProviderByIdBehavior(providerId)
                }

                override suspend fun getCategories(parentId: String?): Result<List<Category>> =
                    getCategoriesBehavior(parentId)

                override suspend fun getCategoryById(categoryId: String): Result<Category> =
                    Result.failure(AppError.NotFound)

                override suspend fun getProviderServices(providerId: String, categoryId: String?): Result<List<Service>> {
                    servicesInvokeCount++
                    lastCategoryId = categoryId
                    return getProviderServicesBehavior(providerId, categoryId)
                }

                override suspend fun getServiceById(serviceId: String): Result<Service> =
                    Result.failure(AppError.NotFound)

                override suspend fun searchServices(query: String, filters: SearchFilters): Result<SearchResult<Service>> =
                    Result.success(SearchResult.empty())
            }

        return ProviderDetailScreenModel(
            getProviderDetailsUseCase =
                com.aggregateservice.feature.catalog.domain.usecase
                    .GetProviderDetailsUseCase(repository),
            getProviderServicesUseCase =
                com.aggregateservice.feature.catalog.domain.usecase
                    .GetProviderServicesUseCase(repository),
            isFavoriteUseCase = IsFavoriteUseCase(mockFavoritesRepository),
            addFavoriteUseCase = AddFavoriteUseCase(mockFavoritesRepository),
            removeFavoriteUseCase = RemoveFavoriteUseCase(mockFavoritesRepository),
        )
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial state should be loading`() {
        getProviderByIdBehavior = { Result.failure(AppError.NotFound) }

        val screenModel = createScreenModel()

        val state = screenModel.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.provider)
    }

    // ========== Initialize Tests ==========

    @Test
    fun `initialize should load provider details and services`() =
        runTest {
            val provider = createTestProvider()
            val services = createTestServices(3)

            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(services) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertEquals("provider-123", state.provider?.id)
            assertEquals(3, state.services.size)
            assertFalse(state.isLoading)
        }

    @Test
    fun `initialize should not reload if same providerId`() =
        runTest {
            val provider = createTestProvider()
            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            val firstInvokeCount = providerInvokeCount

            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(firstInvokeCount, providerInvokeCount)
        }

    @Test
    fun `initialize should set error on failure`() =
        runTest {
            getProviderByIdBehavior = { Result.failure(AppError.NetworkError(404, "Not Found")) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertTrue(state.error is AppError.NetworkError)
            assertFalse(state.isLoading)
        }

    // ========== Category Selection Tests ==========

    @Test
    fun `onCategorySelected should update selectedCategoryId and reload services`() =
        runTest {
            val provider = createTestProvider()
            val services = createTestServices(3)

            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(services) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            val filteredServices = listOf(createTestService("service-1", "cat-1"))
            getProviderServicesBehavior = { _, _ -> Result.success(filteredServices) }

            screenModel.onCategorySelected("cat-1")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertEquals("cat-1", state.selectedCategoryId)
            assertEquals("cat-1", lastCategoryId)
        }

    @Test
    fun `onCategorySelected with null should clear filter`() =
        runTest {
            val provider = createTestProvider()
            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(createTestServices(3)) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.onCategorySelected(null)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertNull(state.selectedCategoryId)
            assertNull(lastCategoryId)
        }

    // ========== Favorite Toggle Tests ==========

    @Test
    fun `initialize should load isFavorite status`() =
        runTest {
            mockFavoritesRepository.isFavoriteResult = Result.success(true)
            getProviderByIdBehavior = { Result.success(createTestProvider()) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(screenModel.uiState.value.isFavorite)
            assertEquals(1, mockFavoritesRepository.isFavoriteCallCount)
        }

    @Test
    fun `onFavoriteToggle should call addFavoriteUseCase when not favorite`() =
        runTest {
            mockFavoritesRepository.isFavoriteResult = Result.success(false)
            mockFavoritesRepository.addFavoriteResult = Result.success(Unit)
            getProviderByIdBehavior = { Result.success(createTestProvider()) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(screenModel.uiState.value.isFavorite)

            screenModel.onFavoriteToggle()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, mockFavoritesRepository.addFavoriteCallCount)
            assertTrue(screenModel.uiState.value.isFavorite)
        }

    @Test
    fun `onFavoriteToggle should call removeFavoriteUseCase when favorite`() =
        runTest {
            mockFavoritesRepository.isFavoriteResult = Result.success(true)
            mockFavoritesRepository.removeFavoriteResult = Result.success(Unit)
            getProviderByIdBehavior = { Result.success(createTestProvider()) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(screenModel.uiState.value.isFavorite)

            screenModel.onFavoriteToggle()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, mockFavoritesRepository.removeFavoriteCallCount)
            assertFalse(screenModel.uiState.value.isFavorite)
        }

    @Test
    fun `onFavoriteToggle should not flip state on API error`() =
        runTest {
            mockFavoritesRepository.isFavoriteResult = Result.success(false)
            mockFavoritesRepository.addFavoriteResult = Result.failure(RuntimeException("API error"))
            getProviderByIdBehavior = { Result.success(createTestProvider()) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(screenModel.uiState.value.isFavorite)

            screenModel.onFavoriteToggle()
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(screenModel.uiState.value.isFavorite)
            assertNotNull(screenModel.uiState.value.error)
        }

    // ========== Refresh Tests ==========

    @Test
    fun `onRefresh should reload provider and services`() =
        runTest {
            val provider = createTestProvider()
            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            val initialProviderCount = providerInvokeCount
            val initialServicesCount = servicesInvokeCount

            screenModel.onRefresh()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(providerInvokeCount > initialProviderCount)
            assertTrue(servicesInvokeCount > initialServicesCount)
        }

    // ========== Retry Tests ==========

    @Test
    fun `retry should reload after error`() =
        runTest {
            getProviderByIdBehavior = { Result.failure(AppError.NetworkError(500, "Error")) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(screenModel.uiState.value.error != null)

            // Now succeed
            getProviderByIdBehavior = { Result.success(createTestProvider()) }

            screenModel.retry()
            testDispatcher.scheduler.advanceUntilIdle()

            assertNull(screenModel.uiState.value.error)
            assertFalse(screenModel.uiState.value.isLoading)
        }

    // ========== Clear Error Tests ==========

    @Test
    fun `clearError should remove error from state`() =
        runTest {
            getProviderByIdBehavior = { Result.failure(AppError.NetworkError(500, "Error")) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(screenModel.uiState.value.error != null)

            screenModel.clearError()

            assertNull(screenModel.uiState.value.error)
        }

    // ========== UI State Computed Properties Tests ==========

    @Test
    fun `isLoaded should return true when provider is loaded`() =
        runTest {
            val provider = createTestProvider()
            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(screenModel.uiState.value.isLoaded)
        }

    @Test
    fun `filteredServices should filter by selected category`() =
        runTest {
            val provider = createTestProvider()
            val services =
                listOf(
                    createTestService("s1", "cat-1"),
                    createTestService("s2", "cat-2"),
                    createTestService("s3", "cat-1"),
                )

            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(services) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.onCategorySelected("cat-1")

            val filteredServices = screenModel.uiState.value.filteredServices
            assertEquals(2, filteredServices.size)
            assertTrue(filteredServices.all { it.categoryId == "cat-1" })
        }

    @Test
    fun `filteredServices should return all services when no category selected`() =
        runTest {
            val provider = createTestProvider()
            val services = createTestServices(5)

            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(services) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            val filteredServices = screenModel.uiState.value.filteredServices
            assertEquals(5, filteredServices.size)
        }

    @Test
    fun `isOpenNow should reflect provider working hours`() =
        runTest {
            val workingHours =
                WorkingHours(
                    monday = DaySchedule(openTime = "09:00", closeTime = "18:00"),
                )
            val provider = createTestProvider(workingHours = workingHours)
            getProviderByIdBehavior = { Result.success(provider) }
            getProviderServicesBehavior = { _, _ -> Result.success(emptyList()) }

            val screenModel = createScreenModel()
            screenModel.initialize("provider-123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(screenModel.uiState.value.isOpenNow)
        }

    // ========== Helper Methods ==========

    private fun createTestProvider(
        id: String = "provider-123",
        workingHours: WorkingHours = WorkingHours(),
    ): Provider {
        return Provider(
            id = id,
            userId = "user-123",
            businessName = "Test Salon",
            description = "A test salon",
            rating = 4.8,
            reviewCount = 150,
            location =
                Location(
                    latitude = 32.0853,
                    longitude = 34.7818,
                    address = "Dizengoff 100",
                    city = "Tel Aviv",
                ),
            workingHours = workingHours,
            isVerified = true,
            createdAt = Clock.System.now(),
            categories = listOf(Category(id = "cat-1", name = "Haircut")),
        )
    }

    private fun createTestServices(count: Int): List<Service> {
        return (1..count).map { index ->
            createTestService("service-$index", "cat-${(index % 2) + 1}")
        }
    }

    private fun createTestService(id: String, categoryId: String): Service {
        return Service(
            id = id,
            providerId = "provider-123",
            categoryId = categoryId,
            categoryName = "Category $categoryId",
            name = "Service $id",
            description = "Description for $id",
            price = Price(amount = 100.0, currency = "ILS"),
            durationMinutes = 30,
            createdAt = Clock.System.now(),
        )
    }
}

/**
 * Mock implementation of FavoritesRepository for ProviderDetailScreenModel testing.
 */
private class MockFavoritesRepository : FavoritesRepository {
    var getFavoritesResult: Result<List<Favorite>> = Result.success(emptyList())
    var addFavoriteResult: Result<Unit> = Result.success(Unit)
    var removeFavoriteResult: Result<Unit> = Result.success(Unit)
    var isFavoriteResult: Result<Boolean> = Result.success(false)
    var addFavoriteCallCount = 0
    var removeFavoriteCallCount = 0
    var isFavoriteCallCount = 0

    override suspend fun getFavorites(): Result<List<Favorite>> = getFavoritesResult

    override suspend fun addFavorite(providerId: String): Result<Unit> {
        addFavoriteCallCount++
        return addFavoriteResult
    }

    override suspend fun removeFavorite(providerId: String): Result<Unit> {
        removeFavoriteCallCount++
        return removeFavoriteResult
    }

    override suspend fun isFavorite(providerId: String): Result<Boolean> {
        isFavoriteCallCount++
        return isFavoriteResult
    }
}
