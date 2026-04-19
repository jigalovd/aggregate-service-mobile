package com.aggregateservice.feature.profile.presentation.screenmodel

import app.cash.turbine.test
import cafe.adriel.voyager.navigator.Navigator
import co.touchlab.kermit.Logger
import com.aggregateservice.core.auth.contract.LogoutUseCase
import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository
import com.aggregateservice.feature.profile.domain.usecase.GetProfileUseCase
import com.aggregateservice.feature.profile.domain.usecase.UpdateProfileUseCase
import com.aggregateservice.feature.profile.presentation.model.ProfileUiState
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
 * Tests for ProfileScreenModel using Turbine for async StateFlow testing.
 *
 * **Key insight:** loadProfile() is called in ProfileScreen via LaunchedEffect,
 * not automatically in ScreenModel init. Tests must call loadProfile() explicitly.
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

        // Act - create ScreenModel (does NOT auto-load, must call loadProfile())
        val screenModel =
            ProfileScreenModel(
                getProfileUseCase = getProfileUseCase,
                updateProfileUseCase = updateProfileUseCase,
                logoutUseCase = logoutUseCase,
                catalogNavigator = catalogNavigator,
                logger = Logger.withTag("Test"),
            )

        // Assert - initial state is Loading (before loadProfile is called)
        assertTrue(screenModel.uiState.value.isLoading)
    }

    @Test
    fun `loadProfile should emit loading then success state`() =
        runTest {
            // Arrange
            val expectedProfile = createTestProfile()
            mockRepository.getProfileResult = Result.success(expectedProfile)

            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                    logger = Logger.withTag("Test"),
                )

            // Act & Assert using Turbine
            screenModel.uiState.test {
                // Initial state is loading
                val initial = awaitItem()
                assertTrue(initial.isLoading)

                // Call loadProfile and await transitions
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()

                // The initial loading state equals the first update from loadProfile,
                // so StateFlow skips that emission. We await the success state directly.
                val success = awaitItem()
                assertFalse(success.isLoading)
                assertEquals(expectedProfile, success.profile)
                assertNull(success.error)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadProfile should emit loading then error state on failure`() =
        runTest {
            // Arrange - set error result
            val expectedError = RuntimeException("Network error")
            mockRepository.getProfileResult = Result.failure(expectedError)

            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                    logger = Logger.withTag("Test"),
                )

            // Act & Assert using Turbine
            screenModel.uiState.test {
                // Initial state is loading
                val initial = awaitItem()
                assertTrue(initial.isLoading)

                // Call loadProfile and await transitions
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()

                // The initial loading state equals the first update from loadProfile,
                // so StateFlow skips that emission. We await the error state directly.
                val errorState = awaitItem()
                assertFalse(errorState.isLoading)
                assertTrue(errorState.error != null)

                cancelAndIgnoreRemainingEvents()
            }
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
                    logger = Logger.withTag("Test"),
                )

            // Load profile and wait for success
            screenModel.uiState.test {
                val initial = awaitItem()
                assertTrue(initial.isLoading)

                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()

                // Skip loading state
                val loading = awaitItem()
                if (loading.isLoading) {
                    val success = awaitItem()
                    assertFalse(success.isLoading)
                    assertEquals(profile, success.profile)
                } else {
                    assertFalse(loading.isLoading)
                    assertEquals(profile, loading.profile)
                }

                cancelAndIgnoreRemainingEvents()
            }

            // Act
            screenModel.startEditing()

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
                    logger = Logger.withTag("Test"),
                )

            // Load profile
            screenModel.uiState.test {
                val initial = awaitItem()
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                // Skip to loaded state
                if (awaitItem().isLoading) {
                    awaitItem() // success
                }
                cancelAndIgnoreRemainingEvents()
            }

            // Enter edit mode and modify
            screenModel.startEditing()
            screenModel.onFullNameChanged("Changed")

            // Act
            screenModel.cancelEditing()

            // Assert
            val state = screenModel.uiState.value
            assertFalse(state.isEditing)
            assertEquals("Original", state.editFullName)
            assertEquals("+1111111111", state.editPhone)
        }

    @Test
    fun `onFullNameChanged should update editFullName and validate`() =
        runTest {
            // Arrange
            mockRepository.getProfileResult = Result.success(createTestProfile())
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                    logger = Logger.withTag("Test"),
                )

            // Load profile
            screenModel.uiState.test {
                val initial = awaitItem()
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                if (awaitItem().isLoading) {
                    awaitItem()
                }
                cancelAndIgnoreRemainingEvents()
            }

            // Act
            screenModel.onFullNameChanged("New Name")

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
                    logger = Logger.withTag("Test"),
                )

            // Load profile
            screenModel.uiState.test {
                val initial = awaitItem()
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                if (awaitItem().isLoading) {
                    awaitItem()
                }
                cancelAndIgnoreRemainingEvents()
            }

            // Act
            screenModel.onPhoneChanged("+9998887777")

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
                    logger = Logger.withTag("Test"),
                )

            // Load profile and wait for success state
            screenModel.uiState.test {
                val initial = awaitItem()
                assertTrue(initial.isLoading)
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                val success = awaitItem()
                assertFalse(success.isLoading)
                assertEquals(originalProfile, success.profile)
                cancelAndIgnoreRemainingEvents()
            }

            // Enter edit mode
            screenModel.startEditing()
            screenModel.onFullNameChanged("Updated")
            screenModel.onPhoneChanged("+2222222222")

            // Act - call saveProfile
            screenModel.saveProfile()
            testDispatcher.scheduler.runCurrent()

            // Assert - StateFlow always has latest value
            val state = screenModel.uiState.value
            assertFalse(state.isEditing)
            assertTrue(state.saveSuccess)
            assertEquals(updatedProfile, state.profile)
            assertFalse(state.isSaving)
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
                    logger = Logger.withTag("Test"),
                )

            // Load profile and wait for success state
            screenModel.uiState.test {
                val initial = awaitItem()
                assertTrue(initial.isLoading)
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                val success = awaitItem()
                assertFalse(success.isLoading)
                assertEquals(profile, success.profile)
                cancelAndIgnoreRemainingEvents()
            }

            // Enter edit mode
            screenModel.startEditing()
            screenModel.onFullNameChanged("Changed")

            // Act - call saveProfile
            screenModel.saveProfile()
            testDispatcher.scheduler.runCurrent()

            // Assert - StateFlow always has latest value
            val state = screenModel.uiState.value
            assertFalse(state.saveSuccess)
            assertTrue(state.error != null)
        }

    @Test
    fun `clearError should clear error state`() =
        runTest {
            // Arrange
            mockRepository.getProfileResult = Result.failure(RuntimeException("Error"))
            val screenModel =
                ProfileScreenModel(
                    getProfileUseCase = getProfileUseCase,
                    updateProfileUseCase = updateProfileUseCase,
                    logoutUseCase = logoutUseCase,
                    catalogNavigator = catalogNavigator,
                    logger = Logger.withTag("Test"),
                )

            // Load profile to trigger error
            screenModel.uiState.test {
                val initial = awaitItem()
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                if (awaitItem().isLoading) {
                    awaitItem() // error state
                }
                cancelAndIgnoreRemainingEvents()
            }

            // Act
            screenModel.clearError()

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
                    logger = Logger.withTag("Test"),
                )

            // Load profile and wait for success state
            screenModel.uiState.test {
                val initial = awaitItem()
                assertTrue(initial.isLoading)
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                val success = awaitItem()
                assertFalse(success.isLoading)
                assertEquals(profile, success.profile)
                cancelAndIgnoreRemainingEvents()
            }

            // Save profile to set saveSuccess
            screenModel.startEditing()
            screenModel.onFullNameChanged("Updated")

            // Call saveProfile and let it complete
            screenModel.saveProfile()
            testDispatcher.scheduler.runCurrent()

            // Verify saveSuccess is set
            assertTrue(screenModel.uiState.value.saveSuccess)

            // Act
            screenModel.clearSaveSuccess()

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
                    logger = Logger.withTag("Test"),
                )

            // Load profile
            screenModel.uiState.test {
                val initial = awaitItem()
                screenModel.loadProfile()
                testDispatcher.scheduler.runCurrent()
                if (awaitItem().isLoading) {
                    awaitItem()
                }
                cancelAndIgnoreRemainingEvents()
            }

            // Act
            screenModel.logout(mockNavigator)
            testDispatcher.scheduler.runCurrent()

            // Assert
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