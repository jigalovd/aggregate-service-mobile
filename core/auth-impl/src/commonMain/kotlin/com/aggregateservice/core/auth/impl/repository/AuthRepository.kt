package com.aggregateservice.core.auth.impl.repository

import com.aggregateservice.core.auth.impl.repository.dto.RefreshTokenResponse
import com.aggregateservice.core.auth.impl.repository.dto.UserResponse
import com.aggregateservice.core.auth.state.VerifyResult

interface AuthRepository {
    suspend fun verifyFirebaseToken(provider: String, token: String): Result<VerifyResult>

    suspend fun refreshToken(): Result<RefreshTokenResponse>

    suspend fun logout()

    suspend fun getCurrentUser(): Result<UserResponse>
}
