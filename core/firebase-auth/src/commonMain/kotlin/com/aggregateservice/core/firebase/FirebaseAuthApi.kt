package com.aggregateservice.core.firebase

/**
 * Firebase Authentication API (KMP expect).
 *
 * Provides unified interface for Firebase Auth operations across platforms.
 * Android implementation uses Firebase Auth SDK.
 * iOS is stub (KMP-only implementation).
 */
expect class FirebaseAuthApi() {

    /**
     * Sign in with Google and return Firebase ID token.
     *
     * @return Result with FirebaseToken containing idToken and auth_provider
     * @throws Exception on Firebase SDK errors
     */
    suspend fun signInWithGoogle(): Result<FirebaseToken>

    /**
     * Sign in with Apple and return Firebase ID token.
     *
     * @return Result with FirebaseToken containing idToken and auth_provider
     * @throws Exception on Firebase SDK errors
     */
    suspend fun signInWithApple(): Result<FirebaseToken>

    /**
     * Start phone authentication - sends SMS code.
     *
     * @param phoneNumber E.164 formatted phone number (e.g., "+79261234567")
     * @return Result with verificationId needed to confirm the code
     */
    suspend fun signInWithPhoneStart(phoneNumber: String): Result<String>

    /**
     * Confirm phone authentication with verification code.
     *
     * @param verificationId From signInWithPhoneStart
     * @param code SMS verification code
     * @return Result with FirebaseToken
     */
    suspend fun confirmPhoneCode(verificationId: String, code: String): Result<FirebaseToken>

    /**
     * Sign out from Firebase (does not clear app tokens).
     */
    suspend fun signOut()

    /**
     * Get current Firebase user (null if not signed in).
     */
    fun getCurrentUser(): FirebaseUser?
}

/**
 * Firebase token from auth_provider sign-in.
 *
 * @property idToken Firebase ID token to send to backend
 * @property authProvider Firebase auth provider identifier: "google.com" | "apple.com" | "phone"
 */
data class FirebaseToken(
    val idToken: String,
    val authProvider: String,
)

/**
 * Firebase user info.
 */
interface FirebaseUser {
    val uid: String
    val email: String?
    val displayName: String?
}