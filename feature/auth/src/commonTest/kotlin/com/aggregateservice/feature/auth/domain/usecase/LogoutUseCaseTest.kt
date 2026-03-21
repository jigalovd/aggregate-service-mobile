package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogoutUseCaseTest {

    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var mockRepository: MockAuthRepository

    @BeforeTest
    fun setup() {
        Config.initialize(
            AppConfig(
                apiBaseUrl = "https://api.test.com",
                apiKey = "test-api-key",
                environmentCode = Environment.DEV.name,
                languageCode = Language.RU.code,
                isDebug = true,
                enableLogging = false,
                networkTimeoutMs = 30_000L,
                apiVersion = "v1",
                passwordMinLength = 12,
                passwordMaxLength = 128,
            )
        )
        mockRepository = MockAuthRepository()
        logoutUseCase = LogoutUseCase(mockRepository)
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
    }

    @Test
    fun `should call logout on repository`() = runTest {
        mockRepository.setAuthenticated("test_token", "test@example.com")
        assertTrue(mockRepository.isLogoutCalled.not())

        logoutUseCase()

        assertTrue(mockRepository.isLogoutCalled)
    }

    @Test
    fun `should clear authentication state after logout`() = runTest {
        mockRepository.setAuthenticated("test_token", "test@example.com")
        assertTrue(mockRepository.getCurrentAuthState().isAuthenticated)

        logoutUseCase()

        assertFalse(mockRepository.getCurrentAuthState().isAuthenticated)
    }

    @Test
    fun `should not throw exception when logging out unauthenticated user`() = runTest {
        // User is not authenticated (Initial state)
        assertFalse(mockRepository.getCurrentAuthState().isAuthenticated)

        // Should not throw
        logoutUseCase()

        assertTrue(mockRepository.isLogoutCalled)
    }

    @Test
    fun `should be callable multiple times without error`() = runTest {
        mockRepository.setAuthenticated("test_token", "test@example.com")

        // Logout multiple times should not throw
        logoutUseCase()
        logoutUseCase()
        logoutUseCase()

        assertTrue(mockRepository.isLogoutCalled)
    }

    @Test
    fun `should reset to initial state after logout`() = runTest {
        val token = "access_token_12345"
        val email = "user@example.com"
        mockRepository.setAuthenticated(token, email)

        val beforeLogout = mockRepository.getCurrentAuthState()
        assertTrue(beforeLogout.isAuthenticated)

        logoutUseCase()

        val afterLogout = mockRepository.getCurrentAuthState()
        assertFalse(afterLogout.isAuthenticated)
        assertEquals(AuthState.Initial, afterLogout)
    }

    /**
     * Mock implementation of AuthRepository for testing
     */
    private class MockAuthRepository : AuthRepository {
        private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Initial)
        var isLogoutCalled = false

        fun setAuthenticated(token: String, email: String) {
            authStateFlow.value = AuthState.Authenticated(
                accessToken = token,
                userId = email,
                userEmail = email
            )
        }

        override fun observeAuthState(): StateFlow<AuthState> = authStateFlow

        override fun getCurrentAuthState(): AuthState = authStateFlow.value

        override suspend fun login(credentials: LoginCredentials): Result<AuthState> {
            val state = AuthState.Authenticated(
                accessToken = "token",
                userId = credentials.email,
                userEmail = credentials.email
            )
            authStateFlow.value = state
            return Result.success(state)
        }

        override suspend fun logout() {
            isLogoutCalled = true
            authStateFlow.value = AuthState.Initial
        }

        override suspend fun refreshToken(): Result<String> {
            return Result.success("new_token")
        }
    }
}
