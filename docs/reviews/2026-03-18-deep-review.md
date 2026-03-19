# Deep Code Review - KMP/CMP Project

**Date:** 2026-03-18
**Reviewer:** Senior KMP/CMP Architect
**Project:** aggregate-service-mobile
**Kotlin Version:** 2.2.20
**Compose Multiplatform:** 1.10.2
**AGP:** 8.12.0

---

## Summary

| Category | Issues Found | Critical | Blocking |
|----------|--------------|----------|----------|
| Build Logic & Gradle | 4 | 2 | 1 |
| Architecture Violations | 5 | 3 | 2 |
| Performance Issues | 2 | 1 | 0 |
| Concurrency Issues | 2 | 2 | 1 |

---

## Critical Issues (Gradle / Architecture)

### CRITICAL-1: Version Catalog Access without Extension API

**File:** `build-logic/src/main/kotlin/core-module.gradle.kts:1`
**Risk Level:** HIGH
**Impact:** Gradle sync crash due to incorrect plugin application order

**Evidence:**
```kotlin
val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")
```

**Problem:** This line is outside the `plugins {}` block or function. In precompiled scripts, access to VersionCatalogsExtension must be deferred or used inside configuration blocks.

**Fix:**
```kotlin
// build-logic/src/main/kotlin/core-module.gradle.kts
plugins {
    id("kmp-base")
    id("kmp-android")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            implementation(libs.findLibrary("kotlinx.coroutines.core").get())
        }
    }
}
```

---

### CRITICAL-2: Missing androidTarget() in kmp-compose.gradle.kts

**File:** `build-logic/src/main/kotlin/kmp-compose.gradle.kts:1-18`
**Risk Level:** BLOCKING
**Impact:** Build crash when connecting Compose module to Android target

**Evidence:**
```kotlin
plugins {
    id("kmp-base")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}
// NO androidTarget()!
```

**Problem:** The `kmp-compose` plugin doesn't declare `androidTarget()`, but is used in modules that should support Android (app, androidApp). When applying `kmp-compose` to a module without Android target, Compose won't be properly configured.

**Fix:**
```kotlin
// build-logic/src/main/kotlin/kmp-compose.gradle.kts
plugins {
    id("kmp-base")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

// Add conditional Android configuration
plugins.withId("com.android.library") {
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
        androidTarget {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            }
        }
    }
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            val compose = project.extensions.getByType<org.jetbrains.compose.ComposeExtension>().dependencies
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}
```

---

### CRITICAL-3: kmp-base.gradle.kts doesn't configure JVM target

**File:** `build-logic/src/main/kotlin/kmp-base.gradle.kts:1-16`
**Risk Level:** HIGH
**Impact:** JVM target mismatch between modules, potential compatibility errors

**Evidence:**
```kotlin
configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = project.name
            isStatic = true
        }
    }
    // MISSING: jvmToolchain(21) or compilerOptions { jvmTarget.set(JVM_21) }
}
```

**Problem:** Base plugin doesn't set JVM 21 by default. Each module must explicitly specify JVM target.

**Fix:**
```kotlin
// build-logic/src/main/kotlin/kmp-base.gradle.kts
plugins {
    kotlin("multiplatform")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    // JVM toolchain for all modules
    jvmToolchain(21)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = project.name
            isStatic = true
        }
    }
}
```

---

### CRITICAL-4: Duplicate Koin Initialization

**File 1:** `app/src/androidMain/kotlin/com/aggregateservice/app/Platform.android.kt:7-16`
**File 2:** `androidApp/src/androidMain/kotlin/com/aggregateservice/androidApp/AggregateApplication.kt:12-25`
**Risk Level:** HIGH
**Impact:** Application crash at startup - Koin already initialized

**Evidence:**
```kotlin
// Platform.android.kt:11-14
GlobalContext.startKoin {
    androidContext(this@BeautyApplication)
    modules(appModule)
}

// AggregateApplication.kt:15-23
startKoin {
    androidLogger(Level.DEBUG)
    androidContext(this@BeautyApplication)
    modules(platformCoreModule, coreModule, appModule)
}
```

**Problem:** Koin is initialized TWICE. This will throw `KoinApplicationAlreadyStartedException`.

**Fix:** Remove duplicate in `Platform.android.kt`:
```kotlin
// app/src/androidMain/kotlin/com/aggregateservice/app/Platform.android.kt
package com.aggregateservice.app

// DELETE THIS CLASS - it duplicates Koin initialization
// Initialization should be ONLY in androidApp/AggregateApplication.kt
```

---

## Architecture Violations (Clean Arch)

### ARCH-1: Domain Model contains @Serializable annotation

**File:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/model/AuthTokens.kt:5-10`
**File:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/model/User.kt:5-20`
**Pattern Broken:** Domain Layer Leak - kotlinx.serialization dependency

**Current:**
```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(...)
```

**Correct Pattern:** Domain models should be POJOs without serialization annotations. Serialization is an infrastructure concern of the Data layer.

**Fix:**
```kotlin
// feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/model/AuthTokens.kt
package com.aggregateservice.feature.auth.domain.model

// REMOVE @Serializable and kotlinx.serialization import
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

// feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/model/User.kt
package com.aggregateservice.feature.auth.domain.model

// REMOVE @Serializable
data class User(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val roles: Set<UserRole>,
    val isVerified: Boolean,
    val createdAt: String
)

enum class UserRole {
    CLIENT,
    PROVIDER
}
```

---

### ARCH-2: Result.Error loses original Throwable

**File:** `core/utils/src/commonMain/kotlin/com/aggregateservice/core/utils/Result.kt:5`
**Pattern Broken:** Loss of exception stacktrace

**Current:**
```kotlin
data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
```

**Problem:** In AuthRepositoryImpl, error is created as `Result.Error(e.message ?: "...")` — `cause` is not passed, stacktrace is lost.

**Evidence:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt:27`
```kotlin
} catch (e: Exception) {
    Result.Error(e.message ?: "Login failed")  // cause lost!
}
```

**Fix:**
```kotlin
// AuthRepositoryImpl.kt - ALL catch blocks
} catch (e: Exception) {
    Result.Error(e.message ?: "Login failed", e)  // Pass cause
}
```

---

### ARCH-3: AuthViewModel declared as factory instead of single

**File:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/di/AuthModule.kt:25`
**Pattern Broken:** Voyager ScreenModel should survive configuration changes

**Current:**
```kotlin
factory { AuthViewModel(get(), get(), get()) }
```

**Problem:** When using `factory`, ViewModel will be recreated on every access. Voyager ScreenModel should be `scoped` or managed through Voyager Koin integration.

**Fix:**
```kotlin
// feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/di/AuthModule.kt
val authModule = module {
    single { AuthApiService(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory { LoginUseCase(get(), get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get(), get()) }

    // REMOVE factory - Voyager manages ScreenModel lifecycle
    // Use getScreenModel() in Screen, not injection through Koin
}
```

---

### ARCH-4: UserRole duplicated in two places

**File 1:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/domain/model/User.kt:17-20`
**File 2:** `core/storage/src/commonMain/kotlin/com/aggregateservice/core/storage/UserPreferences.kt:10-14`
**Pattern Broken:** Duplication of business concept

**Evidence:**
```kotlin
// User.kt
enum class UserRole { CLIENT, PROVIDER }

// UserPreferences.kt
enum class UserRole { CLIENT, PROVIDER, BOTH }
```

**Problem:** Two different enums with different value sets (`BOTH` only in UserPreferences). This will lead to bugs during mapping.

**Fix:**
```kotlin
// core/user/src/commonMain/kotlin/com/aggregateservice/core/user/UserRole.kt
package com.aggregateservice.core.user

enum class UserRole {
    CLIENT,
    PROVIDER,
    BOTH;

    fun canActAsClient(): Boolean = this == CLIENT || this == BOTH
    fun canActAsProvider(): Boolean = this == PROVIDER || this == BOTH
}
```

---

### ARCH-5: AuthState and LoginState contain User (Domain model in Presentation)

**File:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/model/AuthState.kt:5-10`
**Pattern Broken:** Presentation depends on Domain

**Current:**
```kotlin
import com.aggregateservice.feature.auth.domain.model.User

data class AuthState(
    val user: User? = null,  // Domain model!
    ...
)
```

**Correct Pattern:** Presentation state should contain only UI data. If User is needed, create a `UserUi` model.

**Fix:**
```kotlin
// feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/model/UserUi.kt
package com.aggregateservice.feature.auth.presentation.model

data class UserUi(
    val id: String,
    val displayName: String,
    val email: String,
    val initials: String
)

// Mapper in Presentation layer
fun User.toUi(): UserUi = UserUi(
    id = id,
    displayName = listOfNotNull(firstName, lastName).joinToString(" "),
    email = email,
    initials = listOfNotNull(firstName?.first(), lastName?.first()).joinToString("")
)

// AuthState.kt
data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: UserUi? = null,  // UI model!
    val error: String? = null
)
```

---

## Performance Issues (Compose / Concurrency)

### PERF-1: runBlocking in AuthInterceptor blocks Main Thread

**File:** `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/AuthInterceptor.kt:12`
**Risk Level:** CRITICAL
**Impact:** Main Thread blocking, ANR on Android, freezing on iOS

**Evidence:**
```kotlin
class AuthInterceptor(
    private val tokenProvider: suspend () -> String?
) {
    fun install(client: HttpClient) {
        client.install(DefaultRequest) {
            val token = kotlinx.coroutines.runBlocking { tokenProvider() }  // BLOCKING!
            if (token != null) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}
```

**Problem:** `runBlocking` blocks the current thread. On Main Thread this causes ANR/freezing.

**Fix:**
```kotlin
// core/network/src/commonMain/kotlin/com/aggregateservice/core/network/AuthInterceptor.kt
package com.aggregateservice.core.network

import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.http.*

class AuthInterceptor(
    private val tokenProvider: suspend () -> String?
) {
    fun install(client: HttpClient) {
        client.install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenProvider()
                    if (token != null) {
                        BearerTokens(accessToken = token, refreshToken = "")
                    } else {
                        null
                    }
                }
            }
        }
    }
}
```

---

### PERF-2: LoginState not annotated as @Immutable

**File:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/model/AuthState.kt:12-19`
**Current:** Compose considers LoginState unstable due to nullable String fields
**Target:** Stable recomposition

**Optimization:**
```kotlin
// feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/presentation/model/AuthState.kt
package com.aggregateservice.feature.auth.presentation.model

import androidx.compose.runtime.Immutable
import com.aggregateservice.feature.auth.domain.model.User

@Immutable  // Add
data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@Immutable  // Add
data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@Immutable  // Add
data class RegisterState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

---

## Concurrency Issues

### CONC-1: Missing safeApiCall wrapper

**File:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt`
**Current:** Each method duplicates try-catch
**Target:** Centralized Ktor error handling
**Problem:** No distinction between network errors, parsing errors, timeouts, etc.

**Fix:**
```kotlin
// core/network/src/commonMain/kotlin/com/aggregateservice/core/network/SafeApiCall.kt
package com.aggregateservice.core.network

import com.aggregateservice.core.utils.Result
import io.ktor.client.plugins.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    try {
        Result.Success(block())
    } catch (e: ClientRequestException) {
        Result.Error("Client error: ${e.response.status}", e)
    } catch (e: ServerResponseException) {
        Result.Error("Server error: ${e.response.status}", e)
    } catch (e: HttpRequestTimeoutException) {
        Result.Error("Request timeout", e)
    } catch (e: IOException) {
        Result.Error("Network error: ${e.message}", e)
    } catch (e: Exception) {
        Result.Error("Unknown error: ${e.message}", e)
    }
}

// Usage in AuthRepositoryImpl:
override suspend fun login(email: String, password: String): Result<AuthTokens> =
    safeApiCall {
        val response = apiService.login(LoginRequest(email, password))
        response.toAuthTokens()
    }
```

---

### CONC-2: isAuthenticated() - synchronous call in coroutine without withContext

**File:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt:75-77`
**Problem:** Method `isAuthenticated()` doesn't use `withContext(Dispatchers.IO)`, but is marked as `suspend`

**Evidence:**
```kotlin
override suspend fun isAuthenticated(): Boolean {
    return tokenStorage.getAccessToken() != null  // Where is withContext?
}
```

**Fix:**
```kotlin
override suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
    tokenStorage.getAccessToken() != null
}
```

---

## Positive Aspects (Code Quality)

| Aspect | Status | Comment |
|--------|--------|---------|
| maybeCreate in sourceSets | CORRECT | All modules use `maybeCreate` |
| JVM 21 in androidTarget | CORRECT | `kmp-android.gradle.kts` and `app-module.gradle.kts` |
| UDF in Compose | CORRECT | LoginScreen uses StateFlow and callbacks |
| Domain isolation | MOSTLY | Except for @Serializable |
| DTO -> Domain mapping | CORRECT | `LoginResponse.toAuthTokens()`, `UserResponse.toUser()` |
| ScreenModel scope | CORRECT | Uses `screenModelScope` |

---

## Completion Checklist

- [x] All `.gradle.kts` files in `build-logic` checked for `maybeCreate`
- [x] DTO leak to Presentation layer checked
- [x] Business logic `try-catch` verified to be in Data layer
- [x] Version Catalog access syntax verified
- [x] All `import` in `commonMain` checked for absence of `java.*`
- [x] Exact `file:line` specified for each finding
- [x] Ready-to-use code snippets provided
- [x] Code corresponds to JVM 21

---

## Final Verdict

**Question:** *"If this KMP code is merged to main, will Gradle build crash on CI, will Ktor leak to iOS target, and will Compose UI lag at 60fps?"*

**Answer: YES, there are blocking issues:**

1. **Application crash at startup** - duplicate Koin initialization (CRITICAL-4)
2. **ANR/Freezing on load** - `runBlocking` in AuthInterceptor (PERF-1)
3. **Potential Gradle sync crash** - VersionCatalogsExtension access outside correct context (CRITICAL-1)

**Recommendation:** DO NOT MERGE without fixing CRITICAL-1, CRITICAL-4, and PERF-1.

---

## Priority Action Items

| Priority | Issue | Action |
|----------|-------|--------|
| P0 | CRITICAL-4 | Remove duplicate Koin initialization |
| P0 | PERF-1 | Replace runBlocking with Ktor Auth plugin |
| P0 | CRITICAL-1 | Fix VersionCatalog access pattern |
| P1 | CRITICAL-2 | Add conditional androidTarget to kmp-compose |
| P1 | CRITICAL-3 | Add jvmToolchain(21) to kmp-base |
| P1 | CONC-1 | Implement safeApiCall wrapper |
| P2 | ARCH-1 | Remove @Serializable from domain models |
| P2 | ARCH-2 | Pass cause to Result.Error |
| P2 | ARCH-4 | Unify UserRole enum |
| P3 | PERF-2 | Add @Immutable to state classes |
