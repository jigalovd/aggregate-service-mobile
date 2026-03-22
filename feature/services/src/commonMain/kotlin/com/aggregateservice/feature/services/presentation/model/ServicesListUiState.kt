package com.aggregateservice.feature.services.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.services.domain.model.ProviderService

/**
 * UI State for services list screen.
 *
 * **UDF Pattern:** Immutable state, updated through ScreenModel.
 *
 * @property services List of provider services
 * @property isLoading Loading state
 * @property isDeleting Deleting in progress
 * @property error Error state
 * @property serviceToDelete Service pending deletion (for confirmation dialog)
 */
@Stable
data class ServicesListUiState(
    val services: List<ProviderService> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val error: AppError? = null,
    val serviceToDelete: ProviderService? = null,
) {
    /**
     * Whether there are any services.
     */
    val hasServices: Boolean
        get() = services.isNotEmpty()

    /**
     * Number of active services.
     */
    val activeServicesCount: Int
        get() = services.count { it.isActive }

    /**
     * Whether delete confirmation dialog should be shown.
     */
    val showDeleteDialog: Boolean
        get() = serviceToDelete != null

    companion object {
        /**
         * Initial loading state.
         */
        val Loading = ServicesListUiState(isLoading = true)

        /**
         * Error state.
         */
        fun error(error: AppError) = ServicesListUiState(
            isLoading = false,
            error = error,
        )
    }
}
