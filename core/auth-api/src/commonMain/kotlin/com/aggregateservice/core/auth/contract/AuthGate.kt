package com.aggregateservice.core.auth.contract

/**
 * Deferred authentication gate.
 *
 * Wraps actions that require authentication. If the user is authenticated,
 * the action executes immediately. If the user is a guest, triggers the
 * sign-in flow (via [SignInUseCase]) and then executes the action.
 *
 * Usage:
 * ```kotlin
 * Button(onClick = {
 *     scope.launch {
 *         authGate.run(trigger = AuthPromptTrigger.BOOKING) {
 *             viewModel.createBooking(data)
 *         }
 *     }
 * })
 * ```
 */
interface AuthGate {
    /**
     * Run [action] if authenticated, or prompt sign-in first if guest.
     *
     * @param trigger What triggered the auth prompt (for analytics/UI).
     * @param action The action to execute once authenticated.
     * @return Result with the action's return value, or failure if sign-in was cancelled/failed.
     */
    suspend fun <T> run(trigger: AuthPromptTrigger, action: suspend () -> T): Result<T>
}
