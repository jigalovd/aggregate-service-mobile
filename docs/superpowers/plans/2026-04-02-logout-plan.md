# Logout Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement logout from Profile screen — Firebase session termination, backend logout call, token cleanup, navigation to Catalog.

**Architecture:** Firebase Auth flow moved to Domain layer (SignInWithFirebaseUseCase). LogoutUseCase coordinates AuthRepository + FirebaseAuthApi. ProfileScreen gains logout button, calls useCase, navigates to CatalogScreen.

**Tech Stack:** Kotlin 2.2.20 / KMP / Koin DI / Voyager Navigation / Firebase Auth / Material3

---

## File Map

```
// NEW
feature/auth/src/commonMain/kotlin/.../domain/usecase/SignInWithFirebaseUseCase.kt

// MODIFY (1 task per logical component)
feature/auth/src/commonMain/kotlin/.../domain/usecase/LogoutUseCase.kt
feature/auth/src/commonMain/kotlin/.../data/repository/AuthRepositoryImpl.kt
feature/auth/src/commonMain/kotlin/.../presentation/screen/GoogleLoginScreen.kt
feature/auth/src/commonMain/kotlin/.../di/AuthModule.kt
feature/profile/src/commonMain/kotlin/.../presentation/screenmodel/ProfileScreenModel.kt
feature/profile/src/commonMain/kotlin/.../presentation/screen/ProfileScreen.kt
feature/profile/src/commonMain/kotlin/.../di/ProfileModule.kt
core/firebase-auth/src/iosMain/kotlin/.../FirebaseAuthApiIos.kt

// TESTS
feature/auth/src/commonTest/kotlin/.../domain/usecase/SignInWithFirebaseUseCaseTest.kt
feature/auth/src/commonTest/kotlin/.../domain/usecase/LogoutUseCaseTest.kt
feature/profile/src/commonTest/kotlin/.../presentation/screenmodel/ProfileScreenModelTest.kt
```

---

## Task 1: Create SignInWithFirebaseUseCase

**Files:**
- Create: `aggregate-mobile/feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/usecase/SignInWithFirebaseUseCase.kt`
- Test: `aggregate-mobile/feature/auth/src/commonTest/kotlin/com/aggregateservice/feature/auth/domain/usecase/SignInWithFirebaseUseCaseTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// SignInWithFirebaseUseCaseTest.kt
class SignInWithFirebaseUseCaseTest {
    private lateinit var useCase: SignInWithFirebaseUseCase
    private lateinit var mockFirebaseAuthApi: FirebaseAuthApi
    private lateinit var mockRepository: AuthRepository

    @BeforeEach
    fun setup() {
        mockFirebaseAuthApi = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        useCase = SignInWithFirebaseUseCase(mockFirebaseAuthApi, mockRepository)
    }

    @Test
    fun `should delegate sign-in to firebase and verify with repository`() = runTest {
        // given
        val firebaseToken = FirebaseToken("idToken", "google.com")
        val authState = AuthState.Authenticated("token", "userId", "email@test.com")
        coEvery { mockFirebaseAuthApi.signInWithGoogle() } returns Result.success(firebaseToken)
        coEvery { mockRepository.verifyFirebaseToken("google.com", "idToken") } returns Result.success(authState)

        // when
        val result = useCase()

        // then
        assertTrue(result.isSuccess)
        assertEquals(authState, result.getOrNull())
        coVerify { mockFirebaseAuthApi.signInWithGoogle() }
        coVerify { mockRepository.verifyFirebaseToken("google.com", "idToken") }
    }

    @Test
    fun `should propagate firebase error`() = runTest {
        // given
        coEvery { mockFirebaseAuthApi.signInWithGoogle() } returns Result.failure(Exception("Firebase error"))

        // when
        val result = useCase()

        // then
        assertTrue(result.isFailure)
        coVerify { mockRepository.verifyFirebaseToken(any(), any()) wasNot Called }
    }

    @Test
    fun `should propagate repository error`() = runTest {
        // given
        val firebaseToken = FirebaseToken("idToken", "google.com")
        coEvery { mockFirebaseAuthApi.signInWithGoogle() } returns Result.success(firebaseToken)
        coEvery { mockRepository.verifyFirebaseToken("google.com", "idToken") } returns Result.failure(AppError.Unauthorized())

        // when
        val result = useCase()

        // then
        assertTrue(result.isFailure)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:auth:testDebugUnitTest --tests "SignInWithFirebaseUseCaseTest" -v`
Expected: FAIL — "SignInWithFirebaseUseCase" not found

- [ ] **Step 3: Write minimal implementation**

```kotlin
// SignInWithFirebaseUseCase.kt
package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.repository.AuthRepository

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
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :feature:auth:testDebugUnitTest --tests "SignInWithFirebaseUseCaseTest" -v`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/usecase/SignInWithFirebaseUseCase.kt feature/auth/src/commonTest/kotlin/com/aggregateservice/feature/auth/domain/usecase/SignInWithFirebaseUseCaseTest.kt
git commit -m "feat(auth): add SignInWithFirebaseUseCase

SignInWithFirebaseUseCase orchestrates Firebase sign-in and backend
verification in Domain layer. FirebaseAuthApi stays in Infrastructure.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: Update LogoutUseCase — add FirebaseAuthApi dependency

**Files:**
- Modify: `aggregate-mobile/feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/usecase/LogoutUseCase.kt`
- Test: `aggregate-mobile/feature/auth/src/commonTest/kotlin/com/aggregateservice/feature/auth/domain/usecase/LogoutUseCaseTest.kt`

- [ ] **Step 1: Update LogoutUseCase constructor — add FirebaseAuthApi**

Read current `LogoutUseCase.kt` (lines 14-23):

```kotlin
// Current:
class LogoutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
```

Edit to:

```kotlin
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

- [ ] **Step 2: Update LogoutUseCaseTest — add FirebaseAuthApi mock**

Read current test (lines 21-50). The test uses `mockRepository`. Add `mockFirebaseAuthApi`:

```kotlin
private lateinit var logoutUseCase: LogoutUseCase
private lateinit var mockRepository: AuthRepository
private lateinit var mockFirebaseAuthApi: FirebaseAuthApi

@BeforeEach
fun setup() {
    mockRepository = mockk(relaxed = true)
    mockFirebaseAuthApi = mockk(relaxed = true)
    logoutUseCase = LogoutUseCase(mockRepository, mockFirebaseAuthApi)
}
```

Add test:

```kotlin
@Test
fun `should call firebase signOut after repository logout`() = runTest {
    // when
    logoutUseCase()

    // then
    coVerify { mockRepository.logout() }
    coVerify { mockFirebaseAuthApi.signOut() }
    coVerifyOrder {
        mockRepository.logout()
        mockFirebaseAuthApi.signOut()
    }
}
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :feature:auth:testDebugUnitTest --tests "LogoutUseCaseTest" -v`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/usecase/LogoutUseCase.kt feature/auth/src/commonTest/kotlin/com/aggregateservice/feature/auth/domain/usecase/LogoutUseCaseTest.kt
git commit -m "feat(auth): add FirebaseAuthApi to LogoutUseCase

LogoutUseCase now calls firebaseAuthApi.signOut() after repository.logout()
to terminate Firebase session.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: Update AuthRepositoryImpl — add backend logout call

**Files:**
- Modify: `aggregate-mobile/feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt`

- [ ] **Step 1: Read AuthRepositoryImpl — find logout() method**

Read lines 114-120 of `AuthRepositoryImpl.kt`:

```kotlin
override suspend fun logout() {
    // Очищаем токены
    tokenStorage.clearTokens()

    // Сбрасываем состояние в Guest
    _authState.value = AuthState.Guest
}
```

- [ ] **Step 2: Add backend logout call**

Edit `logout()` to:

```kotlin
override suspend fun logout() {
    // Call backend logout endpoint. Errors are ignored — client-side
    // logout proceeds regardless (session expires on server naturally).
    safeApiCall { httpClient.post("/api/v1/auth/logout") }
    tokenStorage.clearTokens()
    _authState.value = AuthState.Guest
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :feature:auth:compileKotlinMetadata -v`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt
git commit -m "feat(auth): call backend /auth/logout on logout()

Backend errors are silently ignored — client-side logout proceeds
regardless.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: Update GoogleLoginScreen — use SignInWithFirebaseUseCase

**Files:**
- Modify: `aggregate-mobile/feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/screen/GoogleLoginScreen.kt`

- [ ] **Step 1: Read GoogleLoginScreen**

Read full file. Find where `firebaseAuthApi.signInWithGoogle()` is called directly (around lines 51-90).

- [ ] **Step 2: Replace firebaseAuthApi injection with SignInWithFirebaseUseCase**

Change injection from:
```kotlin
val firebaseAuthApi: FirebaseAuthApi = koinInject()
```

To:
```kotlin
val signInWithFirebaseUseCase: SignInWithFirebaseUseCase = koinInject()
```

Also remove `FirebaseAuthApi` import if no longer used.

- [ ] **Step 3: Replace signInWithGoogle() call**

Find the block where `firebaseAuthApi.signInWithGoogle()` is called. Replace with:

```kotlin
signInWithFirebaseUseCase().fold(
    onSuccess = { authState ->
        // existing onSuccess logic
    },
    onFailure = { error ->
        // existing onFailure logic
    }
)
```

Keep all existing success/failure UI updates (loading state, error message, navigation) unchanged.

- [ ] **Step 4: Verify compilation**

Run: `./gradlew :feature:auth:compileKotlinMetadata -v`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/screen/GoogleLoginScreen.kt
git commit -m "refactor(auth): GoogleLoginScreen uses SignInWithFirebaseUseCase

FirebaseAuthApi removed from Presentation layer. Sign-in flow now
orchestrated through Domain layer.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: Update AuthModule — register SignInWithFirebaseUseCase

**Files:**
- Modify: `aggregate-mobile/feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/di/AuthModule.kt`

- [ ] **Step 1: Add import for SignInWithFirebaseUseCase**

```kotlin
import com.aggregateservice.feature.auth.domain.usecase.SignInWithFirebaseUseCase
```

- [ ] **Step 2: Register SignInWithFirebaseUseCase in authModule**

Read `AuthModule.kt`. Add after the existing `singleOf(::LogoutUseCase)` line:

```kotlin
singleOf(::SignInWithFirebaseUseCase)
```

The `LogoutUseCase` registration already exists — Koin will automatically resolve its new `FirebaseAuthApi` dependency from the `single<FirebaseAuthApi>` registered above it.

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :feature:auth:compileKotlinMetadata -v`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/di/AuthModule.kt
git commit -m "feat(auth): register SignInWithFirebaseUseCase in AuthModule

FirebaseAuthApi dependency resolved automatically for LogoutUseCase.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: Update ProfileScreenModel — add logout()

**Files:**
- Modify: `aggregate-mobile/feature/profile/src/commonMain/kotlin/com/aggregateservice/feature/profile/presentation/screenmodel/ProfileScreenModel.kt`

- [ ] **Step 1: Read ProfileScreenModel — identify constructor and dependencies**

Read full file. Note constructor (lines 28-31):

```kotlin
class ProfileScreenModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ScreenModel
```

- [ ] **Step 2: Add new constructor parameters**

Add imports:
```kotlin
import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.feature.auth.domain.usecase.LogoutUseCase
import cafe.adriel.voyager.core.screen.Screen
```

Update constructor:

```kotlin
class ProfileScreenModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val catalogNavigator: CatalogNavigator,
) : ScreenModel
```

- [ ] **Step 3: Add logout() function**

Add after `clearError()` method:

```kotlin
/**
 * Performs logout and navigates to CatalogScreen.
 */
fun logout(navigator: Navigator) {
    screenModelScope.launch {
        logoutUseCase()
        navigator.replace(catalogNavigator.createCatalogScreen())
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew :feature:profile:compileKotlinMetadata -v`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add feature/profile/src/commonMain/kotlin/com/aggregateservice/feature/profile/presentation/screenmodel/ProfileScreenModel.kt
git commit -m "feat(profile): ProfileScreenModel gains logout()

logoutUseCase clears Firebase + backend session. Navigator redirects
to CatalogScreen.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: Update ProfileScreen UI — add logout button

**Files:**
- Modify: `aggregate-mobile/feature/profile/src/commonMain/kotlin/com/aggregateservice/feature/profile/presentation/screen/ProfileScreen.kt`

- [ ] **Step 1: Read ViewProfileInfo — find button section**

Read `ProfileScreen.kt` lines 250-282. The `ViewProfileInfo` composable contains the Edit button around line 275.

Current structure:
```kotlin
OutlinedButton(
    onClick = onEdit,
    modifier = Modifier.fillMaxWidth(),
) {
    Text(i18nProvider[StringKey.Profile.EDIT])
}
```

- [ ] **Step 2: Add logout button parameter to ViewProfileInfo**

Update `ViewProfileInfo` signature from:
```kotlin
fun ViewProfileInfo(
    profile: Profile,
    onEdit: () -> Unit,
    i18nProvider: I18nProvider,
)
```

To:
```kotlin
fun ViewProfileInfo(
    profile: Profile,
    onEdit: () -> Unit,
    onLogout: () -> Unit,
    i18nProvider: I18nProvider,
)
```

- [ ] **Step 3: Add logout button below edit button**

Replace the existing edit button block with:

```kotlin
OutlinedButton(
    onClick = onEdit,
    modifier = Modifier.fillMaxWidth(),
) {
    Text(i18nProvider[StringKey.Profile.EDIT])
}
Spacer(modifier = Modifier.height(Spacing.SM))
OutlinedButton(
    onClick = onLogout,
    modifier = Modifier.fillMaxWidth(),
) {
    Text(i18nProvider[StringKey.Profile.LOGOUT])
}
```

- [ ] **Step 4: Update ProfileScreenContent — pass onLogout**

In `ProfileScreenContent`, find where `ViewProfileInfo` is called. Add `onLogout`:

```kotlin
ViewProfileInfo(
    profile = uiState.profile,
    onEdit = onStartEditing,
    onLogout = onLogout,
    i18nProvider = i18nProvider,
)
```

Also add `onLogout: () -> Unit` parameter to `ProfileScreenContent`.

- [ ] **Step 5: Update ProfileScreen.Content() — pass logout handler**

Read `ProfileScreen.Content()` (around line 63). Find where `ProfileScreenContent` is called and `onLogout` needs to be provided.

Add to the `ProfileScreenContent` call:
```kotlin
onLogout = {
    screenModel.logout(navigator)
},
```

Also update `ProfileScreenContent` signature and `ProfileScreenContent` call to pass `navigator`.

- [ ] **Step 6: Verify compilation**

Run: `./gradlew :feature:profile:compileKotlinMetadata -v`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add feature/profile/src/commonMain/kotlin/com/aggregateservice/feature/profile/presentation/screen/ProfileScreen.kt
git commit -m "feat(profile): add logout button to ProfileScreen

Logout button styled same as Edit (OutlinedButton, fillMaxWidth).
Stacked below Edit button in ViewProfileInfo section.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: Update ProfileModule — register LogoutUseCase for ProfileScreenModel

**Files:**
- Modify: `aggregate-mobile/feature/profile/src/commonMain/kotlin/com/aggregateservice/feature/profile/di/ProfileModule.kt`

- [ ] **Step 1: Read ProfileModule**

No changes needed if `ProfileScreenModel` is `factoryOf(::ProfileScreenModel)`. Koin resolves `logoutUseCase: LogoutUseCase` from `authModule` automatically since they share the same Koin context.

However, if ProfileScreenModel needs explicit construction (not factoryOf), add:
```kotlin
factoryOf(::ProfileScreenModel)
```

Verify that `ProfileScreenModel` constructor parameters (`LogoutUseCase`, `CatalogNavigator`) are available in the DI graph. They should be — `CatalogNavigator` comes from `core:navigation`, `LogoutUseCase` from `authModule`.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :feature:profile:compileKotlinMetadata -v`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit (only if changed)**

```bash
git add feature/profile/src/commonMain/kotlin/com/aggregateservice/feature/profile/di/ProfileModule.kt
git commit -m "chore(profile): ProfileModule DI ready for logout

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: Implement iOS FirebaseAuthApiIos.signOut()

**Files:**
- Modify: `aggregate-mobile/core/firebase-auth/src/iosMain/kotlin/com/aggregateservice/core/firebase/FirebaseAuthApiIos.kt`

- [ ] **Step 1: Read current iOS stub**

Read `FirebaseAuthApiIos.kt` lines 30-32:

```kotlin
actual suspend fun signOut() = withContext(Dispatchers.Main) {
    // No-op for iOS stub
}
```

- [ ] **Step 2: Implement using Firebase Auth SDK**

```kotlin
import platform.FirebaseAuth.AUTH_API
import platform.FirebaseAuth.FIRAuth

actual suspend fun signOut(): Result<Unit> = withContext(Dispatchers.Main) {
    try {
        FIRAuth.auth().signOut()
        Result.success(Unit)
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
```

**Note:** Verify actual Firebase Auth bridging API for Kotlin/Native. The exact API depends on how Firebase is bridged to Kotlin (may need `Auth.auth().signOut()` or similar). If the bridging is not available, use expect/actual pattern with a native Swift implementation called via Kotlin Native interop.

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :core:firebase-auth:compileKotlinMetadata -v`
Expected: BUILD SUCCESSFUL (or appropriate iOS interop errors if bridging not set up)

- [ ] **Step 4: Commit**

```bash
git add core/firebase-auth/src/iosMain/kotlin/com/aggregateservice/core/firebase/FirebaseAuthApiIos.kt
git commit -m "feat(ios): implement FirebaseAuthApiIos.signOut()

FirebaseAuthApiIos.signOut() now calls FIRAuth.auth().signOut().
Previously was a no-op stub.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 10: Add ProfileScreenModel logout test

**Files:**
- Modify: `aggregate-mobile/feature/profile/src/commonTest/kotlin/com/aggregateservice/feature/profile/presentation/screenmodel/ProfileScreenModelTest.kt`

- [ ] **Step 1: Read existing ProfileScreenModelTest**

- [ ] **Step 2: Add logout mock dependencies and test**

Add to constructor mock:
```kotlin
private lateinit var logoutUseCase: LogoutUseCase
private lateinit var catalogNavigator: CatalogNavigator
private lateinit var mockNavigator: Navigator

@BeforeEach
fun setup() {
    logoutUseCase = mockk(relaxed = true)
    catalogNavigator = mockk(relaxed = true)
    mockNavigator = mockk(relaxed = true)

    // Mock CatalogNavigator to return a Screen
    every { catalogNavigator.createCatalogScreen() } returns mockk(relaxed = true)

    screenModel = ProfileScreenModel(
        getProfileUseCase = mockGetProfile,
        updateProfileUseCase = mockUpdateProfile,
        logoutUseCase = logoutUseCase,
        catalogNavigator = catalogNavigator,
    )
}
```

Add test:
```kotlin
@Test
fun `logout should call useCase and navigate to catalog`() = runTest {
    // when
    screenModel.logout(mockNavigator)

    // then
    coVerify { logoutUseCase() }
    verify { mockNavigator.replace(any<Screen>()) }
}
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :feature:profile:testDebugUnitTest --tests "ProfileScreenModelTest" -v`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add feature/profile/src/commonTest/kotlin/com/aggregateservice/feature/profile/presentation/screenmodel/ProfileScreenModelTest.kt
git commit -m "test(profile): add logout test to ProfileScreenModelTest

logout() calls logoutUseCase() and replaces navigator with CatalogScreen.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 11: Final verification — all tests pass

- [ ] **Step 1: Run all auth tests**

```bash
./gradlew :feature:auth:testDebugUnitTest -v
```

- [ ] **Step 2: Run all profile tests**

```bash
./gradlew :feature:profile:testDebugUnitTest -v
```

- [ ] **Step 3: Compile all affected modules**

```bash
./gradlew :feature:auth:compileKotlinMetadata :feature:profile:compileKotlinMetadata :core:firebase-auth:compileKotlinMetadata -v
```

Expected: All BUILD SUCCESSFUL

- [ ] **Step 4: Final commit — if all clean**

```bash
git add -A
git commit -m "feat: implement logout feature end-to-end

- SignInWithFirebaseUseCase (Domain layer)
- LogoutUseCase with FirebaseAuthApi
- Backend /auth/logout on logout
- ProfileScreen logout button
- iOS FirebaseAuthApi.signOut()

All tests pass.

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Spec Coverage Checklist

- [x] SignInWithFirebaseUseCase — Task 1
- [x] LogoutUseCase with FirebaseAuthApi — Task 2
- [x] AuthRepository backend logout call — Task 3
- [x] GoogleLoginScreen → UseCase — Task 4
- [x] AuthModule registrations — Task 5
- [x] ProfileScreenModel logout() + deps — Task 6
- [x] ProfileScreen logout button UI — Task 7
- [x] ProfileModule DI — Task 8
- [x] iOS signOut() implementation — Task 9
- [x] ProfileScreenModelTest — Task 10
- [x] Final verification — Task 11
