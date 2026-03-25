package com.aggregateservice.feature.auth.presentation.screenmodel

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.model.RegistrationRequest
import com.aggregateservice.feature.auth.domain.usecase.LoginUseCase
import com.aggregateservice.feature.auth.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Initial)

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase
    private lateinit var screenModel: LoginScreenModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

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

        observeAuthStateUseCase = ObserveAuthStateUseCase(
            repository = object : com.aggregateservice.feature.auth.domain.repository.AuthRepository {
                override fun observeAuthState(): StateFlow<AuthState> = authStateFlow
                override fun getCurrentAuthState(): AuthState = authStateFlow.value
                override suspend fun login(credentials: LoginCredentials): Result<AuthState> {
                    return Result.failure(NotImplementedError("Use mock"))
                }
                override suspend fun logout() {}
                override suspend fun refreshToken(): Result<String> {
                    return Result.failure(NotImplementedError("Use mock"))
                }
                override suspend fun register(request: RegistrationRequest): Result<AuthState> {
                    return Result.failure(NotImplementedError("Use mock"))
                }
            }
        )
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty email and password`() {
        loginUseCase = LoginUseCase(
            repository = createMockRepository(Result.success(
                AuthState.Authenticated(accessToken = "token", userId = "test@test.com", userEmail = "test@test.com")
            ))
        )
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        val state = screenModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onEmailChanged updates email and clears error when valid`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals("test@example.com", state.email)
        assertNull(state.emailError)
    }

    @Test
    fun `onEmailChanged sets error when email is invalid`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("invalid-email")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals("invalid-email", state.email)
        assertTrue(state.emailError != null)
    }

    @Test
    fun `onPasswordChanged updates password and clears error when valid`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals("ValidPassword123", state.password)
        assertNull(state.passwordError)
    }

    @Test
    fun `onPasswordChanged sets error when password is too short`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onPasswordChanged("short")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals("short", state.password)
        assertTrue(state.passwordError != null)
    }

    @Test
    fun `clearError removes all error messages`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("invalid")
        screenModel.onPasswordChanged("short")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.uiState.value.emailError != null)
        assertTrue(screenModel.uiState.value.passwordError != null)

        screenModel.clearError()

        val state = screenModel.uiState.value
        assertNull(state.errorMessage)
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `canLogin returns false when email is empty`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(screenModel.uiState.value.canLogin())
    }

    @Test
    fun `canLogin returns false when password is empty`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(screenModel.uiState.value.canLogin())
    }

    @Test
    fun `canLogin returns true when all fields are valid`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(AuthState.Initial)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.uiState.value.canLogin())
    }

    @Test
    fun `onLoginClick sets loading state during login`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(
            AuthState.Authenticated(accessToken = "token", userId = "test@test.com", userEmail = "test@test.com")
        )))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onLoginClick()

        assertTrue(screenModel.uiState.value.isLoading)
    }

    @Test
    fun `onLoginClick sets isLoginSuccess on successful login`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(
            AuthState.Authenticated(accessToken = "token", userId = "test@test.com", userEmail = "test@test.com")
        )))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoginSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onLoginClick sets errorMessage on login failure`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.failure(AppError.Unauthorized)))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccess)
        assertTrue(state.errorMessage != null)
        assertTrue(state.errorMessage!!.contains("Неверный email или пароль"))
    }

    @Test
    fun `onLoginClick handles AccountLocked error`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.failure(AppError.AccountLocked("2024-01-01T00:00:00Z"))))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertTrue(state.errorMessage != null)
        assertTrue(state.errorMessage!!.contains("заблокирован"))
    }

    @Test
    fun `onLoginClick handles NetworkError`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.failure(AppError.NetworkError(500, "Server error"))))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertTrue(state.errorMessage != null)
        assertTrue(state.errorMessage!!.contains("Ошибка сети"))
    }

    @Test
    fun `onLoginClick does nothing when canLogin is false`() = runTest {
        loginUseCase = LoginUseCase(createMockRepository(Result.success(
            AuthState.Authenticated(accessToken = "token", userId = "test@test.com", userEmail = "test@test.com")
        )))
        screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase)

        screenModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(screenModel.uiState.value.isLoading)
        assertFalse(screenModel.uiState.value.isLoginSuccess)
    }

    private fun createMockRepository(result: Result<AuthState>): com.aggregateservice.feature.auth.domain.repository.AuthRepository {
        return object : com.aggregateservice.feature.auth.domain.repository.AuthRepository {
            override fun observeAuthState(): StateFlow<AuthState> = authStateFlow
            override fun getCurrentAuthState(): AuthState = authStateFlow.value
            override suspend fun login(credentials: LoginCredentials): Result<AuthState> = result
            override suspend fun logout() {}
            override suspend fun refreshToken(): Result<String> = Result.success("new_token")
            override suspend fun register(request: RegistrationRequest): Result<AuthState> = result
        }
    }
}
