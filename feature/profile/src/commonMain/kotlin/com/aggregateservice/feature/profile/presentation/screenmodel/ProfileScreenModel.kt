package com.aggregateservice.feature.profile.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.profile.domain.model.UpdateProfileRequest
import com.aggregateservice.feature.profile.domain.usecase.GetProfileUseCase
import com.aggregateservice.feature.profile.domain.usecase.UpdateProfileUseCase
import com.aggregateservice.feature.profile.presentation.model.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for profile screen.
 *
 * **Responsibilities:**
 * - Loading user profile
 * - Managing edit mode
 * - Validating form input
 * - Saving profile changes
 *
 * @property getProfileUseCase UseCase for loading profile
 * @property updateProfileUseCase UseCase for updating profile
 */
class ProfileScreenModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Loads the user profile.
     */
    fun loadProfile() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getProfileUseCase().fold(
                onSuccess = { profile ->
                    _uiState.update {
                        ProfileUiState(
                            profile = profile,
                            isLoading = false,
                            editFullName = profile.fullName ?: "",
                            editPhone = profile.phone ?: "",
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        ProfileUiState.error(error.toAppError())
                    }
                },
            )
        }
    }

    /**
     * Enters edit mode.
     */
    fun startEditing() {
        val profile = _uiState.value.profile ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                editFullName = profile.fullName ?: "",
                editPhone = profile.phone ?: "",
                fullNameError = null,
                phoneError = null,
            )
        }
    }

    /**
     * Cancels editing and reverts changes.
     */
    fun cancelEditing() {
        val profile = _uiState.value.profile ?: return
        _uiState.update {
            it.copy(
                isEditing = false,
                editFullName = profile.fullName ?: "",
                editPhone = profile.phone ?: "",
                fullNameError = null,
                phoneError = null,
            )
        }
    }

    /**
     * Updates the full name field.
     */
    fun onFullNameChanged(value: String) {
        _uiState.update {
            it.copy(
                editFullName = value,
                fullNameError = validateFullName(value),
            )
        }
    }

    /**
     * Updates the phone field.
     */
    fun onPhoneChanged(value: String) {
        _uiState.update {
            it.copy(
                editPhone = value,
                phoneError = validatePhone(value),
            )
        }
    }

    /**
     * Saves the profile changes.
     */
    fun saveProfile() {
        val currentState = _uiState.value

        // Validate before saving
        val fullNameError = validateFullName(currentState.editFullName)
        val phoneError = validatePhone(currentState.editPhone)

        if (fullNameError != null || phoneError != null) {
            _uiState.update { it.copy(fullNameError = fullNameError, phoneError = phoneError) }
            return
        }

        // Check if there are actual changes
        if (!currentState.hasChanges) {
            _uiState.update { it.copy(isEditing = false) }
            return
        }

        screenModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val request = UpdateProfileRequest(
                fullName = currentState.editFullName.takeIf { it.isNotBlank() },
                phone = currentState.editPhone.takeIf { it.isNotBlank() },
            )

            updateProfileUseCase(request).fold(
                onSuccess = { updatedProfile ->
                    _uiState.update {
                        it.copy(
                            profile = updatedProfile,
                            isEditing = false,
                            isSaving = false,
                            saveSuccess = true,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.toAppError(),
                        )
                    }
                },
            )
        }
    }

    /**
     * Clears the success message.
     */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    /**
     * Clears the error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateFullName(value: String): String? {
        return when {
            value.isNotBlank() && value.length > 255 -> "Full name must be at most 255 characters"
            else -> null
        }
    }

    private fun validatePhone(value: String): String? {
        return when {
            value.isBlank() -> null // Phone is optional
            !value.matches(PHONE_REGEX) -> "Invalid phone number format"
            else -> null
        }
    }

    companion object {
        private val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
    }
}
