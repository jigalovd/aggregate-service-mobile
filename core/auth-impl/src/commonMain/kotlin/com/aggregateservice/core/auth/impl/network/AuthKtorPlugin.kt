package com.aggregateservice.core.auth.impl.network

import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.impl.token.TokenManager
import io.ktor.client.plugins.auth.providers.BearerTokens

data class AuthLambdas(
    val loadTokens: suspend () -> BearerTokens?,
    val refreshTokens: suspend () -> BearerTokens?,
)

fun createAuthLambdas(
    tokenManager: TokenManager,
    refreshTokenUseCase: RefreshTokenUseCase,
): AuthLambdas {
    return AuthLambdas(
        loadTokens = {
            tokenManager.getAccessToken()?.let { BearerTokens(accessToken = it, refreshToken = "") }
        },
        refreshTokens = {
            val result = refreshTokenUseCase()
            result.getOrNull()?.let { BearerTokens(accessToken = it, refreshToken = "") }
        },
    )
}
