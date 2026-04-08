package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.token.TokenManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe token refresh with Mutex.
 * Called by Ktor's BearerAuthProvider on 401 responses and by AuthStateMachine.initialize().
 *
 * Circular dependency resolution: receives onRefreshFailed as a lambda
 * (same pattern as LogoutUseCaseImpl.platformSignOut), breaking the
 * AuthStateMachine ↔ RefreshTokenUseCaseImpl constructor cycle.
 */
class RefreshTokenUseCaseImpl(
    private val tokenManager: TokenManager,
    private val repository: AuthRepository,
    private val onRefreshFailed: suspend () -> Unit,
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
                onRefreshFailed()
                Result.failure(it)
            },
        )
    }
}
