package com.aggregateservice.feature.auth.data.dto

import com.aggregateservice.feature.auth.domain.model.RegistrationRequest
import com.aggregateservice.feature.auth.domain.model.UserRole
import kotlinx.serialization.Serializable

/**
 * DTO для registration запроса (Data слой).
 *
 * **API Contract:**
 * ```json
 * POST /api/v1/auth/register
 * {
 *   "email": "user@example.com",
 *   "password": "SecurePass123!",
 *   "roles": ["client"],
 *   "phone": "+972501234567",
 *   "language_code": "ru"
 * }
 * ```
 *
 * **Mapping:**
 * - Domain: RegistrationRequest → DTO: RegisterRequestDto
 * - Network: RegisterRequestDto → JSON (Ktor сериализация)
 *
 * @see BACKEND_API_REFERENCE.md секция 3.2 "Registration"
 */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val roles: List<String>,
    val phone: String? = null,
    val language_code: String? = null,
)

/**
 * Extension function для конвертации Domain модели в DTO.
 */
fun RegistrationRequest.toDto(): RegisterRequestDto =
    RegisterRequestDto(
        email = email,
        password = password,
        roles = roles.map { it.value },
        phone = phone,
        language_code = languageCode,
    )

/**
 * Extension для получения строкового значения роли (как ожидает API).
 */
private val UserRole.value: String
    get() = when (this) {
        UserRole.CLIENT -> "client"
        UserRole.PROVIDER -> "provider"
    }
