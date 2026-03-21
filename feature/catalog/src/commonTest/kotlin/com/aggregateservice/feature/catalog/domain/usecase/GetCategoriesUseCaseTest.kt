package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Provider
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

class GetCategoriesUseCaseTest {

    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var mockRepository: MockCatalogRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockCatalogRepository()
        getCategoriesUseCase = GetCategoriesUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return categories on successful fetch`() = runTest {
        val expectedCategories = createTestCategories(5)
        mockRepository.categoriesResult = Result.success(expectedCategories)

        val result = getCategoriesUseCase()

        assertTrue(result.isSuccess)
        val categories = result.getOrNull()!!
        assertEquals(5, categories.size)
        assertNull(mockRepository.lastParentId)
    }

    @Test
    fun `should return empty list when no categories exist`() = runTest {
        mockRepository.categoriesResult = Result.success(emptyList())

        val result = getCategoriesUseCase()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `should pass parentId to repository`() = runTest {
        val parentId = "parent-123"
        mockRepository.categoriesResult = Result.success(emptyList())

        getCategoriesUseCase(parentId = parentId)

        assertEquals(parentId, mockRepository.lastParentId)
    }

    @Test
    fun `should pass null parentId for root categories`() = runTest {
        mockRepository.categoriesResult = Result.success(emptyList())

        getCategoriesUseCase(parentId = null)

        assertNull(mockRepository.lastParentId)
    }

    @Test
    fun `should return subcategories when parentId is provided`() = runTest {
        val subcategories = listOf(
            Category(id = "sub-1", name = "Subcategory 1", parentId = "parent-1"),
            Category(id = "sub-2", name = "Subcategory 2", parentId = "parent-1"),
        )
        mockRepository.categoriesResult = Result.success(subcategories)

        val result = getCategoriesUseCase(parentId = "parent-1")

        assertTrue(result.isSuccess)
        val categories = result.getOrNull()!!
        assertEquals(2, categories.size)
        categories.forEach { assertEquals("parent-1", it.parentId) }
    }

    @Test
    fun `getRootCategories should call repository with null parentId`() = runTest {
        val rootCategories = listOf(
            Category(id = "root-1", name = "Root 1"),
            Category(id = "root-2", name = "Root 2"),
        )
        mockRepository.categoriesResult = Result.success(rootCategories)

        val result = getCategoriesUseCase.getRootCategories()

        assertTrue(result.isSuccess)
        assertNull(mockRepository.lastParentId)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun `should return categories with correct properties`() = runTest {
        val categories = listOf(
            Category(
                id = "cat-1",
                name = "Haircut",
                icon = "https://example.com/icon.png",
                parentId = null,
                isActive = true,
                sortOrder = 1,
            ),
        )
        mockRepository.categoriesResult = Result.success(categories)

        val result = getCategoriesUseCase()

        assertTrue(result.isSuccess)
        val category = result.getOrNull()!!.first()
        assertEquals("cat-1", category.id)
        assertEquals("Haircut", category.name)
        assertEquals("https://example.com/icon.png", category.icon)
        assertTrue(category.isRootCategory)
        assertTrue(category.isActive)
        assertEquals(1, category.sortOrder)
    }

    // ========== Error Cases ==========

    @Test
    fun `should return network error when repository fails with network error`() = runTest {
        mockRepository.categoriesResult = Result.failure(
            AppError.NetworkError(503, "Service Unavailable")
        )

        val result = getCategoriesUseCase()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.NetworkError)
        assertEquals(503, (error as AppError.NetworkError).code)
    }

    @Test
    fun `should return network error when repository fails with server error`() = runTest {
        mockRepository.categoriesResult = Result.failure(
            AppError.NetworkError(500, "Internal Server Error")
        )

        val result = getCategoriesUseCase()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.NetworkError)
        assertEquals(500, (error as AppError.NetworkError).code)
    }

    @Test
    fun `should return not found error when repository fails`() = runTest {
        mockRepository.categoriesResult = Result.failure(AppError.NotFound)

        val result = getCategoriesUseCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NotFound)
    }

    @Test
    fun `should return unknown error when repository throws unexpected exception`() = runTest {
        mockRepository.categoriesResult = Result.failure<List<Category>>(RuntimeException("Unexpected"))

        val result = getCategoriesUseCase()

        assertTrue(result.isFailure)
    }

    // ========== Category Model Tests ==========

    @Test
    fun `category isRootCategory should return true when parentId is null`() = runTest {
        val categories = listOf(Category(id = "root", name = "Root", parentId = null))
        mockRepository.categoriesResult = Result.success(categories)

        val result = getCategoriesUseCase()

        assertTrue(result.getOrNull()!!.first().isRootCategory)
    }

    @Test
    fun `category isRootCategory should return false when parentId is set`() = runTest {
        val categories = listOf(Category(id = "child", name = "Child", parentId = "parent"))
        mockRepository.categoriesResult = Result.success(categories)

        val result = getCategoriesUseCase()

        assertTrue(!result.getOrNull()!!.first().isRootCategory)
    }

    // ========== Helper Methods ==========

    private fun createTestCategories(count: Int): List<Category> {
        return (1..count).map { index ->
            Category(
                id = "cat-$index",
                name = "Category $index",
                icon = if (index % 2 == 0) "icon_$index.png" else null,
                parentId = if (index > 3) "cat-1" else null,
                isActive = index % 2 != 0,
                sortOrder = index,
            )
        }
    }

    /**
     * Mock implementation of CatalogRepository for testing
     */
    private class MockCatalogRepository : CatalogRepository {
        var categoriesResult: Result<List<Category>> = Result.success(emptyList())
        var lastParentId: String? = null

        override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
            return Result.success(SearchResult.empty())
        }

        override suspend fun getProviderById(providerId: String): Result<Provider> {
            return Result.failure(AppError.NotFound)
        }

        override suspend fun getCategories(parentId: String?): Result<List<Category>> {
            lastParentId = parentId
            return categoriesResult
        }

        override suspend fun getCategoryById(categoryId: String): Result<Category> {
            return Result.failure(AppError.NotFound)
        }

        override suspend fun getProviderServices(
            providerId: String,
            categoryId: String?,
        ): Result<List<com.aggregateservice.feature.catalog.domain.model.Service>> {
            return Result.success(emptyList())
        }

        override suspend fun getServiceById(serviceId: String): Result<com.aggregateservice.feature.catalog.domain.model.Service> {
            return Result.failure(AppError.NotFound)
        }

        override suspend fun searchServices(
            query: String,
            filters: SearchFilters,
        ): Result<SearchResult<com.aggregateservice.feature.catalog.domain.model.Service>> {
            return Result.success(SearchResult.empty())
        }
    }
}
