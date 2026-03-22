package com.aggregateservice.feature.profile.domain.model

import androidx.compose.runtime.Stable

/**
 * Value object for profile update request.
 *
 * All fields are optional - only provided fields will be updated.
 *
 * @property fullName New full name (optional)
 * @property phone New phone number (optional)
 */
@Stable
data class UpdateProfileRequest(
    val fullName: String? = null,
    val phone: String? = null,
) {
    init {
        require(fullName == null || fullName.length <= 255) {
            "Full name must be at most 255 characters"
        }
        require(phone == null || phone.matches(PHONE_REGEX)) {
            "Invalid phone number format"
        }
    }

    companion object {
        /**
         * Regex for validating phone numbers.
         * Accepts international format with optional + prefix.
         */
        private val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
    }
}
