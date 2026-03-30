package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для auth ответа от бэкенда (Data слой).
 *
 * **Mapping:**
 * - Network: JSON → DTO: AuthResponse (Ktor десериализация)
 * - DTO: AuthResponse → Domain: AuthState (в Repository)
 *
 * @property accessToken JWT access token (15 min expiration)
 *
 * **Note:** Refresh token приходит в HTTP-only cookie,
 * его НЕ нужно парсить из JSON.
 *
 * @see BACKEND_API_REFERENCE.md секция 3.1 "JWT токены"
 */
@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String,
    val user: FirebaseUserResponse,
)
