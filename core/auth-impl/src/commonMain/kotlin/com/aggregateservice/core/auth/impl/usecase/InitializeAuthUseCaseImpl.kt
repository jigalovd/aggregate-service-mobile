package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.InitializeAuthUseCase
import com.aggregateservice.core.auth.impl.state.AuthStateMachine

class InitializeAuthUseCaseImpl(
    private val authStateMachine: AuthStateMachine,
) : InitializeAuthUseCase {
    override suspend fun invoke() {
        authStateMachine.initialize()
    }
}
