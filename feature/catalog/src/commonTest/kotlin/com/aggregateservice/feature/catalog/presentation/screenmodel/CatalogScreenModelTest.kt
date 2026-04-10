package com.aggregateservice.feature.catalog.presentation.screenmodel

import co.touchlab.kermit.Logger
import com.aggregateservice.core.common.model.Location
import com.aggregateservice.core.location.LocationProvider
import com.aggregateservice.core.location.LocationProviderFactory
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.presentation.cache.LocationCache
import com.aggregateservice.feature.catalog.domain.repository.LocationRepository
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.domain.model.WorkingHours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for CatalogScreenModel using functional mocks.
 *
 * Note: LocationProvider is an expect class with platform-specific implementations.
 * This test uses LocationProviderFactory to create instances.
 */

private class FakeLocationRepository : LocationRepository {
    private var saved: Location? = null
    override suspend fun getSavedLocation(): Location? = saved
    override suspend fun saveLocation(location: Location) { saved = location }
    override suspend fun clearSavedLocation() { saved = null }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private var searchProvidersBehavior: suspend (SearchFilters) -> Result<SearchResult<Provider>> =
        { Result.success(SearchResult.empty()) }

    private var getCategoriesBehavior: suspend (String?) -> Result<List<Category>> =
        { Result.success(emptyList()) }

    private var searchInvokeCount = 0
    private var lastSearchFilters: SearchFilters? = null

    /**
     * LocationProvider instance created via factory.
     * On iOS: returns Granted and default Haifa location
     * On Android: requires setActivity() to be called first
     */
    private lateinit var locationProvider: LocationProvider
    private lateinit var locationCache: LocationCache

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchInvokeCount = 0
        lastSearchFilters = null

        // Create LocationProvider via factory
        locationProvider = LocationProviderFactory.create()
        locationCache = LocationCache(FakeLocationRepository())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createScreenModel(): CatalogScreenModel {
        return CatalogScreenModel(
            searchProvidersUseCase = createSearchProvidersUseCase(),
            getCategoriesUseCase = createGetCategoriesUseCase(),
            locationProvider = locationProvider,
            locationCache = locationCache,
            logger = Logger.withTag("Test"),
        )
    }

    private fun createSearchProvidersUseCase(): com.aggregateservice.feature.catalog.domain.usecase.SearchProvidersUseCase {
        return com.aggregateservice.feature.catalog.domain.usecase.SearchProvidersUseCase(
            repository =
                object : com.aggregateservice.feature.catalog.domain.repository.CatalogRepository {
                    override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
                        searchInvokeCount++
                        lastSearchFilters = filters
                        return searchProvidersBehavior(filters)
                    }

                    override suspend fun getProviderById(providerId: String): Result<Provider> =
                        Result.failure(AppError.NotFound)

                    override suspend fun getCategories(parentId: String?) = getCategoriesBehavior(parentId)

                    override suspend fun getCategoryById(categoryId: String): Result<Category> =
                        Result.failure(AppError.NotFound)

                    override suspend fun getProviderServices(providerId: String, categoryId: String?): Result<List<Service>> =
                        Result.success(emptyList())

                    override suspend fun getServiceById(serviceId: String): Result<Service> =
                        Result.failure(AppError.NotFound)

                    override suspend fun searchServices(query: String, filters: SearchFilters): Result<SearchResult<Service>> =
                        Result.success(SearchResult.empty())

                    override fun invalidateCache() { /* no-op for tests */ }
                },
        )
    }

    private fun createGetCategoriesUseCase(): com.aggregateservice.feature.catalog.domain.usecase.GetCategoriesUseCase {
        return com.aggregateservice.feature.catalog.domain.usecase.GetCategoriesUseCase(
            repository =
                object : com.aggregateservice.feature.catalog.domain.repository.CatalogRepository {
                    override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> =
                        Result.success(SearchResult.empty())

                    override suspend fun getProviderById(providerId: String): Result<Provider> =
                        Result.failure(AppError.NotFound)

                    override suspend fun getCategories(parentId: String?) = getCategoriesBehavior(parentId)

                    override suspend fun getCategoryById(categoryId: String): Result<Category> =
                        Result.failure(AppError.NotFound)

                    override suspend fun getProviderServices(providerId: String, categoryId: String?): Result<List<Service>> =
                        Result.success(emptyList())

                    override suspend fun getServiceById(serviceId: String): Result<Service> =
                        Result.failure(AppError.NotFound)

                    override suspend fun searchServices(query: String, filters: SearchFilters): Result<SearchResult<Service>> =
                        Result.success(SearchResult.empty())

                    override fun invalidateCache() { /* no-op for tests */ }
                },
        )
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial state should have default values`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("", state.searchQuery)
            assertNull(state.selectedCategory)
            assertNull(state.error)
        }

    @Test
    fun `init should load providers and categories`() =
        runTest {
            val providers = createTestProviders(3)
            val categories = createTestCategories(2)

            searchProvidersBehavior = { Result.success(SearchResult(items = providers, totalCount = 3, totalPages = 1, currentPage = 1)) }
            getCategoriesBehavior = { Result.success(categories) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertEquals(3, state.providers.size)
            assertEquals(2, state.categories.size)
        }

    // ========== Search Tests ==========

    @Test
    fun `searchProviders should update providers on success`() =
        runTest {
            val providers = createTestProviders(5)
            searchProvidersBehavior = { Result.success(SearchResult(items = providers, totalCount = 5, totalPages = 1, currentPage = 1)) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.searchProviders()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertEquals(5, state.providers.size)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun `searchProviders should set error on failure`() =
        runTest {
            searchProvidersBehavior = { Result.failure(AppError.NetworkError(503, "Service Unavailable")) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.searchProviders()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertTrue(state.error is AppError.NetworkError)
            assertFalse(state.isLoading)
        }

    @Test
    fun `searchProviders should set loading state during fetch`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.searchProviders()

            assertTrue(screenModel.uiState.value.isLoading)
        }

    // ========== Load More Tests ==========

    @Test
    fun `loadMore should append providers to existing list`() =
        runTest {
            val initialProviders = createTestProviders(3, startId = 1)
            val additionalProviders = createTestProviders(2, startId = 4)

            searchProvidersBehavior = { Result.success(SearchResult(items = initialProviders, totalCount = 5, totalPages = 2, currentPage = 1)) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            // Setup for loadMore
            searchProvidersBehavior = { Result.success(SearchResult(items = additionalProviders, totalCount = 5, totalPages = 2, currentPage = 2)) }

            screenModel.loadMore()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertEquals(5, state.providers.size)
            assertFalse(state.isLoadingMore)
        }

    @Test
    fun `loadMore should not load if canLoadMore is false`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult(items = createTestProviders(3), totalCount = 3, totalPages = 1, currentPage = 1)) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.loadMore()
            testDispatcher.scheduler.advanceUntilIdle()

            // Should not trigger another search since hasMore is false
            assertEquals(1, searchInvokeCount)
        }

    // ========== Category Selection Tests ==========

    @Test
    fun `onCategorySelected should update selectedCategory and trigger search`() =
        runTest {
            val category = Category(id = "cat-1", name = "Haircut")
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(listOf(category)) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            searchProvidersBehavior = { Result.success(SearchResult.empty()) }

            screenModel.onCategorySelected(category)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertEquals("cat-1", state.selectedCategory?.id)
            assertTrue(lastSearchFilters?.categoryIds?.contains("cat-1") == true)
        }

    @Test
    fun `onCategorySelected with null should clear category filter`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.onCategorySelected(null)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertNull(state.selectedCategory)
            assertTrue(state.filters.categoryIds.isEmpty())
        }

    // ========== Search Query Tests ==========

    @Test
    fun `onSearchQueryChanged should update searchQuery`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.onSearchQueryChanged("haircut")

            assertEquals("haircut", screenModel.uiState.value.searchQuery)
        }

    // ========== Filter Tests ==========

    @Test
    fun `onMinRatingChanged should update filters and trigger search`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.onMinRatingChanged(4.5)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(4.5, screenModel.uiState.value.filters.minRating)
        }

    @Test
    fun `onSortByChanged should update sort field and trigger search`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.onSortByChanged(SearchFilters.SortBy.REVIEW_COUNT)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(SearchFilters.SortBy.REVIEW_COUNT, screenModel.uiState.value.filters.sortBy)
        }

    @Test
    fun `onClearFilters should reset all filters`() =
        runTest {
            searchProvidersBehavior = { Result.success(SearchResult.empty()) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            // Set some filters
            screenModel.onCategorySelected(Category(id = "cat-1", name = "Test"))
            screenModel.onMinRatingChanged(4.5)
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.onClearFilters()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = screenModel.uiState.value
            assertNull(state.selectedCategory)
            assertEquals("", state.searchQuery)
            assertNull(state.filters.minRating)
        }

    // ========== Error Handling Tests ==========

    @Test
    fun `clearError should remove error from state`() =
        runTest {
            searchProvidersBehavior = { Result.failure(AppError.NetworkError(500, "Error")) }
            getCategoriesBehavior = { Result.success(emptyList()) }

            val screenModel = createScreenModel()
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.searchProviders()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(screenModel.uiState.value.error != null)

            screenModel.clearError()

            assertNull(screenModel.uiState.value.error)
        }

    // ========== Helper Methods ==========

    private fun createTestProviders(count: Int, startId: Int = 1): List<Provider> {
        return (startId..startId + count - 1).map { index ->
            Provider(
                id = "provider-$index",
                userId = "user-$index",
                businessName = "Test Provider $index",
                rating = 4.0 + (index * 0.1),
                reviewCount = index * 10,
                location =
                    Location(
                        latitude = 32.0853 + (index * 0.01),
                        longitude = 34.7818 + (index * 0.01),
                        address = "Address $index",
                        city = "Tel Aviv",
                    ),
                workingHours = WorkingHours(),
                createdAt = Clock.System.now(),
            )
        }
    }

    private fun createTestCategories(count: Int): List<Category> {
        return (1..count).map { index ->
            Category(id = "cat-$index", name = "Category $index")
        }
    }
}
