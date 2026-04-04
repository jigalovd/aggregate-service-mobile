package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.core.common.model.Location
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.WorkingHours
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Instant

class SearchProvidersUseCaseTest {
    private lateinit var searchProvidersUseCase: SearchProvidersUseCase
    private lateinit var mockRepository: MockCatalogRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockCatalogRepository()
        searchProvidersUseCase = SearchProvidersUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return search results on successful search`() =
        runTest {
            val filters = SearchFilters(query = "haircut")
            val expectedProviders = createTestProviders(3)
            val expectedResult =
                SearchResult(
                    items = expectedProviders,
                    totalCount = 3,
                    totalPages = 1,
                    currentPage = 1,
                )
            mockRepository.searchProvidersResult = Result.success(expectedResult)

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isSuccess)
            val searchResult = result.getOrNull()!!
            assertEquals(3, searchResult.items.size)
            assertEquals(3, searchResult.totalCount)
            assertEquals(filters, mockRepository.lastSearchFilters)
        }

    @Test
    fun `should return empty results when no providers match`() =
        runTest {
            val filters = SearchFilters(query = "nonexistent")
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!.items.isEmpty())
            assertEquals(0, result.getOrNull()!!.totalCount)
        }

    @Test
    fun `should pass filters with category ids to repository`() =
        runTest {
            val filters =
                SearchFilters(
                    query = "manicure",
                    categoryIds = listOf("cat-1", "cat-2"),
                )
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            searchProvidersUseCase(filters)

            assertEquals(listOf("cat-1", "cat-2"), mockRepository.lastSearchFilters?.categoryIds)
        }

    @Test
    fun `should pass geo search filters to repository`() =
        runTest {
            val filters =
                SearchFilters(
                    latitude = 32.0853,
                    longitude = 34.7818,
                    radiusKm = 10.0,
                )
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            searchProvidersUseCase(filters)

            assertTrue(mockRepository.lastSearchFilters!!.isGeoSearch)
            assertEquals(32.0853, mockRepository.lastSearchFilters!!.latitude!!)
            assertEquals(34.7818, mockRepository.lastSearchFilters!!.longitude!!)
            assertEquals(10.0, mockRepository.lastSearchFilters!!.radiusKm!!)
        }

    @Test
    fun `should pass rating filter to repository`() =
        runTest {
            val filters = SearchFilters(minRating = 4.5)
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            searchProvidersUseCase(filters)

            assertEquals(4.5, mockRepository.lastSearchFilters!!.minRating)
        }

    @Test
    fun `should pass verified filter to repository`() =
        runTest {
            val filters = SearchFilters(isVerified = true)
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            searchProvidersUseCase(filters)

            assertEquals(true, mockRepository.lastSearchFilters!!.isVerified)
        }

    @Test
    fun `should pass pagination parameters to repository`() =
        runTest {
            val filters = SearchFilters(page = 2, pageSize = 50)
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            searchProvidersUseCase(filters)

            assertEquals(2, mockRepository.lastSearchFilters!!.page)
            assertEquals(50, mockRepository.lastSearchFilters!!.pageSize)
        }

    @Test
    fun `should pass sort parameters to repository`() =
        runTest {
            val filters =
                SearchFilters(
                    sortBy = SearchFilters.SortBy.REVIEW_COUNT,
                    sortOrder = SearchFilters.SortOrder.ASC,
                )
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            searchProvidersUseCase(filters)

            assertEquals(SearchFilters.SortBy.REVIEW_COUNT, mockRepository.lastSearchFilters!!.sortBy)
            assertEquals(SearchFilters.SortOrder.ASC, mockRepository.lastSearchFilters!!.sortOrder)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should return validation error when page is zero`() =
        runTest {
            val filters = SearchFilters(page = 0)

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("page", (error as AppError.ValidationError).field)
            assertTrue(error.message.contains("1"))
        }

    @Test
    fun `should return validation error when page is negative`() =
        runTest {
            val filters = SearchFilters(page = -1)

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("page", (error as AppError.ValidationError).field)
        }

    @Test
    fun `should return validation error when pageSize is zero`() =
        runTest {
            val filters = SearchFilters(pageSize = 0)

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("pageSize", (error as AppError.ValidationError).field)
        }

    @Test
    fun `should return validation error when pageSize exceeds 100`() =
        runTest {
            val filters = SearchFilters(pageSize = 101)

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("pageSize", (error as AppError.ValidationError).field)
            assertTrue(error.message.contains("100"))
        }

    @Test
    fun `should allow pageSize of exactly 100`() =
        runTest {
            val filters = SearchFilters(pageSize = 100)
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `should allow pageSize of exactly 1`() =
        runTest {
            val filters = SearchFilters(pageSize = 1)
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `should return validation error when radius is zero for geo search`() =
        runTest {
            val filters =
                SearchFilters(
                    latitude = 32.0853,
                    longitude = 34.7818,
                    radiusKm = 0.0,
                )

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("radiusKm", (error as AppError.ValidationError).field)
        }

    @Test
    fun `should return validation error when radius is negative for geo search`() =
        runTest {
            val filters =
                SearchFilters(
                    latitude = 32.0853,
                    longitude = 34.7818,
                    radiusKm = -5.0,
                )

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("radiusKm", (error as AppError.ValidationError).field)
        }

    @Test
    fun `should allow positive radius for geo search`() =
        runTest {
            val filters =
                SearchFilters(
                    latitude = 32.0853,
                    longitude = 34.7818,
                    radiusKm = 0.1,
                )
            mockRepository.searchProvidersResult = Result.success(SearchResult.empty())

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isSuccess)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return network error when repository fails with network error`() =
        runTest {
            val filters = SearchFilters(query = "test")
            mockRepository.searchProvidersResult =
                Result.failure(
                    AppError.NetworkError(503, "Service Unavailable"),
                )

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.NetworkError)
            assertEquals(503, (error as AppError.NetworkError).code)
        }

    @Test
    fun `should return network error when repository fails with server error`() =
        runTest {
            val filters = SearchFilters(query = "test")
            mockRepository.searchProvidersResult =
                Result.failure(
                    AppError.NetworkError(500, "Internal Server Error"),
                )

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.NetworkError)
            assertEquals(500, (error as AppError.NetworkError).code)
        }

    @Test
    fun `should return not found error when repository fails with not found`() =
        runTest {
            val filters = SearchFilters(query = "test")
            mockRepository.searchProvidersResult = Result.failure(AppError.NotFound)

            val result = searchProvidersUseCase(filters)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppError.NotFound)
        }

    // ========== Helper Methods ==========

    private fun createTestProviders(count: Int): List<Provider> {
        return (1..count).map { index ->
            Provider(
                id = "provider-$index",
                userId = "user-$index",
                businessName = "Test Provider $index",
                description = "Description for provider $index",
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
                isVerified = index % 2 == 0,
                createdAt = Instant.fromEpochMilliseconds(0),
                categories =
                    listOf(
                        Category(id = "cat-1", name = "Category 1"),
                    ),
            )
        }
    }

    /**
     * Mock implementation of CatalogRepository for testing
     */
    private class MockCatalogRepository : CatalogRepository {
        var searchProvidersResult: Result<SearchResult<Provider>> = Result.success(SearchResult.empty())
        var lastSearchFilters: SearchFilters? = null

        override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
            lastSearchFilters = filters
            return searchProvidersResult
        }

        override suspend fun getProviderById(providerId: String): Result<Provider> {
            return Result.failure(AppError.NotFound)
        }

        override suspend fun getCategories(parentId: String?): Result<List<Category>> {
            return Result.success(emptyList())
        }

        override suspend fun getCategoryById(categoryId: String): Result<Category> {
            return Result.failure(AppError.NotFound)
        }

        override suspend fun getProviderServices(
            providerId: String,
            categoryId: String?,
        ): Result<List<com.aggregateservice.feature.catalog.domain.model.Service>> {
            return Result.success(emptyList())
        }

        override suspend fun getServiceById(serviceId: String): Result<com.aggregateservice.feature.catalog.domain.model.Service> {
            return Result.failure(AppError.NotFound)
        }

        override suspend fun searchServices(
            query: String,
            filters: SearchFilters,
        ): Result<SearchResult<com.aggregateservice.feature.catalog.domain.model.Service>> {
            return Result.success(SearchResult.empty())
        }
    }
}
