package com.aggregateservice.core.firebase

expect class AuthProviderApi() {
    suspend fun signInWithGoogle(context: PlatformAuthContext): Result<AuthProviderResult>

    suspend fun signInWithApple(): Result<AuthProviderResult>

    suspend fun signInWithPhoneStart(phone: String): Result<String>

    suspend fun confirmPhoneCode(verificationId: String, code: String): Result<AuthProviderResult>

    suspend fun signOut()
}
