package com.aggregateservice.feature.auth.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.core.firebase.FirebaseAuthApiFactory
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.EmailValidator
import com.aggregateservice.core.utils.PasswordValidator
import com.aggregateservice.core.utils.ValidationResult
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.feature.auth.domain.usecase.LoginUseCase
import com.aggregateservice.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.aggregateservice.feature.auth.presentation.model.LinkAccountState
import com.aggregateservice.feature.auth.presentation.model.LoginUiState
import com.aggregateservice.feature.auth.presentation.model.PhoneAuthState
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
 * @property authRepository Repository для Firebase операций
 * @property firebaseAuthApi Firebase Auth API
 */
class LoginScreenModel(
    private val loginUseCase: LoginUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val authRepository: AuthRepository,
    private val firebaseAuthApi: FirebaseAuthApi? = null,
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
     * Обрабатывает ввод email с валидацией.
     *
     * **Intent:** Пользователь ввел email
     *
     * **Валидация:**
     * - Проверяет формат email
     * - Обновляет emailError если есть ошибка
     * - Очищает ошибку если email валиден
     */
    fun onEmailChanged(email: String) {
        val emailError = when (val result = EmailValidator.validate(email)) {
            is ValidationResult.Invalid -> result.errorMessage
            is ValidationResult.Valid -> null
        }

        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = emailError
        )
    }

    /**
     * Обрабатывает ввод пароля с валидацией.
     *
     * **Intent:** Пользователь ввел пароль
     *
     * **Валидация:**
     * - Проверяет длину пароля (минимум 8 символов)
     * - Проверяет наличие букв
     * - Обновляет passwordError если есть ошибка
     * - Очищает ошибку если пароль валиден
     */
    fun onPasswordChanged(password: String) {
        val passwordError = when (val result = PasswordValidator.validate(password)) {
            is ValidationResult.Invalid -> result.errorMessage
            is ValidationResult.Valid -> null
        }

        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = passwordError
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
            passwordError = null
        )
    }

    /**
     * Выполняет вход.
     *
     * **Intent:** Пользователь нажал кнопку "Войти"
     */
    fun onLoginClick() {
        val state = _uiState.value
        if (!state.canLogin()) return

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        screenModelScope.launch {
            val credentials = LoginCredentials(
                email = state.email,
                password = state.password,
            )

            loginUseCase(credentials)
                .fold(
                    onSuccess = { authState ->
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
     * Toggle phone auth mode visibility.
     */
    fun onPhoneModeToggle() {
        _uiState.value = _uiState.value.copy(
            phoneAuth = _uiState.value.phoneAuth.copy(
                isInPhoneMode = !_uiState.value.phoneAuth.isInPhoneMode,
                isWaitingForCode = false,
                verificationId = null,
                verificationCode = "",
                phoneNumber = "",
            ),
        )
    }

    /**
     * Update phone number input.
     */
    fun onPhoneNumberChanged(phone: String) {
        _uiState.value = _uiState.value.copy(
            phoneAuth = _uiState.value.phoneAuth.copy(phoneNumber = phone),
        )
    }

    /**
     * Update country code selection.
     */
    fun onCountryCodeChanged(countryCode: String) {
        _uiState.value = _uiState.value.copy(
            phoneAuth = _uiState.value.phoneAuth.copy(countryCode = countryCode),
        )
    }

    /**
     * Send verification code for phone auth.
     */
    fun onSendPhoneCode() {
        val phone = _uiState.value.phoneAuth
        if (phone.phoneNumber.isBlank()) return
        val api = firebaseAuthApi ?: return

        val fullPhone = "${phone.countryCode}${phone.phoneNumber}"
        _uiState.value = _uiState.value.copy(
            isFirebaseLoading = true,
            phoneAuth = phone.copy(isWaitingForCode = true),
        )

        screenModelScope.launch {
            api.signInWithPhoneStart(fullPhone)
                .fold(
                    onSuccess = { verificationId ->
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            phoneAuth = _uiState.value.phoneAuth.copy(
                                verificationId = verificationId,
                                isResendAvailable = false,
                            ),
                        )
                        // Start countdown for resend
                        startResendCountdown()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            errorMessage = "SMS send failed: ${error.message}",
                            phoneAuth = _uiState.value.phoneAuth.copy(isWaitingForCode = false),
                        )
                    },
                )
        }
    }

    /**
     * Confirm phone verification code.
     */
    fun onVerifyPhoneCode() {
        val phone = _uiState.value.phoneAuth
        val verificationId = phone.verificationId ?: return
        if (phone.verificationCode.isBlank()) return
        val api = firebaseAuthApi ?: return

        _uiState.value = _uiState.value.copy(isFirebaseLoading = true)

        screenModelScope.launch {
            api.confirmPhoneCode(verificationId, phone.verificationCode)
                .fold(
                    onSuccess = { token ->
                        verifyFirebaseWithBackend(token.authProvider, token.idToken)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            errorMessage = "Verification failed: ${error.message}",
                        )
                    },
                )
        }
    }

    /**
     * Handle Google Sign-In.
     */
    fun onGoogleSignIn() {
        val api = firebaseAuthApi ?: return
        _uiState.value = _uiState.value.copy(isFirebaseLoading = true)

        screenModelScope.launch {
            api.signInWithGoogle()
                .fold(
                    onSuccess = { token ->
                        verifyFirebaseWithBackend(token.authProvider, token.idToken)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            errorMessage = "Google sign-in failed: ${error.message}",
                        )
                    },
                )
        }
    }

    /**
     * Handle Apple Sign-In.
     */
    fun onAppleSignIn() {
        val api = firebaseAuthApi ?: return
        _uiState.value = _uiState.value.copy(isFirebaseLoading = true)

        screenModelScope.launch {
            api.signInWithApple()
                .fold(
                    onSuccess = { token ->
                        verifyFirebaseWithBackend(token.authProvider, token.idToken)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            errorMessage = "Apple sign-in failed: ${error.message}",
                        )
                    },
                )
        }
    }

    /**
     * Internal: Verify Firebase token with backend.
     */
    private fun verifyFirebaseWithBackend(authProvider: String, firebaseToken: String) {
        screenModelScope.launch {
            authRepository.verifyFirebaseToken(authProvider, firebaseToken)
                .fold(
                    onSuccess = { authState ->
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            isLoginSuccess = true,
                        )
                    },
                    onFailure = { error ->
                        // Check if link_required using typed error
                        if (error is AppError.FirebaseLinkRequired) {
                            _uiState.value = _uiState.value.copy(
                                isFirebaseLoading = false,
                                linkAccount = LinkAccountState(
                                    email = error.email,
                                    tempToken = error.tempToken,
                                    firebaseUid = error.firebaseUid,
                                    authProvider = authProvider,
                                    showDialog = true,
                                ),
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isFirebaseLoading = false,
                                errorMessage = error.message ?: "Authentication failed",
                            )
                        }
                    },
                )
        }
    }

    /**
     * Handle account linking with password.
     */
    fun onLinkAccount(password: String) {
        val linkState = _uiState.value.linkAccount

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFirebaseLoading = true,
                linkAccount = linkState.copy(showDialog = false),
            )

            // Use tempToken to link Firebase account with existing account
            authRepository.linkFirebaseAccount(linkState.tempToken, password)
                .fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            isLoginSuccess = true,
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            errorMessage = if (error.message?.contains("invalid_credentials") == true) {
                                "Wrong password"
                            } else {
                                error.message ?: "Linking failed"
                            },
                            linkAccount = linkState.copy(showDialog = true),
                        )
                    },
                )
        }
    }

    /**
     * Dismiss link account dialog.
     */
    fun onDismissLinkDialog() {
        _uiState.value = _uiState.value.copy(
            linkAccount = _uiState.value.linkAccount.copy(showDialog = false),
        )
    }

    private fun startResendCountdown() {
        screenModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.value = _uiState.value.copy(
                    phoneAuth = _uiState.value.phoneAuth.copy(
                        resendCountdown = i,
                        isResendAvailable = i == 0,
                    ),
                )
                kotlinx.coroutines.delay(1000)
            }
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
