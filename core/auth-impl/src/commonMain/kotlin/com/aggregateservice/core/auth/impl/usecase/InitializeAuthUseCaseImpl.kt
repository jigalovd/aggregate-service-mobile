package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.InitializeAuthUseCase
import com.aggregateservice.core.auth.impl.state.AuthStateMachine
import com.aggregateservice.core.auth.impl.token.TokenManager

class InitializeAuthUseCaseImpl(
    private val tokenManager: TokenManager,
    private val authStateMachine: AuthStateMachine,
) : InitializeAuthUseCase {
    override suspend fun invoke() {
        tokenManager.initFromStorage()
        authStateMachine.initialize()
    }
}
