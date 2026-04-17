package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.core.api.models.CategoryResponse
import com.aggregateservice.core.api.models.I18nStringSchema
import com.aggregateservice.feature.catalog.domain.model.Category

/**
 * Mapper for converting CategoryResponse (generated DTO) to Category domain model.
 */
object CategoryMapper {
    /**
     * Converts CategoryResponse to Category.
     * Extracts localized name using priority: ru -> he -> en.
     *
     * @param dto Generated DTO from API
     * @return Domain model
     */
    fun toDomain(dto: CategoryResponse): Category =
        Category(
            id = dto.id,
            name = dto.name.localized(),
            icon = dto.iconUrl,
            parentId = dto.parentId,
            isActive = dto.isActive ?: true,
            sortOrder = dto.sortOrder ?: 0,
        )
}

/**
 * Extension to extract a localized string from I18nStringSchema.
 * Priority: ru -> he -> en
 */
internal fun I18nStringSchema.localized(): String =
    ru.ifBlank { he.ifBlank { en } }
