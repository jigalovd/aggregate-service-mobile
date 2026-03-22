package com.aggregateservice.feature.profile.domain.usecase

import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository

/**
 * UseCase for updating the current user's profile.
 *
 * @property repository Profile repository
 */
class UpdateProfileUseCase(
    private val repository: ProfileRepository,
) {
    /**
     * Updates the current user's profile.
     *
     * @param request The update request containing fields to update
     * @return Result containing the updated profile or an error
     */
    suspend operator fun invoke(request: UpdateProfileRequest): Result<Profile> {
        return repository.updateProfile(request)
    }
}
