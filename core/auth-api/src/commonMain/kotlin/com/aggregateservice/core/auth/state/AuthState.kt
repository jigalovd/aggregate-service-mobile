package com.aggregateservice.core.auth.state

sealed interface AuthState {
    val isAuthenticated: Boolean

    data object Loading : AuthState {
        override val isAuthenticated = false
    }

    data object Guest : AuthState {
        override val isAuthenticated = false
    }

    data class Authenticated(
        val userId: String,
        val email: String?,
        val roles: Set<String>,
        val currentRole: String?,
    ) : AuthState {
        override val isAuthenticated = true
    }

    data class Error(
        val error: AuthError,
    ) : AuthState {
        override val isAuthenticated = false
    }
}
