package com.aggregateservice.feature.profile.domain.usecase

import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.repository.ProfileRepository

/**
 * UseCase for retrieving the current user's profile.
 *
 * @property repository Profile repository
 */
class GetProfileUseCase(
    private val repository: ProfileRepository,
) {
    /**
     * Retrieves the current user's profile.
     *
     * @return Result containing the profile or an error
     */
    suspend operator fun invoke(): Result<Profile> {
        return repository.getProfile()
    }
}
