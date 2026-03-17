package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.utils.Result
import com.aggregateservice.feature.auth.domain.model.AuthTokens
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.core.storage.TokenStorage

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthTokens> {
        val result = authRepository.login(email, password)

        result.onSuccess { tokens ->
            tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
        }

        return result
    }
}
