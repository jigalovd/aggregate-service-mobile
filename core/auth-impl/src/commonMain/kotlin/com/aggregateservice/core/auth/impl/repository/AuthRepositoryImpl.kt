package com.aggregateservice.core.auth.impl.repository

import com.aggregateservice.core.auth.impl.repository.dto.AuthResponse
import com.aggregateservice.core.auth.impl.repository.dto.RefreshTokenResponse
import com.aggregateservice.core.auth.impl.repository.dto.UserResponse
import com.aggregateservice.core.auth.state.VerifyResult
import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.core.storage.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val authClient: HttpClient,
    private val tokenStore: TokenStore,
) : AuthRepository {
    override suspend fun verifyFirebaseToken(
        provider: String,
        token: String,
    ): Result<VerifyResult> =
        safeApiCall<AuthResponse> {
            authClient.post("/api/v1/auth/provider/verify") {
                contentType(ContentType.Application.Json)
                setBody(FirebaseVerifyRequest(firebaseToken = token, provider = provider))
            }
        }.map { response: AuthResponse ->
            response.refreshToken?.let { tokenStore.saveTokens(response.accessToken, it) }
            response.user.currentRole?.let { tokenStore.saveCurrentRole(it) }
            VerifyResult.Authenticated(
                accessToken = response.accessToken,
                userId = response.user.id,
                email = response.user.email,
                roles = response.user.roles.toSet(),
                currentRole = response.user.currentRole,
            )
        }

    override suspend fun refreshToken(): Result<RefreshTokenResponse> =
        safeApiCall<RefreshTokenResponse> {
            val refreshToken = tokenStore.getRefreshToken()
            authClient.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                if (refreshToken != null) {
                    setBody(mapOf("refresh_token" to refreshToken))
                }
            }
        }.onSuccess { response ->
            val newRefresh = response.refreshToken
            if (newRefresh != null) {
                tokenStore.saveTokens(response.accessToken, newRefresh)
            }
        }

    override suspend fun logout() {
        runCatching {
            safeApiCall<Unit> {
                httpClient.post("/api/v1/auth/logout")
            }
        }
        tokenStore.clearTokens()
    }

    override suspend fun getCurrentUser(): Result<UserResponse> =
        safeApiCall {
            httpClient.get("/api/v1/auth/me")
        }

    override suspend fun switchRole(role: String): Result<UserResponse> =
        safeApiCall<UserResponse> {
            httpClient.put("/api/v1/users/me/context") {
                contentType(ContentType.Application.Json)
                setBody(RoleSwitchRequest(role = role))
            }
        }.onSuccess { response ->
            tokenStore.saveCurrentRole(response.currentRole)
        }
}

@Serializable
data class FirebaseVerifyRequest(
    @SerialName("firebase_token")
    val firebaseToken: String,
    val provider: String,
)

@Serializable
data class RoleSwitchRequest(
    val role: String,
)
