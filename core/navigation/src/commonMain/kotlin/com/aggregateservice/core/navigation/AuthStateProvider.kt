package com.aggregateservice.core.navigation

import kotlinx.coroutines.flow.StateFlow

/**
 * Абстракция для доступа к состоянию авторизации.
 *
 * **Purpose:** Позволяет features зависеть от core:navigation вместо feature:auth,
 * соблюдая Feature Isolation principle.
 *
 * **Usage:**
 * ```kotlin
 * val authProvider: AuthStateProvider = koin.get()
 * val isAuthenticated by authProvider.isAuthenticatedFlow.collectAsState()
 *
 * executeProtectedAction(
 *     isAuthenticated = authProvider.isAuthenticated,
 *     trigger = AuthPromptTrigger.Booking,
 *     onShowPrompt = { showAuthPrompt = true },
 * ) {
 *     // Protected action
 * }
 * ```
 *
 * **Implementation:** feature:auth предоставляет реализацию через Koin.
 *
 * @see AuthGuard
 * @see AuthPromptTrigger
 */
interface AuthStateProvider {
    /**
     * Flow с упрощенным состоянием авторизации для UI.
     *
     * Используйте collectAsState() в Composable для реактивного обновления.
     */
    val isAuthenticatedFlow: StateFlow<Boolean>

    /**
     * Текущее состояние авторизации (sync access).
     *
     * Для Composable предпочтительно использовать isAuthenticatedFlow.collectAsState().
     */
    val isAuthenticated: Boolean
        get() = isAuthenticatedFlow.value

    /**
     * Current user's ID if authenticated, null otherwise.
     */
    val currentUserId: String?
        get() = currentUserIdFlow.value

    /**
     * Flow of current user's ID.
     */
    val currentUserIdFlow: StateFlow<String?>
}
