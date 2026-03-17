package com.aggregateservice.feature.auth.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.utils.Result
import com.aggregateservice.core.utils.Validators
import com.aggregateservice.feature.auth.domain.model.User
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.feature.auth.domain.usecase.LoginUseCase
import com.aggregateservice.feature.auth.domain.usecase.LogoutUseCase
import com.aggregateservice.feature.auth.presentation.model.AuthState
import com.aggregateservice.feature.auth.presentation.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        screenModelScope.launch {
            _authState.value = _authState.value.copy(
                isAuthenticated = authRepository.isAuthenticated()
            )
        }
    }

    fun onEmailChange(email: String) {
        _loginState.value = _loginState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChange(password: String) {
        _loginState.value = _loginState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun login() {
        val state = _loginState.value

        if (!Validators.isValidEmail(state.email)) {
            _loginState.value = state.copy(emailError = "Invalid email")
            return
        }

        if (state.password.length < 6) {
            _loginState.value = state.copy(passwordError = "Password too short")
            return
        }

        screenModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)

            when (val result = loginUseCase(state.email, state.password)) {
                is Result.Success -> {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        user = result.data
                    )
                    _loginState.value = LoginState()
                }

                is Result.Error -> {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun logout() {
        screenModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            logoutUseCase()

            _authState.value = AuthState(isAuthenticated = false)
        }
    }

    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
        _authState.value = _authState.value.copy(error = null)
    }
}
