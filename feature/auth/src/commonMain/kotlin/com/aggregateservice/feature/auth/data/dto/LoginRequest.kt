package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO для login запроса (Data слой).
 *
 * **Mapping:**
 * - Domain: LoginCredentials → DTO: LoginRequest (в Repository)
 * - Network: LoginRequest → JSON (Ktor сериализация)
 *
 * @see BACKEND_API_REFERENCE.md секция 3.2 "Login"
 */
@Serializable
data class LoginRequest(
    val username: String, // Бэкенд ожидает 'username' (это email)
    val password: String,
)
