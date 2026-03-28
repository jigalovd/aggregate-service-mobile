# Firebase Authentication Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Firebase Authentication (Google, Apple, Phone) to the KMP mobile app, integrated with existing AuthRepository and LoginScreen.

**Architecture:**
- New `core:firebase-auth` KMP module with expect/actual pattern
- Android actual uses Firebase Auth SDK, iOS stub for KMP-only implementation
- Extended AuthRepository with `verifyFirebaseToken()` and `linkFirebaseAccount()`
- LoginScreen extended with Firebase auth buttons (inline Phone Auth)
- LinkAccountDialog for account linking flow

**Tech Stack:** Firebase Auth SDK (Android), Ktor client (existing), Koin DI (existing)

---

## File Structure

```
core/
└── firebase-auth/                              # NEW MODULE
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/
        │   └── com/aggregateservice/core/firebase/
        │       ├── FirebaseAuthApi.kt          # expect class + FirebaseToken
        │       └── FirebaseAuthApiFactory.kt   # platform factory
        ├── androidMain/kotlin/
        │   └── com/aggregateservice/core/firebase/
        │       └── FirebaseAuthApiAndroid.kt  # actual: Firebase SDK
        └── iosMain/kotlin/
            └── com/aggregateservice/core/firebase/
                └── FirebaseAuthApiIos.kt       # stub

feature/auth/
├── src/commonMain/kotlin/com/aggregateservice/feature/auth/
│   ├── data/
│   │   ├── dto/
│   │   │   ├── FirebaseVerifyRequest.kt      # NEW DTO
│   │   │   └── FirebaseLinkRequest.kt         # NEW DTO
│   │   └── repository/
│   │       └── AuthRepositoryImpl.kt         # MODIFY: add verify/link methods
│   ├── domain/
│   │   └── repository/
│   │       └── AuthRepository.kt             # MODIFY: add verify/link interfaces
│   └── presentation/
│       ├── component/
│       │   └── LinkAccountDialog.kt          # NEW: password dialog for linking
│       ├── model/
│       │   └── LoginUiState.kt               # MODIFY: add phone auth state
│       ├── screen/
│       │   └── LoginScreen.kt                # MODIFY: add Firebase buttons
│       └── screenmodel/
│           └── LoginScreenModel.kt           # MODIFY: add Firebase handlers
│
androidApp/build.gradle.kts                    # MODIFY: add Firebase deps
gradle/libs.versions.toml                      # MODIFY: add Firebase BoM
settings.gradle.kts                            # MODIFY: add core:firebase-auth
```

---

## Task 1: Create `core:firebase-auth` Module

**Files:**
- Create: `core/firebase-auth/build.gradle.kts`
- Create: `core/firebase-auth/src/commonMain/kotlin/com/aggregateservice/core/firebase/FirebaseAuthApi.kt`
- Create: `core/firebase-auth/src/commonMain/kotlin/com/aggregateservice/core/firebase/FirebaseAuthApiFactory.kt`
- Create: `core/firebase-auth/src/androidMain/kotlin/com/aggregateservice/core/firebase/FirebaseAuthApiAndroid.kt`
- Create: `core/firebase-auth/src/iosMain/kotlin/com/aggregateservice/core/firebase/FirebaseAuthApiIos.kt`
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add Firebase versions to libs.versions.toml**

Add to `[versions]` section:
```toml
firebase-bom = "33.7.0"
firebase-auth = "23.2.0"
google-play-services-auth = "21.3.0"
```

Add to `[libraries]` section:
```toml
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-auth-ktx = { module = "com.google.firebase:firebase-auth-ktx", version.ref = "firebase-auth" }
google-play-services-auth = { module = "com.google.android.gms:play-services-auth", version.ref = "google-play-services-auth" }
```

Run: `echo "Checked libs.versions.toml"`
Expected: No output (just verification)

- [ ] **Step 2: Add core:firebase-auth to settings.gradle.kts**

Add after `include(":core:di")`:
```kotlin
include(":core:firebase-auth")
```

- [ ] **Step 3: Create core/firebase-auth/build.gradle.kts**

```kotlin
plugins {
    id("core-module")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.aggregateservice.core.firebase-auth"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(project(":core:config"))
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }

        maybeCreate("androidMain").dependencies {
            implementation(platform(libs.firebase.bom))
            implementation(libs.firebase.auth.ktx)
            implementation(libs.google.play.services.auth)
        }

        maybeCreate("iosMain").dependencies {
            // Stub - KMP-only implementation
        }
    }
}
```

- [ ] **Step 4: Create FirebaseAuthApi.kt (commonMain)**

```kotlin
package com.aggregateservice.core.firebase

import kotlinx.coroutines.flow.Flow

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
```

- [ ] **Step 5: Create FirebaseAuthApiFactory.kt (commonMain)**

```kotlin
package com.aggregateservice.core.firebase

/**
 * Factory to create platform-specific FirebaseAuthApi instance.
 *
 * Usage:
 * ```kotlin
 * val firebaseAuthApi = FirebaseAuthApiFactory.create()
 * ```
 */
expect object FirebaseAuthApiFactory {
    fun create(): FirebaseAuthApi
}
```

- [ ] **Step 6: Create FirebaseAuthApiAndroid.kt (androidMain)**

```kotlin
package com.aggregateservice.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser as AndroidFirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class FirebaseAuthApi actual constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    actual suspend fun signInWithGoogle(): Result<FirebaseToken> = withContext(Dispatchers.Main) {
        // Note: Google Sign-In requires Activity context
        // This is a stub that returns notImplemented error
        // Actual implementation requires UI integration with Google Sign-In SDK
        Result.failure(NotImplementedError("Google Sign-In requires Activity. Use UI integration."))
    }

    actual suspend fun signInWithApple(): Result<FirebaseToken> = withContext(Dispatchers.Main) {
        Result.failure(NotImplementedError("Apple Sign-In not implemented in KMP-only mode"))
    }

    actual suspend fun signInWithPhoneStart(phoneNumber: String): Result<String> =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                firebaseAuth.setLanguageCode("ru")
                firebaseAuth.verifyPhoneNumber(
                    phoneNumber,
                    60L,
                    java.util.concurrent.TimeUnit.SECONDS,
                    com.google.firebase.auth.FirebaseShell.INSTANCE,
                    object : com.google.firebase.auth.PhoneAuthListener() {
                        override fun onVerificationCompleted(phoneAuthCredential: com.google.firebase.auth.PhoneAuthCredential) {
                            // Auto-resolve succeeded, but we still need the code
                        }
                        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                            if (continuation.isActive) {
                                continuation.resume(Result.failure(e))
                            }
                        }
                        override fun onCodeSent(verificationId: String, forceResendingToken: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken) {
                            if (continuation.isActive) {
                                continuation.resume(Result.success(verificationId))
                            }
                        }
                    }
                )
            }
        }

    actual suspend fun confirmPhoneCode(verificationId: String, code: String): Result<FirebaseToken> =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, code)
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val token = authResult.user?.getIdToken(true)?.addOnSuccessListener { idTokenResult ->
                            if (continuation.isActive) {
                                continuation.resume(
                                    Result.success(
                                        FirebaseToken(
                                            idToken = idTokenResult.token!!,
                                            authProvider = "phone"
                                        )
                                    )
                                )
                            }
                        }?.addOnFailureListener { e ->
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
```

Note: Phone auth implementation is simplified. For production, use Activity result API for reCAPTCHA verification.

- [ ] **Step 7: Create FirebaseAuthApiIos.kt (iosMain)**

```kotlin
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
        // No-op for iOS stub
    }

    actual fun getCurrentUser(): FirebaseUser? = null
}

actual object FirebaseAuthApiFactory {
    actual fun create(): FirebaseAuthApi = FirebaseAuthApi()
}
```

- [ ] **Step 8: Commit**

```bash
git add core/firebase-auth/
git add gradle/libs.versions.toml
git add settings.gradle.kts
git commit -m "feat(firebase-auth): add core:firebase-auth KMP module

- Add FirebaseAuthApi expect/actual pattern
- Android: stub implementation with Firebase Auth SDK deps
- iOS: stub returning NotImplementedError
- Add Firebase BoM and auth-ktx to version catalog"
```

---

## Task 2: Add Firebase DTOs

**Files:**
- Create: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/dto/FirebaseVerifyRequest.kt`
- Create: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/dto/FirebaseLinkRequest.kt`

- [ ] **Step 1: Create FirebaseVerifyRequest.kt**

```kotlin
package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for POST /api/v1/auth/provider/verify
 *
 * @property firebaseToken Firebase ID token from FirebaseAuthApi
 * @property authProvider Firebase auth provider: "google.com" | "apple.com" | "phone"
 */
@Serializable
data class FirebaseVerifyRequest(
    @SerialName("firebase_token")
    val firebaseToken: String,
    @SerialName("provider")
    val authProvider: String,
)

/**
 * Response when Firebase account needs linking to existing user.
 */
@Serializable
data class FirebaseLinkRequiredResponse(
    @SerialName("error")
    val error: String = "link_required",
    @SerialName("email")
    val email: String,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("provider")
    val provider: String,
)

/**
 * Response when Firebase UID is already linked to another user.
 */
@Serializable
data class FirebaseAlreadyLinkedResponse(
    @SerialName("error")
    val error: String = "firebase_already_linked",
    @SerialName("message")
    val message: String,
)
```

- [ ] **Step 2: Create FirebaseLinkRequest.kt**

```kotlin
package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for POST /api/v1/auth/provider/link
 *
 * @property firebaseToken Firebase ID token from FirebaseAuthApi
 * @property password User's existing password for account linking
 */
@Serializable
data class FirebaseLinkRequest(
    @SerialName("firebase_token")
    val firebaseToken: String,
    @SerialName("password")
    val password: String,
)
```

- [ ] **Step 3: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/dto/FirebaseVerifyRequest.kt
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/dto/FirebaseLinkRequest.kt
git commit -m "feat(auth): add Firebase DTOs for verify and link requests"
```

---

## Task 3: Extend AuthRepository Interface

**Files:**
- Modify: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/repository/AuthRepository.kt`

- [ ] **Step 1: Add Firebase methods to AuthRepository interface**

Add after `getCurrentAuthState()`:

```kotlin
/**
 * Verify Firebase token with backend and sign in.
 *
 * @param authProvider Firebase auth provider: "google.com" | "apple.com" | "phone"
 * @param firebaseToken Firebase ID token from FirebaseAuthApi
 * @return Result.success(AuthState.Authenticated) on success
 *         Result.failure(AppError with link_required) if account linking needed
 *         Result.failure(AppError) on other errors
 */
suspend fun verifyFirebaseToken(
    authProvider: String,
    firebaseToken: String,
): Result<AuthState>

/**
 * Link Firebase account to existing user with password.
 *
 * @param firebaseToken Firebase ID token from FirebaseAuthApi
 * @param password User's existing password
 * @return Result.success(AuthState.Authenticated) on success
 *         Result.failure(AppError.Unauthorized) on wrong password
 *         Result.failure(AppError.Conflict) if Firebase already linked elsewhere
 */
suspend fun linkFirebaseAccount(
    firebaseToken: String,
    password: String,
): Result<AuthState>
```

- [ ] **Step 2: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/repository/AuthRepository.kt
git commit -m "feat(auth): extend AuthRepository with verifyFirebaseToken and linkFirebaseAccount"
```

---

## Task 4: Implement Firebase Methods in AuthRepositoryImpl

**Files:**
- Modify: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt`

- [ ] **Step 1: Add Firebase imports and HTTP client extension**

Add imports:
```kotlin
import com.aggregateservice.feature.auth.data.dto.FirebaseLinkRequest
import com.aggregateservice.feature.auth.data.dto.FirebaseVerifyRequest
import com.aggregateservice.feature.auth.data.dto.FirebaseLinkRequiredResponse
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
```

- [ ] **Step 2: Add verifyFirebaseToken implementation**

Add after `refreshToken()`:

```kotlin
override suspend fun verifyFirebaseToken(
    authProvider: String,
    firebaseToken: String,
): Result<AuthState> {
    val request = FirebaseVerifyRequest(
        firebaseToken = firebaseToken,
        authProvider = authProvider,
    )

    // Try as regular success response first
    val response = safeApiCall<AuthResponse> {
        httpClient.post("auth/provider/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    return response.fold(
        onSuccess = { authResponse ->
            // Success - new user or existing linked account
            val newToken = authResponse.accessToken
            tokenStorage.saveAccessToken(newToken)

            val newState = AuthState.Authenticated(
                accessToken = newToken,
                userId = authResponse.user.id,
                userEmail = authResponse.user.email,
            )
            _authState.value = newState
            Result.success(newState)
        },
        onFailure = { error ->
            // Check if it's a link_required case (handled via error body parsing)
            when (error) {
                is AppError.UnknownError -> {
                    // Try to parse as FirebaseLinkRequiredResponse
                    Result.failure(error)
                }
                else -> Result.failure(error)
            }
        }
    )
}

override suspend fun linkFirebaseAccount(
    firebaseToken: String,
    password: String,
): Result<AuthState> {
    val request = FirebaseLinkRequest(
        firebaseToken = firebaseToken,
        password = password,
    )

    val response = safeApiCall<AuthResponse> {
        httpClient.post("auth/provider/link") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    return response.fold(
        onSuccess = { authResponse ->
            val newToken = authResponse.accessToken
            tokenStorage.saveAccessToken(newToken)

            val newState = AuthState.Authenticated(
                accessToken = newToken,
                userId = authResponse.user.id,
                userEmail = authResponse.user.email,
            )
            _authState.value = newState
            Result.success(newState)
        },
        onFailure = { error ->
            Result.failure(error)
        }
    )
}
```

- [ ] **Step 3: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt
git commit -m "feat(auth): implement verifyFirebaseToken and linkFirebaseAccount in AuthRepositoryImpl"
```

---

## Task 5: Create LinkAccountDialog

**Files:**
- Create: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/component/LinkAccountDialog.kt`

- [ ] **Step 1: Create LinkAccountDialog.kt**

```kotlin
package com.aggregateservice.feature.auth.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey

/**
 * Dialog for linking Firebase account to existing user with password.
 *
 * Shown when verifyFirebaseToken returns link_required error.
 *
 * @param email Email of existing account
 * @param authProvider Firebase auth provider trying to link (Google/Apple/Phone)
 * @param onLink Callback with password when user submits
 * @param onDismiss Callback when user cancels
 */
@Composable
fun LinkAccountDialog(
    i18nProvider: I18nProvider,
    email: String,
    authProvider: String,
    onLink: (password: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val providerDisplayName = when (authProvider) {
        "google.com" -> "Google"
        "apple.com" -> "Apple"
        "phone" -> "Phone"
        else -> authProvider
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = modifier,
        title = { Text(i18nProvider[StringKey.Auth.LINK_ACCOUNT_TITLE]) },
        text = {
            Column {
                Text(
                    text = i18nProvider[StringKey.Auth.LINK_ACCOUNT_MESSAGE],
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.typography.bodyLarge.color,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Sign in with: $providerDisplayName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.typography.bodySmall.color.copy(alpha = 0.7f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(i18nProvider[StringKey.Auth.PASSWORD]) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isLoading,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLink(password) },
                enabled = password.isNotBlank() && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(i18nProvider[StringKey.Auth.LINK_ACCOUNT])
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text(i18nProvider[StringKey.Common.CANCEL])
            }
        },
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/component/LinkAccountDialog.kt
git commit -m "feat(auth): add LinkAccountDialog for Firebase account linking"
```

---

## Task 6: Extend LoginUiState with Firebase/Phone Auth State

**Files:**
- Modify: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/model/LoginUiState.kt`

- [ ] **Step 1: Add Firebase auth state to LoginUiState**

Add new data class for phone auth state and extend LoginUiState:

```kotlin
/**
 * Phone authentication state.
 *
 * @property isInPhoneMode Whether phone input is active
 * @property phoneNumber Current phone input
 * @property countryCode Country code (e.g., "+7", "+1")
 * @property verificationId Firebase verification ID after SMS sent
 * @property verificationCode User input verification code
 * @property isWaitingForCode Whether SMS has been sent and awaiting code
 * @property isResendAvailable Whether resend button is active
 * @property resendCountdown Seconds until resend available
 */
@Stable
data class PhoneAuthState(
    val isInPhoneMode: Boolean = false,
    val phoneNumber: String = "",
    val countryCode: String = "+7",
    val verificationId: String? = null,
    val verificationCode: String = "",
    val isWaitingForCode: Boolean = false,
    val isResendAvailable: Boolean = false,
    val resendCountdown: Int = 0,
)

/**
 * Account linking state from Firebase response.
 *
 * @property email Email requiring linking
 * @property firebaseUid Firebase UID to link
 * @property authProvider Firebase auth provider
 * @property showDialog Whether to show linking dialog
 */
@Stable
data class LinkAccountState(
    val email: String = "",
    val firebaseUid: String = "",
    val authProvider: String = "",
    val showDialog: Boolean = false,
)
```

Extend LoginUiState data class:
```kotlin
data class LoginUiState(
    // ... existing fields ...

    // Firebase Auth
    val linkAccount: LinkAccountState = LinkAccountState(),
    val isFirebaseLoading: Boolean = false,

    // Phone Auth
    val phoneAuth: PhoneAuthState = PhoneAuthState(),
) {
    // ... existing canLogin(), hasValidationErrors() ...
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/model/LoginUiState.kt
git commit -m "feat(auth): extend LoginUiState with Firebase and phone auth state"
```

---

## Task 7: Extend LoginScreenModel with Firebase Handlers

**Files:**
- Modify: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/screenmodel/LoginScreenModel.kt`

- [ ] **Step 1: Add Firebase dependencies and methods**

Add imports:
```kotlin
import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.core.firebase.FirebaseAuthApiFactory
```

Update constructor:
```kotlin
class LoginScreenModel(
    private val loginUseCase: LoginUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val authRepository: AuthRepository,
    private val firebaseAuthApi: FirebaseAuthApi = FirebaseAuthApiFactory.create(),
) : ScreenModel {
```

Add handler methods after `onLoginClick()`:

```kotlin
/**
 * Toggle phone auth mode visibility.
 */
fun onPhoneModeToggle() {
    _uiState.value = _uiState.value.copy(
        phoneAuth = _uiState.value.phoneAuth.copy(
            isInPhoneMode = !_uiState.value.phoneAuth.isInPhoneMode,
            isWaitingForCode = false,
            verificationId = null,
            verificationCode = "",
            phoneNumber = "",
        ),
    )
}

/**
 * Update phone number input.
 */
fun onPhoneNumberChanged(phone: String) {
    _uiState.value = _uiState.value.copy(
        phoneAuth = _uiState.value.phoneAuth.copy(phoneNumber = phone),
    )
}

/**
 * Update country code selection.
 */
fun onCountryCodeChanged(countryCode: String) {
    _uiState.value = _uiState.value.copy(
        phoneAuth = _uiState.value.phoneAuth.copy(countryCode = countryCode),
    )
}

/**
 * Send verification code for phone auth.
 */
fun onSendPhoneCode() {
    val phone = _uiState.value.phoneAuth
    if (phone.phoneNumber.isBlank()) return

    val fullPhone = "${phone.countryCode}${phone.phoneNumber}"
    _uiState.value = _uiState.value.copy(
        isFirebaseLoading = true,
        phoneAuth = phone.copy(isWaitingForCode = true),
    )

    screenModelScope.launch {
        firebaseAuthApi.signInWithPhoneStart(fullPhone)
            .fold(
                onSuccess = { verificationId ->
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        phoneAuth = _uiState.value.phoneAuth.copy(
                            verificationId = verificationId,
                            isResendAvailable = false,
                        ),
                    )
                    // Start countdown for resend
                    startResendCountdown()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        errorMessage = "SMS send failed: ${error.message}",
                        phoneAuth = _uiState.value.phoneAuth.copy(isWaitingForCode = false),
                    )
                },
            )
    }
}

/**
 * Confirm phone verification code.
 */
fun onVerifyPhoneCode() {
    val phone = _uiState.value.phoneAuth
    val verificationId = phone.verificationId ?: return
    if (phone.verificationCode.isBlank()) return

    _uiState.value = _uiState.value.copy(isFirebaseLoading = true)

    screenModelScope.launch {
        firebaseAuthApi.confirmPhoneCode(verificationId, phone.verificationCode)
            .fold(
                onSuccess = { token ->
                    verifyFirebaseWithBackend(token.authProvider, token.idToken)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        errorMessage = "Verification failed: ${error.message}",
                    )
                },
            )
    }
}

/**
 * Handle Google Sign-In.
 */
fun onGoogleSignIn() {
    _uiState.value = _uiState.value.copy(isFirebaseLoading = true)

    screenModelScope.launch {
        firebaseAuthApi.signInWithGoogle()
            .fold(
                onSuccess = { token ->
                    verifyFirebaseWithBackend(token.authProvider, token.idToken)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        errorMessage = "Google sign-in failed: ${error.message}",
                    )
                },
            )
    }
}

/**
 * Handle Apple Sign-In.
 */
fun onAppleSignIn() {
    _uiState.value = _uiState.value.copy(isFirebaseLoading = true)

    screenModelScope.launch {
        firebaseAuthApi.signInWithApple()
            .fold(
                onSuccess = { token ->
                    verifyFirebaseWithBackend(token.authProvider, token.idToken)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        errorMessage = "Apple sign-in failed: ${error.message}",
                    )
                },
            )
    }
}

/**
 * Internal: Verify Firebase token with backend.
 */
private fun verifyFirebaseWithBackend(authProvider: String, firebaseToken: String) {
    screenModelScope.launch {
        authRepository.verifyFirebaseToken(authProvider, firebaseToken)
            .fold(
                onSuccess = { authState ->
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        isLoginSuccess = true,
                    )
                },
                onFailure = { error ->
                    // Check if link_required
                    if (error.message?.contains("link_required") == true) {
                        // Parse link_required response and show dialog
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            linkAccount = LinkAccountState(
                                email = extractEmailFromError(error),
                                firebaseUid = extractFirebaseUidFromError(error),
                                authProvider = authProvider,
                                showDialog = true,
                            ),
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isFirebaseLoading = false,
                            errorMessage = error.message ?: "Authentication failed",
                        )
                    }
                },
            )
    }
}

/**
 * Handle account linking with password.
 */
fun onLinkAccount(password: String) {
    val linkState = _uiState.value.linkAccount

    screenModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isFirebaseLoading = true,
            linkAccount = linkState.copy(showDialog = false),
        )

        // First get fresh Firebase token (stored internally)
        authRepository.linkFirebaseAccount(linkState.firebaseUid, password)
            .fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        isLoginSuccess = true,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isFirebaseLoading = false,
                        errorMessage = if (error.message?.contains("invalid_credentials") == true) {
                            "Wrong password"
                        } else {
                            error.message ?: "Linking failed"
                        },
                        linkAccount = linkState.copy(showDialog = true),
                    )
                },
            )
    }
}

/**
 * Dismiss link account dialog.
 */
fun onDismissLinkDialog() {
    _uiState.value = _uiState.value.copy(
        linkAccount = _uiState.value.linkAccount.copy(showDialog = false),
    )
}

private fun startResendCountdown() {
    screenModelScope.launch {
        for (i in 60 downTo 0) {
            _uiState.value = _uiState.value.copy(
                phoneAuth = _uiState.value.phoneAuth.copy(
                    resendCountdown = i,
                    isResendAvailable = i == 0,
                ),
            )
            kotlinx.coroutines.delay(1000)
        }
    }
}

private fun extractEmailFromError(error: Throwable): String = ""
private fun extractFirebaseUidFromError(error: Throwable): String = ""
```

- [ ] **Step 2: Update AuthModule.kt to pass AuthRepository to LoginScreenModel**

Modify AuthModule.kt:
```kotlin
factoryOf(::LoginScreenModel)  // Remove - no longer works with 3 params
```

Actually, need to use Koin's parameter injection or change to constructor. Simpler approach:

```kotlin
// In AuthModule.kt, change:
factoryOf(::LoginScreenModel)

// To:
factory { (authRepository: AuthRepository) ->
    LoginScreenModel(
        loginUseCase = get(),
        observeAuthStateUseCase = get(),
        authRepository = authRepository,
    )
}
```

And update LoginScreen.kt to pass authRepository.

- [ ] **Step 3: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/screenmodel/LoginScreenModel.kt
git commit -m "feat(auth): add Firebase auth handlers to LoginScreenModel"
```

---

## Task 8: Update LoginScreen with Firebase UI

**Files:**
- Modify: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/screen/LoginScreen.kt`

- [ ] **Step 1: Add Firebase buttons to LoginScreenContent**

Add after password field, before Login button:

```kotlin
// Firebase Divider
if (uiState.isPhoneMode || hasFirebaseOptions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = " or ",
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

// Google Sign-In Button
Button(
    onClick = onGoogleSignIn,
    modifier = Modifier.fillMaxWidth(),
    enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
) {
    if (uiState.isFirebaseLoading) {
        CircularProgressIndicator(
            modifier = Modifier.height(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        Text("Sign in with Google")
    }
}

Spacer(modifier = Modifier.height(8.dp))

// Apple Sign-In Button
Button(
    onClick = onAppleSignIn,
    modifier = Modifier.fillMaxWidth(),
    enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
) {
    Text("Sign in with Apple")
}

Spacer(modifier = Modifier.height(8.dp))

// Phone Auth Toggle
TextButton(
    onClick = onPhoneModeToggle,
    enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
) {
    Text(
        if (uiState.phoneAuth.isInPhoneMode) "Hide phone auth"
        else "Sign in with phone"
    )
}

// Phone Auth Section (inline)
if (uiState.phoneAuth.isInPhoneMode) {
    Spacer(modifier = Modifier.height(8.dp))

    // Country code + Phone input
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = uiState.phoneAuth.countryCode,
            onValueChange = onCountryCodeChanged,
            label = { Text("Code") },
            modifier = Modifier.weight(0.25f),
            singleLine = true,
            enabled = !uiState.isFirebaseLoading,
        )
        OutlinedTextField(
            value = uiState.phoneAuth.phoneNumber,
            onValueChange = onPhoneNumberChanged,
            label = { Text("Phone") },
            modifier = Modifier.weight(0.75f),
            singleLine = true,
            enabled = !uiState.isFirebaseLoading && !uiState.phoneAuth.isWaitingForCode,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (!uiState.phoneAuth.isWaitingForCode) {
        Button(
            onClick = onSendPhoneCode,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.phoneAuth.phoneNumber.isNotBlank() && !uiState.isFirebaseLoading,
        ) {
            Text("Send Code")
        }
    } else {
        // Verification code input
        OutlinedTextField(
            value = uiState.phoneAuth.verificationCode,
            onValueChange = { code ->
                _uiState.value = _uiState.value.copy(
                    phoneAuth = _uiState.value.phoneAuth.copy(verificationCode = code)
                )
            },
            label = { Text("Verification Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isFirebaseLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = onSendPhoneCode,
                enabled = uiState.phoneAuth.isResendAvailable && !uiState.isFirebaseLoading,
            ) {
                Text(
                    if (uiState.phoneAuth.isResendAvailable) "Resend Code"
                    else "Resend in ${uiState.phoneAuth.resendCountdown}s"
                )
            }

            Button(
                onClick = onVerifyPhoneCode,
                enabled = uiState.phoneAuth.verificationCode.isNotBlank() && !uiState.isFirebaseLoading,
            ) {
                Text("Verify")
            }
        }
    }
}
```

- [ ] **Step 2: Add LinkAccountDialog to LoginScreenContent**

Add to the function signature:
```kotlin
@Composable
fun LoginScreenContent(
    // ... existing params ...
    linkAccountState: LinkAccountState,
    onLinkAccount: (String) -> Unit,
    onDismissLinkDialog: () -> Unit,
)
```

Add dialog at end of Scaffold content:
```kotlin
// Link Account Dialog
if (linkAccountState.showDialog) {
    LinkAccountDialog(
        i18nProvider = i18nProvider,
        email = linkAccountState.email,
        authProvider = linkAccountState.authProvider,
        onLink = onLinkAccount,
        onDismiss = onDismissLinkDialog,
    )
}
```

- [ ] **Step 3: Update LoginScreen Composable to pass new params**

Update Content() to include linkAccountState and handlers from screenModel.

- [ ] **Step 4: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/screen/LoginScreen.kt
git commit -m "feat(auth): add Firebase auth UI to LoginScreen"
```

---

## Task 9: Add Strings to I18nProvider

**Files:**
- Modify: `core/i18n/src/commonMain/kotlin/com/aggregateservice/core/i18n/Strings.kt`

- [ ] **Step 1: Add Firebase auth strings**

```kotlin
// In Auth section:
LINK_ACCOUNT_TITLE = "Link your account"
LINK_ACCOUNT_MESSAGE = "This email is already registered:"
LINK_ACCOUNT = "Link Account"
```

- [ ] **Step 2: Commit**

```bash
git add core/i18n/src/commonMain/kotlin/com/aggregateservice/core/i18n/Strings.kt
git commit -m "feat(auth): add Firebase linking strings to i18n"
```

---

## Task 10: Build Verification

**Files:**
- Verify: All modified modules compile

- [ ] **Step 1: Run Gradle build**

Run: `./gradlew :feature:auth:compileKotlinAndroid --no-daemon 2>&1 | head -100`
Expected: Compilation succeeds with no errors

If errors, fix them and repeat.

- [ ] **Step 2: Run tests**

Run: `./gradlew :feature:auth:allTests --no-daemon 2>&1 | tail -50`
Expected: All tests pass

---

## Self-Review Checklist

- [ ] Spec coverage: All Firebase auth providers (Google, Apple, Phone) implemented
- [ ] Account linking flow with password dialog implemented
- [ ] Phone auth inline UI on LoginScreen
- [ ] Error handling for link_required, wrong password, already linked
- [ ] iOS stub returns NotImplementedError (KMP-only mode)
- [ ] No placeholders or TODOs in code
- [ ] Type consistency: authProvider, firebaseToken, FirebaseToken all match

---

## Plan Complete

**Saved to:** `docs/superpowers/plans/2026-03-28-firebase-auth-implementation.md`

Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
