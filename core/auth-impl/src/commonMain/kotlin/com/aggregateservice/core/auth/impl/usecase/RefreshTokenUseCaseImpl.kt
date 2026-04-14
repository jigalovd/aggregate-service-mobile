package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.storage.TokenStore
import kotlinx.coroutines.sync.Mutex

/**
 * Thread-safe token refresh with non-reentrant guard.
 *
 * Reads refresh_token from TokenStore WITHOUT clearing it.
 * AuthClient has no Bearer plugin — stale access_token is never auto-attached.
 * On success, saves new tokens to TokenStore.
 * On failure, triggers Guest state via onRefreshFailed.
 *
 * Circular dependency resolution: receives onRefreshFailed as a lambda
 * (same pattern as LogoutUseCaseImpl.platformSignOut), breaking the
 * AuthStateMachine ↔ RefreshTokenUseCaseImpl constructor cycle.
 */
class RefreshTokenUseCaseImpl(
    private val tokenStore: TokenStore,
    private val repository: AuthRepository,
    private val onRefreshFailed: suspend () -> Unit,
) : RefreshTokenUseCase {
    private val refreshMutex = Mutex()

    override suspend fun invoke(): Result<String> {
        if (!refreshMutex.tryLock()) {
            return Result.failure(Exception("Token refresh already in progress"))
        }
        try {
            val result = repository.refreshToken()
            return result.fold(
                onSuccess = { response ->
                    val refreshToken = response.refreshToken
                    if (refreshToken != null) {
                        tokenStore.saveTokens(response.accessToken, refreshToken)
                    }
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
