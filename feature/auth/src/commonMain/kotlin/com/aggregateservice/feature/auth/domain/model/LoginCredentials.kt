package com.aggregateservice.feature.auth.domain.model

/**
 * Чистая доменная модель для credential'ов пользователя.
 *
 * **Important:** Это доменная модель, не DTO для API.
 * Сетевые DTO находятся в data/dto пакете.
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
        require(password.length >= 6) { "Password must be at least 6 characters" }
    }
}
