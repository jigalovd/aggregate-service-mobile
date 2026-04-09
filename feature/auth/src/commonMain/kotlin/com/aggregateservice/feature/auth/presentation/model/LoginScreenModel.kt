package com.aggregateservice.feature.auth.presentation.model

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.SignInUseCase
import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.core.firebase.AuthProviderApi
import com.aggregateservice.core.firebase.PlatformAuthContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginScreenModel(
    private val signInUseCase: SignInUseCase,
    private val authStateProvider: AuthStateProvider,
    private val authProviderApi: AuthProviderApi,
    private val logger: Logger,
) : ScreenModel {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        screenModelScope.launch {
            authStateProvider.authState.collectLatest { state ->
                _uiState.value = _uiState.value.copy(authState = state)
                if (state is AuthState.Authenticated) {
                    _uiState.value = _uiState.value.copy(isLoginSuccess = true)
                }
            }
        }
    }

    fun setError(message: String) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                errorMessage = message,
            )
    }

    fun signIn(context: PlatformAuthContext) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            logger.d { "signIn() started" }

            val result = authProviderApi.signInWithGoogle(context)
            logger.d { "signInWithGoogle returned, success=${result.isSuccess}" }
            result.fold(
                onSuccess = { authResult ->
                    logger.d { "Firebase auth success, calling signInUseCase" }
                    val signInResult = signInUseCase(authResult.provider, authResult.idToken)
                    logger.d { "signInUseCase returned, success=${signInResult.isSuccess}" }
                    signInResult.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        },
                        onFailure = {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = it.message,
                                )
                        },
                    )
                },
                onFailure = {
                    logger.w(it) { "Firebase auth failed" }
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = it.message,
                        )
                },
            )
        }
    }
}
