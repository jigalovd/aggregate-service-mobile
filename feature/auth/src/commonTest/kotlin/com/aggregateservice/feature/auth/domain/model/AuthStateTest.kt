package com.aggregateservice.feature.auth.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStateTest {

    @Test
    fun `Initial state should not be authenticated`() {
        val state = AuthState.Initial

        assertFalse(state.isAuthenticated)
        assertNull(state.accessToken)
        assertNull(state.userEmail)
    }

    @Test
    fun `authenticated factory should create authenticated state with all fields`() {
        val token = "access_token_abc123"
        val email = "user@example.com"

        val state = AuthState.authenticated(token, email)

        assertTrue(state.isAuthenticated)
        assertEquals(token, state.accessToken)
        assertEquals(email, state.userEmail)
    }

    @Test
    fun `authenticated factory should accept null email`() {
        val token = "access_token_abc123"

        val state = AuthState.authenticated(token, null)

        assertTrue(state.isAuthenticated)
        assertEquals(token, state.accessToken)
        assertNull(state.userEmail)
    }

    @Test
    fun `Initial should be singleton`() {
        val state1 = AuthState.Initial
        val state2 = AuthState.Initial

        assertEquals(state1, state2)
    }

    @Test
    fun `should be a data class with proper equals`() {
        val token = "token123"
        val email = "user@example.com"

        val state1 = AuthState.authenticated(token, email)
        val state2 = AuthState.authenticated(token, email)

        assertEquals(state1, state2)
    }

    @Test
    fun `should have proper hashCode`() {
        val token = "token123"
        val email = "user@example.com"

        val state1 = AuthState.authenticated(token, email)
        val state2 = AuthState.authenticated(token, email)

        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `different tokens should produce different states`() {
        val state1 = AuthState.authenticated("token1", "user@example.com")
        val state2 = AuthState.authenticated("token2", "user@example.com")

        assertTrue(state1 != state2)
    }

    @Test
    fun `different emails should produce different states`() {
        val state1 = AuthState.authenticated("token", "user1@example.com")
        val state2 = AuthState.authenticated("token", "user2@example.com")

        assertTrue(state1 != state2)
    }

    @Test
    fun `authenticated state with null email should differ from state with email`() {
        val token = "token123"

        val state1 = AuthState.authenticated(token, null)
        val state2 = AuthState.authenticated(token, "user@example.com")

        assertTrue(state1 != state2)
    }

    @Test
    fun `should have proper toString`() {
        val state = AuthState.authenticated("token123", "user@example.com")

        val stringRepresentation = state.toString()

        assertTrue(stringRepresentation.contains("AuthState"))
        assertTrue(stringRepresentation.contains("isAuthenticated=true"))
    }

    @Test
    fun `Initial toString should show unauthenticated state`() {
        val state = AuthState.Initial

        val stringRepresentation = state.toString()

        assertTrue(stringRepresentation.contains("isAuthenticated=false"))
    }

    @Test
    fun `should support copy with modified fields`() {
        val original = AuthState.authenticated("token123", "user@example.com")

        val copied = original.copy(userEmail = "newemail@example.com")

        assertTrue(copied.isAuthenticated)
        assertEquals("token123", copied.accessToken)
        assertEquals("newemail@example.com", copied.userEmail)
    }

    @Test
    fun `should support copy to unauthenticated state`() {
        val authenticated = AuthState.authenticated("token123", "user@example.com")

        val unauthenticated = authenticated.copy(
            isAuthenticated = false,
            accessToken = null,
            userEmail = null
        )

        assertFalse(unauthenticated.isAuthenticated)
        assertNull(unauthenticated.accessToken)
        assertNull(unauthenticated.userEmail)
    }

    @Test
    fun `default constructor should create unauthenticated state`() {
        val state = AuthState()

        assertFalse(state.isAuthenticated)
        assertNull(state.accessToken)
        assertNull(state.userEmail)
    }
}
