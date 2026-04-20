package com.aggregateservice.feature.provider.onboarding

import androidx.compose.runtime.Immutable

/**
 * Represents the different steps in the provider onboarding wizard.
 *
 * Each step contains the form data for that stage of onboarding.
 *
 * **Wizard Flow:**
 * 1. [BasicInfoStep] — business name, bio, phone number
 * 2. [LocationStep] — service address and coverage radius
 * 3. [ServicesStep] — selected service categories
 *
 * @see OnboardingUiState for UI presentation states
 */
@Immutable
sealed class OnboardingState {
    /**
     * Step 1: Basic business information.
     *
     * @property businessName Name of the provider's business
     * @property bio Provider description/bio (max 500 chars)
     * @property phone Business contact phone (10+ digits)
     */
    data class BasicInfoStep(
        val businessName: String = "",
        val bio: String = "",
        val phone: String = "",
    ) : OnboardingState() {
        companion object {
            const val MAX_BIO_LENGTH = 500
            const val MIN_PHONE_LENGTH = 10
        }
    }

    /**
     * Step 2: Location and service area.
     *
     * @property address Service address
     * @property serviceRadiusKm Coverage radius in kilometers (min 5km)
     */
    data class LocationStep(
        val address: String = "",
        val serviceRadiusKm: Float = 10f,
    ) : OnboardingState() {
        companion object {
            const val MIN_RADIUS_KM = 5f
            const val MAX_RADIUS_KM = 100f
            const val DEFAULT_RADIUS_KM = 10f
        }
    }

    /**
     * Step 3: Service categories selection.
     *
     * @property selectedCategoryIds Set of selected category IDs
     */
    data class ServicesStep(
        val selectedCategoryIds: Set<String> = emptySet(),
    ) : OnboardingState() {
        companion object {
            const val MIN_CATEGORIES = 1
        }
    }

    /**
     * Complete state — onboarding finished successfully.
     */
    data object Completed : OnboardingState()

    companion object {
        /**
         * Total number of wizard steps (0-indexed, so last step is 2).
         */
        const val TOTAL_STEPS = 3
        const val LAST_STEP_INDEX = TOTAL_STEPS - 1

        /**
         * Creates the initial step state.
         */
        fun initial(): BasicInfoStep = BasicInfoStep()
    }
}

/**
 * Form data container for the entire onboarding process.
 * Aggregates data from all wizard steps.
 */
data class OnboardingFormData(
    val basicInfo: OnboardingState.BasicInfoStep = OnboardingState.BasicInfoStep(),
    val location: OnboardingState.LocationStep = OnboardingState.LocationStep(),
    val services: OnboardingState.ServicesStep = OnboardingState.ServicesStep(),
) {
    /**
     * Validates the entire form and returns errors per field.
     */
    fun validate(): Map<String, String> = buildMap {
        if (basicInfo.businessName.isBlank()) {
            put("businessName", "Business name is required")
        }
        if (basicInfo.phone.length < OnboardingState.BasicInfoStep.MIN_PHONE_LENGTH) {
            put("phone", "Phone must be at least ${OnboardingState.BasicInfoStep.MIN_PHONE_LENGTH} digits")
        }
        if (location.address.isBlank()) {
            put("address", "Service address is required")
        }
        if (location.serviceRadiusKm < OnboardingState.LocationStep.MIN_RADIUS_KM) {
            put("serviceRadius", "Service radius must be at least ${OnboardingState.LocationStep.MIN_RADIUS_KM.toInt()} km")
        }
        if (services.selectedCategoryIds.size < OnboardingState.ServicesStep.MIN_CATEGORIES) {
            put("categories", "Select at least ${OnboardingState.ServicesStep.MIN_CATEGORIES} category")
        }
    }

    /**
     * Checks if the form is completely valid.
     */
    val isValid: Boolean
        get() = validate().isEmpty()
}