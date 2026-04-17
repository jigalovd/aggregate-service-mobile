package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.core.api.models.CategoryResponse
import com.aggregateservice.core.api.models.I18nStringSchema
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoryMapperTest {
    // ========== Basic Mapping Tests ==========

    @Test
    fun `should map CategoryResponse to Category with all fields`() {
        val dto =
            CategoryResponse(
                id = "cat-123",
                name = I18nStringSchema(ru = "", he = "", en = "Haircut"),
                iconUrl = "https://example.com/icon.png",
                parentId = null,
                isActive = true,
                sortOrder = 5,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
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
        val dto =
            CategoryResponse(
                id = "cat-child",
                name = I18nStringSchema(ru = "", he = "", en = "Men's Haircut"),
                parentId = "cat-parent",
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("cat-child", category.id)
        assertEquals("Men's Haircut", category.name)
        assertEquals("cat-parent", category.parentId)
        assertFalse(category.isRootCategory)
    }

    @Test
    fun `should map root category without parentId`() {
        val dto =
            CategoryResponse(
                id = "cat-root",
                name = I18nStringSchema(ru = "", he = "", en = "Services"),
                parentId = null,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertTrue(category.isRootCategory)
    }

    // ========== Nullable Fields Tests ==========

    @Test
    fun `should handle null icon`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Test Category"),
                iconUrl = null,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertNull(category.icon)
    }

    @Test
    fun `should handle null parentId`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Test Category"),
                parentId = null,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertNull(category.parentId)
        assertTrue(category.isRootCategory)
    }

    // ========== Default Values Tests ==========

    @Test
    fun `should use default values for optional fields`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Test Category"),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertNull(category.icon)
        assertNull(category.parentId)
        assertTrue(category.isActive)
        assertEquals(0, category.sortOrder)
    }

    @Test
    fun `should map isActive as false`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Inactive Category"),
                isActive = false,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertFalse(category.isActive)
    }

    // ========== Sort Order Tests ==========

    @Test
    fun `should map custom sort order`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Test"),
                sortOrder = 100,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(100, category.sortOrder)
    }

    @Test
    fun `should map zero sort order`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Test"),
                sortOrder = 0,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(0, category.sortOrder)
    }

    @Test
    fun `should map negative sort order`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Test"),
                sortOrder = -1,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(-1, category.sortOrder)
    }

    // ========== Category Types Tests ==========

    @Test
    fun `should map category with cyrillic name`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "Стрижка", he = "", en = ""),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("Стрижка", category.name)
    }

    @Test
    fun `should map category with special characters in name`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Hair & Beauty - Premium"),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("Hair & Beauty - Premium", category.name)
    }

    @Test
    fun `should map category with empty name`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = ""),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("", category.name)
    }

    // ========== Edge Cases ==========

    @Test
    fun `should map category with very long id`() {
        val longId = "category-${"a".repeat(100)}"
        val dto =
            CategoryResponse(
                id = longId,
                name = I18nStringSchema(ru = "", he = "", en = "Test"),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals(longId, category.id)
    }

    @Test
    fun `should map category with url-safe icon`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Test"),
                iconUrl = "https://cdn.example.com/icons/icon%20with%20spaces.png?size=64&color=blue",
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
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
        val dto =
            CategoryResponse(
                id = "root",
                name = I18nStringSchema(ru = "", he = "", en = "Root"),
                parentId = null,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertTrue(category.isRootCategory)
    }

    @Test
    fun `category isRootCategory should return false for subcategory`() {
        val dto =
            CategoryResponse(
                id = "child",
                name = I18nStringSchema(ru = "", he = "", en = "Child"),
                parentId = "root",
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertFalse(category.isRootCategory)
    }

    // ========== I18n Priority Tests ==========

    @Test
    fun `should prefer Russian name when available`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "Стрижка", he = "תספורת", en = "Haircut"),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("Стрижка", category.name)
    }

    @Test
    fun `should fall back to Hebrew when Russian is blank`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "תספורת", en = "Haircut"),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("תספורת", category.name)
    }

    @Test
    fun `should fall back to English when Russian and Hebrew are blank`() {
        val dto =
            CategoryResponse(
                id = "cat-1",
                name = I18nStringSchema(ru = "", he = "", en = "Haircut"),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        val category = CategoryMapper.toDomain(dto)

        assertEquals("Haircut", category.name)
    }
}
