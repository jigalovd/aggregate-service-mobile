package com.aggregateservice.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser as AndroidFirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

actual class FirebaseAuthApi actual constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    actual suspend fun signInWithGoogle(): Result<FirebaseToken> = withContext(Dispatchers.Main) {
        // Note: Google Sign-In requires Activity context with GoogleSignInClient
        // This is a stub - actual implementation requires UI integration
        @Suppress("UNCHECKED_CAST")
        Result.failure<FirebaseToken>(NotImplementedError("Google Sign-In requires Activity with GoogleSignInClient. Use LinkAccountDialog integration."))
    }

    actual suspend fun signInWithApple(): Result<FirebaseToken> = withContext(Dispatchers.Main) {
        @Suppress("UNCHECKED_CAST")
        Result.failure<FirebaseToken>(NotImplementedError("Apple Sign-In requires native iOS implementation"))
    }

    actual suspend fun signInWithPhoneStart(phoneNumber: String): Result<String> =
        withContext(Dispatchers.Main) {
            // Phone auth requires Activity context - this is a limitation of KMP
            // For proper implementation, use ActivityResultLauncher with Firebase UI
            @Suppress("UNCHECKED_CAST")
            Result.failure<String>(NotImplementedError("Phone auth requires Activity context. Use FirebaseUI PhoneAuthActivity."))
        }

    actual suspend fun confirmPhoneCode(verificationId: String, code: String): Result<FirebaseToken> =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        authResult.user?.getIdToken(true)
                            ?.addOnSuccessListener { idTokenResult ->
                                val token = idTokenResult.token
                                if (continuation.isActive && token != null) {
                                    continuation.resume(
                                        Result.success(
                                            FirebaseToken(
                                                idToken = token,
                                                authProvider = "phone"
                                            )
                                        )
                                    )
                                } else if (continuation.isActive) {
                                    continuation.resume(Result.failure(Exception("Failed to get ID token")))
                                }
                            }
                            ?.addOnFailureListener { e ->
                                if (continuation.isActive) {
                                    continuation.resume(Result.failure(e))
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(e))
                        }
                    }
            }
        }

    actual suspend fun signOut() = withContext(Dispatchers.Main) {
        firebaseAuth.signOut()
    }

    actual fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser?.let { AndroidFirebaseUserImpl(it) }
    }

    private class AndroidFirebaseUserImpl(private val user: AndroidFirebaseUser) : FirebaseUser {
        override val uid: String = user.uid
        override val email: String? = user.email
        override val displayName: String? = user.displayName
    }
}

actual object FirebaseAuthApiFactory {
    actual fun create(): FirebaseAuthApi = FirebaseAuthApi()
}