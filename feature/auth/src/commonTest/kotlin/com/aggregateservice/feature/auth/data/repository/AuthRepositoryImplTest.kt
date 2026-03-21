package com.aggregateservice.feature.auth.data.repository

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.feature.auth.data.dto.AuthResponse
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
import kotlinx.coroutines.flow.first
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

class AuthRepositoryImplTest {

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
    fun `should save token on successful login`() = runTest {
        val email = "test@example.com"
        val password = "ValidPassword123"
        val accessToken = "test_access_token_12345"

        var savedToken: String? = null
        val tokenStorage = createMockTokenStorage(
            onSaveAccessToken = { savedToken = it }
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(AuthResponse(accessToken)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val result = repository.login(
            LoginCredentials(email = email, password = password)
        )

        assertTrue(result.isSuccess, "Login should be successful")
        assertEquals(accessToken, savedToken, "Access token should be saved")

        val authState = result.getOrNull()!!
        assertTrue(authState.isAuthenticated, "Auth state should be authenticated")
        when (authState) {
            is AuthState.Authenticated -> {
                assertEquals(accessToken, authState.accessToken)
                assertEquals(email, authState.userEmail)
            }
            is AuthState.Guest -> throw AssertionError("Expected Authenticated state")
        }
    }

    @Test
    fun `should clear tokens on logout`() = runTest {
        var clearedTokens = false
        val tokenStorage = createMockTokenStorage(
            onClearTokens = { clearedTokens = true }
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(AuthResponse("token")),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )

        repository.logout()

        assertTrue(clearedTokens, "Tokens should be cleared on logout")
        val authState = repository.getCurrentAuthState()
        assertFalse(authState.isAuthenticated, "Auth state should not be authenticated after logout")
    }

    @Test
    fun `should observe auth state changes`() = runTest {
        val tokenStorage = createMockTokenStorage()
        val accessToken = "test_access_token"

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(AuthResponse(accessToken)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val repository = AuthRepositoryImpl(httpClient, tokenStorage)

        val initialState = repository.observeAuthState().first()
        assertFalse(initialState.isAuthenticated, "Initial auth state should not be authenticated")

        repository.login(
            LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        )
        val loggedInState = repository.observeAuthState().first()
        assertTrue(loggedInState.isAuthenticated, "Auth state should be authenticated after login")

        repository.logout()
        val loggedOutState = repository.observeAuthState().first()
        assertFalse(loggedOutState.isAuthenticated, "Auth state should not be authenticated after logout")
    }

    private fun createMockTokenStorage(
        onGetAccessToken: () -> String? = { null },
        onSaveAccessToken: suspend (String) -> Unit = {},
        onClearTokens: suspend () -> Unit = {}
    ): TokenStorage {
        return object : TokenStorage {
            override fun getAccessToken() = flowOf(onGetAccessToken())
            override suspend fun getAccessTokenSync() = onGetAccessToken()
            override suspend fun saveAccessToken(token: String) = onSaveAccessToken(token)
            override suspend fun clearTokens() = onClearTokens()
        }
    }
}
