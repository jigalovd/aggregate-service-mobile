package com.aggregateservice.core.firebase

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.aggregateservice.core.auth.contract.AuthProvider
import com.aggregateservice.core.firebaseAuth.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

actual class AuthProviderApi actual constructor() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var credentialManager: CredentialManager? = null

    private fun getCredentialManager(context: PlatformAuthContext): CredentialManager {
        return credentialManager ?: CredentialManager.create(context).also {
            credentialManager = it
        }
    }

    actual suspend fun signInWithGoogle(context: PlatformAuthContext): Result<AuthProviderResult> =
        withContext(Dispatchers.Main) {
            try {
                val cm = getCredentialManager(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = cm.getCredential(
                    request = request,
                    context = context,
                )

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                exchangeGoogleTokenForFirebase(googleIdToken)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun exchangeGoogleTokenForFirebase(googleIdToken: String): Result<AuthProviderResult> =
        suspendCancellableCoroutine { continuation ->
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            var resumed = false

            continuation.invokeOnCancellation {
                // Firebase does not support cancelling signInWithCredential.
                // Mark as resumed to prevent the callback from crashing.
                resumed = true
            }

            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    if (resumed) return@addOnSuccessListener
                    authResult.user?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                        if (resumed) return@addOnSuccessListener
                        val firebaseIdToken = tokenResult.token
                        if (firebaseIdToken != null) {
                            resumed = true
                            continuation.resume(Result.success(
                                AuthProviderResult(
                                    idToken = firebaseIdToken,
                                    provider = AuthProvider.GOOGLE,
                                )
                            ))
                        } else {
                            resumed = true
                            continuation.resume(Result.failure(Exception("Firebase ID token is null")))
                        }
                    }?.addOnFailureListener { e ->
                        if (resumed) return@addOnFailureListener
                        resumed = true
                        continuation.resume(Result.failure(e))
                    }
                }
                .addOnFailureListener { e ->
                    if (resumed) return@addOnFailureListener
                    resumed = true
                    continuation.resume(Result.failure(e))
                }
        }

    actual suspend fun signInWithApple(): Result<AuthProviderResult> =
        Result.failure(NotImplementedError("Apple Sign-In not implemented on Android"))

    actual suspend fun signInWithPhoneStart(phone: String): Result<String> =
        Result.failure(NotImplementedError("Phone auth not configured"))

    actual suspend fun confirmPhoneCode(verificationId: String, code: String): Result<AuthProviderResult> =
        Result.failure(NotImplementedError("Phone auth not configured"))

    actual suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
