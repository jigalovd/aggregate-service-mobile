package com.aggregateservice.feature.auth.presentation.model

import com.aggregateservice.core.auth.state.AuthState

data class LoginUiState(
    val isLoading: Boolean = false,
    val authState: AuthState = AuthState.Loading,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null,
)
