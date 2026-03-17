package com.aggregateservice.feature.auth.data.repository

import com.aggregateservice.core.utils.Result
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.feature.auth.data.remote.AuthApiService
import com.aggregateservice.feature.auth.data.remote.dto.LoginRequest
import com.aggregateservice.feature.auth.data.remote.dto.RefreshTokenRequest
import com.aggregateservice.feature.auth.data.remote.dto.RegisterRequest
import com.aggregateservice.feature.auth.domain.model.AuthTokens
import com.aggregateservice.feature.auth.domain.model.User
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthTokens> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                Result.Success(response.toAuthTokens())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Login failed")
            }
        }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String?,
        lastName: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(
                RegisterRequest(email, password, firstName, lastName)
            )
            Result.Success(response.toUser())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Logout failed")
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                Result.Success(response.toAuthTokens())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Token refresh failed")
            }
        }

    override suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            Result.Success(response.toUser())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get user")
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenStorage.getAccessToken() != null
    }

    override suspend fun sendVerificationEmail(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                apiService.sendVerificationEmail(email)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to send verification email")
            }
        }

    override suspend fun resetPassword(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                apiService.resetPassword(email)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to reset password")
            }
        }
}
