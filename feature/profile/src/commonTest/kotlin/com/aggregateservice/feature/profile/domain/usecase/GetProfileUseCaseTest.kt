package com.aggregateservice.feature.profile.domain.usecase

import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for GetProfileUseCase.
 */
class GetProfileUseCaseTest {
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var mockRepository: MockProfileRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockProfileRepository()
        getProfileUseCase = GetProfileUseCase(mockRepository)
    }

    @Test
    fun `should return profile on successful fetch`() =
        runTest {
            // Arrange
            val expectedProfile = createTestProfile()
            mockRepository.getProfileResult = Result.success(expectedProfile)

            // Act
            val result = getProfileUseCase()

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedProfile, result.getOrNull())
        }

    @Test
    fun `should return error when repository fails`() =
        runTest {
            // Arrange
            val expectedError = RuntimeException("Network error")
            mockRepository.getProfileResult = Result.failure(expectedError)

            // Act
            val result = getProfileUseCase()

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should call repository getProfile`() =
        runTest {
            // Arrange
            mockRepository.getProfileResult = Result.success(createTestProfile())

            // Act
            getProfileUseCase()

            // Assert
            assertEquals(1, mockRepository.getProfileCallCount)
        }

    private fun createTestProfile() =
        Profile(
            id = "profile-123",
            userId = "user-456",
            fullName = "Test User",
            phone = "+1234567890",
            avatarUrl = "https://example.com/avatar.jpg",
            noShowCount = 0,
            noShowRate = 0.0,
        )
}

/**
 * Mock implementation of ProfileRepository for testing.
 */
class MockProfileRepository : ProfileRepository {
    var getProfileResult: Result<Profile> = Result.success(createDefaultProfile())
    var updateProfileResult: Result<Profile> = Result.success(createDefaultProfile())
    var getProfileCallCount = 0
    var updateProfileCallCount = 0

    override suspend fun getProfile(): Result<Profile> {
        getProfileCallCount++
        return getProfileResult
    }

    override suspend fun updateProfile(request: com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest): Result<Profile> {
        updateProfileCallCount++
        return updateProfileResult
    }

    private fun createDefaultProfile() =
        Profile(
            id = "default-id",
            userId = "default-user",
            fullName = null,
            phone = null,
            avatarUrl = null,
            noShowCount = 0,
            noShowRate = 0.0,
        )
}
