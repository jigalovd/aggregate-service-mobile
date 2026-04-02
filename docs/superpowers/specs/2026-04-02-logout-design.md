# Logout Feature Design

**Date:** 2026-04-02
**Status:** Approved
**Feature:** Logout from Profile screen via Firebase Auth flow

---

## 1. Overview

Implement logout functionality for the aggregate-mobile app. User taps "Logout" on the Profile screen, Firebase session is terminated, backend logout endpoint is called, local tokens are cleared, and the user is navigated to the Catalog screen.

**Scope:** Firebase Auth flow only (Google Sign-In). Backend token-based auth is not yet implemented.

---

## 2. Architecture

### 2.1 Firebase Auth → Domain Layer

Current problem: `GoogleLoginScreen` (Presentation) calls `FirebaseAuthApi.signInWithGoogle()` directly, violating clean architecture.

**Solution:** New `SignInWithFirebaseUseCase` in Domain layer.

```kotlin
// feature/auth/src/commonMain/kotlin/.../domain/usecase/SignInWithFirebaseUseCase.kt
class SignInWithFirebaseUseCase(
    private val firebaseAuthApi: FirebaseAuthApi,
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<AuthState> {
        val tokenResult = firebaseAuthApi.signInWithGoogle()
        return tokenResult.fold(
            onSuccess = { token ->
                repository.verifyFirebaseToken(token.authProvider, token.idToken)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
```

`GoogleLoginScreen` changes:
- Remove direct `firebaseAuthApi.signInWithGoogle()` call
- Inject `SignInWithFirebaseUseCase` via Koin
- Call `signInWithFirebaseUseCase()` instead

**Why:** Firebase API stays in Infrastructure/Data layer, Domain layer orchestrates, Presentation only knows about UseCase.

### 2.2 LogoutUseCase (Domain Layer)

```kotlin
// feature/auth/src/commonMain/kotlin/.../domain/usecase/LogoutUseCase.kt
class LogoutUseCase(
    private val repository: AuthRepository,
    private val firebaseAuthApi: FirebaseAuthApi,
) {
    suspend operator fun invoke(): Result<Unit> {
        repository.logout()
        firebaseAuthApi.signOut()
        return Result.success(Unit)
    }
}
```

**Responsibilities:**
- Coordinate logout flow
- Call AuthRepository for backend + token cleanup
- Call FirebaseAuthApi for Firebase sign-out
- Return Result (currently always success, but structured for future error handling)

### 2.3 AuthRepository.logout() (Data Layer)

```kotlin
// feature/auth/src/commonMain/kotlin/.../data/repository/AuthRepositoryImpl.kt
override suspend fun logout() {
    // Call backend. Errors are ignored — client-side logout proceeds regardless.
    safeApiCall { httpClient.post("/api/v1/auth/logout") }
    tokenStorage.clearTokens()
    _authState.value = AuthState.Guest
}
```

**Why ignore backend errors:** If backend is unreachable, the session will expire on the server-side anyway. User experience is not affected — they are already logged out locally.

### 2.4 FirebaseAuthApi.signOut() (Infrastructure Layer)

- **Android:** Already implemented — `FirebaseAuth.getInstance().signOut()`
- **iOS:** Currently a no-op stub. Must be implemented using Firebase Auth SDK for iOS (`Auth.auth().signOut()`)

---

## 3. Profile Screen Changes

### 3.1 ProfileScreenModel

New parameters:
- `logoutUseCase: LogoutUseCase`
- `catalogNavigator: CatalogNavigator`
- `navigator: Navigator`

```kotlin
class ProfileScreenModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val catalogNavigator: CatalogNavigator,
    private val navigator: Navigator,
) : ScreenModel {

    fun logout() {
        screenModelScope.launch {
            logoutUseCase()
            navigator.replace(catalogNavigator.createCatalogScreen())
        }
    }
}
```

### 3.2 ProfileScreen UI

Button placement — inside `ViewProfileInfo`, below Edit button:

```kotlin
@Composable
fun ViewProfileInfo(
    profile: Profile,
    onEdit: () -> Unit,
    onLogout: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.MD)) {
        ProfileInfoRow(
            label = i18nProvider[StringKey.Profile.FULL_NAME],
            value = profile.fullName ?: i18nProvider[StringKey.Profile.NOT_SET],
        )
        Spacer(modifier = Modifier.height(Spacing.XXS))
        ProfileInfoRow(
            label = i18nProvider[StringKey.Profile.PHONE],
            value = profile.phone ?: i18nProvider[StringKey.Profile.NOT_SET],
        )
        Spacer(modifier = Modifier.height(Spacing.LG))
        OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
            Text(i18nProvider[StringKey.Profile.EDIT])
        }
        Spacer(modifier = Modifier.height(Spacing.SM))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text(i18nProvider[StringKey.Profile.LOGOUT])
        }
    }
}
```

ProfileScreen.Content() passes `screenModel::logout` as `onLogout`.

### 3.3 ProfileUiState

No changes to UiState model. Logout is a fire-and-forget action with no loading/success/error state in UI (error is silently ignored).

---

## 4. Dependency Injection

### 4.1 AuthModule (feature:auth)

```kotlin
val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<FirebaseAuthApi> { FirebaseAuthApiFactory.create() }
    singleOf(::InitializeAuthUseCase)
    singleOf(::LogoutUseCase)                        // Updated: now takes FirebaseAuthApi
    singleOf(::ObserveAuthStateUseCase)
    singleOf(::SignInWithFirebaseUseCase)            // NEW
    single<AuthStateProvider> { AuthStateProviderImpl(get()) }
    single<AuthNavigator> { AuthNavigatorImpl() }
}
```

**Note:** `LogoutUseCase` now requires `FirebaseAuthApi` in constructor. Koin resolves cross-feature dependencies automatically.

### 4.2 ProfileModule (feature:profile)

```kotlin
val profileModule = module {
    single { ProfileApiService(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factoryOf(::GetProfileUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::ProfileScreenModel)  // Updated: Koin resolves LogoutUseCase, CatalogNavigator automatically
}
```

`ProfileScreenModel` constructor needs `LogoutUseCase`, `CatalogNavigator`, `Navigator`. Koin resolves `LogoutUseCase` from `authModule` (same Koin context), `CatalogNavigator` from `core:navigation`.

---

## 5. Data Flow

```
User taps "Logout" (ProfileScreen)
  → ProfileScreenModel.logout()
    → LogoutUseCase()
      → AuthRepository.logout()
        → POST /api/v1/auth/logout  (errors ignored)
        → tokenStorage.clearTokens()
        → _authState.value = AuthState.Guest
      → FirebaseAuthApi.signOut()
    → navigator.replace(CatalogScreen)
  → AuthPromptDialog no longer shown (authState = Guest)
```

---

## 6. iOS Implementation

### FirebaseAuthApiIos.signOut()

**Current (stub):**
```kotlin
actual suspend fun signOut() = withContext(Dispatchers.Main) {
    // No-op for iOS stub
}
```

**Required:** Real implementation using Firebase Auth SDK:
```swift
import FirebaseAuth

Auth.auth().signOut { error in
    // handle error
}
```

This requires bridging Swift Firebase Auth to Kotlin Multiplatform (expect/actual pattern already in place).

---

## 7. Files to Change

### New Files
- `feature/auth/src/commonMain/kotlin/.../domain/usecase/SignInWithFirebaseUseCase.kt`

### Modified Files
- `feature/auth/src/commonMain/kotlin/.../domain/usecase/LogoutUseCase.kt` — add FirebaseAuthApi parameter
- `feature/auth/src/commonMain/kotlin/.../data/repository/AuthRepositoryImpl.kt` — add backend logout call
- `feature/auth/src/commonMain/kotlin/.../presentation/screen/GoogleLoginScreen.kt` — use SignInWithFirebaseUseCase
- `feature/auth/src/commonMain/kotlin/.../di/AuthModule.kt` — register SignInWithFirebaseUseCase, update LogoutUseCase
- `feature/profile/src/commonMain/kotlin/.../presentation/screen/ProfileScreen.kt` — add Logout button, pass onLogout
- `feature/profile/src/commonMain/kotlin/.../presentation/screenmodel/ProfileScreenModel.kt` — add logout(), inject dependencies
- `feature/profile/src/commonMain/kotlin/.../di/ProfileModule.kt` — ProfileScreenModel factory
- `core/firebase-auth/src/iosMain/kotlin/.../FirebaseAuthApiIos.kt` — implement signOut()

### Test Files
- `feature/auth/src/commonTest/kotlin/.../domain/usecase/SignInWithFirebaseUseCaseTest.kt` (new)
- `feature/auth/src/commonTest/kotlin/.../domain/usecase/LogoutUseCaseTest.kt` — update mocks
- `feature/profile/src/commonTest/kotlin/.../presentation/screenmodel/ProfileScreenModelTest.kt` — add logout test

---

## 8. Constraints

- Firebase Auth flow only — no email/password registration/login in scope
- No snackbar or error UI on logout failure
- iOS implementation required for complete feature
- Backend `/api/v1/auth/logout` must exist (already documented in API reference)
