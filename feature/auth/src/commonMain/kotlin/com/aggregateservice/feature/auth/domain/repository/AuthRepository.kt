package com.aggregateservice.feature.auth.domain.repository

import com.aggregateservice.feature.auth.domain.model.AuthTokens
import com.aggregateservice.feature.auth.domain.model.User
import com.aggregateservice.core.utils.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthTokens>
    suspend fun register(email: String, password: String, firstName: String?, lastName: String?): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<AuthTokens>
    suspend fun getCurrentUser(): Result<User>
    suspend fun isAuthenticated(): Boolean
    suspend fun sendVerificationEmail(email: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
}
