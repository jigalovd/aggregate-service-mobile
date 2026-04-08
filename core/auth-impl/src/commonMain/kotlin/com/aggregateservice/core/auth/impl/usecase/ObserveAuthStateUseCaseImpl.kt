package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.ObserveAuthStateUseCase
import com.aggregateservice.core.auth.impl.state.AuthStateMachine
import com.aggregateservice.core.auth.state.AuthState
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCaseImpl(
    private val authStateMachine: AuthStateMachine,
) : ObserveAuthStateUseCase {
    override operator fun invoke(): StateFlow<AuthState> = authStateMachine.state
}
