package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для refresh token ответа от бэкенда (Data слой).
 *
 * **Mapping:**
 * - Network: JSON → DTO: RefreshTokenResponse (Ktor десериализация)
 * - DTO: RefreshTokenResponse → Domain: String (access token)
 *
 * @property accessToken Новый JWT access token
 *
 * @see BACKEND_API_REFERENCE.md секция 3.3 "Refresh Token"
 */
@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
)
