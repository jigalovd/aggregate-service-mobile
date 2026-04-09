package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.token.TokenManager
import kotlinx.coroutines.sync.Mutex

/**
 * Thread-safe token refresh with non-reentrant guard.
 * Called by Ktor's BearerAuthProvider on 401 responses and by AuthStateMachine.initialize().
 *
 * Uses tryLock instead of withLock to prevent deadlock: the refresh HTTP request goes
 * through the same Ktor Bearer plugin, which can trigger refreshTokens() again on 401.
 * With withLock (non-reentrant Mutex), this would deadlock. tryLock returns immediately
 * if the mutex is already held, letting the caller handle the failure gracefully.
 *
 * Tokens are cleared before the refresh request so loadTokens() returns null,
 * preventing Bearer from attaching a stale token to the refresh request.
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

    override suspend fun invoke(): Result<String> {
        if (!refreshMutex.tryLock()) {
            return Result.failure(Exception("Token refresh already in progress"))
        }
        try {
            tokenManager.clearTokens()
            val result = repository.refreshToken()
            return result.fold(
                onSuccess = { response ->
                    tokenManager.setTokens(response.accessToken)
                    Result.success(response.accessToken)
                },
                onFailure = {
                    onRefreshFailed()
                    Result.failure(it)
                },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
