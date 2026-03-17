package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.utils.Result
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.core.storage.TokenStorage

class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(): Result<Unit> {
        val result = authRepository.logout()

        result.onSuccess {
            tokenStorage.clearTokens()
        }

        return result
    }
}
