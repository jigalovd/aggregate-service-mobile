package com.aggregateservice.feature.auth.presentation.model

import androidx.compose.runtime.Stable

/**
 * UI State для экрана логина (Presentation слой).
 *
 * **UDF Pattern:** Unidirectional Data Flow
 * - UI отображает state
 * - UI отправляет intents (события)
 * - ScreenModel обрабатывает intents и обновляет state
 *
 * **Compose Optimization:**
 * - @Stable аннотация позволяет Comose compiler оптимизировать рекомпозицию
 * - Все параметры immutable (val), что гарантирует стабильность
 *
 * @property email Текущий email
 * @property password Текущий пароль
 * @property emailError Ошибка валидации email (если есть)
 * @property passwordError Ошибка валидации пароля (если есть)
 * @property isLoading Флаг загрузки
 * @property errorMessage Сообщение об ошибке (если есть)
 * @property isLoginSuccess Флаг успешного входа
 */
@Stable
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,

    // Firebase Auth
    val linkAccount: LinkAccountState = LinkAccountState(),
    val isFirebaseLoading: Boolean = false,

    // Phone Auth
    val phoneAuth: PhoneAuthState = PhoneAuthState(),
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

/**
 * Phone authentication state.
 *
 * @property isInPhoneMode Whether phone input is active
 * @property phoneNumber Current phone input
 * @property countryCode Country code (e.g., "+7", "+1")
 * @property verificationId Firebase verification ID after SMS sent
 * @property verificationCode User input verification code
 * @property isWaitingForCode Whether SMS has been sent and awaiting code
 * @property isResendAvailable Whether resend button is active
 * @property resendCountdown Seconds until resend available
 */
@Stable
data class PhoneAuthState(
    val isInPhoneMode: Boolean = false,
    val phoneNumber: String = "",
    val countryCode: String = "+7",
    val verificationId: String? = null,
    val verificationCode: String = "",
    val isWaitingForCode: Boolean = false,
    val isResendAvailable: Boolean = false,
    val resendCountdown: Int = 0,
)

/**
 * Account linking state from Firebase response.
 *
 * @property email Email requiring linking
 * @property firebaseUid Firebase UID to link
 * @property authProvider Firebase auth provider
 * @property showDialog Whether to show linking dialog
 */
@Stable
data class LinkAccountState(
    val email: String = "",
    val firebaseUid: String = "",
    val authProvider: String = "",
    val showDialog: Boolean = false,
)
