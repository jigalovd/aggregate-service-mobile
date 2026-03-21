package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Location
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.WorkingHours
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class GetProviderDetailsUseCaseTest {

    private lateinit var getProviderDetailsUseCase: GetProviderDetailsUseCase
    private lateinit var mockRepository: MockCatalogRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockCatalogRepository()
        getProviderDetailsUseCase = GetProviderDetailsUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return provider on successful fetch`() = runTest {
        val providerId = "provider-123"
        val expectedProvider = createTestProvider(providerId)
        mockRepository.providerResult = Result.success(expectedProvider)

        val result = getProviderDetailsUseCase(providerId)

        assertTrue(result.isSuccess)
        val provider = result.getOrNull()!!
        assertEquals(providerId, provider.id)
        assertEquals(providerId, mockRepository.lastProviderId)
    }

    @Test
    fun `should return provider with all properties`() = runTest {
        val provider = Provider(
            id = "provider-1",
            userId = "user-1",
            businessName = "Test Salon",
            description = "A beautiful salon",
            logoUrl = "https://example.com/logo.png",
            photos = listOf("photo1.jpg", "photo2.jpg"),
            rating = 4.8,
            reviewCount = 150,
            location = Location(
                latitude = 32.0853,
                longitude = 34.7818,
                address = "Dizengoff 100",
                city = "Tel Aviv",
                postalCode = "12345",
                country = "Israel",
            ),
            workingHours = WorkingHours(),
            isVerified = true,
            isActive = true,
            createdAt = Clock.System.now(),
            categories = listOf(Category(id = "cat-1", name = "Haircut")),
            servicesCount = 25,
        )
        mockRepository.providerResult = Result.success(provider)

        val result = getProviderDetailsUseCase("provider-1")

        assertTrue(result.isSuccess)
        val fetchedProvider = result.getOrNull()!!
        assertEquals("Test Salon", fetchedProvider.businessName)
        assertEquals(4.8, fetchedProvider.rating)
        assertEquals(150, fetchedProvider.reviewCount)
        assertTrue(fetchedProvider.isVerified)
        assertEquals(25, fetchedProvider.servicesCount)
    }

    @Test
    fun `should return provider with computed properties`() = runTest {
        val provider = Provider(
            id = "provider-1",
            userId = "user-1",
            businessName = "Test",
            description = "This is a very long description that should be truncated for short description. We need more than 100 characters to test truncation properly here.",
            photos = listOf("primary.jpg", "secondary.jpg"),
            rating = 4.567,
            location = Location(
                latitude = 32.0,
                longitude = 34.0,
                address = "Address",
                city = "City",
            ),
            workingHours = WorkingHours(),
            createdAt = Clock.System.now(),
        )
        mockRepository.providerResult = Result.success(provider)

        val result = getProviderDetailsUseCase("provider-1")

        assertTrue(result.isSuccess)
        val fetchedProvider = result.getOrNull()!!
        assertEquals("4.6", fetchedProvider.formattedRating)
        assertEquals("primary.jpg", fetchedProvider.primaryPhotoUrl)
        assertEquals(100, fetchedProvider.shortDescription?.length)
    }

    @Test
    fun `should use logoUrl as primaryPhotoUrl when photos list is empty`() = runTest {
        val provider = Provider(
            id = "provider-1",
            userId = "user-1",
            businessName = "Test",
            logoUrl = "logo.png",
            photos = emptyList(),
            rating = 4.0,
            location = Location(
                latitude = 32.0,
                longitude = 34.0,
                address = "Address",
                city = "City",
            ),
            workingHours = WorkingHours(),
            createdAt = Clock.System.now(),
        )
        mockRepository.providerResult = Result.success(provider)

        val result = getProviderDetailsUseCase("provider-1")

        assertTrue(result.isSuccess)
        assertEquals("logo.png", result.getOrNull()!!.primaryPhotoUrl)
    }

    // ========== Validation Error Cases ==========

    @Test
    fun `should return validation error when providerId is empty`() = runTest {
        val result = getProviderDetailsUseCase("")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.ValidationError)
        assertEquals("providerId", (error as AppError.ValidationError).field)
        assertTrue(error.message.contains("empty", ignoreCase = true))
    }

    @Test
    fun `should return validation error when providerId is blank`() = runTest {
        val result = getProviderDetailsUseCase("   ")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.ValidationError)
        assertEquals("providerId", (error as AppError.ValidationError).field)
    }

    @Test
    fun `should not call repository when validation fails`() = runTest {
        getProviderDetailsUseCase("")

        assertEquals(null, mockRepository.lastProviderId)
    }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return not found error when provider does not exist`() = runTest {
        mockRepository.providerResult = Result.failure(AppError.NotFound)

        val result = getProviderDetailsUseCase("nonexistent")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NotFound)
    }

    @Test
    fun `should return network error when repository fails with network error`() = runTest {
        mockRepository.providerResult = Result.failure(
            AppError.NetworkError(503, "Service Unavailable")
        )

        val result = getProviderDetailsUseCase("provider-1")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.NetworkError)
        assertEquals(503, (error as AppError.NetworkError).code)
    }

    @Test
    fun `should return network error when repository fails with server error`() = runTest {
        mockRepository.providerResult = Result.failure(
            AppError.NetworkError(500, "Internal Server Error")
        )

        val result = getProviderDetailsUseCase("provider-1")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.NetworkError)
        assertEquals(500, (error as AppError.NetworkError).code)
    }

    @Test
    fun `should return unauthorized error when user is not authorized`() = runTest {
        mockRepository.providerResult = Result.failure(AppError.Unauthorized)

        val result = getProviderDetailsUseCase("provider-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.Unauthorized)
    }

    // ========== Helper Methods ==========

    private fun createTestProvider(id: String): Provider {
        return Provider(
            id = id,
            userId = "user-$id",
            businessName = "Test Provider $id",
            description = "Description for $id",
            rating = 4.5,
            reviewCount = 100,
            location = Location(
                latitude = 32.0853,
                longitude = 34.7818,
                address = "Test Address",
                city = "Tel Aviv",
            ),
            workingHours = WorkingHours(),
            isVerified = true,
            createdAt = Clock.System.now(),
            categories = listOf(Category(id = "cat-1", name = "Category 1")),
        )
    }

    /**
     * Mock implementation of CatalogRepository for testing
     */
    private class MockCatalogRepository : CatalogRepository {
        var providerResult: Result<Provider> = Result.failure(AppError.NotFound)
        var lastProviderId: String? = null

        override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
            return Result.success(SearchResult.empty())
        }

        override suspend fun getProviderById(providerId: String): Result<Provider> {
            lastProviderId = providerId
            return providerResult
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
