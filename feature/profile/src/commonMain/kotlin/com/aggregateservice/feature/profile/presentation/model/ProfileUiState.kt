package com.aggregateservice.feature.profile.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.profile.domain.model.Profile

/**
 * UI State for profile screen.
 *
 * **UDF Pattern:** Immutable state, updated through ScreenModel.
 *
 * @property profile Current user profile
 * @property isLoading Initial loading state
 * @property isEditing Whether in edit mode
 * @property isSaving Saving in progress
 * @property error Error state
 * @property editFullName Editing value for full name
 * @property editPhone Editing value for phone
 * @property fullNameError Validation error for full name
 * @property phoneError Validation error for phone
 * @property saveSuccess Whether save was successful (for showing snackbar)
 */
@Stable
data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    // Edit fields
    val editFullName: String = "",
    val editPhone: String = "",
    val fullNameError: String? = null,
    val phoneError: String? = null,
    // Success feedback
    val saveSuccess: Boolean = false,
    // Role information for multi-role users
    val currentRole: String? = null,
    val roles: List<String> = emptyList(),
) {
    /**
     * Whether there is a profile loaded.
     */
    val hasProfile: Boolean
        get() = profile != null

    /**
     * Whether user can switch roles.
     */
    val canSwitchRole: Boolean
        get() = roles.size > 1

    /**
     * Whether edit form is valid.
     */
    val isFormValid: Boolean
        get() = fullNameError == null && phoneError == null

    /**
     * Whether any edit has been made.
     */
    val hasChanges: Boolean
        get() =
            profile?.let { p ->
                editFullName != (p.fullName ?: "") ||
                    editPhone != (p.phone ?: "")
            } ?: false

    companion object {
        /**
         * Initial loading state.
         */
        val Loading = ProfileUiState(isLoading = true)

        /**
         * Error state.
         */
        fun error(error: AppError) =
            ProfileUiState(
                isLoading = false,
                error = error,
            )
    }
}
