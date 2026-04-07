package com.aggregateservice.core.network

import com.aggregateservice.core.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthManagerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `loadTokens returns BearerTokens when token exists`() = runTest {
        val tokenStorage = TestTokenStorage("existing_token")
        val authEventBus = AuthEventBus()
        val authManager = AuthManager(
            httpClientProvider = { HttpClient(MockEngine) },
            tokenStorage = tokenStorage,
            authEventBus = authEventBus,
        )

        val tokens = authManager.loadTokens()
        assertNotNull(tokens)
        assertEquals("existing_token", tokens.accessToken)
    }

    @Test
    fun `loadTokens returns null when no token`() = runTest {
        val tokenStorage = TestTokenStorage(null)
        val authEventBus = AuthEventBus()
        val authManager = AuthManager(
            httpClientProvider = { HttpClient(MockEngine) },
            tokenStorage = tokenStorage,
            authEventBus = authEventBus,
        )

        val tokens = authManager.loadTokens()
        assertNull(tokens)
    }

    @Test
    fun `refreshTokens returns new tokens on 200`() = runTest {
        val tokenStorage = TestTokenStorage("old_token")
        val authEventBus = AuthEventBus()
        val refreshResponse = """{"accessToken":"new_token"}"""

        val mockEngine = MockEngine { _ ->
            respond(
                content = refreshResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val authManager = AuthManager(
            httpClientProvider = { client },
            tokenStorage = tokenStorage,
            authEventBus = authEventBus,
        )

        val result = authManager.refreshTokens()
        assertNotNull(result)
        assertEquals("new_token", result.accessToken)
        assertEquals("new_token", tokenStorage.savedToken)
    }

    @Test
    fun `refreshTokens clears tokens and emits logout on 401`() = runTest {
        val tokenStorage = TestTokenStorage("old_token")
        var logoutReceived = false
        val authEventBus = AuthEventBus()

        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"message":"Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val authManager = AuthManager(
            httpClientProvider = { client },
            tokenStorage = tokenStorage,
            authEventBus = authEventBus,
        )

        authEventBus.events.collect { event ->
            if (event is AuthEvent.Logout) logoutReceived = true
        }

        val result = authManager.refreshTokens()
        assertNull(result)
        assertTrue(tokenStorage.cleared)
    }

    @Test
    fun `refreshTokens clears tokens and emits logout on exception`() = runTest {
        val tokenStorage = TestTokenStorage("old_token")
        val authEventBus = AuthEventBus()

        val mockEngine = MockEngine { _ ->
            throw RuntimeException("Connection failed")
        }
        val client = HttpClient(mockEngine)

        val authManager = AuthManager(
            httpClientProvider = { client },
            tokenStorage = tokenStorage,
            authEventBus = authEventBus,
        )

        val result = authManager.refreshTokens()
        assertNull(result)
        assertTrue(tokenStorage.cleared)
    }
}

private class TestTokenStorage(
    initialToken: String?,
) : TokenStorage {
    var savedToken: String? = initialToken
    var cleared = false

    override fun getAccessToken(): Flow<String?> = flowOf(savedToken)

    override suspend fun getAccessTokenSync(): String? = savedToken

    override suspend fun saveAccessToken(token: String) {
        savedToken = token
        cleared = false
    }

    override suspend fun clearTokens() {
        savedToken = null
        cleared = true
    }
}
