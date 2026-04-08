package com.aggregateservice.core.auth.impl

import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.ObserveAuthStateUseCase
import com.aggregateservice.core.auth.state.AuthState
import kotlinx.coroutines.flow.StateFlow

class AuthStateProviderImpl(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
) : AuthStateProvider {
    override val authState: StateFlow<AuthState> = observeAuthStateUseCase()
}
