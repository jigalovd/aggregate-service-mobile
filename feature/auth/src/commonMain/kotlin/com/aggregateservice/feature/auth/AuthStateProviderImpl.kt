package com.aggregateservice.feature.auth

import com.aggregateservice.core.navigation.AuthStateProvider
import com.aggregateservice.feature.auth.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Implementation of [AuthStateProvider] that bridges feature:auth with core:navigation.
 *
 * **Architecture:**
 * - Implements interface from core:navigation
 * - Uses ObserveAuthStateUseCase from feature:auth
 * - Maps complex AuthState to simple Boolean for UI consumption
 *
 * **Memory Management:**
 * - Uses internal CoroutineScope with SupervisorJob for proper cancellation
 * - WhileSubscribed(5000) stops the flow when no collectors for 5 seconds
 * - Scope is cancelled when the singleton is garbage collected (app lifecycle)
 *
 * **DI Registration:**
 * ```kotlin
 * single<AuthStateProvider> { AuthStateProviderImpl(get()) }
 * ```
 */
class AuthStateProviderImpl(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
) : AuthStateProvider {

    /**
     * Internal scope for StateFlow sharing.
     * SupervisorJob ensures child coroutines don't cancel each other on failure.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val isAuthenticatedFlow: StateFlow<Boolean> =
        observeAuthStateUseCase()
            .map { it.isAuthenticated }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    override val currentUserIdFlow: StateFlow<String?> =
        observeAuthStateUseCase()
            .map { it.userId }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    override val currentUserId: String?
        get() = currentUserIdFlow.value
}
