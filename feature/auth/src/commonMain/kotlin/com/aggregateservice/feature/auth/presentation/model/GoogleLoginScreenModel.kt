package com.aggregateservice.feature.auth.presentation.model

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.auth.domain.usecase.SignInWithFirebaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ScreenModel для экрана Google Sign-In.
 *
 * **Architecture:**
 * - Separates business logic from UI (Screen)
 * - Survives configuration changes (unlike composable state)
 * - Manages sign-in state via StateFlow
 *
 * @property signInWithFirebaseUseCase UseCase для Firebase Sign-In
 * @property i18nProvider Провайдер локализации
 */
class GoogleLoginScreenModel(
    private val signInWithFirebaseUseCase: SignInWithFirebaseUseCase,
    private val i18nProvider: I18nProvider,
) : ScreenModel {

    private val _uiState = MutableStateFlow(GoogleLoginUiState())
    val uiState: StateFlow<GoogleLoginUiState> = _uiState.asStateFlow()

    /**
     * Выполняет вход через Google/Firebase.
     */
    suspend fun signIn() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        signInWithFirebaseUseCase().fold(
            onSuccess = {
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: i18nProvider[StringKey.ERROR],
                    )
                }
            },
        )
    }
}

/**
 * UI State для Google Login экрана.
 */
@Stable
data class GoogleLoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
)
