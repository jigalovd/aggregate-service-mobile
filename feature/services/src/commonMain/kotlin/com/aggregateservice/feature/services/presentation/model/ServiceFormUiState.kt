package com.aggregateservice.feature.services.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.services.domain.model.ProviderService

/**
 * UI State for service create/edit form screen.
 *
 * **UDF Pattern:** Immutable state, updated through ScreenModel.
 *
 * @property isLoading Initial loading state (for edit mode)
 * @property isSaving Saving in progress
 * @property serviceId Service ID (null for create mode)
 * @property name Service name
 * @property description Service description
 * @property basePrice Base price as string
 * @property durationMinutes Duration as string
 * @property categoryId Category ID
 * @property isActive Active status
 * @property nameError Name validation error
 * @property priceError Price validation error
 * @property durationError Duration validation error
 * @property error General error state
 * @property saveSuccess Whether save was successful
 */
@Stable
data class ServiceFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val serviceId: String? = null,
    val name: String = "",
    val description: String = "",
    val basePrice: String = "",
    val durationMinutes: String = "",
    val categoryId: String = "",
    val isActive: Boolean = true,
    val nameError: String? = null,
    val priceError: String? = null,
    val durationError: String? = null,
    val error: AppError? = null,
    val saveSuccess: Boolean = false,
) {
    /**
     * Whether this is edit mode (existing service).
     */
    val isEditMode: Boolean
        get() = serviceId != null

    /**
     * Whether form is valid and can be saved.
     */
    val isValid: Boolean
        get() = name.isNotBlank() &&
                basePrice.isNotBlank() &&
                durationMinutes.isNotBlank() &&
                categoryId.isNotBlank() &&
                nameError == null &&
                priceError == null &&
                durationError == null

    /**
     * Parsed base price.
     */
    val parsedPrice: Double?
        get() = basePrice.toDoubleOrNull()

    /**
     * Parsed duration.
     */
    val parsedDuration: Int?
        get() = durationMinutes.toIntOrNull()

    companion object {
        /**
         * Initial state for create mode.
         */
        val Create = ServiceFormUiState()

        /**
         * Initial state from existing service (edit mode).
         */
        fun fromService(service: ProviderService) = ServiceFormUiState(
            isLoading = false,
            serviceId = service.id,
            name = service.name,
            description = service.description ?: "",
            basePrice = service.basePrice.toString(),
            durationMinutes = service.durationMinutes.toString(),
            categoryId = service.categoryId,
            isActive = service.isActive,
        )
    }
}
