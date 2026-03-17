package com.aggregateservice.feature.auth.data.remote.dto

import com.aggregateservice.feature.auth.domain.model.AuthTokens
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserResponse
) {
    fun toAuthTokens(): AuthTokens = AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )
}
