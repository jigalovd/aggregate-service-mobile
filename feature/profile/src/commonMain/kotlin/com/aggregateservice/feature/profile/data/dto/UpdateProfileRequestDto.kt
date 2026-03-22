package com.aggregateservice.feature.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for profile update request.
 *
 * All fields are optional - only provided fields will be updated.
 *
 * @property fullName New full name (optional)
 * @property phone New phone number (optional)
 */
@Serializable
data class UpdateProfileRequestDto(
    @SerialName("full_name")
    val fullName: String? = null,
    val phone: String? = null,
)
