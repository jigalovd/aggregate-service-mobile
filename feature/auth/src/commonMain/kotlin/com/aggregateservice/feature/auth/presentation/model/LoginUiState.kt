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
 * @property emailError Ошибка валидации email (если есть)
 * @property passwordError Ошибка валидации пароля (если есть)
 * @property isLoading Флаг загрузки
 * @property errorMessage Сообщение об ошибке (если есть)
 * @property isLoginSuccess Флаг успешного входа
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
) {
    /**
     * Проверяет, можно ли выполнить вход.
     *
     * **Условия:**
     * - Email не пустой
     * - Пароль не пустой
     * - Нет ошибок валидации
     * - Не идет загрузка
     */
    fun canLogin(): Boolean =
        email.isNotBlank() &&
        password.isNotBlank() &&
        emailError == null &&
        passwordError == null &&
        !isLoading

    /**
     * Проверяет, есть ли ошибки валидации.
     */
    fun hasValidationErrors(): Boolean =
        emailError != null || passwordError != null
}
