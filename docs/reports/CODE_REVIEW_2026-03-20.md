# DEEP CODE REVIEW REPORT - KMP/CMP PROJECT

**Date:** 2026-03-20
**Reviewer:** Senior KMP/CMP Architect & Build Engineer
**Project:** aggregate-mobile
**Scope:** Full Codebase Audit (Gradle Build-Logic, Architecture, Compose Performance)

---

## EXECUTIVE SUMMARY

| Category | Status | Count |
|----------|--------|-------|
| CRITICAL (Blocking) | FAIL | 4 |
| HIGH (Urgent) | WARN | 3 |
| MEDIUM (Fixable) | INFO | 2 |
| COMPLIANT | PASS | - |

**Overall Status: NOT READY FOR MERGE**

---

## CRITICAL ISSUES (GRADLE / ARCHITECTURE)

### CRITICAL-01: Compose Dependency Access Violation in androidApp

**File:** `androidApp/build.gradle.kts:64-67`
**Risk Level:** BLOCKING
**Impact:** Compilation error: Unresolved reference `compose` in precompiled context

**Evidence:**
```kotlin
implementation(compose.runtime)
implementation(compose.foundation)
implementation(compose.material3)
implementation(compose.ui)
```

**Fix Required:**
```kotlin
// androidApp/build.gradle.kts - Replace lines 64-67 with:
val compose = project.extensions.getByType<org.jetbrains.compose.ComposeExtension>().dependencies
implementation(compose.runtime)
implementation(compose.foundation)
implementation(compose.material3)
implementation(compose.ui)
```

**Timeline:** IMMEDIATE

---

### CRITICAL-02: Version Catalog Mismatch with AGENTS.md Specification

**File:** `gradle/libs.versions.toml:3-6`
**Risk Level:** HIGH
**Impact:** Build inconsistency, potential incompatibility with Kotlin 2.2+ features

**Evidence:**
```toml
kotlin = "2.1.0"        # AGENTS.md requires 2.2.20
compose-plugin = "1.7.1" # AGENTS.md requires 1.10.2
agp = "8.7.3"           # AGENTS.md requires 8.12.0
```

**Fix Required:**
```toml
[versions]
kotlin = "2.2.20"
compose-plugin = "1.10.2"
compose-multiplatform = "1.10.2"
agp = "8.12.0"
```

**Timeline:** IMMEDIATE - Before any feature development

---

### CRITICAL-03: Missing `safeApiCall` Wrapper in Network Layer

**File:** `core/network/src/commonMain/kotlin/.../PlatformEngine.kt`
**Risk Level:** HIGH (Architecture Violation)
**Impact:** Ktor exceptions will leak to Domain/Presentation layers

**Evidence:** No `safeApiCall` wrapper exists. HttpClient created directly.

**AGENTS.md Requirement:** "Implement a global `safeApiCall { ... }` in `:core:network`. Catch Ktor exceptions here and map them to a domain `AppError` sealed interface."

**Fix Required - Create new file:**

```kotlin
// core/network/src/commonMain/kotlin/com/aggregateservice/core/network/SafeApiCall.kt
package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.utils.io.errors.IOException

sealed interface AppError {
    data class Network(val code: Int, val message: String) : AppError
    data class Server(val code: Int, val message: String) : AppError
    data class Unknown(val throwable: Throwable) : AppError
    data object Timeout : AppError
    data object NoConnection : AppError
}

suspend inline fun <reified T> safeApiCall(
    client: HttpClient,
    crossinline block: suspend HttpClient.() -> T
): Result<T> = runCatching {
    block(client)
}.recoverCatching { exception ->
    when (exception) {
        is ClientRequestException -> throw AppError.Network(
            exception.response.status.value,
            exception.message
        )
        is ServerResponseException -> throw AppError.Server(
            exception.response.status.value,
            exception.message
        )
        is SocketTimeoutException -> throw AppError.Timeout
        is IOException -> throw AppError.NoConnection
        else -> throw AppError.Unknown(exception)
    }
}
```

**Timeline:** IMMEDIATE

---

### CRITICAL-04: Hardcoded Plugin Versions in Convention Plugins

**File:** `build-logic/src/main/kotlin/detekt-configuration.gradle.kts:28`
**File:** `build-logic/src/main/kotlin/ktlint-configuration.gradle.kts:7`
**Risk Level:** HIGH
**Impact:** Version drift, maintenance nightmare

**Evidence:**
```kotlin
// detekt-configuration.gradle.kts:28
detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")  // HARDCODED

// ktlint-configuration.gradle.kts:7
version.set("13.0.0")  // HARDCODED
```

**Fix Required:**

```kotlin
// detekt-configuration.gradle.kts - Add at top:
val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

// Then replace line 28:
detektPlugins(libs.findLibrary("detekt-formatting").get())
```

```kotlin
// ktlint-configuration.gradle.kts - Add at top:
val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

// Then replace line 7:
version.set(libs.findVersion("ktlint").get().requiredVersion)
```

**Timeline:** IMMEDIATE

---

## PERFORMANCE / ARCHITECTURE ISSUES

### PERF-01: Feature Modules Duplicate Ktor Dependencies

**Files:**
- `feature/catalog/build.gradle.kts:12-14`
- `feature/auth/build.gradle.kts:12-14`
- `feature/booking/build.gradle.kts:12-14`
- `feature/profile/build.gradle.kts:12-14`
- `feature/favorites/build.gradle.kts:12-14`
- `feature/schedule/build.gradle.kts:12-14`
- `feature/reviews/build.gradle.kts:12-14`

**Risk Level:** MEDIUM
**Impact:** Redundant dependency resolution, potential version conflicts

**Evidence:**
```kotlin
// feature-module.gradle.kts already provides via api(project(":core:network"))
// which includes Ktor dependencies. Features re-declare:
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
```

**Fix Required:** Remove duplicate Ktor declarations from all feature modules. They're already available via `:core:network` api export.

**Timeline:** BEFORE FEATURE IMPLEMENTATION

---

### ARCH-01: Feature Modules Expose Core via `api()` - Tight Coupling

**File:** `build-logic/src/main/kotlin/feature-module.gradle.kts:14-20`
**Risk Level:** MEDIUM
**Impact:** Transitive dependency pollution, longer build times

**Evidence:**
```kotlin
api(project(":core:network"))
api(project(":core:storage"))
api(project(":core:theme"))
// ... etc
```

**Recommendation:** Features should use `implementation()` for core modules. Only the `:app` aggregator should use `api()` if needed for transitive exposure.

**Fix Required:**
```kotlin
// feature-module.gradle.kts - Replace api with implementation:
implementation(project(":core:network"))
implementation(project(":core:storage"))
implementation(project(":core:theme"))
implementation(project(":core:i18n"))
implementation(project(":core:utils"))
implementation(project(":core:navigation"))
implementation(project(":core:di"))
```

**Timeline:** BEFORE FEATURE IMPLEMENTATION

---

### ARCH-02: App Module Uses Wrong Plugin Pattern

**File:** `app/build.gradle.kts:1-3`
**Risk Level:** MEDIUM
**Impact:** Incorrect plugin hierarchy - app is treated as a feature

**Evidence:**
```kotlin
plugins {
    id("feature-module")  // App is NOT a feature!
}
```

**Fix Required:** Create dedicated `app-aggregator.gradle.kts` convention plugin or use simpler configuration:
```kotlin
// app/build.gradle.kts
plugins {
    id("kmp-compose")
    id("kmp-android")
    kotlin("plugin.serialization")
}
```

**Timeline:** BEFORE PRODUCTION

---

## COMPLIANT AREAS (VERIFIED)

| Check | Status | Evidence |
|-------|--------|----------|
| `maybeCreate()` usage | PASS | All build-logic scripts use `maybeCreate()` |
| `libs` accessor initialization | PASS | `feature-module.gradle.kts:1`, `core-module.gradle.kts:1` |
| `androidTarget()` placement | PASS | Only in `kmp-android.gradle.kts` and `app-module.gradle.kts` |
| JVM 21 enforcement | PASS | All android targets configured with `JVM_21` |
| Compose plugins | PASS | Both `org.jetbrains.compose` and `plugin.compose` applied |
| Version Catalog config | PASS | `build-logic/settings.gradle.kts:16-20` |
| `by getting` in build-logic | PASS | None found - correct! |
| `java.*` in commonMain | PASS | Only in platform-specific sources (`*androidMain`) |
| expect/actual pattern | PASS | Config and PlatformEngine properly implemented |

---

## ACTION ITEMS (PRIORITY ORDER)

| # | Action | File | Priority |
|---|--------|------|----------|
| 1 | Fix Compose accessor in androidApp | `androidApp/build.gradle.kts:64-67` | BLOCKING |
| 2 | Update version catalog to match AGENTS.md | `gradle/libs.versions.toml` | BLOCKING |
| 3 | Implement safeApiCall + AppError | `core/network` (new file) | BLOCKING |
| 4 | Use version catalog in detekt/ktlint configs | `build-logic/*.gradle.kts` | HIGH |
| 5 | Remove duplicate Ktor deps from features | `feature/*/build.gradle.kts` | MEDIUM |
| 6 | Change api->implementation for core modules | `feature-module.gradle.kts` | MEDIUM |
| 7 | Refactor app module plugin usage | `app/build.gradle.kts` | LOW |

---

## FINAL VERIFICATION

**Question:** *"If this KMP code is merged to main, will the Gradle build fail on CI, will Ktor leak into iOS-target, and will Compose UI lag at 60fps?"*

**Answer:** **NO, code is NOT ready for merge.**

**Blocking Issues:**
1. `androidApp/build.gradle.kts` **will break compilation** - `compose.runtime` unresolved
2. Any Ktor call **will leak exceptions** into Presentation - no `safeApiCall`
3. Kotlin/Compose versions **do not match** AGENTS.md specification

**Required Actions Before Merge:**
- [ ] Fix CRITICAL-01 (Compose accessor)
- [ ] Fix CRITICAL-02 (Version catalog)
- [ ] Fix CRITICAL-03 (safeApiCall implementation)
- [ ] Fix CRITICAL-04 (Hardcoded versions)

---

## FILES ANALYZED

### Build Logic (build-logic/)
- `settings.gradle.kts`
- `build.gradle.kts`
- `kmp-base.gradle.kts`
- `kmp-android.gradle.kts`
- `kmp-compose.gradle.kts`
- `feature-module.gradle.kts`
- `core-module.gradle.kts`
- `app-module.gradle.kts`
- `detekt-configuration.gradle.kts`
- `ktlint-configuration.gradle.kts`

### Core Modules
- `core/network/build.gradle.kts`
- `core/storage/build.gradle.kts`
- `core/config/build.gradle.kts`
- `core/navigation/build.gradle.kts`
- `core/di/build.gradle.kts`
- `core/theme/build.gradle.kts`
- `core/i18n/build.gradle.kts`
- `core/utils/build.gradle.kts`

### Feature Modules
- `feature/auth/build.gradle.kts`
- `feature/catalog/build.gradle.kts`
- `feature/booking/build.gradle.kts`
- `feature/profile/build.gradle.kts`
- `feature/favorites/build.gradle.kts`
- `feature/schedule/build.gradle.kts`
- `feature/reviews/build.gradle.kts`

### App & Configuration
- `app/build.gradle.kts`
- `androidApp/build.gradle.kts`
- `build.gradle.kts` (root)
- `settings.gradle.kts`
- `gradle/libs.versions.toml`

### Source Files
- `core/config/src/commonMain/.../AppConfig.kt`
- `core/config/src/androidMain/.../AppConfig.android.kt`
- `core/config/src/iosMain/.../AppConfig.ios.kt`
- `core/network/src/commonMain/.../PlatformEngine.kt`
- `core/network/src/androidMain/.../PlatformEngine.android.kt`
- `core/network/src/iosMain/.../PlatformEngine.ios.kt`

---

**Report Generated:** 2026-03-20
**Next Review:** After CRITICAL issues resolved
