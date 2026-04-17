package com.aggregateservice.feature.catalog.data.repository

import com.aggregateservice.feature.catalog.data.api.CatalogApiService
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogRepositoryCacheTest {
    private val testDispatcher = StandardTestDispatcher()
    private var apiCallCount = 0

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        apiCallCount = 0
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createClient(json: String): HttpClient {
        val engine =
            MockEngine { _ ->
                apiCallCount++
                respond(
                    content = ByteReadChannel(json),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    // JSON matching actual DTO @SerialName annotations
    // Verified against: CategoryDto, ProviderDto, ServiceDto, response DTOs

    private val categoriesJson = """{
        "categories": [
            {
                "id": "cat-1",
                "name": {"ru": "Test", "he": "Test", "en": "Test"},
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-01T00:00:00Z",
                "icon_url": null,
                "parent_id": null,
                "sort_order": 1,
                "is_active": true
            }
        ],
        "total": 1
    }"""

    private val providerJson = """{
        "id": "prov-1",
        "user_id": "u-1",
        "display_name": "Test Salon",
        "bio": null,
        "avatar_url": null,
        "location": {"lat": 32.8, "lon": 35.0},
        "rating_cached": 4.5,
        "reviews_count": 10,
        "is_verified": true,
        "is_active": true,
        "created_at": "2025-01-01T00:00:00Z",
        "updated_at": "2025-01-01T00:00:00Z"
    }"""

    private val searchResponseJson = """{
        "providers": [
            {
                "id": "prov-1",
                "user_id": "u-1",
                "display_name": "Test Salon",
                "bio": null,
                "avatar_url": null,
                "location": {"lat": 32.8, "lon": 35.0},
                "rating_cached": 4.5,
                "reviews_count": 10,
                "is_verified": true,
                "is_active": true,
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-01T00:00:00Z"
            }
        ],
        "total": 1,
        "limit": 20,
        "offset": 0
    }"""

    private val servicesJson = """{
        "services": [
            {
                "id": "s-1",
                "category_id": "c-1",
                "category_name": {"ru": "Cat", "he": "Cat", "en": "Cat"},
                "title": {"ru": "Service", "he": "Service", "en": "Service"},
                "description": null,
                "base_price": 10000,
                "duration_minutes": 60,
                "is_combinable": true,
                "is_active": true,
                "created_at": "2025-01-01T00:00:00Z"
            }
        ],
        "total": 1
    }"""

    @Test
    fun `getCategories returns cached result on second call within TTL`() =
        runTest {
            val client = createClient(categoriesJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            val result1 = repository.getCategories(null)
            assertEquals(1, apiCallCount)
            assertTrue(result1.isSuccess)
            assertEquals(1, result1.getOrThrow().size)

            val result2 = repository.getCategories(null)
            assertEquals(1, apiCallCount) // No additional API call
            assertEquals(result1.getOrThrow().first().id, result2.getOrThrow().first().id)
        }

    @Test
    fun `getCategories misses cache for different parentId`() =
        runTest {
            val client = createClient(categoriesJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            repository.getCategories(null)
            assertEquals(1, apiCallCount)

            repository.getCategories("parent-1")
            assertEquals(2, apiCallCount)
        }

    @Test
    fun `getProviderById returns cached result on second call`() =
        runTest {
            val client = createClient(providerJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            repository.getProviderById("prov-1")
            assertEquals(1, apiCallCount)

            repository.getProviderById("prov-1")
            assertEquals(1, apiCallCount)
        }

    @Test
    fun `getProviderById misses cache for different id`() =
        runTest {
            val client = createClient(providerJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            repository.getProviderById("prov-1")
            assertEquals(1, apiCallCount)

            repository.getProviderById("prov-2")
            assertEquals(2, apiCallCount)
        }

    @Test
    fun `searchProviders returns cached result for same filters`() =
        runTest {
            val client = createClient(searchResponseJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            repository.searchProviders(SearchFilters(page = 1))
            assertEquals(1, apiCallCount)

            repository.searchProviders(SearchFilters(page = 1))
            assertEquals(1, apiCallCount)
        }

    @Test
    fun `searchProviders misses cache for different filters`() =
        runTest {
            val client = createClient(searchResponseJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            repository.searchProviders(SearchFilters(page = 1))
            assertEquals(1, apiCallCount)

            repository.searchProviders(SearchFilters(page = 2))
            assertEquals(2, apiCallCount)
        }

    @Test
    fun `getProviderServices returns cached result on second call`() =
        runTest {
            val client = createClient(servicesJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            repository.getProviderServices("prov-1", null)
            assertEquals(1, apiCallCount)

            repository.getProviderServices("prov-1", null)
            assertEquals(1, apiCallCount)
        }

    @Test
    fun `invalidateCache clears provider and search caches`() =
        runTest {
            val client = createClient(providerJson)
            val apiService = CatalogApiService(client)
            val repository = CatalogRepositoryImpl(apiService)

            repository.getProviderById("prov-1")
            assertEquals(1, apiCallCount)

            repository.invalidateCache()

            repository.getProviderById("prov-1")
            assertEquals(2, apiCallCount) // Cache was invalidated
        }
}
