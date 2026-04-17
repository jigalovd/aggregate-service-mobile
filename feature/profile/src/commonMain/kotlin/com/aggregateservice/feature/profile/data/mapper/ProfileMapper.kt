package com.aggregateservice.feature.profile.data.mapper

import com.aggregateservice.core.api.models.ProfileResponse
import com.aggregateservice.core.api.models.ProfileUpdate
import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest

/**
 * Mapper for converting between Profile API models and Domain models.
 */
object ProfileMapper {
    /**
     * Converts API response to domain model.
     */
    fun toDomain(response: ProfileResponse): Profile =
        Profile(
            id = response.id,
            userId = response.userId,
            fullName = response.fullName,
            phone = response.phone,
            avatarUrl = response.avatarUrl,
            noShowCount = response.noShowCount ?: 0,
            noShowRate = response.noShowRate ?: 0.0,
        )

    /**
     * Converts domain UpdateProfileRequest to API request model.
     */
    fun toApiRequest(request: UpdateProfileRequest): ProfileUpdate =
        ProfileUpdate(
            fullName = request.fullName,
            phone = request.phone,
        )
}
