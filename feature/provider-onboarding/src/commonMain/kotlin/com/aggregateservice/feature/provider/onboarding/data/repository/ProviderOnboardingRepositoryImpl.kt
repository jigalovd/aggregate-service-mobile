package com.aggregateservice.feature.provider.onboarding.data.repository

import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.provider.onboarding.data.api.ProviderOnboardingApiService
import com.aggregateservice.feature.provider.onboarding.data.api.ProviderOnboardingRequest
import com.aggregateservice.feature.provider.onboarding.domain.repository.ProviderOnboardingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of ProviderOnboardingRepository.
 *
 * Uses ProviderOnboardingApiService for network requests.
 *
 * **Architecture:**
 * - Data layer implements Domain layer interface
 * - All network operations run in Dispatchers.IO
 * - Logger tag "ProviderOnboarding" for observability
 * - Result.fold() for error handling with logging
 *
 * @property apiService API service for onboarding
 * @property logger Logger for observability (tag: "ProviderOnboarding")
 */
class ProviderOnboardingRepositoryImpl(
    private val apiService: ProviderOnboardingApiService,
    private val logger: Logger,
) : ProviderOnboardingRepository {

    override suspend fun submitOnboarding(
        businessName: String,
        bio: String,
        phone: String,
        address: String,
        serviceRadiusKm: Float,
        categoryIds: List<String>,
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderOnboarding") {
                "submitOnboarding(businessName=$businessName, address=$address, categories=${categoryIds.size})"
            }

            val request = ProviderOnboardingRequest(
                businessName = businessName,
                bio = bio,
                phone = phone,
                address = address,
                serviceRadiusKm = serviceRadiusKm,
                categoryIds = categoryIds,
            )

            apiService.submitOnboarding(request).fold(
                onSuccess = {
                    logger.d("ProviderOnboarding") { "submitOnboarding: success" }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    val appError = error as? AppError ?: error.toAppError()
                    logger.e("ProviderOnboarding") {
                        "submitOnboarding: failed - ${appError::class.simpleName}: ${appError.message}"
                    }
                    Result.failure(appError)
                },
            )
        }
    }

    override suspend fun isOnboardingComplete(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderOnboarding") { "isOnboardingComplete()" }
            // TODO: Implement when backend provides status check endpoint
            // For now, return false - user needs to complete onboarding
            Result.success(false)
        }
    }
}