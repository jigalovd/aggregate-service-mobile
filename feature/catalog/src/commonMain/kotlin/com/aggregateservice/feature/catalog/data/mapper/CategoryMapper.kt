package com.aggregateservice.feature.catalog.data.mapper
import com.aggregateservice.feature.catalog.data.dto.CategoryDto
import com.aggregateservice.feature.catalog.data.dto.ServiceDto
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Price
import com.aggregateservice.feature.catalog.domain.model.Service
import kotlinx.datetime.Instant

/**
 * Mapper для преобразования CategoryDto в Category.
 */
object CategoryMapper {
    /**
     * Преобразует CategoryDto в Category.
     *
     * @param dto DTO из API
     * @return Domain model
     */
    fun toDomain(dto: CategoryDto): Category = Category(
        id = dto.id,
        name = dto.name,
        icon = dto.icon,
        parentId = dto.parentId,
        isActive = dto.isActive,
        sortOrder = dto.sortOrder,
    )
}
