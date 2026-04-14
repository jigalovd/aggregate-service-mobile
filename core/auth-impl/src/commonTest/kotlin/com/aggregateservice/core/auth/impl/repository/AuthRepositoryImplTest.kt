package com.aggregateservice.core.auth.impl.repository

import com.aggregateservice.core.auth.impl.repository.dto.AuthResponse
import com.aggregateservice.core.auth.impl.repository.dto.UserResponse
import com.aggregateservice.core.auth.state.VerifyResult
import com.aggregateservice.core.storage.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var repository: AuthRepositoryImpl
    private lateinit var tokenStore: FakeTokenStore

    @BeforeTest
    fun setup() {
        tokenStore = FakeTokenStore()
        val mockEngine =
            MockEngine { request ->
                when {
                    request.url.encodedPath.contains("/auth/me") -> {
                        respond(
                            content =
                                json.encodeToString(
                                    UserResponse.serializer(),
                                    UserResponse(id = "user-123", email = "test@example.com"),
                                ),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                    else -> {
                        respond(
                            content =
                                json.encodeToString(
                                    AuthResponse.serializer(),
                                    AuthResponse(
                                        accessToken = "test-token",
                                        refreshToken = "test-refresh-token",
                                        user = UserResponse(id = "user-123", email = "test@example.com"),
                                    ),
                                ),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                }
            }
        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) { json(json) }
            }
        repository =
            AuthRepositoryImpl(
                httpClient = httpClient,
                authClient = httpClient,
                tokenStore = tokenStore,
            )
    }

    @Test
    fun `verifyFirebaseToken returns VerifyResult Authenticated on success`() =
        runTest {
            val result = repository.verifyFirebaseToken("google", "firebase-token")
            assertTrue(result.isSuccess)
            val verifyResult = result.getOrNull()
            assertIs<VerifyResult.Authenticated>(verifyResult)
            assertEquals("test-token", verifyResult.accessToken)
            assertEquals("user-123", verifyResult.userId)
        }

    @Test
    fun `getCurrentUser returns UserResponse on success`() =
        runTest {
            val result = repository.getCurrentUser()
            assertTrue(result.isSuccess)
            assertEquals("user-123", result.getOrNull()?.id)
        }

    @Test
    fun `verifyFirebaseToken saves tokens to TokenStore`() =
        runTest {
            assertNull(tokenStore.savedRefreshToken)
            repository.verifyFirebaseToken("google", "firebase-token")
            assertEquals("test-refresh-token", tokenStore.savedRefreshToken)
            assertEquals("test-token", tokenStore.savedAccessToken)
        }
}

class FakeTokenStore : TokenStore {
    var savedAccessToken: String? = null
        private set
    var savedRefreshToken: String? = null
        private set

    override suspend fun getAccessToken(): String? = savedAccessToken

    override suspend fun getRefreshToken(): String? = savedRefreshToken

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        savedAccessToken = accessToken
        savedRefreshToken = refreshToken
    }

    override suspend fun clearTokens() {
        savedAccessToken = null
        savedRefreshToken = null
    }
}
