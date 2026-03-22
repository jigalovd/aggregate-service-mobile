package com.aggregateservice.feature.favorites.data.mapper

import com.aggregateservice.feature.favorites.data.dto.FavoriteDto
import com.aggregateservice.feature.favorites.domain.model.Favorite

/**
 * Mapper for converting between Favorite DTO and Domain models.
 */
object FavoriteMapper {

    /**
     * Converts DTO to Domain model.
     */
    fun toDomain(dto: FavoriteDto): Favorite = Favorite(
        providerId = dto.providerId,
        businessName = dto.businessName,
        logoUrl = dto.logoUrl,
        rating = dto.rating,
        reviewCount = dto.reviewCount,
        address = dto.address,
        addedAt = dto.addedAt,
    )

    /**
     * Converts list of DTOs to list of Domain models.
     */
    fun toDomain(dtos: List<FavoriteDto>): List<Favorite> = dtos.map(::toDomain)
}
