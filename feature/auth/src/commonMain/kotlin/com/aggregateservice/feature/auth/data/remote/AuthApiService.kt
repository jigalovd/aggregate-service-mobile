package com.aggregateservice.feature.auth.data.remote

import com.aggregateservice.feature.auth.data.remote.dto.LoginRequest
import com.aggregateservice.feature.auth.data.remote.dto.LoginResponse
import com.aggregateservice.feature.auth.data.remote.dto.RegisterRequest
import com.aggregateservice.feature.auth.data.remote.dto.RefreshTokenRequest
import com.aggregateservice.feature.auth.data.remote.dto.UserResponse
import io.ktor.client.*
import io.ktor.client.request.*

class AuthApiService(
    private val client: HttpClient
) {
    suspend fun login(request: LoginRequest): LoginResponse =
        client.post("auth/login") {
            setBody(request)
        }

    suspend fun register(request: RegisterRequest): UserResponse =
        client.post("auth/register") {
            setBody(request)
        }

    suspend fun logout() {
        client.post("auth/logout")
    }

    suspend fun refreshToken(request: RefreshTokenRequest): LoginResponse =
        client.post("auth/refresh") {
            setBody(request)
        }

    suspend fun getCurrentUser(): UserResponse =
        client.get("auth/me")

    suspend fun sendVerificationEmail(email: String) {
        client.post("auth/verify/send") {
            setBody(mapOf("email" to email))
        }
    }

    suspend fun resetPassword(email: String) {
        client.post("auth/password/reset") {
            setBody(mapOf("email" to email))
        }
    }
}
