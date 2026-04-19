package com.aggregateservice.core.navigation

import com.aggregateservice.core.auth.contract.AuthPromptTrigger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [executeProtectedAction] function.
 * Note: [AuthGuard] Composable is tested indirectly through this function.
 *
 * The AuthGuard Composable logic is:
 * - If isAuthenticated: render content
 * - If not authenticated: call onShowPrompt(trigger)
 *
 * This test covers the executeProtectedAction extension function which shares the same logic.
 */
class AuthGuardTest {

    // --- Happy path: authenticated ---

    @Test
    fun `executeProtectedAction executes action when authenticated`() {
        var actionExecuted = false
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { actionExecuted = true },
        )

        assertTrue(actionExecuted)
        assertEquals(null, promptTriggered)
    }

    @Test
    fun `executeProtectedAction does not trigger prompt when authenticated`() {
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { /* no-op */ },
        )

        assertEquals(null, promptTriggered)
    }

    @Test
    fun `executeProtectedAction does not execute action when not authenticated`() {
        var actionExecuted = false
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.REVIEW,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { actionExecuted = true },
        )

        assertFalse(actionExecuted)
        assertEquals(AuthPromptTrigger.REVIEW, promptTriggered)
    }

    @Test
    fun `executeProtectedAction triggers prompt with correct trigger when not authenticated`() {
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.FAVORITES,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { /* no-op */ },
        )

        assertEquals(AuthPromptTrigger.FAVORITES, promptTriggered)
    }

    // --- All AuthPromptTrigger values ---

    @Test
    fun `executeProtectedAction works with BOOKING trigger`() {
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { /* no-op */ },
        )

        assertEquals(AuthPromptTrigger.BOOKING, promptTriggered)
    }

    @Test
    fun `executeProtectedAction works with REVIEW trigger`() {
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.REVIEW,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { /* no-op */ },
        )

        assertEquals(AuthPromptTrigger.REVIEW, promptTriggered)
    }

    @Test
    fun `executeProtectedAction works with FAVORITES trigger`() {
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.FAVORITES,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { /* no-op */ },
        )

        assertEquals(AuthPromptTrigger.FAVORITES, promptTriggered)
    }

    // --- Callback invocation ---

    @Test
    fun `executeProtectedAction calls onShowPrompt only once when not authenticated`() {
        var promptCallCount = 0

        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { promptCallCount++ },
            action = { /* no-op */ },
        )

        assertEquals(1, promptCallCount)
    }

    @Test
    fun `executeProtectedAction does not call onShowPrompt when authenticated`() {
        var promptCallCount = 0

        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { promptCallCount++ },
            action = { /* no-op */ },
        )

        assertEquals(0, promptCallCount)
    }

    // --- Action side effects ---

    @Test
    fun `executeProtectedAction executes action and returns when authenticated`() {
        var actionExecuted = false
        var promptTriggered: AuthPromptTrigger? = null

        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { trigger -> promptTriggered = trigger },
            action = { actionExecuted = true },
        )

        assertTrue(actionExecuted)
        assertEquals(null, promptTriggered)
    }

    @Test
    fun `executeProtectedAction returns Unit when action returns Unit`() {
        var executed = false
        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { /* no-op */ },
            action = { executed = true },
        )

        assertTrue(executed)
    }

    // --- Edge cases ---

    @Test
    fun `executeProtectedAction with empty action when authenticated`() {
        var actionExecuted = false

        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { /* no-op */ },
            action = { actionExecuted = true },
        )

        assertTrue(actionExecuted)
    }

    @Test
    fun `executeProtectedAction with null-returning action`() {
        var result: String? = "not null"
        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { /* no-op */ },
            action = { result = null },
        )

        assertEquals(null, result)
    }

    @Test
    fun `executeProtectedAction action can throw when authenticated`() {
        var exceptionThrown = false
        var thrownMessage: String? = null

        try {
            executeProtectedAction(
                isAuthenticated = true,
                trigger = AuthPromptTrigger.BOOKING,
                onShowPrompt = { /* no-op */ },
                action = { throw IllegalStateException("test exception") },
            )
        } catch (e: IllegalStateException) {
            exceptionThrown = true
            thrownMessage = e.message
        }

        assertTrue(exceptionThrown)
        assertEquals("test exception", thrownMessage)
    }

    @Test
    fun `executeProtectedAction action does not throw when not authenticated`() {
        // When not authenticated, action should not be called, so no exception should be thrown
        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { /* no-op */ },
            action = { throw IllegalStateException("should not reach here") },
        )
        // If we get here without exception, the test passes
    }

    // --- Sequential calls ---

    @Test
    fun `executeProtectedAction can be called multiple times`() {
        var callCount = 0
        var promptCount = 0

        // First call: not authenticated
        executeProtectedAction(
            isAuthenticated = false,
            trigger = AuthPromptTrigger.BOOKING,
            onShowPrompt = { promptCount++ },
            action = { callCount++ },
        )

        // Second call: now authenticated (simulating login)
        executeProtectedAction(
            isAuthenticated = true,
            trigger = AuthPromptTrigger.REVIEW,
            onShowPrompt = { promptCount++ },
            action = { callCount++ },
        )

        assertEquals(1, callCount) // Only executed when authenticated
        assertEquals(1, promptCount) // Only triggered when not authenticated
    }
}