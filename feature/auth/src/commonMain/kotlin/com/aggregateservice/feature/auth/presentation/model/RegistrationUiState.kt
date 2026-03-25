package com.aggregateservice.feature.auth.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.feature.auth.domain.model.UserRole

/**
 * UI State для экрана регистрации (Presentation слой).
 *
 * **UDF Pattern:** Unidirectional Data Flow
 * - UI отображает state
 * - UI отправляет intents (события)
 * - ScreenModel обрабатывает intents и обновляет state
 *
 * **Compose Optimization:**
 * - @Stable аннотация позволяет Compose compiler оптимизировать рекомпозицию
 * - Все параметры immutable (val), что гарантирует стабильность
 *
 * @property email Текущий email
 * @property password Текущий пароль
 * @property confirmPassword Подтверждение пароля
 * @property phone Номер телефона (опционально)
 * @property selectedRoles Выбранные роли пользователя
 * @property languageCode Код языка
 * @property emailError Ошибка валидации email (если есть)
 * @property passwordError Ошибка валидации пароля (если есть)
 * @property confirmPasswordError Ошибка подтверждения пароля (если есть)
 * @property phoneError Ошибка валидации телефона (если есть)
 * @property isLoading Флаг загрузки
 * @property errorMessage Сообщение об ошибке от API (если есть)
 * @property isRegistrationSuccess Флаг успешной регистрации
 * @property navigateToLogin Флаг для навигации на экран логина
 */
@Stable
data class RegistrationUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val selectedRoles: Set<UserRole> = setOf(UserRole.CLIENT),
    val languageCode: String = "ru",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccess: Boolean = false,
    val navigateToLogin: Boolean = false,
) {
    /**
     * Проверяет, можно ли выполнить регистрацию.
     *
     * **Условия:**
     * - Email не пустой и без ошибок
     * - Пароль не пустой и без ошибок
     * - Подтверждение пароля совпадает
     * - Телефон валиден (если указан)
     * - Выбрана хотя бы одна роль
     * - Не идет загрузка
     */
    fun canRegister(): Boolean =
        email.isNotBlank() &&
        password.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        emailError == null &&
        passwordError == null &&
        confirmPasswordError == null &&
        phoneError == null &&
        selectedRoles.isNotEmpty() &&
        !isLoading &&
        password == confirmPassword

    /**
     * Проверяет, есть ли ошибки валидации.
     */
    fun hasValidationErrors(): Boolean =
        emailError != null ||
        passwordError != null ||
        confirmPasswordError != null ||
        phoneError != null

    /**
     * Проверяет, выбрана ли роль провайдера.
     */
    fun isProviderSelected(): Boolean =
        UserRole.PROVIDER in selectedRoles
}
