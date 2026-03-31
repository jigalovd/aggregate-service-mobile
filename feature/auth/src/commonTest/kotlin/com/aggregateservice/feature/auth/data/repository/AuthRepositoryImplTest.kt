package com.aggregateservice.feature.auth.data.repository

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.feature.auth.data.dto.AuthResponse
import com.aggregateservice.feature.auth.data.dto.FirebaseAlreadyLinkedResponse
import com.aggregateservice.feature.auth.data.dto.FirebaseLinkRequiredResponse
import com.aggregateservice.feature.auth.data.dto.FirebaseUserResponse
import com.aggregateservice.feature.auth.data.dto.FirebaseVerifyResponse
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.test.assertIs
import kotlin.test.assertTrue
import io.mockk.mockk
import app.cash.turbine.test

class AuthRepositoryImplTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        classDiscriminator = "response_type"
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
        val userId = "user-123"

        var savedToken: String? = null
        val tokenStorage = createMockTokenStorage(
            onSaveAccessToken = { savedToken = it }
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(
                    AuthResponse(
                        accessToken = accessToken,
                        user = FirebaseUserResponse(
                            id = userId,
                            email = email
                        )
                    )
                ),
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
                content = json.encodeToString(
                    AuthResponse(
                        accessToken = "token",
                        user = FirebaseUserResponse(id = "user-123", email = "test@example.com")
                    )
                ),
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
        val userId = "user-456"

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(
                    AuthResponse(
                        accessToken = accessToken,
                        user = FirebaseUserResponse(id = userId, email = "test@example.com")
                    )
                ),
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

    // MARK: - MockK Tests with Turbine (Plan 03)

    private val testUserId = "550e8400-e29b-41d4-a716-446655440000"
    private val testEmail = "test@example.com"
    private val testAccessToken = "test-access-token"

    private lateinit var mockkHttpClient: HttpClient
    private lateinit var mockkTokenStorage: TokenStorage
    private lateinit var mockkRepository: AuthRepositoryImpl

    @BeforeTest
    fun mockkSetup() {
        mockkHttpClient = mockk(relaxed = true)
        mockkTokenStorage = mockk(relaxed = true)
        mockkRepository = AuthRepositoryImpl(mockkHttpClient, mockkTokenStorage)
    }

    @AfterTest
    fun mockkTearDown() {
        // Clean up if needed
    }

    @Test
    fun `verifyFirebaseToken returns userId from firebaseResponse`() = runTest {
        // Arrange
        val firebaseResponse: FirebaseVerifyResponse = FirebaseAlreadyLinkedResponse(
            accessToken = testAccessToken,
            message = "Already linked",
            user = FirebaseUserResponse(
                id = testUserId,
                email = testEmail,
                isActive = true,
                isVerified = true,
                roles = listOf("client"),
                currentRole = "client"
            )
        )

        // Create a mock that returns the correct response type
        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString<FirebaseVerifyResponse>(firebaseResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val repository = AuthRepositoryImpl(httpClient, mockkTokenStorage)

        // Act
        val result = repository.verifyFirebaseToken("google", "firebase-token")

        // Assert
        assertTrue(result.isSuccess)
        val authState = result.getOrNull()
        assertIs<AuthState.Authenticated>(authState)
        assertEquals(testUserId, authState.userId)
        assertEquals(testEmail, authState.userEmail)
        assertEquals(testAccessToken, authState.accessToken)
    }

    @Test
    fun `linkFirebaseAccount returns userId from authResponse`() = runTest {
        // Arrange
        val authResponse = AuthResponse(
            accessToken = testAccessToken,
            user = FirebaseUserResponse(
                id = testUserId,
                email = testEmail,
                isActive = true,
                isVerified = true,
                roles = listOf("client"),
                currentRole = "client"
            )
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(authResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val repository = AuthRepositoryImpl(httpClient, mockkTokenStorage)

        // Act
        val result = repository.linkFirebaseAccount("temp-token", "password")

        // Assert
        assertTrue(result.isSuccess)
        val authState = result.getOrNull()
        assertIs<AuthState.Authenticated>(authState)
        assertEquals(testUserId, authState.userId)
        assertEquals(testEmail, authState.userEmail)
    }

    @Test
    fun `observeAuthState emits updated state after verifyFirebaseToken succeeds`() = runTest {
        // Arrange
        val firebaseResponse: FirebaseVerifyResponse = FirebaseAlreadyLinkedResponse(
            accessToken = testAccessToken,
            message = "Already linked",
            user = FirebaseUserResponse(
                id = testUserId,
                email = testEmail,
                isActive = true,
                isVerified = true,
                roles = listOf("client"),
                currentRole = "client"
            )
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString<FirebaseVerifyResponse>(firebaseResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val repository = AuthRepositoryImpl(httpClient, mockkTokenStorage)

        // Act
        repository.verifyFirebaseToken("google", "firebase-token")

        // Assert - collect using Turbine test{}
        repository._authState.test {
            val state = awaitItem()
            assertIs<AuthState.Authenticated>(state)
            assertEquals(testUserId, state.userId)
            assertEquals(testAccessToken, state.accessToken)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Plan 07-02 Tests (link-required, link flow, unknown response_type)

    @Test
    fun `verifyFirebaseToken returns FirebaseLinkRequired when response_type is link_required`() = runTest {
        // Arrange
        val linkRequiredResponse: FirebaseVerifyResponse = FirebaseLinkRequiredResponse(
            email = "existing@example.com",
            firebaseUid = "firebase-uid-123",
            provider = "google.com",
            message = "Account linking required",
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString<FirebaseVerifyResponse>(linkRequiredResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val repository = AuthRepositoryImpl(httpClient, createMockTokenStorage())

        // Act
        val result = repository.verifyFirebaseToken("google.com", "original-firebase-token")

        // Assert
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AppError.FirebaseLinkRequired>(error)
        assertEquals("existing@example.com", error.email)
        assertEquals("firebase-uid-123", error.firebaseUid)
        assertEquals("google.com", error.provider)
        assertEquals("original-firebase-token", error.firebaseToken)
    }

    @Test
    fun `verifyFirebaseToken then linkFirebaseAccount completes link flow`() = runTest {
        // Arrange - verify returns link_required
        val linkRequiredResponse: FirebaseVerifyResponse = FirebaseLinkRequiredResponse(
            email = "existing@example.com",
            firebaseUid = "firebase-uid-123",
            provider = "google.com",
        )

        var requestCount = 0
        val mockEngine = MockEngine { _ ->
            requestCount++
            if (requestCount == 1) {
                // First call: verify returns link_required
                respond(
                    content = json.encodeToString<FirebaseVerifyResponse>(linkRequiredResponse),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                // Second call: link returns AuthResponse
                val authResponse = AuthResponse(
                    accessToken = "linked-access-token",
                    user = FirebaseUserResponse(
                        id = "user-id-123",
                        email = "existing@example.com",
                        isActive = true,
                        isVerified = true,
                        roles = listOf("client"),
                        currentRole = "client",
                    )
                )
                respond(
                    content = json.encodeToString(authResponse),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val repository = AuthRepositoryImpl(httpClient, createMockTokenStorage())

        // Act - Step 1: verify
        val verifyResult = repository.verifyFirebaseToken("google.com", "original-firebase-token")
        assertTrue(verifyResult.isFailure)
        val linkError = verifyResult.exceptionOrNull() as AppError.FirebaseLinkRequired

        // Act - Step 2: link using firebaseToken from error
        val linkResult = repository.linkFirebaseAccount(linkError.firebaseToken, "user-password")

        // Assert - link succeeds
        assertTrue(linkResult.isSuccess)
        val authState = linkResult.getOrNull()
        assertIs<AuthState.Authenticated>(authState)
        assertEquals("user-id-123", authState.userId)
    }

    @Test
    fun `verifyFirebaseToken throws on unknown response_type`() = runTest {
        // Arrange - response with unknown discriminator
        val unknownJson = """{"response_type": "unknown_type", "email": "test@test.com"}"""

        val mockEngine = MockEngine { _ ->
            respond(
                content = unknownJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val repository = AuthRepositoryImpl(httpClient, createMockTokenStorage())

        // Act
        val result = repository.verifyFirebaseToken("google.com", "firebase-token")

        // Assert - deserialization fails for unknown discriminator value
        assertTrue(result.isFailure)
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
