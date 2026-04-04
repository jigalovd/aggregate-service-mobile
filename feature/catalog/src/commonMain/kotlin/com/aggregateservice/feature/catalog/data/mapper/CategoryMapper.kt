package com.aggregateservice.feature.catalog.data.mapper
import com.aggregateservice.feature.catalog.data.dto.CategoryDto
import com.aggregateservice.feature.catalog.domain.model.Category

/**
 * Mapper для преобразования CategoryDto в Category.
 */
object CategoryMapper {
    /**
     * Преобразует CategoryDto в Category.
     * Извлекает локализованное имя (ru -> he -> en -> first available).
     *
     * @param dto DTO из API
     * @return Domain model
     */
    fun toDomain(dto: CategoryDto): Category =
        Category(
            id = dto.id,
            name = dto.name["ru"] ?: dto.name["he"] ?: dto.name["en"] ?: dto.name.values.firstOrNull() ?: "",
            icon = dto.icon,
            parentId = dto.parentId,
            isActive = dto.isActive,
            sortOrder = dto.sortOrder,
        )
}
