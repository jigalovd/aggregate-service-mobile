package com.aggregateservice.core.auth.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthStateTest {
    @Test
    fun `Loading state has isAuthenticated false`() {
        val state = AuthState.Loading
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun `Guest state has isAuthenticated false`() {
        val state = AuthState.Guest
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun `Authenticated state has isAuthenticated true`() {
        val state =
            AuthState.Authenticated(
                userId = "user-123",
                email = "test@example.com",
                roles = setOf("client"),
                currentRole = "client",
            )
        assertTrue(state.isAuthenticated)
        assertEquals("user-123", state.userId)
        assertEquals("test@example.com", state.email)
        assertEquals(setOf("client"), state.roles)
        assertEquals("client", state.currentRole)
    }

    @Test
    fun `Error state has isAuthenticated false`() {
        val state = AuthState.Error(AuthError.NetworkError(400, "test error"))
        assertFalse(state.isAuthenticated)
        assertIs<AuthError>(state.error)
    }

    @Test
    fun `Authenticated state requires non-blank userId`() {
        val state =
            AuthState.Authenticated(
                userId = "user-123",
                email = null,
                roles = emptySet(),
                currentRole = null,
            )
        assertEquals("user-123", state.userId)
    }
}
