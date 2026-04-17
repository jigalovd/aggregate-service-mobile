package com.aggregateservice.feature.favorites.data.mapper

import com.aggregateservice.core.api.models.FavoriteResponse
import com.aggregateservice.feature.favorites.domain.model.Favorite

/**
 * Mapper for converting between API DTO and Domain models.
 *
 * Источник истины: OpenAPI spec (core:api-models).
 */
object FavoriteMapper {
    /**
     * Converts FavoriteResponse to Domain model.
     */
    fun toDomain(dto: FavoriteResponse): Favorite =
        Favorite(
            providerId = dto.providerId,
            businessName = dto.provider.displayName,
            logoUrl = dto.provider.avatarUrl,
            rating = dto.provider.ratingCached,
            reviewCount = dto.provider.reviewsCount,
            address = dto.provider.address ?: "",
            addedAt = dto.createdAt,
        )

    /**
     * Converts list of FavoriteResponses to list of Domain models.
     */
    fun toDomainList(dtos: List<FavoriteResponse>): List<Favorite> = dtos.map(::toDomain)
}
