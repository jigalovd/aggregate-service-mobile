package com.aggregateservice.core.auth.impl.repository

import com.aggregateservice.core.auth.impl.repository.dto.AuthResponse
import com.aggregateservice.core.auth.impl.repository.dto.UserResponse
import com.aggregateservice.core.auth.state.VerifyResult
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var repository: AuthRepositoryImpl
    private lateinit var tokenStorage: FakeTokenStorage

    @BeforeTest
    fun setup() {
        tokenStorage = FakeTokenStorage()
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
                tokenStorage = tokenStorage,
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
    fun `verifyFirebaseToken saves refresh_token to TokenStorage`() =
        runTest {
            assertNull(tokenStorage.savedRefreshToken)
            repository.verifyFirebaseToken("google", "firebase-token")
            assertEquals("test-refresh-token", tokenStorage.savedRefreshToken)
        }
}

class FakeTokenStorage : TokenStorage {
    var savedRefreshToken: String? = null
        private set

    override fun getAccessToken(): Flow<String?> = flowOf(null)

    override suspend fun getAccessTokenSync(): String? = null

    override suspend fun saveAccessToken(token: String) = Unit

    override suspend fun getRefreshTokenSync(): String? = null

    override suspend fun saveRefreshToken(token: String) {
        savedRefreshToken = token
    }

    override suspend fun clearTokens() {
        savedRefreshToken = null
    }
}
