package com.aggregateservice.feature.profile.presentation.screenmodel

import cafe.adriel.voyager.navigator.Navigator
import com.aggregateservice.core.auth.contract.LogoutUseCase
import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository
import com.aggregateservice.feature.profile.domain.usecase.GetProfileUseCase
import com.aggregateservice.feature.profile.domain.usecase.UpdateProfileUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for ProfileScreenModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockProfileRepository
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var catalogNavigator: CatalogNavigator
    private lateinit var mockNavigator: Navigator

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockProfileRepository()
        getProfileUseCase = GetProfileUseCase(mockRepository)
        updateProfileUseCase = UpdateProfileUseCase(mockRepository)
        logoutUseCase = mockk(relaxed = true)
        catalogNavigator = mockk(relaxed = true)
        mockNavigator = mockk(relaxed = true)
        every { catalogNavigator.createCatalogScreen() } returns mockk(relaxed = true)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() {
        // Arrange - set result BEFORE creating ScreenModel
        mockRepository.getProfileResult = Result.success(createTestProfile())

        // Act
        val screenModel =
            ProfileScreenModel(
                getProfileUseCase = getProfileUseCase,
                updateProfileUseCase = updateProfileUseCase,
                logoutUseCase = logoutUseCase,
                catalogNavigator = catalogNavigator,
            )

        // Assert - initial state before coroutine completes
        assertTrue(screenModel.uiState.value.isLoading)
    }

    @Test
    fun `should load profile on init`() =
        runTest {
            // Arrange - set result BEFORE creating ScreenModel
            val expectedProfile = createTestProfile()
            mockRepository.getProfileResult = Result.success(expectedProfile)

            // Act
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = screenModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(expectedProfile, state.profile)
        }

    @Test
    fun `should set error state when loading fails`() =
        runTest {
            // Arrange - set error result BEFORE creating ScreenModel
            val expectedError = RuntimeException("Network error")
            mockRepository.getProfileResult = Result.failure(expectedError)

            // Act
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = screenModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.error != null)
        }

    @Test
    fun `startEditing should enable edit mode with current values`() =
        runTest {
            // Arrange
            val profile = createTestProfile(fullName = "Test User", phone = "+1234567890")
            mockRepository.getProfileResult = Result.success(profile)
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.startEditing()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = screenModel.uiState.value
            assertTrue(state.isEditing)
            assertEquals("Test User", state.editFullName)
            assertEquals("+1234567890", state.editPhone)
        }

    @Test
    fun `cancelEditing should disable edit mode and revert changes`() =
        runTest {
            // Arrange
            val profile = createTestProfile(fullName = "Original", phone = "+1111111111")
            mockRepository.getProfileResult = Result.success(profile)
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()
            screenModel.startEditing()
            screenModel.onFullNameChanged("Changed")
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.cancelEditing()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = screenModel.uiState.value
            assertFalse(state.isEditing)
            assertEquals("Original", state.editFullName)
            assertEquals("+1111111111", state.editPhone)
        }

    @Test
    fun `onFullNameChanged should update editFullName`() =
        runTest {
            // Arrange
            mockRepository.getProfileResult = Result.success(createTestProfile())
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.onFullNameChanged("New Name")
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            assertEquals("New Name", screenModel.uiState.value.editFullName)
        }

    @Test
    fun `onPhoneChanged should update editPhone`() =
        runTest {
            // Arrange
            mockRepository.getProfileResult = Result.success(createTestProfile())
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.onPhoneChanged("+9998887777")
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            assertEquals("+9998887777", screenModel.uiState.value.editPhone)
        }

    @Test
    fun `saveProfile should update profile and exit edit mode`() =
        runTest {
            // Arrange
            val originalProfile = createTestProfile(fullName = "Original", phone = "+1111111111")
            val updatedProfile = createTestProfile(fullName = "Updated", phone = "+2222222222")
            mockRepository.getProfileResult = Result.success(originalProfile)
            mockRepository.updateProfileResult = Result.success(updatedProfile)
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.startEditing()
            screenModel.onFullNameChanged("Updated")
            screenModel.onPhoneChanged("+2222222222")
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.saveProfile()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = screenModel.uiState.value
            assertFalse(state.isEditing)
            assertEquals(updatedProfile, state.profile)
            assertTrue(state.saveSuccess)
        }

    @Test
    fun `saveProfile should set error when update fails`() =
        runTest {
            // Arrange
            val profile = createTestProfile()
            mockRepository.getProfileResult = Result.success(profile)
            mockRepository.updateProfileResult = Result.failure(RuntimeException("Update failed"))
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.startEditing()
            screenModel.onFullNameChanged("Changed")
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.saveProfile()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = screenModel.uiState.value
            assertTrue(state.error != null)
            assertFalse(state.saveSuccess)
        }

    @Test
    fun `clearError should clear error state`() =
        runTest {
            // Arrange
            val expectedError = RuntimeException("Error")
            mockRepository.getProfileResult = Result.failure(expectedError)
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.clearError()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            assertNull(screenModel.uiState.value.error)
        }

    @Test
    fun `clearSaveSuccess should clear saveSuccess flag`() =
        runTest {
            // Arrange
            val profile = createTestProfile()
            val updatedProfile = createTestProfile(fullName = "Updated")
            mockRepository.getProfileResult = Result.success(profile)
            mockRepository.updateProfileResult = Result.success(updatedProfile)
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            screenModel.startEditing()
            screenModel.onFullNameChanged("Updated")
            screenModel.saveProfile()
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            screenModel.clearSaveSuccess()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            assertFalse(screenModel.uiState.value.saveSuccess)
        }

    @Test
    fun `logout should call useCase and navigate to catalog`() =
        runTest {
            // Arrange
            mockRepository.getProfileResult = Result.success(createTestProfile())
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // when
            screenModel.logout(mockNavigator)
            testDispatcher.scheduler.advanceUntilIdle()

            // then
            coVerify { logoutUseCase() }
            verify { mockNavigator.replace(any()) }
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

/**
 * Mock implementation of ProfileRepository for testing.
 */
class MockProfileRepository : ProfileRepository {
    var getProfileResult: Result<Profile> =
        Result.success(
            Profile(
                id = "default",
                userId = "default",
                fullName = null,
                phone = null,
                avatarUrl = null,
                noShowCount = 0,
                noShowRate = 0.0,
            ),
        )
    var updateProfileResult: Result<Profile> =
        Result.success(
            Profile(
                id = "default",
                userId = "default",
                fullName = null,
                phone = null,
                avatarUrl = null,
                noShowCount = 0,
                noShowRate = 0.0,
            ),
        )
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
}
