package com.aggregateservice.feature.profile.domain.usecase

import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for UpdateProfileUseCase.
 */
class UpdateProfileUseCaseTest {

    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var mockRepository: MockProfileRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockProfileRepository()
        updateProfileUseCase = UpdateProfileUseCase(mockRepository)
    }

    @Test
    fun `should return updated profile on successful update`() = runTest {
        // Arrange
        val request = UpdateProfileRequest(
            fullName = "Updated Name",
            phone = "+9876543210",
        )
        val expectedProfile = createTestProfile(
            fullName = "Updated Name",
            phone = "+9876543210",
        )
        mockRepository.updateProfileResult = Result.success(expectedProfile)

        // Act
        val result = updateProfileUseCase(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedProfile, result.getOrNull())
    }

    @Test
    fun `should return error when repository fails`() = runTest {
        // Arrange
        val request = UpdateProfileRequest(fullName = "New Name")
        val expectedError = RuntimeException("Update failed")
        mockRepository.updateProfileResult = Result.failure(expectedError)

        // Act
        val result = updateProfileUseCase(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `should call repository updateProfile with correct request`() = runTest {
        // Arrange
        val request = UpdateProfileRequest(
            fullName = "Test Name",
            phone = "+1112223333",
        )
        mockRepository.updateProfileResult = Result.success(createTestProfile())

        // Act
        updateProfileUseCase(request)

        // Assert
        assertEquals(1, mockRepository.updateProfileCallCount)
    }

    @Test
    fun `should update only fullName when phone is null`() = runTest {
        // Arrange
        val request = UpdateProfileRequest(fullName = "Only Name")
        val expectedProfile = createTestProfile(fullName = "Only Name")
        mockRepository.updateProfileResult = Result.success(expectedProfile)

        // Act
        val result = updateProfileUseCase(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Only Name", result.getOrNull()?.fullName)
    }

    private fun createTestProfile(
        fullName: String? = "Test User",
        phone: String? = "+1234567890",
    ) = Profile(
        id = "profile-123",
        userId = "user-456",
        fullName = fullName,
        phone = phone,
        avatarUrl = "https://example.com/avatar.jpg",
        noShowCount = 0,
        noShowRate = 0.0,
    )
}
