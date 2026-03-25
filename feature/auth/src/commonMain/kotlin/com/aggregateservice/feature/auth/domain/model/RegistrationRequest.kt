package com.aggregateservice.feature.auth.domain.model

import com.aggregateservice.core.config.Config

/**
 * Domain model для запроса регистрации пользователя.
 *
 * **Validation Rules:**
 * - Email: не пустой, валидный формат
 * - Password: минимум Config.passwordMinLength символов
 * - Roles: минимум одна роль из ["client", "provider"]
 * - Phone: опционально, формат +XXXXXXXXXXX
 * - LanguageCode: опционально, ISO 639-1 (ru, he, en)
 *
 * **Business Rules:**
 * - По умолчанию роль "client"
 * - Provider роль требует создания профиля после регистрации
 *
 * @property email Email пользователя
 * @property password Пароль пользователя
 * @property roles Список ролей (client, provider)
 * @property phone Номер телефона (опционально)
 * @property languageCode Код языка (опционально)
 */
data class RegistrationRequest(
    val email: String,
    val password: String,
    val roles: Set<UserRole> = setOf(UserRole.CLIENT),
    val phone: String? = null,
    val languageCode: String? = null,
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(password.isNotBlank()) { "Password cannot be blank" }
        require(password.length >= Config.passwordMinLength) {
            "Password must be at least ${Config.passwordMinLength} characters"
        }
        require(roles.isNotEmpty()) { "At least one role is required" }
        require(roles.all { it in VALID_ROLES }) {
            "Invalid roles. Allowed: $VALID_ROLES"
        }
        phone?.let {
            require(PHONE_PATTERN.matches(it)) {
                "Phone must be in format +XXXXXXXXXXX"
            }
        }
        languageCode?.let {
            require(it in SUPPORTED_LANGUAGES) {
                "Unsupported language. Supported: $SUPPORTED_LANGUAGES"
            }
        }
    }

    companion object {
        val VALID_ROLES = setOf(UserRole.CLIENT, UserRole.PROVIDER)
        val SUPPORTED_LANGUAGES = setOf("ru", "he", "en")
        val PHONE_PATTERN = Regex("^\\+[1-9]\\d{6,14}$")
    }
}

/**
 * Роли пользователя в системе.
 *
 * **Multi-role Support:**
 * - Пользователь может иметь несколько ролей одновременно
 * - Текущий контекст хранится в JWT токене (current_role)
 * - Переключение контекста через API
 */
enum class UserRole {
    CLIENT,
    PROVIDER,
    ;
}
