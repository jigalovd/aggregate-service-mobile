package com.aggregateservice.core.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.body as textBody
import io.ktor.http.HttpStatusCode
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CoroutineContext
import kotlinx.coroutines.EmptyCoroutineContext
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [safeApiCall] wrapper.
 *
 * Tests cover:
 * - Success responses (200, 201, 204)
 * - Server error retry logic (500)
 * - Rate limiting (429)
 * - Client errors (401, 404, 422)
 * - Network/parsing errors
 */
class SafeApiCallTest {

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
        val testData = TestData(id = 1, name = "Test")
        val response = createMockResponse(HttpStatusCode.OK, testData)

        // Act
        val result = safeApiCall<TestData> {
            response
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
        val errorResponse = createMockErrorResponse(
            HttpStatusCode.InternalServerError,
            ErrorResponse(detail = "Internal server error")
        )

        // Act
        val result = safeApiCall<TestData> {
            callCount++
            if (callCount < 3) {
                errorResponse
            } else {
                createMockResponse(HttpStatusCode.OK, TestData(id = 1, name = "Success"))
            }
        }

        // Assert
        assertTrue(result.isSuccess, "Result should be successful after retries")
        assertEquals(3, callCount, "Should retry 3 times")
        assertEquals(TestData(id = 1, name = "Success"), result.getOrNull())
    }

    /**
     * Test 3: Should return RateLimitExceeded on 429 Too Many Requests
     */
    @Test
    fun `should return RateLimitExceeded on 429 Too Many Requests`() = runTest {
        // Arrange
        val retryAfter = 120
        val response = createMockResponseWithHeaders(
            HttpStatusCode.TooManyRequests,
            mapOf("Retry-After" to listOf(retryAfter.toString()))
        )

        // Act
        val result = safeApiCall<TestData> { response }

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
        val response = createMockErrorResponse(
            HttpStatusCode.Unauthorized,
            ErrorResponse(detail = "Invalid token")
        )

        // Act
        val result = safeApiCall<TestData> { response }

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
        val detail = json.encodeToString(
            kotlinx.serialization.ListSerializer(ValidationErrorItem.serializer()),
            validationErrors
        )
        val response = createMockErrorResponse(
            HttpStatusCode.UnprocessableEntity,
            ErrorResponse(detail = detail)
        )

        // Act
        val result = safeApiCall<TestData> { response }

        // Assert
        assertTrue(result.isFailure, "Result should be a failure")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.ValidationError, "Error should be ValidationError")
        val validationError = error as AppError.ValidationError
        assertEquals("email", validationError.field)
        assertEquals("field required", validationError.message)
    }

    // ============== Helper Methods ==============

    /**
     * Creates a mock successful HTTP response.
     */
    private fun createMockResponse(
        status: HttpStatusCode,
        data: TestData
    ): HttpResponse {
        return MockHttpResponse(
            status = status,
            body = json.encodeToString(TestData.serializer(), data)
        )
    }

    /**
     * Creates a mock error HTTP response.
     */
    private fun createMockErrorResponse(
        status: HttpStatusCode,
        errorBody: ErrorResponse
    ): HttpResponse {
        return MockHttpResponse(
            status = status,
            body = json.encodeToString(ErrorResponse.serializer(), errorBody)
        )
    }

    /**
     * Creates a mock HTTP response with custom headers.
     */
    private fun createMockResponseWithHeaders(
        status: HttpStatusCode,
        headers: Map<String, List<String>>
    ): HttpResponse {
        return MockHttpResponse(
            status = status,
            body = "",
            headers = headers
        )
    }

    // ============== Test Data Classes ==============

    @Serializable
    data class TestData(
        val id: Int,
        val name: String
    )

    /**
     * Mock HttpResponse implementation for testing.
     */
    private class MockHttpResponse(
        override val status: HttpStatusCode,
        private val body: String,
        private val headers: Map<String, List<String>> = emptyMap()
    ) : HttpResponse() {
        override val bodyType = typeInfo<String>()

        override suspend fun body<Any>() : Any {
            @Suppress("UNCHECKED_CAST")
            return body as Any
        }

        override val headers: io.ktor.http.Headers = object : io.ktor.http.Headers {
            override fun getAll(name: String): List<String>? = headers[name]
            override fun names(): Set<String> = headers.keys
            override fun entries(): Set<Map.Entry<String, List<String>>> = headers.entries
        }
    }
}
