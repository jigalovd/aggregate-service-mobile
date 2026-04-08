package com.aggregateservice.feature.auth

import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.state.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Bridge adapter that implements the OLD [com.aggregateservice.core.navigation.AuthStateProvider]
 * by delegating to the NEW [AuthStateProvider] from core:auth-api.
 *
 * This adapter exists to maintain backward compatibility with features that still depend on
 * the old core:navigation AuthStateProvider interface. It will be removed in Task 19 when
 * all features are migrated to the new core:auth-api contracts.
 */
class LegacyAuthStateBridge(
    newAuthStateProvider: AuthStateProvider,
) : com.aggregateservice.core.navigation.AuthStateProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val isAuthenticatedFlow: StateFlow<Boolean> =
        newAuthStateProvider.authState
            .map { it.isAuthenticated }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    override val currentUserIdFlow: StateFlow<String?> =
        newAuthStateProvider.authState
            .map { state -> (state as? AuthState.Authenticated)?.userId }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )
}
