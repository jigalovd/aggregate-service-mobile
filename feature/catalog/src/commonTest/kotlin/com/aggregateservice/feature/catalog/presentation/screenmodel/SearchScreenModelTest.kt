package com.aggregateservice.feature.catalog.presentation.screenmodel

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Location
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.domain.model.WorkingHours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import kotlin.test.assertTrue
import kotlin.test.assertNull

/**
 * Tests for SearchScreenModel using functional mocks.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private var searchProvidersBehavior: suspend (SearchFilters) -> Result<SearchResult<Provider>> =
        { Result.success(SearchResult.empty()) }

    private var getCategoriesBehavior: suspend (String?) -> Result<List<Category>> =
        { Result.success(emptyList()) }

    private var searchInvokeCount = 0

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchInvokeCount = 0
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createScreenModel(): SearchScreenModel {
        val repository = object : com.aggregateservice.feature.catalog.domain.repository.CatalogRepository {
            override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
                searchInvokeCount++
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
        }

        return SearchScreenModel(
            searchProvidersUseCase = com.aggregateservice.feature.catalog.domain.usecase.SearchProvidersUseCase(repository),
            getCategoriesUseCase = com.aggregateservice.feature.catalog.domain.usecase.GetCategoriesUseCase(repository),
        )
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial state should have default values`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.providers.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init should load categories`() = runTest {
        val categories = createTestCategories(3)
        getCategoriesBehavior = { Result.success(categories) }
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals(3, state.categories.size)
    }

    // ========== Search Query Tests ==========

    @Test
    fun `onSearchQueryChanged should update searchQuery`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onSearchQueryChanged("haircut")

        assertEquals("haircut", screenModel.uiState.value.searchQuery)
    }

    @Test
    fun `onSearchQueryChanged with blank query should clear results`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onSearchQueryChanged("haircut")
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onSearchQueryChanged("")

        val state = screenModel.uiState.value
        assertTrue(state.providers.isEmpty())
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `onSearchSubmit should trigger search and add to recent`() = runTest {
        val providers = createTestProviders(2)
        searchProvidersBehavior = { Result.success(SearchResult(items = providers, totalCount = 2, totalPages = 1, currentPage = 1)) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onSearchQueryChanged("haircut")
        screenModel.onSearchSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals(2, state.providers.size)
        assertTrue(state.recentSearches.contains("haircut"))
    }

    // ========== Category Filter Tests ==========

    @Test
    fun `onCategoryToggle should add category when not selected`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onCategoryToggle("cat-1")

        assertTrue("cat-1" in screenModel.uiState.value.selectedCategories)
    }

    @Test
    fun `onCategoryToggle should remove category when already selected`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onCategoryToggle("cat-1")
        screenModel.onCategoryToggle("cat-1")

        assertFalse("cat-1" in screenModel.uiState.value.selectedCategories)
    }

    // ========== Filter Tests ==========

    @Test
    fun `onMinRatingChanged should update filters`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onMinRatingChanged(4.5)

        assertEquals(4.5, screenModel.uiState.value.filters.minRating)
    }

    @Test
    fun `onClearFilters should reset all filters`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onCategoryToggle("cat-1")
        screenModel.onMinRatingChanged(4.5)
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onClearFilters()

        val state = screenModel.uiState.value
        assertTrue(state.selectedCategories.isEmpty())
        assertNull(state.filters.minRating)
    }

    // ========== Filter Sheet Tests ==========

    @Test
    fun `onFilterSheetToggle should toggle filter sheet state`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(screenModel.uiState.value.isFilterSheetOpen)

        screenModel.onFilterSheetToggle()

        assertTrue(screenModel.uiState.value.isFilterSheetOpen)

        screenModel.onFilterSheetToggle()

        assertFalse(screenModel.uiState.value.isFilterSheetOpen)
    }

    // ========== Recent Searches Tests ==========

    @Test
    fun `onRecentSearchClick should set query and trigger search`() = runTest {
        val providers = createTestProviders(1)
        searchProvidersBehavior = { Result.success(SearchResult(items = providers, totalCount = 1, totalPages = 1, currentPage = 1)) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onRecentSearchClick("haircut")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("haircut", screenModel.uiState.value.searchQuery)
        assertEquals(1, screenModel.uiState.value.providers.size)
    }

    @Test
    fun `recent searches should be limited to 5 items`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        repeat(7) { index ->
            screenModel.onSearchQueryChanged("search-$index")
            screenModel.onSearchSubmit()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        val recentSearches = screenModel.uiState.value.recentSearches
        assertEquals(5, recentSearches.size)
    }

    // ========== Load More Tests ==========

    @Test
    fun `loadMore should not load if canLoadMore is false`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult(items = createTestProviders(2), totalCount = 2, totalPages = 1, currentPage = 1)) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        screenModel.onSearchQueryChanged("test")
        screenModel.onSearchSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val initialInvokeCount = searchInvokeCount

        screenModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialInvokeCount, searchInvokeCount)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `search should set error on failure`() = runTest {
        searchProvidersBehavior = { Result.failure(AppError.NetworkError(500, "Server Error")) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onSearchQueryChanged("test")
        screenModel.onSearchSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.uiState.value.error is AppError.NetworkError)
    }

    @Test
    fun `clearError should remove error from state`() = runTest {
        searchProvidersBehavior = { Result.failure(AppError.NetworkError(500, "Error")) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onSearchQueryChanged("test")
        screenModel.onSearchSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.uiState.value.error != null)

        screenModel.clearError()

        assertNull(screenModel.uiState.value.error)
    }

    // ========== UI State Computed Properties Tests ==========

    @Test
    fun `hasActiveFilters should return true when categories selected`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onCategoryToggle("cat-1")

        assertTrue(screenModel.uiState.value.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters should return true when minRating is set`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onMinRatingChanged(4.5)

        assertTrue(screenModel.uiState.value.hasActiveFilters)
    }

    @Test
    fun `activeFiltersCount should return correct count`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult.empty()) }
        getCategoriesBehavior = { Result.success(emptyList()) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onCategoryToggle("cat-1")
        screenModel.onCategoryToggle("cat-2")
        screenModel.onMinRatingChanged(4.5)

        // Categories count as 1, minRating counts as 1
        assertEquals(2, screenModel.uiState.value.activeFiltersCount)
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
                location = Location(
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
