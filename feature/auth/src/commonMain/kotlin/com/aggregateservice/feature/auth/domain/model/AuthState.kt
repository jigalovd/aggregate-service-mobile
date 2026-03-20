package com.aggregateservice.feature.auth.domain.model

/**
 * Чистая доменная модель состояния аутентификации.
 *
 * **Important:** Этот класс НЕ содержит никаких платформенных зависимостей
 * или DTO из network слоя. Только чистые бизнес-данные.
 *
 * @property isAuthenticated Флаг авторизации
 * @property accessToken Токен доступа (может быть null если не авторизован)
 * @property userEmail Email пользователя (может быть null)
 */
data class AuthState(
    val isAuthenticated: Boolean = false,
    val accessToken: String? = null,
    val userEmail: String? = null,
) {
    companion object {
        /**
         * Начальное состояние (не авторизован).
         */
        val Initial = AuthState()

        /**
         * Состояние успешной авторизации.
         *
         * @param token JWT access token
         * @param email Email пользователя (может быть null при восстановлении сессии)
         */
        fun authenticated(token: String, email: String?) = AuthState(
            isAuthenticated = true,
            accessToken = token,
            userEmail = email,
        )
    }
}
