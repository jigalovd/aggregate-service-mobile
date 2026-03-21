package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.feature.catalog.data.dto.CategoryDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertFalse

class CategoryMapperTest {

    // ========== Basic Mapping Tests ==========

    @Test
    fun `should map CategoryDto to Category with all fields`() {
        val dto = CategoryDto(
            id = "cat-123",
            name = "Haircut",
            icon = "https://example.com/icon.png",
            parentId = null,
            isActive = true,
            sortOrder = 5,
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("cat-123", category.id)
        assertEquals("Haircut", category.name)
        assertEquals("https://example.com/icon.png", category.icon)
        assertNull(category.parentId)
        assertTrue(category.isActive)
        assertEquals(5, category.sortOrder)
    }

    @Test
    fun `should map category with parentId as subcategory`() {
        val dto = CategoryDto(
            id = "cat-child",
            name = "Men's Haircut",
            parentId = "cat-parent",
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("cat-child", category.id)
        assertEquals("Men's Haircut", category.name)
        assertEquals("cat-parent", category.parentId)
        assertFalse(category.isRootCategory)
    }

    @Test
    fun `should map root category without parentId`() {
        val dto = CategoryDto(
            id = "cat-root",
            name = "Services",
            parentId = null,
        )
        val category = CategoryMapper.toDomain(dto)

        assertTrue(category.isRootCategory)
    }

    // ========== Nullable Fields Tests ==========

    @Test
    fun `should handle null icon`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Test Category",
            icon = null,
        )
        val category = CategoryMapper.toDomain(dto)

        assertNull(category.icon)
    }

    @Test
    fun `should handle null parentId`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Test Category",
            parentId = null,
        )
        val category = CategoryMapper.toDomain(dto)

        assertNull(category.parentId)
        assertTrue(category.isRootCategory)
    }

    // ========== Default Values Tests ==========

    @Test
    fun `should use default values for optional fields`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Test Category",
        )
        val category = CategoryMapper.toDomain(dto)

        assertNull(category.icon)
        assertNull(category.parentId)
        assertTrue(category.isActive)
        assertEquals(0, category.sortOrder)
    }

    @Test
    fun `should map isActive as false`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Inactive Category",
            isActive = false,
        )
        val category = CategoryMapper.toDomain(dto)

        assertFalse(category.isActive)
    }

    // ========== Sort Order Tests ==========

    @Test
    fun `should map custom sort order`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Test",
            sortOrder = 100,
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(100, category.sortOrder)
    }

    @Test
    fun `should map zero sort order`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Test",
            sortOrder = 0,
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(0, category.sortOrder)
    }

    @Test
    fun `should map negative sort order`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Test",
            sortOrder = -1,
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(-1, category.sortOrder)
    }

    // ========== Category Types Tests ==========

    @Test
    fun `should map category with cyrillic name`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Стрижка",
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("Стрижка", category.name)
    }

    @Test
    fun `should map category with special characters in name`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Hair & Beauty - Premium",
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("Hair & Beauty - Premium", category.name)
    }

    @Test
    fun `should map category with empty name`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "",
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("", category.name)
    }

    // ========== Edge Cases ==========

    @Test
    fun `should map category with very long id`() {
        val longId = "category-${"a".repeat(100)}"
        val dto = CategoryDto(
            id = longId,
            name = "Test",
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(longId, category.id)
    }

    @Test
    fun `should map category with url-safe icon`() {
        val dto = CategoryDto(
            id = "cat-1",
            name = "Test",
            icon = "https://cdn.example.com/icons/icon%20with%20spaces.png?size=64&color=blue",
        )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(
            "https://cdn.example.com/icons/icon%20with%20spaces.png?size=64&color=blue",
            category.icon,
        )
    }

    // ========== Computed Properties Tests ==========

    @Test
    fun `category isRootCategory should return true for root category`() {
        val dto = CategoryDto(
            id = "root",
            name = "Root",
            parentId = null,
        )
        val category = CategoryMapper.toDomain(dto)

        assertTrue(category.isRootCategory)
    }

    @Test
    fun `category isRootCategory should return false for subcategory`() {
        val dto = CategoryDto(
            id = "child",
            name = "Child",
            parentId = "root",
        )
        val category = CategoryMapper.toDomain(dto)

        assertFalse(category.isRootCategory)
    }
}
