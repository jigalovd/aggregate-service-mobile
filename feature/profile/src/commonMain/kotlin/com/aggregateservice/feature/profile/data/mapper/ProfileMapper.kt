package com.aggregateservice.feature.profile.data.mapper

import com.aggregateservice.feature.profile.data.dto.ProfileDto
import com.aggregateservice.feature.profile.data.dto.UpdateProfileRequestDto
import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest

/**
 * Mapper for converting between Profile DTOs and Domain models.
 */
object ProfileMapper {

    /**
     * Converts DTO to domain model.
     */
    fun toDomain(dto: ProfileDto): Profile = Profile(
        id = dto.id,
        userId = dto.userId,
        fullName = dto.fullName,
        phone = dto.phone,
        avatarUrl = dto.avatarUrl,
        noShowCount = dto.noShowCount,
        noShowRate = dto.noShowRate,
    )

    /**
     * Converts domain UpdateProfileRequest to DTO.
     */
    fun toDto(request: UpdateProfileRequest): UpdateProfileRequestDto =
        UpdateProfileRequestDto(
            fullName = request.fullName,
            phone = request.phone,
        )
}
