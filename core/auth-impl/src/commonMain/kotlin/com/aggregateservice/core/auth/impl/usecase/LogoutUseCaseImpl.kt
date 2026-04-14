package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.LogoutUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.state.AuthStateMachine
import com.aggregateservice.core.storage.TokenStore

class LogoutUseCaseImpl(
    private val authStateMachine: AuthStateMachine,
    private val platformSignOut: suspend () -> Unit,
    private val repository: AuthRepository,
    private val tokenStore: TokenStore,
) : LogoutUseCase {
    override suspend fun invoke() {
        runCatching { platformSignOut() }
        runCatching { repository.logout() }
        tokenStore.clearTokens()
        authStateMachine.emitGuest()
    }
}
