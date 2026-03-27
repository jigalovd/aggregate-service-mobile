# Firebase Authentication Integration Checklist

**Project:** KMP/CMP (Kotlin Multiplatform / Compose Multiplatform)  
**Target Platforms:** Android, iOS  
**Created:** 2026-03-27  
**Backend:** beauty-service backend (already implemented)

---

## 1. Firebase Console Setup

### 1.1 Create/Configure Firebase Project

- [ ] Create Firebase project at https://console.firebase.google.com/
- [ ] Add Android app to project (package name: `com.beautyservice.app`)
- [ ] Add iOS app to project (bundle ID from Xcode project)
- [ ] Download `google-services.json` (Android) → place in `android/app/`
- [ ] Download `GoogleService-Info.plist` (iOS) → place in `ios/Runner/`

### 1.2 Enable Authentication Providers

- [ ] **Google Sign-In** → Enable + configure SHA-1 fingerprint
- [ ] **Apple Sign-In** → Enable + configure Services ID + Key
- [ ] **Phone Authentication** → Enable + add test phone numbers

### 1.3 Generate Service Account (Backend)

- [ ] Go to Project Settings → Service Accounts
- [ ] Generate new private key (JSON)
- [ ] Send JSON to backend team OR store in secure backend config

---

## 2. Android Setup

### 2.1 Gradle Configuration

```kotlin
// android/build.gradle.kts (project level)
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// android/app/build.gradle.kts
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
}
```

- [ ] Add Google services plugin to project
- [ ] Add Firebase Auth dependencies
- [ ] Add Play Services Auth for Google Sign-In
- [ ] Configure SHA-1 fingerprint in Firebase Console

### 2.2 Android Manifest

- [ ] Add Internet permission (if not present)

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 2.3 Firebase Initialization

- [ ] Verify `google-services.json` is in `android/app/`
- [ ] Ensure Firebase is initialized in Application class or entry point

---

## 3. iOS Setup

### 3.1 Xcode Configuration

- [ ] Open `ios/Runner.xcworkspace` in Xcode
- [ ] Add `GoogleService-Info.plist` to Runner target
- [ ] Configure code signing and capabilities
- [ ] Enable "Push Notifications" capability (for Phone Auth)

### 3.2 Podfile

```ruby
# ios/Podfile
platform :ios, '14.0'

target 'Runner' do
  use_frameworks!
  
  # Firebase
  pod 'FirebaseAuth', '~> 11.6'
  pod 'FirebaseCore', '~> 11.6'
  
  # Google Sign-In
  pod 'GoogleSignIn', '~> 8.0'
end
```

- [ ] Run `cd ios && pod install`
- [ ] Verify all pods installed successfully

### 3.3 Info.plist Additions

- [ ] Add URL Schemes for Google Sign-In callback
- [ ] Add NSAppleSignInUsageDescription for Apple Sign-In

---

## 4. KMP Shared Module Setup

### 4.1 Dependencies (build.gradle.kts)

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("io.ktor:ktor-client-core:3.0.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
        }
    }
}
```

- [ ] Add Ktor HTTP client for API calls
- [ ] Add kotlinx.serialization for JSON parsing
- [ ] Verify multiplatform targets configured

### 4.2 KMP Module Structure

```
shared/
├── src/
│   ├── androidMain/
│   │   └── kotlin/          # Android-specific Firebase init
│   ├── iosMain/
│   │   └── kotlin/          # iOS-specific Firebase init
│   └── commonMain/
│       └── kotlin/
│           └── firebase/     # FirebaseAuthApi interface
├── build.gradle.kts
└── podspec                   # For iOS CocoaPods integration
```

- [ ] Create `FirebaseAuthApi` expect/actual pattern
- [ ] Implement Android actual (using firebase-auth-ktx)
- [ ] Implement iOS actual (using FirebaseAuth CocoaPods)
- [ ] Create `FirebaseAuthRepository` in shared module

---

## 5. Authentication Flow Implementation

### 5.1 Google Sign-In Flow

```
1. User taps "Sign in with Google"
2. App calls Google Sign-In SDK → receives ID token
3. App sends ID token to backend:
   POST /api/v1/auth/provider/verify
   { "firebase_token": "<google_id_token>", "provider": "google.com" }
4. Backend verifies token with Firebase Admin SDK
5. Backend returns:
   - New user: HTTP 201 + tokens
   - Existing user: HTTP 200 + tokens  
   - Link required: HTTP 200 + { link_required: true, email: "..." }
6. App stores access_token and refresh_token securely
```

- [ ] Implement Google Sign-In button UI
- [ ] Implement `signInWithGoogle()` suspend function
- [ ] Handle link_required response (prompt password)
- [ ] Store tokens in secure storage (Keychain/Keystore)

### 5.2 Apple Sign-In Flow

```
1. User taps "Sign in with Apple"
2. App calls ASAuthorizationController → receives ID token
3. App sends ID token to backend (same as Google)
4. Backend returns same response types
```

- [ ] Implement Apple Sign-In button UI (use Sign in with Apple button)
- [ ] Implement `signInWithApple()` suspend function
- [ ] Handle link_required response

### 5.3 Phone Sign-In Flow

```
1. User enters phone number
2. App calls firebase.auth().signInWithPhoneNumber()
3. User receives SMS with verification code
4. App submits code → receives Firebase phone token
5. App sends phone token to backend
```

- [ ] Implement phone input UI with country picker
- [ ] Implement `signInWithPhone(phone, verificationCode)` function
- [ ] Handle reCAPTCHA verification
- [ ] Handle link_required response

### 5.4 Account Linking Flow (Password Login)

```
1. User attempts Firebase sign-in
2. Backend returns { link_required: true, email: "..." }
3. App shows "Enter password" dialog
4. User enters existing password
5. App calls:
   POST /api/v1/auth/provider/link
   { "firebase_token": "...", "password": "..." }
6. Backend verifies password, links Firebase account, returns tokens
```

- [ ] Implement password input dialog
- [ ] Implement `linkFirebaseAccount(firebaseToken, password)` function
- [ ] Handle success → navigate to main screen
- [ ] Handle wrong password error

### 5.5 Token Management

- [ ] Store `access_token` (in-memory or encrypted storage)
- [ ] Store `refresh_token` (secure storage - Keychain/EncryptedSharedPreferences)
- [ ] Implement token refresh on 401 response
- [ ] Implement logout (clear tokens + Firebase signOut)

---

## 6. Backend API Integration

### 6.1 Endpoints Summary

| Endpoint                         | Method | Auth | Description                    |
|----------------------------------|--------|------|--------------------------------|
| `/api/v1/auth/provider/verify`   | POST   | None | Verify Firebase token          |
| `/api/v1/auth/provider/link`     | POST   | None | Link Firebase to existing user |
| `/api/v1/auth/provider/unlink`   | DELETE | JWT  | Unlink Firebase account        |
| `/api/v1/auth/provider/accounts` | GET    | JWT  | List linked Firebase accounts  |

### 6.2 Request/Response Types

**Verify Request:**

```json
{
  "firebase_token": "string (Google/Apple/Phone ID token)",
  "provider": "google.com | apple.com | phone"
}
```

**Verify Response (Success):**

```json
{
  "access_token": "string",
  "user": {
    "id": "uuid",
    "email": "string | null",
    "is_active": true,
    "is_verified": true,
    "roles": ["client"],
    "current_role": "client"
  }
}
```

**Verify Response (Link Required):**

```json
{
  "link_required": true,
  "email": "user@example.com",
  "firebase_uid": "firebase-uid",
  "provider": "google.com"
}
```

- [ ] Create `FirebaseAuthApi` interface in KMP shared module
- [ ] Implement HTTP client with proper error handling
- [ ] Map API responses to domain models

---

## 7. Testing Checklist

### 7.1 Unit Tests

- [ ] Test `FirebaseAuthRepository` methods with mock API
- [ ] Test token parsing logic
- [ ] Test error handling for invalid tokens

### 7.2 Integration Tests (Manual)

- [ ] Test Google Sign-In on Android device/emulator
- [ ] Test Apple Sign-In on iOS simulator/device
- [ ] Test Phone Sign-In with test number
- [ ] Test account linking flow
- [ ] Test token refresh
- [ ] Test logout and re-login

### 7.3 Security Tests

- [ ] Verify tokens not logged in plain text
- [ ] Verify refresh token stored securely
- [ ] Test invalid token rejection
- [ ] Test expired token handling

---

## 8. Pre-Production Checklist

- [ ] Remove all `mock:` token debug code from backend
- [ ] Configure Firebase App Check for production
- [ ] Set up Firebase Monitoring/Analytics
- [ ] Configure ProGuard/R8 rules for Firebase
- [ ] Test with production Firebase project (not emulator)
- [ ] Verify all 4 API endpoints work in staging

---

## 9. Post-Deployment Monitoring

- [ ] Monitor Firebase Auth dashboard for errors
- [ ] Monitor backend `/auth/provider/verify` endpoint latency
- [ ] Set up alerts for auth failure spikes
- [ ] Monitor linking/unlinking activity

---

## Appendix: Troubleshooting

### Common Issues

| Issue                            | Solution                                    |
|----------------------------------|---------------------------------------------|
| `INVALID_CREDENTIALS` on Android | Check SHA-1 fingerprint in Firebase Console |
| Apple Sign-In not working on iOS | Verify Services ID and Key configured       |
| Phone Auth quota exceeded        | Add test numbers in Firebase Console        |
| Token verification fails         | Check Firebase Admin SDK credentials        |
| `google-services.json` not found | Verify file path and build.gradle plugin    |

---

## Status

| Platform    | Status        | Notes                   |
|-------------|---------------|-------------------------|
| Backend API | ✅ Done        | 4 endpoints implemented |
| Android     | ⬜ Not Started |                         |
| iOS         | ⬜ Not Started |                         |
| KMP Shared  | ⬜ Not Started |                         |
| E2E Testing | ⬜ Not Started |                         |
