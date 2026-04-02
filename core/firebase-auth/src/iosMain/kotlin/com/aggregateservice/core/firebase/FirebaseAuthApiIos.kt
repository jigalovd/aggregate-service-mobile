package com.aggregateservice.core.firebase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class FirebaseAuthApi actual constructor() {

    private var lastVerificationId: String? = null

    actual suspend fun signInWithGoogle(): Result<FirebaseToken> =
        withContext(Dispatchers.Main) {
            Result.failure(NotImplementedError("iOS not implemented in KMP-only mode"))
        }

    actual suspend fun signInWithApple(): Result<FirebaseToken> =
        withContext(Dispatchers.Main) {
            Result.failure(NotImplementedError("iOS not implemented in KMP-only mode"))
        }

    actual suspend fun signInWithPhoneStart(phoneNumber: String): Result<String> =
        withContext(Dispatchers.Main) {
            Result.failure(NotImplementedError("iOS not implemented in KMP-only mode"))
        }

    actual suspend fun confirmPhoneCode(verificationId: String, code: String): Result<FirebaseToken> =
        withContext(Dispatchers.Main) {
            Result.failure(NotImplementedError("iOS not implemented in KMP-only mode"))
        }

    actual suspend fun signOut() = withContext(Dispatchers.Main) {
        // Firebase SDK bridging not available in KMP-only mode
        // Graceful degradation - signOut is idempotent, no-op succeeds
    }

    actual fun getCurrentUser(): FirebaseUser? = null
}

actual object FirebaseAuthApiFactory {
    actual fun create(): FirebaseAuthApi = FirebaseAuthApi()
}