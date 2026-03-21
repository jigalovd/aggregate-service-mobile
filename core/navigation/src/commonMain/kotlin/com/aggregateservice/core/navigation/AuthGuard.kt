package com.aggregateservice.core.navigation

import androidx.compose.runtime.Composable

/**
 * Triggers for authentication prompts.
 * Used to identify which action requires authentication.
 */
enum class AuthPromptTrigger {
    /**
     * User attempted to book a service.
     */
    Booking,

    /**
     * User attempted to leave a review.
     */
    Review,

    /**
     * User attempted to add to favorites.
     */
    Favorites,
}

/**
 * Guard component that requires authentication for protected actions.
 *
 * **Usage:**
 * ```kotlin
 * AuthGuard(
 *     isAuthenticated = authState.isAuthenticated,
 *     trigger = AuthPromptTrigger.Booking,
 *     onShowPrompt = { trigger -> showAuthPrompt(trigger) },
 *     content = { BookingButton() }
 * )
 * ```
 *
 * **Architecture:**
 * - Presentation layer uses this to protect write operations
 * - Does not block navigation, only shows prompt
 * - Guest can still browse all screens
 * - No dependency on feature:auth to avoid circular dependencies
 *
 * @param isAuthenticated Whether user is authenticated (can perform write operations)
 * @param trigger What action triggered the auth prompt
 * @param onShowPrompt Callback to show auth prompt dialog
 * @param content Protected content to render if authenticated
 */
@Suppress("FunctionName")
@Composable
fun AuthGuard(
    isAuthenticated: Boolean,
    trigger: AuthPromptTrigger,
    onShowPrompt: (AuthPromptTrigger) -> Unit,
    content: @Composable () -> Unit,
) {
    if (isAuthenticated) {
        content()
    } else {
        // Trigger prompt on first composition
        onShowPrompt(trigger)
    }
}

/**
 * Extension function for executing protected actions.
 *
 * **Usage:**
 * ```kotlin
 * onProtectedAction(AuthPromptTrigger.Booking) {
 *     // This only runs if user is authenticated
 *     navigator.push(BookingScreen(providerId))
 * }
 * ```
 *
 * @param isAuthenticated Whether user is authenticated
 * @param trigger What action triggered the auth prompt
 * @param onShowPrompt Callback to show auth prompt dialog
 * @param action Protected action to execute if authenticated
 */
fun executeProtectedAction(
    isAuthenticated: Boolean,
    trigger: AuthPromptTrigger,
    onShowPrompt: (AuthPromptTrigger) -> Unit,
    action: () -> Unit,
) {
    if (isAuthenticated) {
        action()
    } else {
        onShowPrompt(trigger)
    }
}
