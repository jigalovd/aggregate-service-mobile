package com.aggregateservice.core.firebase

import com.aggregateservice.core.auth.contract.AuthProvider

data class AuthProviderResult(
    val idToken: String,
    val provider: AuthProvider,
)
