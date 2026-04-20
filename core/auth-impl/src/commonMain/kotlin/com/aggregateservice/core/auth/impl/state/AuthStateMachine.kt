package com.aggregateservice.core.auth.impl.state

import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.repository.dto.UserResponse
import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.core.storage.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

class AuthStateMachine(
    private val tokenStore: TokenStore,
    private val repository: AuthRepository,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) {
    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    suspend fun initialize() {
        val token = tokenStore.getAccessToken()
        if (token == null) {
            _state.value = AuthState.Guest
            return
        }

        // Load saved role preference from TokenStore first
        val savedRole = tokenStore.getCurrentRole()

        val userResult = repository.getCurrentUser()
        userResult.fold(
            onSuccess = { user ->
                // Use saved role if available, otherwise use server-provided role
                val effectiveRole = savedRole ?: user.currentRole
                populateUser(user, effectiveRole)
            },
            onFailure = { tryRefreshOrFail() },
        )
    }

    suspend fun signIn(
        userId: String,
        email: String?,
        roles: Set<String>,
        currentRole: String?,
    ) {
        _state.value =
            AuthState.Authenticated(
                userId = userId,
                email = email,
                roles = roles,
                currentRole = currentRole,
            )
    }

    /**
     * Switch current role and persist preference to TokenStore.
     */
    suspend fun switchRole(newRole: String) {
        val currentState = _state.value
        if (currentState !is AuthState.Authenticated) return

        // Update state immediately for responsive UI
        _state.value = currentState.copy(currentRole = newRole)

        // Persist to TokenStore
        tokenStore.saveCurrentRole(newRole)

        // Sync with backend (fire-and-forget for now)
        repository.switchRole(newRole)
    }

    suspend fun emitGuest() {
        _state.value = AuthState.Guest
    }

    private fun populateUser(user: UserResponse, currentRole: String? = null) {
        _state.value =
            AuthState.Authenticated(
                userId = user.id,
                email = user.email,
                roles = user.roles.toSet(),
                currentRole = currentRole ?: user.currentRole,
            )
    }

    private suspend fun tryRefreshOrFail() {
        val refreshed =
            withTimeoutOrNull(REFRESH_TIMEOUT_MS) {
                val refreshResult = refreshTokenUseCase()
                if (refreshResult.isSuccess) {
                    val retryResult = repository.getCurrentUser()
                    retryResult.fold(
                        onSuccess = { user -> populateUser(user) },
                        onFailure = { emitGuest() },
                    )
                } else {
                    emitGuest()
                }
            }
        if (refreshed == null) {
            emitGuest()
        }
    }

    companion object {
        private const val REFRESH_TIMEOUT_MS = 5000L
    }
}
