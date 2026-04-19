package com.aggregateservice.feature.auth.presentation.model

import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.core.network.AppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [LoginUiState] data class.
 */
class LoginUiStateTest {

    @Test
    fun `default state has correct initial values`() {
        val state = LoginUiState()

        assertFalse(state.isLoading)
        assertEquals(AuthState.Loading, state.authState)
        assertFalse(state.isLoginSuccess)
        assertNull(state.error)
    }

    @Test
    fun `loading state is correctly represented`() {
        val state = LoginUiState(isLoading = true)

        assertTrue(state.isLoading)
        assertEquals(AuthState.Loading, state.authState)
        assertFalse(state.isLoginSuccess)
        assertNull(state.error)
    }

    @Test
    fun `authenticated state is correctly reflected`() {
        val authState = AuthState.Authenticated(
            userId = "user-123",
            email = "test@example.com",
            roles = setOf("admin", "user"),
            currentRole = "user",
        )
        val state = LoginUiState(
            isLoading = false,
            authState = authState,
            isLoginSuccess = true,
        )

        assertFalse(state.isLoading)
        assertTrue(state.authState.isAuthenticated)
        val authenticated = state.authState as AuthState.Authenticated
        assertEquals("user-123", authenticated.userId)
        assertEquals("test@example.com", authenticated.email)
        assertEquals(setOf("admin", "user"), authenticated.roles)
        assertEquals("user", authenticated.currentRole)
        assertTrue(state.isLoginSuccess)
        assertNull(state.error)
    }

    @Test
    fun `guest state has isAuthenticated false`() {
        val state = LoginUiState(authState = AuthState.Guest)

        assertFalse(state.authState.isAuthenticated)
        assertTrue(state.authState is AuthState.Guest)
    }

    @Test
    fun `error state contains error details`() {
        val appError = AppError.Unauthorized
        val authError = com.aggregateservice.core.auth.state.AuthError.TokenExpired
        val state = LoginUiState(
            isLoading = false,
            authState = AuthState.Error(error = authError),
            error = appError,
        )

        assertFalse(state.isLoading)
        assertTrue(state.authState is AuthState.Error)
        val errorState = state.authState as AuthState.Error
        assertTrue(errorState.error is com.aggregateservice.core.auth.state.AuthError.TokenExpired)
        assertEquals(appError, state.error)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = LoginUiState(
            isLoading = true,
            authState = AuthState.Guest,
            isLoginSuccess = false,
            error = null,
        )
        val copied = original.copy(isLoading = false)

        assertFalse(copied.isLoading)
        assertEquals(AuthState.Guest, copied.authState)
        assertFalse(copied.isLoginSuccess)
        assertNull(copied.error)
    }

    @Test
    fun `copy updates only specified fields`() {
        val original = LoginUiState()
        val appError = AppError.UnknownError(message = "Test error")

        val copied = original.copy(
            isLoading = true,
            error = appError,
        )

        assertTrue(copied.isLoading)
        assertEquals(appError, copied.error)
        assertEquals(AuthState.Loading, copied.authState)
        assertFalse(copied.isLoginSuccess)
    }

    @Test
    fun `LoginUiState equality works correctly`() {
        val state1 = LoginUiState(isLoading = false)
        val state2 = LoginUiState(isLoading = false)
        val state3 = LoginUiState(isLoading = true)

        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
        assertTrue(state1 != state3)
    }

    @Test
    fun `LoginUiState with various AppError types`() {
        val networkError = AppError.NetworkError(code = 500, message = "Server error")
        val forbiddenError = AppError.Forbidden(message = "Access denied")
        val unknownError = AppError.UnknownError(message = "Unknown")

        assertEquals(networkError, LoginUiState(error = networkError).error)
        assertEquals(forbiddenError, LoginUiState(error = forbiddenError).error)
        assertEquals(unknownError, LoginUiState(error = unknownError).error)
    }

    @Test
    fun `Authenticated authState isAuthenticated returns true`() {
        val authState = AuthState.Authenticated(
            userId = "user-456",
            email = null,
            roles = emptySet(),
            currentRole = null,
        )

        assertTrue(authState.isAuthenticated)
    }

    @Test
    fun `Guest authState isAuthenticated returns false`() {
        assertFalse(AuthState.Guest.isAuthenticated)
    }

    @Test
    fun `Loading authState isAuthenticated returns false`() {
        assertFalse(AuthState.Loading.isAuthenticated)
    }
}
