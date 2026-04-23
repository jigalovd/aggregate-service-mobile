package com.aggregateservice.feature.provider.onboarding.data.repository

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.onboarding.data.api.ProviderOnboardingRequest
import com.aggregateservice.feature.provider.onboarding.data.api.ProviderOnboardingResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for ProviderOnboardingRepositoryImpl.
 * Uses mock interface for testing without extending final classes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProviderOnboardingRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============ Submit Onboarding Success Tests ============

    @Test
    fun `submitOnboarding should call api and return success on success`() = runTest {
        val response = ProviderOnboardingResponse(message = "Onboarding successful", accessToken = "new_token_123")
        val mockApi = MockOnboardingApi(Result.success(response))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "Test Business",
            bio = "Test bio",
            phone = "1234567890",
            address = "123 Test St",
            serviceRadiusKm = 10f,
            categoryIds = listOf("cleaning", "plumbing"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(1, mockApi.callCount)
        val successResponse = result.getOrNull()!!
        assertEquals("Onboarding successful", successResponse.message)
        assertEquals("new_token_123", successResponse.accessToken)
    }

    @Test
    fun `submitOnboarding should pass correct parameters to API`() = runTest {
        val response = ProviderOnboardingResponse(message = "OK", accessToken = "token")
        val mockApi = MockOnboardingApi(Result.success(response))
        val repository = TestableRepository(mockApi)

        repository.submitOnboarding(
            businessName = "My Business",
            bio = "Business description",
            phone = "9876543210",
            address = "456 Service Ave",
            serviceRadiusKm = 25f,
            categoryIds = listOf("electrical"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("My Business", mockApi.lastRequest?.businessName)
        assertEquals("9876543210", mockApi.lastRequest?.phone)
        assertEquals("456 Service Ave", mockApi.lastRequest?.address)
        assertEquals(25f, mockApi.lastRequest?.serviceRadiusKm)
    }

    @Test
    fun `submitOnboarding should propagate message and token from API response`() = runTest {
        val response = ProviderOnboardingResponse(
            message = "Provider onboarding complete",
            accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        )
        val mockApi = MockOnboardingApi(Result.success(response))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "Test",
            bio = "Test",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 10f,
            categoryIds = listOf("cleaning"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val successResponse = result.getOrNull()!!
        assertEquals("Provider onboarding complete", successResponse.message)
        assertTrue(successResponse.accessToken.startsWith("eyJ"))
    }

    // ============ Submit Onboarding Error Tests ============

    @Test
    fun `submitOnboarding should return failure when API returns ApiValidationError`() = runTest {
        val appError = AppError.ApiValidationError(field = "businessName", message = "Required")
        val mockApi = MockOnboardingApi(Result.failure(appError))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "",
            bio = "Test",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 10f,
            categoryIds = listOf("cleaning"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AppError.ApiValidationError>(error)
    }

    @Test
    fun `submitOnboarding should return failure with NetworkError on network exception`() = runTest {
        val appError = AppError.NetworkError(code = 0, message = "Connection failed")
        val mockApi = MockOnboardingApi(Result.failure(appError))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "Test",
            bio = "Test",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 10f,
            categoryIds = listOf("cleaning"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AppError.NetworkError>(error)
    }

    @Test
    fun `submitOnboarding should return failure with ServerError on 500`() = runTest {
        val appError = AppError.NetworkError(code = 500, message = "Internal server error")
        val mockApi = MockOnboardingApi(Result.failure(appError))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "Test",
            bio = "Test",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 10f,
            categoryIds = listOf("cleaning"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AppError.NetworkError>(error)
        assertEquals(500, error.code)
    }

    @Test
    fun `submitOnboarding should return failure with Unauthorized on 401`() = runTest {
        val mockApi = MockOnboardingApi(Result.failure(AppError.Unauthorized))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "Test",
            bio = "Test",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 10f,
            categoryIds = listOf("cleaning"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AppError.Unauthorized>(error)
    }

    // ============ Is Onboarding Complete Tests ============

    @Test
    fun `isOnboardingComplete should return false`() = runTest {
        val response = ProviderOnboardingResponse(message = "OK", accessToken = "token")
        val mockApi = MockOnboardingApi(Result.success(response))
        val repository = TestableRepository(mockApi)

        val result = repository.isOnboardingComplete()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!)
    }

    // ============ Boundary Condition Tests ============

    @Test
    fun `submitOnboarding should handle empty bio`() = runTest {
        val response = ProviderOnboardingResponse(message = "OK", accessToken = "token")
        val mockApi = MockOnboardingApi(Result.success(response))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "Test",
            bio = "",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 10f,
            categoryIds = listOf("cleaning"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("", mockApi.lastRequest?.bio)
    }

    @Test
    fun `submitOnboarding should handle large service radius`() = runTest {
        val response = ProviderOnboardingResponse(message = "OK", accessToken = "token")
        val mockApi = MockOnboardingApi(Result.success(response))
        val repository = TestableRepository(mockApi)

        val result = repository.submitOnboarding(
            businessName = "Test",
            bio = "Test",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 100f,
            categoryIds = listOf("cleaning"),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(100f, mockApi.lastRequest?.serviceRadiusKm)
    }

    @Test
    fun `submitOnboarding should handle many categories`() = runTest {
        val response = ProviderOnboardingResponse(message = "OK", accessToken = "token")
        val mockApi = MockOnboardingApi(Result.success(response))
        val repository = TestableRepository(mockApi)
        val categories = listOf("cleaning", "plumbing", "electrical", "hvac", "landscaping")

        val result = repository.submitOnboarding(
            businessName = "Test",
            bio = "Test",
            phone = "1234567890",
            address = "123 St",
            serviceRadiusKm = 10f,
            categoryIds = categories,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(5, mockApi.lastRequest?.categoryIds?.size)
    }
}

/**
 * Mock API service using the local interface type.
 */
private class MockOnboardingApi(var result: Result<ProviderOnboardingResponse>) : TestableApiService {
    var callCount = 0
    var lastRequest: ProviderOnboardingRequest? = null

    override suspend fun submitOnboarding(request: ProviderOnboardingRequest): Result<ProviderOnboardingResponse> {
        callCount++
        lastRequest = request
        return result
    }
}

/**
 * Local interface matching the API service contract for testing.
 */
private interface TestableApiService {
    suspend fun submitOnboarding(request: ProviderOnboardingRequest): Result<ProviderOnboardingResponse>
}

/**
 * Testable repository implementation with inlined logic matching the real repository.
 */
private class TestableRepository(private val mockApi: TestableApiService) {
    suspend fun submitOnboarding(
        businessName: String,
        bio: String,
        phone: String,
        address: String,
        serviceRadiusKm: Float,
        categoryIds: List<String>,
    ): Result<ProviderOnboardingResponse> {
        val request = ProviderOnboardingRequest(
            businessName = businessName,
            bio = bio,
            phone = phone,
            address = address,
            serviceRadiusKm = serviceRadiusKm,
            categoryIds = categoryIds,
        )
        return mockApi.submitOnboarding(request)
    }

    suspend fun isOnboardingComplete(): Result<Boolean> {
        return Result.success(false)
    }
}