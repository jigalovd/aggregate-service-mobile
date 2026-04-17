package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.core.api.models.ProviderResponse
import com.aggregateservice.core.common.model.Location
import com.aggregateservice.feature.catalog.domain.model.Provider

/**
 * Mapper for converting ProviderResponse (generated DTO) to Provider domain model.
 *
 * **Important:** Mapper must NOT depend on platform-specific code.
 *
 * Note: The generated ProviderResponse does not include photos, city, postalCode,
 * country, categories, servicesCount, or workingHours. These fields default to
 * empty/zero values. For the full provider detail, use ProviderDetailResponse
 * which includes services and favorite status.
 */
object ProviderMapper {
    /**
     * Converts ProviderResponse to Provider.
     *
     * @param dto Generated DTO from API
     * @return Domain model
     */
    fun toDomain(dto: ProviderResponse): Provider =
        Provider(
            id = dto.id,
            userId = dto.userId,
            businessName = dto.displayName,
            description = dto.bio,
            logoUrl = dto.avatarUrl,
            photos = emptyList(),
            rating = dto.ratingCached,
            reviewCount = dto.reviewsCount,
            location =
                Location(
                    latitude = dto.location?.lat ?: 0.0,
                    longitude = dto.location?.lon ?: 0.0,
                    address = dto.address ?: "",
                    city = "",
                    postalCode = null,
                    country = null,
                ),
            workingHours = com.aggregateservice.feature.catalog.domain.model.WorkingHours(),
            isVerified = dto.isVerified ?: false,
            isActive = dto.isActive ?: true,
            createdAt = dto.createdAt,
            categories = emptyList(),
            servicesCount = 0,
        )
}
