package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Price
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Instant

class GetProviderServicesUseCaseTest {
    private lateinit var getProviderServicesUseCase: GetProviderServicesUseCase
    private lateinit var mockRepository: MockCatalogRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockCatalogRepository()
        getProviderServicesUseCase = GetProviderServicesUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return services on successful fetch`() =
        runTest {
            val providerId = "provider-123"
            val expectedServices = createTestServices(5, providerId)
            mockRepository.servicesResult = Result.success(expectedServices)

            val result = getProviderServicesUseCase(providerId)

            assertTrue(result.isSuccess)
            val services = result.getOrNull()!!
            assertEquals(5, services.size)
            assertEquals(providerId, mockRepository.lastProviderId)
            assertEquals(null, mockRepository.lastCategoryId)
        }

    @Test
    fun `should return empty list when provider has no services`() =
        runTest {
            mockRepository.servicesResult = Result.success(emptyList())

            val result = getProviderServicesUseCase("provider-1")

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!.isEmpty())
        }

    @Test
    fun `should pass categoryId to repository for filtering`() =
        runTest {
            val providerId = "provider-1"
            val categoryId = "cat-123"
            mockRepository.servicesResult = Result.success(emptyList())

            getProviderServicesUseCase(providerId, categoryId)

            assertEquals(providerId, mockRepository.lastProviderId)
            assertEquals(categoryId, mockRepository.lastCategoryId)
        }

    @Test
    fun `should return services with all properties`() =
        runTest {
            val services =
                listOf(
                    Service(
                        id = "service-1",
                        providerId = "provider-1",
                        categoryId = "cat-1",
                        categoryName = "Haircut",
                        name = "Men's Haircut",
                        description = "Classic men's haircut",
                        price = Price(amount = 150.0, currency = "ILS"),
                        durationMinutes = 45,
                        isActive = true,
                        createdAt = Instant.fromEpochMilliseconds(0),
                    ),
                )
            mockRepository.servicesResult = Result.success(services)

            val result = getProviderServicesUseCase("provider-1")

            assertTrue(result.isSuccess)
            val service = result.getOrNull()!!.first()
            assertEquals("service-1", service.id)
            assertEquals("Men's Haircut", service.name)
            assertEquals("Haircut", service.categoryName)
            assertEquals(150.0, service.price.amount)
            assertEquals("ILS", service.price.currency)
            assertEquals(45, service.durationMinutes)
        }

    @Test
    fun `should return services with computed properties`() =
        runTest {
            val service =
                Service(
                    id = "service-1",
                    providerId = "provider-1",
                    categoryId = "cat-1",
                    name = "Test Service",
                    description = "This is a very long description that will be truncated for the short description property. We need more than 100 characters to test truncation.",
                    price = Price(amount = 200.50, currency = "ILS"),
                    durationMinutes = 60,
                    createdAt = Instant.fromEpochMilliseconds(0),
                )
            mockRepository.servicesResult = Result.success(listOf(service))

            val result = getProviderServicesUseCase("provider-1")

            assertTrue(result.isSuccess)
            val fetchedService = result.getOrNull()!!.first()
            assertEquals("60 min", fetchedService.formattedDuration)
            assertEquals("201 ILS", fetchedService.formattedPrice)
            assertEquals(100, fetchedService.shortDescription?.length)
        }

    @Test
    fun `should return filtered services when categoryId is provided`() =
        runTest {
            val filteredServices =
                listOf(
                    Service(
                        id = "service-1",
                        providerId = "provider-1",
                        categoryId = "cat-1",
                        name = "Haircut",
                        price = Price(amount = 100.0, currency = "ILS"),
                        durationMinutes = 30,
                        createdAt = Instant.fromEpochMilliseconds(0),
                    ),
                )
            mockRepository.servicesResult = Result.success(filteredServices)

            val result = getProviderServicesUseCase("provider-1", "cat-1")

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()!!.size)
            assertEquals("cat-1", result.getOrNull()!!.first().categoryId)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should return validation error when providerId is empty`() =
        runTest {
            val result = getProviderServicesUseCase("")

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("providerId", (error as AppError.ValidationError).field)
            assertTrue(error.message.contains("empty", ignoreCase = true))
        }

    @Test
    fun `should return validation error when providerId is blank`() =
        runTest {
            val result = getProviderServicesUseCase("   ")

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.ValidationError)
            assertEquals("providerId", (error as AppError.ValidationError).field)
        }

    @Test
    fun `should not call repository when validation fails`() =
        runTest {
            getProviderServicesUseCase("")

            assertEquals(null, mockRepository.lastProviderId)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return not found error when provider does not exist`() =
        runTest {
            mockRepository.servicesResult = Result.failure(AppError.NotFound)

            val result = getProviderServicesUseCase("nonexistent")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppError.NotFound)
        }

    @Test
    fun `should return network error when repository fails with network error`() =
        runTest {
            mockRepository.servicesResult =
                Result.failure(
                    AppError.NetworkError(503, "Service Unavailable"),
                )

            val result = getProviderServicesUseCase("provider-1")

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.NetworkError)
            assertEquals(503, (error as AppError.NetworkError).code)
        }

    @Test
    fun `should return network error when repository fails with server error`() =
        runTest {
            mockRepository.servicesResult =
                Result.failure(
                    AppError.NetworkError(500, "Internal Server Error"),
                )

            val result = getProviderServicesUseCase("provider-1")

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is AppError.NetworkError)
            assertEquals(500, (error as AppError.NetworkError).code)
        }

    // ========== Price Model Tests ==========

    @Test
    fun `price should format correctly`() =
        runTest {
            val service =
                Service(
                    id = "service-1",
                    providerId = "provider-1",
                    categoryId = "cat-1",
                    name = "Test",
                    price = Price(amount = 150.0, currency = "ILS"),
                    durationMinutes = 30,
                    createdAt = Instant.fromEpochMilliseconds(0),
                )
            mockRepository.servicesResult = Result.success(listOf(service))

            val result = getProviderServicesUseCase("provider-1")

            assertEquals(
                "150 ILS",
                result
                    .getOrNull()!!
                    .first()
                    .price.formatted,
            )
        }

    @Test
    fun `price should calculate amountInCents correctly`() =
        runTest {
            val price = Price(amount = 150.50, currency = "ILS")

            assertEquals(15050L, price.amountInCents)
        }

    // ========== Helper Methods ==========

    private fun createTestServices(count: Int, providerId: String): List<Service> {
        return (1..count).map { index ->
            Service(
                id = "service-$index",
                providerId = providerId,
                categoryId = "cat-$index",
                categoryName = "Category $index",
                name = "Service $index",
                description = "Description for service $index",
                price = Price(amount = 100.0 * index, currency = "ILS"),
                durationMinutes = 30 * index,
                isActive = index % 2 == 0,
                createdAt = Instant.fromEpochMilliseconds(0),
            )
        }
    }

    /**
     * Mock implementation of CatalogRepository for testing
     */
    private class MockCatalogRepository : CatalogRepository {
        var servicesResult: Result<List<Service>> = Result.success(emptyList())
        var lastProviderId: String? = null
        var lastCategoryId: String? = null

        override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
            return Result.success(SearchResult.empty())
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
        ): Result<List<Service>> {
            lastProviderId = providerId
            lastCategoryId = categoryId
            return servicesResult
        }

        override suspend fun getServiceById(serviceId: String): Result<Service> {
            return Result.failure(AppError.NotFound)
        }

        override suspend fun searchServices(
            query: String,
            filters: SearchFilters,
        ): Result<SearchResult<Service>> {
            return Result.success(SearchResult.empty())
        }
    }
}
