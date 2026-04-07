package com.aggregateservice.core.firebase

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser as AndroidFirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENT")
actual class FirebaseAuthApi actual constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var activity: Activity? = null
    private var signInLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var pendingContinuation: CancellableContinuation<Result<FirebaseToken>>? = null

    actual fun setActivity(activity: Activity) {
        this.activity = activity
        val componentActivity = activity as? ComponentActivity
            ?: throw IllegalArgumentException("Activity must be a ComponentActivity to use Activity Result API")
        this.signInLauncher = componentActivity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            handleSignInResult(result.resultCode, result.data)
        }
    }

    private fun handleSignInResult(resultCode: Int, data: Intent?) {
        println("FirebaseAuthApi: handleSignInResult called. resultCode: $resultCode, data: ${data != null}")
        val continuation = pendingContinuation
        if (continuation == null) {
            println("FirebaseAuthApi: continuation is null at handleSignInResult!")
            return
        }
        println("FirebaseAuthApi: continuation isActive: ${continuation.isActive}")

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                println("FirebaseAuthApi: Got Google idToken: ${idToken != null}")
                if (idToken != null) {
                    signInWithFirebaseCredential(idToken)
                } else {
                    println("FirebaseAuthApi: Google idToken is null")
                    pendingContinuation = null
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(Exception("Google ID token is null")))
                    }
                }
            } catch (e: ApiException) {
                println("FirebaseAuthApi: ApiException: ${e.statusCode}")
                pendingContinuation = null
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }
        } else {
            println("FirebaseAuthApi: Sign-in cancelled or failed (resultCode: $resultCode)")
            pendingContinuation = null
            if (continuation.isActive) {
                continuation.resume(Result.failure(Exception("Google Sign-In was cancelled or failed")))
            }
        }
    }

    private fun signInWithFirebaseCredential(idToken: String) {
        val continuation = pendingContinuation ?: run {
            println("FirebaseAuthApi: pendingContinuation is null!")
            return
        }
        println("FirebaseAuthApi: signInWithFirebaseCredential started. user: ${firebaseAuth.currentUser?.uid}")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                println("FirebaseAuthApi: signInWithCredential success. user: ${authResult.user?.uid}")
                authResult.user?.getIdToken(true)
                    ?.addOnSuccessListener { idTokenResult ->
                        val token = idTokenResult.token
                        println("FirebaseAuthApi: getIdToken success. token: ${token != null}")
                        if (continuation.isActive && token != null) {
                            pendingContinuation = null
                            continuation.resume(
                                Result.success(
                                    FirebaseToken(
                                        idToken = token,
                                        authProvider = "google.com"
                                    )
                                )
                            )
                            println("FirebaseAuthApi: continuation resumed successfully")
                        } else if (continuation.isActive) {
                            pendingContinuation = null
                            continuation.resume(Result.failure(Exception("Failed to get Firebase ID token")))
                            println("FirebaseAuthApi: token was null, resuming with failure")
                        } else {
                            println("FirebaseAuthApi: continuation not active")
                        }
                    }
                    ?.addOnFailureListener { e ->
                        println("FirebaseAuthApi: getIdToken failed: ${e.message}")
                        if (continuation.isActive) {
                            pendingContinuation = null
                            continuation.resume(Result.failure(e))
                        }
                    }
            }
            .addOnFailureListener { e ->
                println("FirebaseAuthApi: signInWithCredential failed: ${e.message}")
                if (continuation.isActive) {
                    pendingContinuation = null
                    continuation.resume(Result.failure(e))
                }
            }
    }

    actual suspend fun signInWithGoogle(): Result<FirebaseToken> = suspendCancellableCoroutine { continuation ->
        val act = activity
        val launcher = signInLauncher

        println("FirebaseAuthApi: signInWithGoogle() called. Activity: ${act != null}, Launcher: ${launcher != null}")

        if (act == null || launcher == null) {
            // Log error for debugging
            println("FirebaseAuthApi: signInWithGoogle() called with null activity or launcher. Activity: $act, Launcher: $launcher")
            continuation.resume(Result.failure(Exception("Activity not set. Call setActivity() before signInWithGoogle().")))
            return@suspendCancellableCoroutine
        }

        if (pendingContinuation != null) {
            // Clear any stale continuation
            println("FirebaseAuthApi: Clearing stale pendingContinuation")
            pendingContinuation = null
        }

        pendingContinuation = continuation

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("55183234968-pn4t94d6s66ue76qmcbvkf3c2qpoffu9.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(act, gso)
        val signInIntent = googleSignInClient.signInIntent

        val intentSenderRequest = IntentSenderRequest.Builder(
            PendingIntent.getActivity(
                act,
                0,
                signInIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            ).intentSender
        ).build()
        launcher.launch(intentSenderRequest)
        println("FirebaseAuthApi: Google Sign-In intent launched")
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
            suspendCancellableCoroutine { cont ->
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        authResult.user?.getIdToken(true)
                            ?.addOnSuccessListener { idTokenResult ->
                                val token = idTokenResult.token
                                if (cont.isActive && token != null) {
                                    cont.resume(
                                        Result.success(
                                            FirebaseToken(
                                                idToken = token,
                                                authProvider = "phone"
                                            )
                                        )
                                    )
                                } else if (cont.isActive) {
                                    cont.resume(Result.failure(Exception("Failed to get ID token")))
                                }
                            }
                            ?.addOnFailureListener { e ->
                                if (cont.isActive) {
                                    cont.resume(Result.failure(e))
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        if (cont.isActive) {
                            cont.resume(Result.failure(e))
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
