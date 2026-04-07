package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BearerTokenPluginTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `bearer token is sent on regular requests`() = runTest {
        val mockEngine = MockEngine { request ->
            val hasAuth = request.headers.contains(HttpHeaders.Authorization)
            if (hasAuth) {
                respondOk()
            } else {
                respond(content = "", status = HttpStatusCode.Unauthorized)
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
            install(Auth) {
                bearer {
                    loadTokens { BearerTokens("test_token", "") }
                }
            }
        }

        val response = client.get("http://test/api/v1/auth/me")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(mockEngine.requestHistory.first().headers.contains(HttpHeaders.Authorization))
    }

    @Test
    fun `401 triggers refresh and retry`() = runTest {
        var tokenHolder = BearerTokens("old_token", "")
        val mockEngine = MockEngine { request ->
            when (request.headers[HttpHeaders.Authorization]) {
                "Bearer old_token" -> respond(
                    content = "",
                    status = HttpStatusCode.Unauthorized,
                )
                "Bearer new_token" -> respondOk()
                else -> respond(content = "", status = HttpStatusCode.Unauthorized)
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
            install(Auth) {
                bearer {
                    loadTokens { tokenHolder }
                    refreshTokens {
                        tokenHolder = BearerTokens("new_token", "")
                        tokenHolder
                    }
                }
            }
        }

        val response = client.get("http://test/api/v1/auth/me")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, mockEngine.requestHistory.size)
    }

    @Test
    fun `refresh failure does not loop infinitely`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.Unauthorized)
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
            install(Auth) {
                bearer {
                    loadTokens { BearerTokens("expired_token", "") }
                    refreshTokens { null }
                }
            }
        }

        val response = client.get("http://test/api/v1/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(
            mockEngine.requestHistory.size <= 3,
            "Should not retry more than necessary after refresh failure",
        )
    }

    @Test
    fun `no auth header when no tokens available`() = runTest {
        val mockEngine = MockEngine { _ ->
            respondOk()
        }

        val client = HttpClient(mockEngine) {
            install(Auth) {
                bearer {
                    loadTokens { null }
                }
            }
        }

        client.get("http://test/api/v1/catalog/categories")
        val request = mockEngine.requestHistory.first()
        assertFalse(request.headers.contains(HttpHeaders.Authorization))
    }
}
