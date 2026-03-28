package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.model.RegistrationRequest
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveAuthStateUseCaseTest {

    private lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase
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
        observeAuthStateUseCase = ObserveAuthStateUseCase(mockRepository)
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
    }

    @Test
    fun `should return initial state by default`() = runTest {
        val stateFlow = observeAuthStateUseCase()
        val state = stateFlow.first()

        assertFalse(state.isAuthenticated)
        assertEquals(AuthState.Guest, state)
    }

    @Test
    fun `should return authenticated state after login`() = runTest {
        val token = "test_access_token"
        val email = "test@example.com"
        mockRepository.setAuthenticated(token, email)

        val stateFlow = observeAuthStateUseCase()
        val state = stateFlow.first()

        assertTrue(state.isAuthenticated)
        when (state) {
            is AuthState.Authenticated -> {
                assertEquals(token, state.accessToken)
                assertEquals(email, state.userEmail)
            }
            is AuthState.Guest -> throw AssertionError("Expected Authenticated state")
        }
    }

    @Test
    fun `should emit new state when auth state changes`() = runTest {
        val stateFlow = observeAuthStateUseCase()

        // Initial state
        var state = stateFlow.first()
        assertFalse(state.isAuthenticated)

        // After login
        mockRepository.setAuthenticated("token", "user@example.com")
        state = stateFlow.first()
        assertTrue(state.isAuthenticated)

        // After logout
        mockRepository.setUnauthenticated()
        state = stateFlow.first()
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun `should return same StateFlow instance on multiple invocations`() = runTest {
        val flow1 = observeAuthStateUseCase()
        val flow2 = observeAuthStateUseCase()

        // Should return the same underlying flow from repository
        assertEquals(flow1.first(), flow2.first())
    }

    @Test
    fun `should reflect current authentication status`() = runTest {
        val stateFlow = observeAuthStateUseCase()

        // Initially not authenticated
        assertFalse(stateFlow.value.isAuthenticated)

        // After setting authenticated
        mockRepository.setAuthenticated("token123", "user@test.com")
        assertTrue(stateFlow.value.isAuthenticated)

        // After logout
        mockRepository.setUnauthenticated()
        assertFalse(stateFlow.value.isAuthenticated)
    }

    @Test
    fun `should preserve user email in authenticated state`() = runTest {
        val email = "specific.user@domain.com"
        mockRepository.setAuthenticated("token", email)

        val stateFlow = observeAuthStateUseCase()
        val state = stateFlow.first()

        assertTrue(state.isAuthenticated)
        when (state) {
            is AuthState.Authenticated -> assertEquals(email, state.userEmail)
            is AuthState.Guest -> throw AssertionError("Expected Authenticated state")
        }
    }

    @Test
    fun `should preserve access token in authenticated state`() = runTest {
        val token = "unique_access_token_xyz"
        mockRepository.setAuthenticated(token, "user@test.com")

        val stateFlow = observeAuthStateUseCase()
        val state = stateFlow.first()

        assertTrue(state.isAuthenticated)
        when (state) {
            is AuthState.Authenticated -> assertEquals(token, state.accessToken)
            is AuthState.Guest -> throw AssertionError("Expected Authenticated state")
        }
    }

    /**
     * Mock implementation of AuthRepository for testing
     */
    private class MockAuthRepository : AuthRepository {
        private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Guest)

        fun setAuthenticated(token: String, email: String) {
            authStateFlow.value = AuthState.Authenticated(
                accessToken = token,
                userId = email,
                userEmail = email
            )
        }

        fun setUnauthenticated() {
            authStateFlow.value = AuthState.Guest
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
            authStateFlow.value = AuthState.Guest
        }

        override suspend fun refreshToken(): Result<String> {
            return Result.success("new_token")
        }

        override suspend fun register(request: RegistrationRequest): Result<AuthState> {
            val state = AuthState.Authenticated(
                accessToken = "token",
                userId = request.email,
                userEmail = request.email
            )
            authStateFlow.value = state
            return Result.success(state)
        }

        override suspend fun verifyFirebaseToken(authProvider: String, firebaseToken: String): Result<AuthState> {
            return Result.success(AuthState.Authenticated(
                accessToken = "token",
                userId = "test",
                userEmail = "test@test.com"
            ))
        }

        override suspend fun linkFirebaseAccount(tempToken: String, password: String): Result<AuthState> {
            return Result.success(AuthState.Authenticated(
                accessToken = "token",
                userId = "test",
                userEmail = "test@test.com"
            ))
        }
    }
}
