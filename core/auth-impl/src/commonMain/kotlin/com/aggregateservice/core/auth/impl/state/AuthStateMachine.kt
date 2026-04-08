package com.aggregateservice.core.auth.impl.state

import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.token.TokenManager
import com.aggregateservice.core.auth.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthStateMachine(
    private val tokenManager: TokenManager,
    private val repository: AuthRepository,
) {
    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    suspend fun initialize() {
        val token = tokenManager.getAccessToken()
        if (token == null) {
            _state.value = AuthState.Guest
            return
        }

        val userResult = repository.getCurrentUser()
        userResult.fold(
            onSuccess = { user ->
                _state.value = AuthState.Authenticated(
                    userId = user.id,
                    email = user.email,
                    roles = user.roles.toSet(),
                    currentRole = user.currentRole,
                )
            },
            onFailure = {
                tokenManager.clearTokens()
                _state.value = AuthState.Guest
            },
        )
    }

    suspend fun signIn(
        accessToken: String,
        userId: String,
        email: String?,
        roles: Set<String>,
        currentRole: String?,
    ) {
        tokenManager.setTokens(accessToken)
        _state.value = AuthState.Authenticated(
            userId = userId,
            email = email,
            roles = roles,
            currentRole = currentRole,
        )
    }

    suspend fun emitGuest() {
        tokenManager.clearTokens()
        _state.value = AuthState.Guest
    }
}
