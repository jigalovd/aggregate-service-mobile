package com.aggregateservice.feature.provider.onboarding.domain.repository

import com.aggregateservice.feature.provider.onboarding.data.api.ProviderOnboardingResponse

/**
 * Repository interface for provider onboarding.
 *
 * Defines contract for submitting onboarding data and checking onboarding status.
 *
 * **Architecture:**
 * - Domain layer (this interface) does not depend on Data layer
 * - ScreenModels use this interface
 * - Data layer provides implementation through DI
 */
interface ProviderOnboardingRepository {
    /**
     * Submits provider onboarding data.
     *
     * @param businessName Name of the provider's business
     * @param bio Business description/bio
     * @param phone Contact phone number
     * @param address Service address
     * @param serviceRadiusKm Coverage radius in kilometers
     * @param categoryIds List of selected service category IDs
     * @return Result with ProviderOnboardingResponse on success (contains message and accessToken) or error on failure
     */
    suspend fun submitOnboarding(
        businessName: String,
        bio: String,
        phone: String,
        address: String,
        serviceRadiusKm: Float,
        categoryIds: List<String>,
    ): Result<ProviderOnboardingResponse>

    /**
     * Checks if a provider has completed onboarding.
     *
     * @return Result with true if onboarding is complete, false otherwise
     */
    suspend fun isOnboardingComplete(): Result<Boolean>
}