package com.aggregateservice.feature.auth.data.repository

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.feature.auth.data.dto.AuthResponse
import com.aggregateservice.feature.auth.data.dto.RefreshTokenResponse
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthRepositoryErrorHandlingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

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
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
    }

    @Test
    fun `should return Unauthorized error on 401 response`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "Invalid credentials"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertTrue(result.isFailure, "Login should fail with 401")
        assertTrue(result.exceptionOrNull() is AppError.Unauthorized, "Should be Unauthorized error")
    }

    @Test
    fun `should return Forbidden error on 403 response`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "Access denied"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertTrue(result.isFailure, "Login should fail with 403")
        assertTrue(result.exceptionOrNull() is AppError.Forbidden, "Should be Forbidden error")
    }

    @Test
    fun `should return NotFound error on 404 response`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "User not found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertTrue(result.isFailure, "Login should fail with 404")
        assertTrue(result.exceptionOrNull() is AppError.NotFound, "Should be NotFound error")
    }

    @Test
    fun `should return AccountLocked error on 423 response`() = runTest {
        val lockedUntil = "2024-12-31T23:59:59Z"
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "$lockedUntil"}""",
                status = HttpStatusCode.Locked,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertTrue(result.isFailure, "Login should fail with 423")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.AccountLocked, "Should be AccountLocked error, got: $error")
        assertEquals(lockedUntil, error.until)
    }

    @Test
    fun `should return RateLimitExceeded error on 429 response`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "Too many requests"}""",
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    "Retry-After" to listOf("60")
                )
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertTrue(result.isFailure, "Login should fail with 429")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.RateLimitExceeded, "Should be RateLimitExceeded error, got: $error")
    }

    @Test
    fun `should return NetworkError on 500 response after retries`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "Internal server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertTrue(result.isFailure, "Login should fail with 500")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.NetworkError, "Should be NetworkError, got: $error")
        assertEquals(500, error.code)
    }

    @Test
    fun `should return NetworkError on connection failure`() = runTest {
        val mockEngine = MockEngine { _ ->
            throw Exception("Connection refused")
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertTrue(result.isFailure, "Login should fail with connection error")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.UnknownError || error is AppError.NetworkError,
            "Should be error type, got: $error")
    }

    @Test
    fun `should not save token on failed login`() = runTest {
        var saveTokenCalled = false
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage(
            onSaveAccessToken = { saveTokenCalled = true }
        )
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        assertFalse(saveTokenCalled, "Token should not be saved on failed login")
    }

    @Test
    fun `should handle refresh token successfully`() = runTest {
        val newAccessToken = "new_access_token_xyz"
        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(RefreshTokenResponse(newAccessToken)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        var savedToken: String? = null
        val tokenStorage = createMockTokenStorage(
            onSaveAccessToken = { savedToken = it }
        )
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.refreshToken()

        assertTrue(result.isSuccess, "Refresh should succeed")
        assertEquals(newAccessToken, result.getOrNull())
        assertEquals(newAccessToken, savedToken)
    }

    @Test
    fun `should return error when refresh token fails`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"detail": "Refresh token expired"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage()
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.refreshToken()

        assertTrue(result.isFailure, "Refresh should fail")
        assertTrue(result.exceptionOrNull() is AppError.Unauthorized, "Should be Unauthorized error")
    }

    @Test
    fun `should initialize with saved token`() = runTest {
        val savedToken = "saved_access_token"
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage(
            onGetAccessTokenSync = { savedToken }
        )
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        // Initialize loads saved token
        repository.initialize()

        // After initialization, auth state should reflect saved token
        val authState = repository.getCurrentAuthState()
        assertTrue(authState.isAuthenticated, "Should be authenticated with saved token")
        when (authState) {
            is AuthState.Authenticated -> assertEquals(savedToken, authState.accessToken)
            is AuthState.Guest -> throw AssertionError("Expected Authenticated state")
        }
    }

    @Test
    fun `should initialize as unauthenticated when no saved token`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, listOf("application/json"))
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val tokenStorage = createMockTokenStorage(
            onGetAccessTokenSync = { null }
        )
        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        repository.initialize()

        val authState = repository.getCurrentAuthState()
        assertFalse(authState.isAuthenticated)
    }

    private fun createHttpClient(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    private fun createMockTokenStorage(
        onGetAccessToken: () -> String? = { null },
        onGetAccessTokenSync: () -> String? = { null },
        onSaveAccessToken: suspend (String) -> Unit = {},
        onClearTokens: suspend () -> Unit = {}
    ): TokenStorage {
        return object : TokenStorage {
            override fun getAccessToken() = flowOf(onGetAccessToken())
            override suspend fun getAccessTokenSync() = onGetAccessTokenSync()
            override suspend fun saveAccessToken(token: String) = onSaveAccessToken(token)
            override suspend fun clearTokens() = onClearTokens()
        }
    }
}
