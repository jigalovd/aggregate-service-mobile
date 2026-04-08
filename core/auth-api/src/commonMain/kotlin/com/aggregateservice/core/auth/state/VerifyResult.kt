package com.aggregateservice.core.auth.state

sealed interface VerifyResult {
    data class Authenticated(
        val accessToken: String,
        val userId: String,
        val email: String?,
        val roles: Set<String>,
        val currentRole: String?,
    ) : VerifyResult
}
