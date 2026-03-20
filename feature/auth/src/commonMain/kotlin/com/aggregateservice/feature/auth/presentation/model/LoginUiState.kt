package com.aggregateservice.feature.auth.presentation.model

/**
 * UI State для экрана логина (Presentation слой).
 *
 * **UDF Pattern:** Unidirectional Data Flow
 * - UI отображает state
 * - UI отправляет intents (события)
 * - ScreenModel обрабатывает intents и обновляет state
 *
 * @property email Текущий email
 * @property password Текущий пароль
 * @property isLoading Флаг загрузки
 * @property errorMessage Сообщение об ошибке (если есть)
 * @property isLoginSuccess Флаг успешного входа
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
) {
    /**
     * Проверяет, можно ли выполнить вход.
     */
    fun canLogin(): Boolean =
        email.isNotBlank() &&
        password.isNotBlank() &&
        !isLoading
}
