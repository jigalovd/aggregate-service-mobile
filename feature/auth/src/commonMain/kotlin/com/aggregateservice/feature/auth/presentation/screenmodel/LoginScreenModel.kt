package com.aggregateservice.feature.auth.presentation.screenmodel

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.usecase.LoginUseCase
import com.aggregateservice.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.aggregateservice.feature.auth.presentation.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана логина (Presentation слой).
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
 * @property loginUseCase UseCase для входа
 * @property observeAuthStateUseCase UseCase для наблюдения за auth state
 */
class LoginScreenModel(
    private val loginUseCase: LoginUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
) : ScreenModel {

    // UI State
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Наблюдаем за состоянием аутентификации
        screenModelScope.launch {
            observeAuthStateUseCase().collect { authState ->
                _uiState.value = _uiState.value.copy(
                    isLoginSuccess = authState.isAuthenticated,
                )
            }
        }
    }

    /**
     * Обрабатывает ввод email.
     *
     * **Intent:** Пользователь ввел email
     */
    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    /**
     * Обрабатывает ввод пароля.
     *
     * **Intent:** Пользователь ввел пароль
     */
    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    /**
     * Очищает сообщение об ошибке.
     *
     * **Intent:** Пользователь закрыл error dialog
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Выполняет вход.
     *
     * **Intent:** Пользователь нажал кнопку "Войти"
     */
    fun onLoginClick() {
        val state = _uiState.value
        if (!state.canLogin()) return

        screenModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val credentials = LoginCredentials(
                email = state.email,
                password = state.password,
            )

            loginUseCase(credentials)
                .fold(
                    onSuccess = { authState ->
                        // AuthState обновится автоматически через ObserveAuthStateUseCase
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoginSuccess = true,
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
     * Преобразует AppError в понятное пользователю сообщение.
     *
     * **UI Logic:** Это логика представления, не бизнес-логика.
     */
    private fun AppError.toUserMessage(): String = when (this) {
        is AppError.Unauthorized -> "Неверный email или пароль"
        is AppError.AccountLocked -> "Аккаунт заблокирован до $until"
        is AppError.ValidationError -> "Ошибка валидации: $field - $message"
        is AppError.NetworkError -> "Ошибка сети: $message"
        is AppError.UnknownError -> message ?: "Произошла неизвестная ошибка"
        else -> "Произошла ошибка"
    }
}
