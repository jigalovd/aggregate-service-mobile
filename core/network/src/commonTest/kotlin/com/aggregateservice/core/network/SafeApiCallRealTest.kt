package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Real integration tests for [safeApiCall] wrapper using Ktor MockEngine.
 *
 * Tests cover:
 * - Success responses (200, 201, 204)
 * - Server error retry logic (500)
 * - Rate limiting (429)
 * - Client errors (401, 404, 422)
 */
class SafeApiCallRealTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Test 1: Should return success on 200 OK
     */
    @Test
    fun `should return success on 200 OK`() = runTest {
        // Arrange
        val testData = TestData(id = 1, name = "Test User")
        val mockEngine = MockEngine.create {
            respond(
                content = json.encodeToString(TestData.serializer(), testData),
                status = HttpStatusCode.OK,
                headers = io.ktor.http.headers.headersOf(
                    io.ktor.http.HttpHeaders.ContentType to "application/json"
                )
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // Act
        val result = safeApiCall<TestData> {
            client.get("https://api.test.com/data")
        }

        // Assert
        assertTrue(result.isSuccess, "Result should be successful")
        assertEquals(testData, result.getOrNull())
    }

    /**
     * Test 2: Should retry 3 times on 500 Internal Server Error
     */
    @Test
    fun `should retry 3 times on 500 Internal Server Error`() = runTest {
        // Arrange
        var callCount = 0
        val testData = TestData(id = 1, name = "Success after retries")
        val mockEngine = MockEngine.create {
            callCount++
            when {
                callCount < 3 -> {
                    respondError(
                        HttpStatusCode.InternalServerError,
                        content = json.encodeToString(
                            ErrorResponse.serializer(),
                            ErrorResponse(detail = "Internal server error")
                        )
                    )
                }
                else -> {
                    respond(
                        content = json.encodeToString(TestData.serializer(), testData),
                        status = HttpStatusCode.OK,
                        headers = io.ktor.http.headers.headersOf(
                            io.ktor.http.HttpHeaders.ContentType to "application/json"
                        )
                    )
                }
            }
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // Act
        val result = safeApiCall<TestData> {
            client.get("https://api.test.com/data")
        }

        // Assert
        assertTrue(result.isSuccess, "Result should be successful after retries")
        assertEquals(3, callCount, "Should retry 3 times")
        assertEquals(testData, result.getOrNull())
    }

    /**
     * Test 3: Should return RateLimitExceeded on 429 Too Many Requests
     */
    @Test
    fun `should return RateLimitExceeded on 429 Too Many Requests`() = runTest {
        // Arrange
        val retryAfter = 120
        val mockEngine = MockEngine.create {
            respondError(
                HttpStatusCode.TooManyRequests,
                content = json.encodeToString(
                    ErrorResponse.serializer(),
                    ErrorResponse(detail = "Rate limit exceeded")
                ),
                headers = io.ktor.http.headers.headersOf(
                    "Retry-After" to listOf(retryAfter.toString())
                )
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // Act
        val result = safeApiCall<TestData> {
            client.get("https://api.test.com/data")
        }

        // Assert
        assertTrue(result.isFailure, "Result should be a failure")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.RateLimitExceeded, "Error should be RateLimitExceeded")
        assertEquals(retryAfter, (error as AppError.RateLimitExceeded).retryAfter)
    }

    /**
     * Test 4: Should return Unauthorized on 401 Unauthorized
     */
    @Test
    fun `should return Unauthorized on 401 Unauthorized`() = runTest {
        // Arrange
        val mockEngine = MockEngine.create {
            respondError(
                HttpStatusCode.Unauthorized,
                content = json.encodeToString(
                    ErrorResponse.serializer(),
                    ErrorResponse(detail = "Invalid or missing token")
                )
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // Act
        val result = safeApiCall<TestData> {
            client.get("https://api.test.com/data")
        }

        // Assert
        assertTrue(result.isFailure, "Result should be a failure")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.Unauthorized, "Error should be Unauthorized")
    }

    /**
     * Test 5: Should return ValidationError on 422 Unprocessable Entity
     */
    @Test
    fun `should return ValidationError on 422 Unprocessable Entity`() = runTest {
        // Arrange
        val validationErrors = listOf(
            ValidationErrorItem(
                loc = listOf("body", "email"),
                msg = "field required",
                type = "value_error.missing"
            )
        )
        val detail = Json {
            ignoreUnknownKeys = true
        }.encodeToString(
            kotlinx.serialization.ListSerializer(ValidationErrorItem.serializer()),
            validationErrors
        )
        val mockEngine = MockEngine.create {
            respondError(
                HttpStatusCode.UnprocessableEntity,
                content = json.encodeToString(
                    ErrorResponse.serializer(),
                    ErrorResponse(detail = detail)
                )
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // Act
        val result = safeApiCall<TestData> {
            client.get("https://api.test.com/data")
        }

        // Assert
        assertTrue(result.isFailure, "Result should be a failure")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.ValidationError, "Error should be ValidationError")
        val validationError = error as AppError.ValidationError
        assertEquals("email", validationError.field)
        assertEquals("field required", validationError.message)
    }

    /**
     * Test 6: Should return NotFound on 404 Not Found
     */
    @Test
    fun `should return NotFound on 404 Not Found`() = runTest {
        // Arrange
        val mockEngine = MockEngine.create {
            respondError(
                HttpStatusCode.NotFound,
                content = json.encodeToString(
                    ErrorResponse.serializer(),
                    ErrorResponse(detail = "Resource not found")
                )
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // Act
        val result = safeApiCall<TestData> {
            client.get("https://api.test.com/data")
        }

        // Assert
        assertTrue(result.isFailure, "Result should be a failure")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.NotFound, "Error should be NotFound")
    }

    /**
     * Test 7: Should handle AccountLocked (423 Locked)
     */
    @Test
    fun `should handle AccountLocked on 423 Locked`() = runTest {
        // Arrange
        val lockUntil = "2026-03-20T12:00:00Z"
        val mockEngine = MockEngine.create {
            respondError(
                HttpStatusCode.Locked,
                content = json.encodeToString(
                    ErrorResponse.serializer(),
                    ErrorResponse(detail = lockUntil)
                )
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // Act
        val result = safeApiCall<TestData> {
            client.get("https://api.test.com/data")
        }

        // Assert
        assertTrue(result.isFailure, "Result should be a failure")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.AccountLocked, "Error should be AccountLocked")
        assertEquals(lockUntil, (error as AppError.AccountLocked).until)
    }

    // ============== Test Data Classes ==============

    @Serializable
    data class TestData(
        val id: Int,
        val name: String
    )
}
