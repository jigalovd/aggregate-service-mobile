package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для ответа /api/v1/auth/me.
 *
 * **Mapping:**
 * - Network: JSON → DTO: UserResponseDto (Ktor deserialization)
 * - DTO: UserResponseDto → Domain: AuthState.Authenticated (in Repository)
 *
 * @property id User ID (uuid)
 * @property email User email
 * @property isActive Whether the user is active
 * @property isVerified Whether the email is verified
 * @property roles List of user roles ('client', 'provider')
 * @property currentRole Current active role
 * @property createdAt Account creation timestamp
 */
@Serializable
data class UserResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("roles")
    val roles: List<String>,
    @SerialName("current_role")
    val currentRole: String? = null,
    @SerialName("created_at")
    val createdAt: String,
)
