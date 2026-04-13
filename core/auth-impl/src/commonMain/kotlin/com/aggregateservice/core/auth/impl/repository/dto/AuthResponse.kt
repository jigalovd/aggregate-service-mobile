package com.aggregateservice.core.auth.impl.repository.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    val user: UserResponse,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String? = null,
    val isActive: Boolean = true,
    val isVerified: Boolean = true,
    val roles: List<String> = emptyList(),
    val currentRole: String? = null,
)

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
)
