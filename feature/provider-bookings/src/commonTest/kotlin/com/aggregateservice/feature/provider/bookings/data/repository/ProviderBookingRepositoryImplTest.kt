package com.aggregateservice.feature.provider.bookings.data.repository

import co.touchlab.kermit.Logger
import com.aggregateservice.feature.provider.bookings.data.api.ProviderBookingsApiService
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for ProviderBookingRepositoryImpl.
 * Uses MockEngine for HTTP mocking (D009 pattern).
 */
class ProviderBookingRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var logger: Logger

    @BeforeTest
    fun setup() {
        logger = Logger.withTag("ProviderBookingRepoTest")
    }

    private fun createRepository(mockEngine: MockEngine): ProviderBookingRepositoryImpl {
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        return ProviderBookingRepositoryImpl(ProviderBookingsApiService(httpClient), logger)
    }

    // ============ getProviderBookings Tests ============

    @Test
    fun `getProviderBookings should return error on HTTP failure`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/api/v1/bookings/provider/me") -> respond(
                    content = "Error", status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).getProviderBookings().isFailure)
    }

    @Test
    fun `getProviderBookings should handle empty response`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/api/v1/bookings/provider/me") -> respond(
                    content = "[]", status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        val result = createRepository(engine).getProviderBookings()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    // ============ acceptBooking Tests ============

    @Test
    fun `acceptBooking should return success on 200 response`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/confirm") -> respond(
                    content = "{}", status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).acceptBooking("id").isSuccess)
    }

    @Test
    fun `acceptBooking should return error on HTTP failure`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/confirm") -> respond(
                    content = "Not Found", status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).acceptBooking("id").isFailure)
    }

    // ============ rejectBooking Tests ============

    @Test
    fun `rejectBooking should return success on 200 response`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/reject") -> respond(
                    content = "{}", status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).rejectBooking("id", "reason").isSuccess)
    }

    @Test
    fun `rejectBooking should return error on HTTP failure`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/reject") -> respond(
                    content = "Bad Request", status = HttpStatusCode.BadRequest,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).rejectBooking("id", "reason").isFailure)
    }

    // ============ cancelBooking Tests ============

    @Test
    fun `cancelBooking should return success on 200 response`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/cancel") -> respond(
                    content = "{}", status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).cancelBooking("id", "reason").isSuccess)
    }

    @Test
    fun `cancelBooking with null reason should work`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/cancel") -> respond(
                    content = "{}", status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).cancelBooking("id", null).isSuccess)
    }

    @Test
    fun `cancelBooking should return error on HTTP failure`() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/cancel") -> respond(
                    content = "Conflict", status = HttpStatusCode.Conflict,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
                )
                else -> respond(content = "{}", status = HttpStatusCode.NotFound)
            }
        }
        assertTrue(createRepository(engine).cancelBooking("id", "reason").isFailure)
    }
}
