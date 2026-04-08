package com.aggregateservice.core.firebase

actual class AuthProviderApi actual constructor() {
    actual suspend fun signInWithGoogle(context: PlatformAuthContext): Result<AuthProviderResult> =
        Result.failure(NotImplementedError("iOS Google Sign-In not implemented"))

    actual suspend fun signInWithApple(): Result<AuthProviderResult> =
        Result.failure(NotImplementedError("iOS Apple Sign-In not implemented"))

    actual suspend fun signInWithPhoneStart(phone: String): Result<String> =
        Result.failure(NotImplementedError("Phone auth not configured"))

    actual suspend fun confirmPhoneCode(verificationId: String, code: String): Result<AuthProviderResult> =
        Result.failure(NotImplementedError("Phone auth not configured"))

    actual suspend fun signOut() {
        // Firebase SDK bridging not available in KMP-only mode
    }
}
