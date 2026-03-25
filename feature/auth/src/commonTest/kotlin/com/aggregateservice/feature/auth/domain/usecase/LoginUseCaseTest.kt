package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.model.RegistrationRequest
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private lateinit var loginUseCase: LoginUseCase
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
        loginUseCase = LoginUseCase(mockRepository)
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
    }

    @Test
    fun `should return authenticated state on successful login`() = runTest {
        val email = "test@example.com"
        val password = "ValidPassword123"
        val accessToken = "test_access_token"
        val expectedState = AuthState.Authenticated(
            accessToken = accessToken,
            userId = email,
            userEmail = email
        )
        mockRepository.loginResult = Result.success(expectedState)

        val credentials = LoginCredentials(email = email, password = password)
        val result = loginUseCase(credentials)

        assertTrue(result.isSuccess)
        assertEquals(expectedState, result.getOrNull())
        assertEquals(email, mockRepository.lastLoginCredentials?.email)
        assertEquals(password, mockRepository.lastLoginCredentials?.password)
    }

    @Test
    fun `should return validation error when email exceeds 255 characters`() = runTest {
        val longEmail = "a".repeat(250) + "@example.com" // 262 characters
        val credentials = LoginCredentials(email = longEmail, password = "ValidPassword123")

        val result = loginUseCase(credentials)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.ValidationError)
        assertEquals("email", (error as AppError.ValidationError).field)
        assertTrue(error.message.contains("255"))
    }

    @Test
    fun `should return unauthorized error when credentials are invalid`() = runTest {
        mockRepository.loginResult = Result.failure(AppError.Unauthorized)

        val credentials = LoginCredentials(email = "wrong@example.com", password = "WrongPassword")
        val result = loginUseCase(credentials)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.Unauthorized)
    }

    @Test
    fun `should return network error when server is unavailable`() = runTest {
        mockRepository.loginResult = Result.failure(AppError.NetworkError(503, "Service Unavailable"))

        val credentials = LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        val result = loginUseCase(credentials)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.NetworkError)
        assertEquals(503, (error as AppError.NetworkError).code)
    }

    @Test
    fun `should return account locked error when user is blocked`() = runTest {
        val lockedUntil = "2024-12-31T23:59:59Z"
        mockRepository.loginResult = Result.failure(AppError.AccountLocked(lockedUntil))

        val credentials = LoginCredentials(email = "locked@example.com", password = "ValidPassword123")
        val result = loginUseCase(credentials)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.AccountLocked)
        assertEquals(lockedUntil, (error as AppError.AccountLocked).until)
    }

    @Test
    fun `should return validation error when email format is invalid in repository`() = runTest {
        mockRepository.loginResult = Result.failure(
            AppError.ValidationError(field = "email", message = "Invalid email format")
        )

        val credentials = LoginCredentials(email = "invalid-email", password = "ValidPassword123")
        val result = loginUseCase(credentials)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.ValidationError)
    }

    @Test
    fun `should allow login with email at exactly 255 characters`() = runTest {
        // Email with exactly 255 characters
        val email = "a".repeat(243) + "@example.com" // 243 + 12 = 255
        assertEquals(255, email.length)

        val expectedState = AuthState.Authenticated(
            accessToken = "token",
            userId = email,
            userEmail = email
        )
        mockRepository.loginResult = Result.success(expectedState)

        val credentials = LoginCredentials(email = email, password = "ValidPassword123")
        val result = loginUseCase(credentials)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should pass credentials exactly as provided to repository`() = runTest {
        val email = "Test.User+tag@Example.Com" // Mixed case with special chars
        val password = "ComplexPassword!@#$%123"
        mockRepository.loginResult = Result.success(
            AuthState.Authenticated(
                accessToken = "token",
                userId = email,
                userEmail = email
            )
        )

        val credentials = LoginCredentials(email = email, password = password)
        loginUseCase(credentials)

        assertEquals(email, mockRepository.lastLoginCredentials?.email)
        assertEquals(password, mockRepository.lastLoginCredentials?.password)
    }

    @Test
    fun `should return error when repository throws unexpected exception`() = runTest {
        mockRepository.loginResult = Result.failure(RuntimeException("Unexpected error"))

        val credentials = LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        val result = loginUseCase(credentials)

        assertTrue(result.isFailure)
    }

    /**
     * Mock implementation of AuthRepository for testing
     */
    private class MockAuthRepository : AuthRepository {
        var loginResult: Result<AuthState> = Result.success(AuthState.Initial)
        var lastLoginCredentials: LoginCredentials? = null
        private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Initial)

        override fun observeAuthState(): StateFlow<AuthState> = authStateFlow

        override fun getCurrentAuthState(): AuthState = authStateFlow.value

        override suspend fun login(credentials: LoginCredentials): Result<AuthState> {
            lastLoginCredentials = credentials
            return loginResult
        }

        override suspend fun register(request: RegistrationRequest): Result<AuthState> {
            return Result.success(AuthState.Guest)
        }

        override suspend fun logout() {
            authStateFlow.value = AuthState.Initial
        }

        override suspend fun refreshToken(): Result<String> {
            return Result.success("new_token")
        }
    }
}
