package com.aggregateservice.feature.profile.domain.repository

import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest

/**
 * Repository interface for user profile management.
 *
 * Provides operations for fetching and updating the current user's profile.
 */
interface ProfileRepository {
    /**
     * Retrieves the current user's profile.
     *
     * @return Result containing the profile or an error
     */
    suspend fun getProfile(): Result<Profile>

    /**
     * Updates the current user's profile.
     *
     * @param request The update request containing fields to update
     * @return Result containing the updated profile or an error
     */
    suspend fun updateProfile(request: UpdateProfileRequest): Result<Profile>
}
