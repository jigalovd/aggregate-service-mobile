package com.aggregateservice.feature.profile.data.repository

import com.aggregateservice.feature.profile.data.api.ProfileApiService
import com.aggregateservice.feature.profile.data.mapper.ProfileMapper
import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository

/**
 * Implementation of ProfileRepository.
 *
 * **Architecture:**
 * - Data layer implements Domain layer interface
 * - Uses ProfileApiService for network requests
 * - Uses ProfileMapper for DTO -> Domain conversion
 *
 * @property apiService API service for profile operations
 */
class ProfileRepositoryImpl(
    private val apiService: ProfileApiService,
) : ProfileRepository {
    override suspend fun getProfile(): Result<Profile> {
        return apiService.getProfile().fold(
            onSuccess = { dto -> Result.success(ProfileMapper.toDomain(dto)) },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<Profile> {
        val requestDto = ProfileMapper.toApiRequest(request)

        return apiService.updateProfile(requestDto).fold(
            onSuccess = { dto -> Result.success(ProfileMapper.toDomain(dto)) },
            onFailure = { error -> Result.failure(error) },
        )
    }
}
