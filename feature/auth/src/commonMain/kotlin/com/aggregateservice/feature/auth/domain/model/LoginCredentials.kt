package com.aggregateservice.feature.auth.domain.model

import com.aggregateservice.core.config.Config

/**
 * Чистая доменная модель для credential'ов пользователя.
 *
 * **Important:** Это доменная модель, не DTO для API.
 * Сетевые DTO находятся в data/dto пакете.
 *
 * **Валидация:**
 * - Email не должен быть пустым
 * - Пароль не должен быть пустым
 * - Длина пароля проверяется по Config.passwordMinLength (12 символов)
 *
 * @property email Email пользователя
 * @property password Пароль пользователя
 */
data class LoginCredentials(
    val email: String,
    val password: String,
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(password.isNotBlank()) { "Password cannot be blank" }

        // Используем Config для минимальной длины пароля
        val minLength = Config.passwordMinLength
        require(password.length >= minLength) {
            "Password must be at least $minLength characters"
        }
    }
}
