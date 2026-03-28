# Firebase Authentication Integration — Design Spec

**Created:** 2026-03-28
**Status:** Approved
**Stack:** KMP/CMP (Android + iOS), Kotlin Multiplatform
**Backend:** beauty-service (already implemented)

---

## 1. Overview

Integration of Firebase Authentication (Google, Apple, Phone) into existing KMP mobile app.

**Key constraint:** iOS Xcode project doesn't exist yet — KMP-only implementation with iOS stubs.

**Terminology:**
- `auth_provider` — Firebase authentication provider (google.com, apple.com, phone)
- `provider` — service provider (business offering beauty services)

---

## 2. Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                          │
│  LoginScreen                                                       │
│  ├── Google Sign-In Button                                          │
│  ├── Apple Sign-In Button                                          │
│  ├── Phone Auth Section (inline)                                    │
│  │    ├── CountryPicker + PhoneInput                               │
│  │    ├── VerificationCodeInput (after SMS)                        │
│  │    └── ResendButton (with countdown)                            │
│  └── AuthPromptDialog (link_required scenario)                     │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         Domain Layer                                │
│  AuthRepository (extended)                                          │
│  ├── verifyFirebaseToken(authProvider, firebaseToken)              │
│  └── linkFirebaseAccount(firebaseToken, password)                  │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Data Layer                                 │
│  ┌──────────────────────────┐  ┌────────────────────────────────┐  │
│  │  FirebaseAuthApi         │  │  AuthRepositoryImpl             │  │
│  │  (expect/actual KMP)     │  │  (existing, extended)          │  │
│  │  Android: Firebase SDK   │  │                                │  │
│  │  iOS: stub               │  │  HTTP calls to backend         │  │
│  └──────────────────────────┘  └────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Auth Provider Enum

```kotlin
enum class AuthProvider(val id: String) {
    GOOGLE("google.com"),
    APPLE("apple.com"),
    PHONE("phone");
}
```

---

## 4. FirebaseAuthApi (KMP expect/actual)

### 4.1 Common (expect)

```kotlin
expect class FirebaseAuthApi {
    suspend fun signInWithGoogle(): Result<FirebaseToken>
    suspend fun signInWithApple(): Result<FirebaseToken>
    suspend fun signInWithPhone(phone: String): Result<String>  // returns verificationId
    suspend fun confirmPhoneCode(verificationId: String, code: String): Result<FirebaseToken>
    suspend fun signOut()
}

data class FirebaseToken(
    val token: String,
    val authProvider: AuthProvider,
)
```

### 4.2 Android (actual)

Uses Firebase Auth SDK + Play Services Auth for Google Sign-In.

### 4.3 iOS (stub)

KMP-only — returns `NotSupportedError` for all operations. iOS implementation deferred until Xcode project exists.

---

## 5. AuthRepository Interface Changes

```kotlin
interface AuthRepository {
    // ... existing methods ...

    suspend fun verifyFirebaseToken(
        authProvider: AuthProvider,
        firebaseToken: String,
    ): Result<AuthState>

    suspend fun linkFirebaseAccount(
        firebaseToken: String,
        password: String,
    ): Result<AuthState>
}
```

### Backend Responses

| HTTP | Response | Action |
|------|----------|--------|
| 201 | AuthResponse + `{is_new_user: true}` | New user, navigate to main |
| 200 | AuthResponse | Existing user, navigate to main |
| 200 | `{error: "link_required", email, firebase_uid, auth_provider}` | Show LinkAccountDialog |
| 409 | `{error: "firebase_already_linked"}` | Show error: "Firebase already linked to another account" |

---

## 6. LoginScreen UI

### 6.1 Layout

```
┌─────────────────────────────────────┐
│         Sign in                     │
│                                     │
│  ┌─────────────────────────────┐    │
│  │  Sign in with Google  G    │    │  ← Google button
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │  Sign in with Apple    🍎  │    │  ← Apple button
│  └─────────────────────────────┘    │
│                                     │
│  ─────── or ───────                 │
│                                     │
│  ┌────────┬───────────────────┐    │
│  │ +1 ▼  │ Phone number      │    │  ← CountryPicker + PhoneInput
│  └────────┴───────────────────┘    │
│                                     │
│  [ Send Code ]                      │  ← Primary button
│                                     │
│  ─── After SMS sent ───             │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ Verification code           │    │  ← Code input (appears)
│  └─────────────────────────────┘    │
│  Resend in 00:${countdown}          │
│  [ Verify ]                         │
│                                     │
│  ─────────────────────────────      │
│  Already have account? Sign in      │  ← Existing link
└─────────────────────────────────────┘
```

### 6.2 LoginUiState Changes

```kotlin
data class LoginUiState(
    // Existing fields...
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,

    // Phone Auth
    val isPhoneMode: Boolean = false,
    val phoneNumber: String = "",
    val countryCode: String = "+1",
    val verificationCode: String = "",
    val verificationId: String? = null,
    val isWaitingForCode: Boolean = false,
    val isResendAvailable: Boolean = false,
    val resendCountdown: Int = 0,

    // Link Account (link_required scenario)
    val linkDialogVisible: Boolean = false,
    val linkEmail: String? = null,
    val linkAuthProvider: AuthProvider? = null,
    val linkPassword: String = "",
    val linkPasswordError: String? = null,
    val isLinking: Boolean = false,
)
```

---

## 7. LinkAccountDialog

```
┌─────────────────────────────────────┐
│  Link your account                  │
│                                     │
│  This email is already registered:   │
│  john@example.com                   │
│                                     │
│  Sign in with: Google               │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ Password                   │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ Link Account ]    [ Cancel ]     │
└─────────────────────────────────────┘
```

---

## 8. Auth Flow Diagrams

### 8.1 Google/Apple Sign-In

```
User taps "Sign in with Google"
         │
         ▼
FirebaseAuthApi.signInWithGoogle()
         │
         │  (Google SDK → id_token)
         ▼
AuthRepository.verifyFirebaseToken(GOOGLE, id_token)
         │
         ├──► 201/200 → AuthState.Authenticated → navigate to main
         │
         └──► link_required
                  │
                  ▼
            LinkAccountDialog.show(email, GOOGLE)
                  │
                  ├──► User enters password → linkFirebaseAccount()
                  │         │
                  │         ├──► 200 → AuthState.Authenticated
                  │         └──► 401 → "Wrong password" error
                  │
                  └──► Cancel → dismiss
```

### 8.2 Phone Sign-In

```
User enters phone → taps "Send Code"
         │
         ▼
FirebaseAuthApi.signInWithPhone("+1234567890")
         │
         │  (Firebase sends SMS)
         ▼
verificationId returned → show code input
         │
         ▼
User enters code → taps "Verify"
         │
         ▼
FirebaseAuthApi.confirmPhoneCode(verificationId, "123456")
         │
         │  (Firebase returns phone token)
         ▼
AuthRepository.verifyFirebaseToken(PHONE, phone_token)
         │
         └──► Same as Google flow
```

---

## 9. Error Handling

### 9.1 AppError Extensions

```kotlin
// New errors for Firebase auth
data object FirebaseAlreadyLinked : AppError()
data class LinkRequired(val email: String, val firebaseUid: String, val authProvider: AuthProvider) : AppError()
data class InvalidPhoneNumber(val message: String = "Invalid phone number") : AppError()
data class InvalidVerificationCode(val message: String = "Invalid verification code") : AppError()
data object FirebaseNotSupported : AppError()  // iOS stub
```

### 9.2 Error → User Message Mapping

| AppError | User Message (RU) |
|----------|------------------|
| FirebaseAlreadyLinked | "Этот Firebase аккаунт уже привязан к другому пользователю" |
| InvalidPhoneNumber | "Неверный номер телефона" |
| InvalidVerificationCode | "Неверный код подтверждения" |
| FirebaseNotSupported | "Firebase Auth на iOS временно недоступен" |

---

## 10. Files to Create/Modify

### New Module

| Path | Description |
|------|-------------|
| `core/firebase-auth/build.gradle.kts` | KMP module config |
| `core/firebase-auth/src/commonMain/kotlin/FirebaseAuthApi.kt` | expect class + AuthProvider enum |
| `core/firebase-auth/src/androidMain/kotlin/FirebaseAuthApiImpl.kt` | actual: Firebase SDK implementation |
| `core/firebase-auth/src/iosMain/kotlin/FirebaseAuthApiImpl.kt` | stub implementation |

### Feature Auth Changes

| Path | Change |
|------|--------|
| `feature/auth/src/commonMain/.../domain/repository/AuthRepository.kt` | Add verifyFirebaseToken, linkFirebaseAccount |
| `feature/auth/src/commonMain/.../data/repository/AuthRepositoryImpl.kt` | Implement new methods + FirebaseToken DTOs |
| `feature/auth/src/commonMain/.../data/dto/FirebaseTokenDto.kt` | **NEW** — Firebase token DTOs for API |
| `feature/auth/src/commonMain/.../presentation/model/LoginUiState.kt` | Add phone + link fields |
| `feature/auth/src/commonMain/.../presentation/screen/LoginScreen.kt` | Add Google/Apple/Phone UI |
| `feature/auth/src/commonMain/.../presentation/screenmodel/LoginScreenModel.kt` | Add phone/link handlers |
| `feature/auth/src/commonMain/.../presentation/component/LinkAccountDialog.kt` | **NEW** — account linking dialog |
| `feature/auth/src/commonMain/.../di/AuthModule.kt` | Add FirebaseAuthApi to Koin |

### Build Config

| Path | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add Firebase BoM, firebase-auth, play-services-auth versions |
| `androidApp/build.gradle.kts` | Add google-services plugin + Firebase dependencies |
| `settings.gradle.kts` | Include `:core:firebase-auth` |

---

## 11. Dependencies

### libs.versions.toml additions

```toml
[versions]
firebase-bom = "33.7.0"
firebase-auth = "33.7.0"
play-services-auth = "21.3.0"
google-services = "4.4.2"

[libraries]
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-auth-ktx = { module = "com.google.firebase:firebase-auth-ktx", version.ref = "firebase-auth" }
play-services-auth = { module = "com.google.android.gms:play-services-auth", version.ref = "play-services-auth" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "google-services" }
```

---

## 12. Testing

### Unit Tests

- `FirebaseAuthApiImplTest` — mock Firebase SDK, test token parsing
- `AuthRepositoryTest` — verify/link with mocked HttpClient

### Manual (Android only)

1. Google Sign-In on physical device
2. Phone Auth with test number (add in Firebase Console → Phone)
3. Account linking flow
4. Logout → re-login

---

## 13. Deferred Items

- **iOS implementation** — requires Xcode project setup (Podfile, GoogleService-Info.plist, capabilities)
- **Apple Sign-In on iOS** — requires ASAuthorizationController implementation after iOS scaffold exists
