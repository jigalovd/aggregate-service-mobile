package com.aggregateservice.core.auth.impl.repository

import com.aggregateservice.core.auth.impl.repository.dto.AuthResponse
import com.aggregateservice.core.auth.impl.repository.dto.RefreshTokenResponse
import com.aggregateservice.core.auth.impl.repository.dto.UserResponse
import com.aggregateservice.core.auth.state.VerifyResult
import com.aggregateservice.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val authClient: HttpClient,
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
            VerifyResult.Authenticated(
                accessToken = response.accessToken,
                userId = response.user.id,
                email = response.user.email,
                roles = response.user.roles.toSet(),
                currentRole = response.user.currentRole,
            )
        }

    override suspend fun refreshToken(): Result<RefreshTokenResponse> =
        safeApiCall {
            authClient.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
            }
        }

    override suspend fun logout() {
        runCatching {
            safeApiCall<Unit> {
                httpClient.post("/api/v1/auth/logout")
            }
        }
    }

    override suspend fun getCurrentUser(): Result<UserResponse> =
        safeApiCall {
            httpClient.get("/api/v1/auth/me")
        }
}

@Serializable
data class FirebaseVerifyRequest(
    @SerialName("firebase_token")
    val firebaseToken: String,
    val provider: String,
)
