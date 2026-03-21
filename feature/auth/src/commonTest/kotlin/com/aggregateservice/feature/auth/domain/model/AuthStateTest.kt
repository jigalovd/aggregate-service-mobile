package com.aggregateservice.feature.auth.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStateTest {

    // =====================
    // Guest State Tests
    // =====================

    @Test
    fun `Guest state should not be authenticated`() {
        val state = AuthState.Guest

        assertFalse(state.isAuthenticated)
        assertFalse(state.canWrite)
        assertNull(state.userId)
    }

    @Test
    fun `Guest state should be the Initial state`() {
        assertEquals(AuthState.Guest, AuthState.Initial)
    }

    @Test
    fun `Guest state is a singleton`() {
        val guest1 = AuthState.Guest
        val guest2 = AuthState.Guest

        assertEquals(guest1, guest2)
    }

    // =============================
    // Authenticated State Tests
    // =============================

    @Test
    fun `Authenticated state should be authenticated`() {
        val state = AuthState.Authenticated(
            accessToken = "access_token_abc123",
            userId = "user_123",
            userEmail = "user@example.com"
        )

        assertTrue(state.isAuthenticated)
        assertTrue(state.canWrite)
        assertEquals("user_123", state.userId)
    }

    @Test
    fun `Authenticated state should have access token`() {
        val token = "access_token_abc123"
        val state = AuthState.Authenticated(
            accessToken = token,
            userId = "user_123",
            userEmail = "user@example.com"
        )

        assertEquals(token, state.accessToken)
    }

    @Test
    fun `Authenticated state should have user email`() {
        val email = "user@example.com"
        val state = AuthState.Authenticated(
            accessToken = "token123",
            userId = "user_123",
            userEmail = email
        )

        assertEquals(email, state.userEmail)
    }

    @Test
    fun `Authenticated state can have null email for session restoration`() {
        val state = AuthState.Authenticated(
            accessToken = "token123",
            userId = "user_123",
            userEmail = null
        )

        assertTrue(state.isAuthenticated)
        assertNull(state.userEmail)
    }

    @Test
    fun `Authenticated state should support copy`() {
        val original = AuthState.Authenticated(
            accessToken = "token123",
            userId = "user_123",
            userEmail = "user@example.com"
        )

        val copied = original.copy(userEmail = "newemail@example.com")

        assertEquals("token123", copied.accessToken)
        assertEquals("user_123", copied.userId)
        assertEquals("newemail@example.com", copied.userEmail)
    }

    // =============================
    // Pattern Matching Tests
    // =============================

    @Test
    fun `when expression should match Guest state`() {
        val state: AuthState = AuthState.Guest

        val result = when (state) {
            is AuthState.Guest -> "guest"
            is AuthState.Authenticated -> "authenticated"
        }

        assertEquals("guest", result)
    }

    @Test
    fun `when expression should match Authenticated state`() {
        val state: AuthState = AuthState.Authenticated(
            accessToken = "token123",
            userId = "user_123",
            userEmail = "user@example.com"
        )

        val result = when (state) {
            is AuthState.Guest -> "guest"
            is AuthState.Authenticated -> "authenticated"
        }

        assertEquals("authenticated", result)
    }

    @Test
    fun `when expression should extract Authenticated properties`() {
        val state: AuthState = AuthState.Authenticated(
            accessToken = "token123",
            userId = "user_123",
            userEmail = "user@example.com"
        )

        when (state) {
            is AuthState.Guest -> throw AssertionError("Expected Authenticated")
            is AuthState.Authenticated -> {
                assertEquals("token123", state.accessToken)
                assertEquals("user_123", state.userId)
                assertEquals("user@example.com", state.userEmail)
            }
        }
    }

    // =============================
    // canWrite Property Tests
    // =============================

    @Test
    fun `Guest should not have write access`() {
        assertFalse(AuthState.Guest.canWrite)
    }

    @Test
    fun `Authenticated should have write access`() {
        val state = AuthState.Authenticated(
            accessToken = "token",
            userId = "user",
            userEmail = null
        )
        assertTrue(state.canWrite)
    }

    // =============================
    // Equality Tests
    // =============================

    @Test
    fun `Guest instances should be equal`() {
        assertEquals(AuthState.Guest, AuthState.Guest)
    }

    @Test
    fun `Authenticated instances with same values should be equal`() {
        val state1 = AuthState.Authenticated(
            accessToken = "token",
            userId = "user",
            userEmail = "email@example.com"
        )
        val state2 = AuthState.Authenticated(
            accessToken = "token",
            userId = "user",
            userEmail = "email@example.com"
        )

        assertEquals(state1, state2)
    }

    @Test
    fun `Authenticated instances with different tokens should not be equal`() {
        val state1 = AuthState.Authenticated(
            accessToken = "token1",
            userId = "user",
            userEmail = null
        )
        val state2 = AuthState.Authenticated(
            accessToken = "token2",
            userId = "user",
            userEmail = null
        )

        kotlin.test.assertNotEquals(state1, state2)
    }

    // =============================
    // toString Tests
    // =============================

    @Test
    fun `Guest toString should contain Guest`() {
        val string = AuthState.Guest.toString()
        assertTrue(string.contains("Guest", ignoreCase = true))
    }

    @Test
    fun `Authenticated toString should contain Authenticated`() {
        val state = AuthState.Authenticated(
            accessToken = "token",
            userId = "user",
            userEmail = null
        )
        val string = state.toString()
        assertTrue(string.contains("Authenticated", ignoreCase = true))
    }
}
