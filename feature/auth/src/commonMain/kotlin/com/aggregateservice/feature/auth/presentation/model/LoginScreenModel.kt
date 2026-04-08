package com.aggregateservice.feature.auth.presentation.model

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message,
        )
    }

    fun signIn(context: PlatformAuthContext) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authProviderApi.signInWithGoogle(context)
            result.fold(
                onSuccess = { authResult ->
                    val signInResult = signInUseCase(authResult.provider, authResult.idToken)
                    signInResult.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        },
                        onFailure = {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = it.message,
                            )
                        },
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message,
                    )
                },
            )
        }
    }
}
