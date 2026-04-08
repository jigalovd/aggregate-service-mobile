package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.state.AuthStateMachine
import com.aggregateservice.core.auth.impl.token.TokenManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RefreshTokenUseCaseImpl(
    private val tokenManager: TokenManager,
    private val repository: AuthRepository,
    private val authStateMachine: AuthStateMachine,
) : RefreshTokenUseCase {
    private val refreshMutex = Mutex()

    override suspend fun invoke(): Result<String> = refreshMutex.withLock {
        val result = repository.refreshToken()
        result.fold(
            onSuccess = { response ->
                tokenManager.setTokens(response.accessToken)
                Result.success(response.accessToken)
            },
            onFailure = {
                authStateMachine.emitGuest()
                Result.failure(it)
            },
        )
    }
}
