package com.aggregateservice.feature.auth.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.EmailValidator
import com.aggregateservice.core.utils.PasswordValidator
import com.aggregateservice.core.utils.ValidationResult
import com.aggregateservice.feature.auth.domain.model.RegistrationRequest
import com.aggregateservice.feature.auth.domain.model.UserRole
import com.aggregateservice.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.aggregateservice.feature.auth.domain.usecase.RegisterUseCase
import com.aggregateservice.feature.auth.presentation.model.RegistrationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана регистрации (Presentation слой).
 *
 * **Architecture:**
 * - ScreenModel (Voyager) = ViewModel в Android architecture
 * - Хранит UI state
 * - Обрабатывает пользовательские действия (intents)
 * - Вызывает UseCases из Domain слоя
 *
 * **Important:** ScreenModel НЕ должен:
 * - Вызывать напрямую Repository или Ktor
 * - Импортировать Android/iOS классы
 * - Содержать бизнес-логику (только UI-логику)
 *
 * @property registerUseCase UseCase для регистрации
 * @property observeAuthStateUseCase UseCase для наблюдения за auth state
 */
class RegistrationScreenModel(
    private val registerUseCase: RegisterUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
) : ScreenModel {

    // UI State
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    init {
        // Наблюдаем за состоянием аутентификации
        screenModelScope.launch {
            observeAuthStateUseCase().collect { authState ->
                _uiState.value = _uiState.value.copy(
                    isRegistrationSuccess = authState.isAuthenticated,
                )
            }
        }
    }

    /**
     * Обрабатывает ввод email с валидацией.
     *
     * **Intent:** Пользователь ввел email
     */
    fun onEmailChanged(email: String) {
        val emailError = when (val result = EmailValidator.validate(email)) {
            is ValidationResult.Invalid -> result.errorMessage
            is ValidationResult.Valid -> null
        }

        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = emailError,
        )
    }

    /**
     * Обрабатывает ввод пароля с валидацией.
     *
     * **Intent:** Пользователь ввел пароль
     */
    fun onPasswordChanged(password: String) {
        val passwordError = when (val result = PasswordValidator.validate(password)) {
            is ValidationResult.Invalid -> result.errorMessage
            is ValidationResult.Valid -> null
        }

        // Также проверяем совпадение с подтверждением
        val confirmPasswordError = if (_uiState.value.confirmPassword.isNotBlank()) {
            if (password != _uiState.value.confirmPassword) {
                "Пароли не совпадают"
            } else {
                null
            }
        } else {
            _uiState.value.confirmPasswordError
        }

        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
        )
    }

    /**
     * Обрабатывает ввод подтверждения пароля.
     *
     * **Intent:** Пользователь ввел подтверждение пароля
     */
    fun onConfirmPasswordChanged(confirmPassword: String) {
        val confirmPasswordError = if (confirmPassword != _uiState.value.password) {
            "Пароли не совпадают"
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = confirmPasswordError,
        )
    }

    /**
     * Обрабатывает ввод телефона с валидацией.
     *
     * **Intent:** Пользователь ввел телефон
     */
    fun onPhoneChanged(phone: String) {
        val phoneError = if (phone.isNotBlank()) {
            if (!RegistrationRequest.PHONE_PATTERN.matches(phone)) {
                "Неверный формат телефона. Пример: +972501234567"
            } else {
                null
            }
        } else {
            null // Телефон опциональный
        }

        _uiState.value = _uiState.value.copy(
            phone = phone,
            phoneError = phoneError,
        )
    }

    /**
     * Обрабатывает выбор роли.
     *
     * **Intent:** Пользователь выбрал/отменил роль
     */
    fun onRoleToggled(role: UserRole) {
        val currentRoles = _uiState.value.selectedRoles
        val newRoles = if (role in currentRoles) {
            currentRoles - role
        } else {
            currentRoles + role
        }

        _uiState.value = _uiState.value.copy(
            selectedRoles = newRoles,
        )
    }

    /**
     * Обрабатывает выбор языка.
     *
     * **Intent:** Пользователь выбрал язык
     */
    fun onLanguageChanged(languageCode: String) {
        _uiState.value = _uiState.value.copy(
            languageCode = languageCode,
        )
    }

    /**
     * Очищает все сообщения об ошибках.
     *
     * **Intent:** Пользователь закрыл error dialog
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            phoneError = null,
        )
    }

    /**
     * Выполняет регистрацию.
     *
     * **Intent:** Пользователь нажал кнопку "Зарегистрироваться"
     */
    fun onRegisterClick() {
        val state = _uiState.value
        if (!state.canRegister()) return

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        screenModelScope.launch {
            val request = RegistrationRequest(
                email = state.email.trim(),
                password = state.password,
                roles = state.selectedRoles,
                phone = state.phone.ifBlank { null },
                languageCode = state.languageCode,
            )

            registerUseCase(request)
                .fold(
                    onSuccess = { authState ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegistrationSuccess = true,
                        )
                    },
                    onFailure = { error ->
                        val errorMessage = when (error) {
                            is AppError -> error.toUserMessage()
                            else -> "Произошла неизвестная ошибка: ${error.message}"
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = errorMessage,
                        )
                    },
                )
        }
    }

    /**
     * Навигация на экран логина.
     *
     * **Intent:** Пользователь нажал "Уже есть аккаунт? Войти"
     */
    fun onNavigateToLogin() {
        _uiState.value = _uiState.value.copy(navigateToLogin = true)
    }

    /**
     * Сбрасывает флаг навигации.
     */
    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToLogin = false)
    }

    /**
     * Преобразует AppError в понятное пользователю сообщение.
     *
     * **UI Logic:** Это логика представления, не бизнес-логика.
     */
    private fun AppError.toUserMessage(): String = when (this) {
        is AppError.Unauthorized -> "Ошибка авторизации"
        is AppError.Forbidden -> "Доступ запрещён"
        is AppError.Conflict -> "Пользователь с таким email уже существует"
        is AppError.ValidationError -> "Ошибка валидации: ${field} ${message}"
        is AppError.NetworkError -> if (code >= 500) {
            "Ошибка сервера. Попробуйте позже"
        } else {
            "Ошибка сети: $message"
        }
        is AppError.RateLimitExceeded -> "Слишком много попыток. Попробуйте через $retryAfter сек."
        is AppError.AccountLocked -> "Аккаунт заблокирован до $until"
        is AppError.SlotNotAvailable -> reason
        is AppError.NotFound -> "Ресурс не найден"
        is AppError.FirebaseLinkRequired -> "Требуется связывание аккаунта"
        is AppError.UnknownError -> message ?: "Произошла неизвестная ошибка"
    }
}
