package com.aggregateservice.feature.provider.onboarding.presentation.model

import androidx.compose.runtime.Immutable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.onboarding.OnboardingState

/**
 * UI State for the provider onboarding screen.
 *
 * **UDF Pattern:** Immutable state, updated only via ScreenModel.
 *
 * Uses sealed class for strict state typing:
 * - Loading: initial API call or form submission
 * - Content: form data ready for user input
 * - Error: something went wrong (can retry)
 * - Submitted: onboarding completed successfully
 *
 * **State Flow:**
 * ```
 * Loading → Content(step=X) → (nextStep/prevStep) → Content(step=X+1)
 *                                              → (submit) → Loading → Content(submitted=true)
 * Content → Error(on failure) → (retry) → Loading → Content
 * ```
 *
 * @property currentStep Current wizard step (0, 1, or 2)
 * @property isValid Whether the current step's fields are valid
 * @property validationErrors Map of field name → error message
 */
@Immutable
sealed class OnboardingUiState {
    /**
     * Loading state - used during initial load and form submission.
     */
    data object Loading : OnboardingUiState()

    /**
     * Content state - form is ready for user input.
     *
     * @property step Current wizard step (0 = Basic Info, 1 = Location, 2 = Services)
     * @property isValid Whether the current step's form fields are valid
     * @property validationErrors Field-specific validation errors
     * @property isSubmitting Whether the form is being submitted
     * @property isSubmitted Whether onboarding was completed successfully
     */
    data class Content(
        val step: Int = 0,
        val isValid: Boolean = false,
        val validationErrors: Map<String, String> = emptyMap(),
        val isSubmitting: Boolean = false,
        val isSubmitted: Boolean = false,
    ) : OnboardingUiState() {
        val isFirstStep: Boolean get() = step == 0
        val isLastStep: Boolean get() = step == OnboardingState.LAST_STEP_INDEX

        val stepTitle: String
            get() = when (step) {
                0 -> "Basic Information"
                1 -> "Service Location"
                2 -> "Select Services"
                else -> "Onboarding"
            }

        val stepDescription: String
            get() = when (step) {
                0 -> "Tell us about your business"
                1 -> "Where will you provide services?"
                2 -> "Choose categories you offer"
                else -> ""
            }

        companion object {
            fun initial(): Content = Content(
                step = 0,
                isValid = false,
                validationErrors = emptyMap(),
                isSubmitting = false,
                isSubmitted = false,
            )
        }
    }

    /**
     * Error state - something went wrong and user can retry.
     *
     * @property error The error that occurred
     * @property canRetry Whether the operation can be retried
     */
    data class Error(
        val error: AppError,
        val canRetry: Boolean = true,
    ) : OnboardingUiState() {
        companion object {
            fun networkError(message: String = "Network error. Please try again."): Error =
                Error(AppError.NetworkError(0, message))

            fun serverError(message: String = "Server error. Please try again later."): Error =
                Error(AppError.NetworkError(500, message))

            fun unauthorized(): Error =
                Error(AppError.Unauthorized)
        }
    }

    companion object {
        /**
         * Convenience to create a loading state.
         */
        fun loading(): Loading = Loading

        /**
         * Convenience to create an initial content state.
         */
        fun content(): Content = Content.initial()
    }
}