package com.aggregateservice.feature.favorites.data.mapper

import com.aggregateservice.feature.favorites.data.dto.FavoriteDto
import com.aggregateservice.feature.favorites.domain.model.Favorite
import kotlinx.datetime.Instant

/**
 * Mapper for converting between Favorite DTO and Domain models.
 */
object FavoriteMapper {
    /**
     * Converts DTO to Domain model.
     */
    fun toDomain(dto: FavoriteDto): Favorite =
        Favorite(
            providerId = dto.provider.id,
            businessName = dto.provider.displayName,
            logoUrl = dto.provider.avatarUrl,
            rating = dto.provider.ratingCached,
            reviewCount = dto.provider.reviewsCount,
            address = dto.provider.address ?: "",
            addedAt = Instant.parse(dto.createdAt),
        )

    /**
     * Converts list of DTOs to list of Domain models.
     */
    fun toDomain(dtos: List<FavoriteDto>): List<Favorite> = dtos.map(::toDomain)
}
