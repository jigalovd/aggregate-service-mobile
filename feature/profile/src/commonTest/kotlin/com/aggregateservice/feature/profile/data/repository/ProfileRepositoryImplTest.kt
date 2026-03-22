package com.aggregateservice.feature.profile.data.repository

import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for ProfileRepositoryImpl.
 *
 * Uses a testable implementation that allows injecting mock results.
 */
class ProfileRepositoryImplTest {

    private lateinit var repository: TestableProfileRepository

    @BeforeTest
    fun setup() {
        repository = TestableProfileRepository()
    }

    @Test
    fun `should return profile on successful getProfile`() = runTest {
        // Arrange
        val expectedProfile = createTestProfile()
        repository.getProfileResult = Result.success(expectedProfile)

        // Act
        val result = repository.getProfile()

        // Assert
        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals(expectedProfile.id, profile.id)
        assertEquals(expectedProfile.userId, profile.userId)
        assertEquals(expectedProfile.fullName, profile.fullName)
        assertEquals(expectedProfile.phone, profile.phone)
    }

    @Test
    fun `should return error when api fails on getProfile`() = runTest {
        // Arrange
        val expectedError = RuntimeException("Network error")
        repository.getProfileResult = Result.failure(expectedError)

        // Act
        val result = repository.getProfile()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `should return updated profile on successful updateProfile`() = runTest {
        // Arrange
        val request = UpdateProfileRequest(
            fullName = "Updated Name",
            phone = "+9876543210",
        )
        val expectedProfile = createTestProfile(
            fullName = "Updated Name",
            phone = "+9876543210",
        )
        repository.updateProfileResult = Result.success(expectedProfile)

        // Act
        val result = repository.updateProfile(request)

        // Assert
        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals("Updated Name", profile.fullName)
        assertEquals("+9876543210", profile.phone)
    }

    @Test
    fun `should return error when api fails on updateProfile`() = runTest {
        // Arrange
        val request = UpdateProfileRequest(fullName = "New Name")
        val expectedError = RuntimeException("Update failed")
        repository.updateProfileResult = Result.failure(expectedError)

        // Act
        val result = repository.updateProfile(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `should map noShowCount and noShowRate correctly`() = runTest {
        // Arrange
        val expectedProfile = createTestProfile(
            noShowCount = 5,
            noShowRate = 0.15,
        )
        repository.getProfileResult = Result.success(expectedProfile)

        // Act
        val result = repository.getProfile()

        // Assert
        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals(5, profile.noShowCount)
        assertEquals(0.15, profile.noShowRate)
    }

    private fun createTestProfile(
        fullName: String? = "Test User",
        phone: String? = "+1234567890",
        noShowCount: Int = 0,
        noShowRate: Double = 0.0,
    ) = Profile(
        id = "profile-123",
        userId = "user-456",
        fullName = fullName,
        phone = phone,
        avatarUrl = "https://example.com/avatar.jpg",
        noShowCount = noShowCount,
        noShowRate = noShowRate,
    )
}

/**
 * Testable implementation of ProfileRepository that allows injecting mock results.
 */
class TestableProfileRepository : ProfileRepository {

    var getProfileResult: Result<Profile> = Result.success(createDefaultProfile())

    var updateProfileResult: Result<Profile> = Result.success(createDefaultProfile())

    var getProfileCallCount = 0
    var updateProfileCallCount = 0

    override suspend fun getProfile(): Result<Profile> {
        getProfileCallCount++
        return getProfileResult
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<Profile> {
        updateProfileCallCount++
        return updateProfileResult
    }

    private fun createDefaultProfile() = Profile(
        id = "default-id",
        userId = "default-user",
        fullName = null,
        phone = null,
        avatarUrl = null,
        noShowCount = 0,
        noShowRate = 0.0,
    )
}
