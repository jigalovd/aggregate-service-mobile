# Changelog - Aggregate Service Mobile

All notable changes to the Aggregate Service mobile application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added - 2026-03-20

#### Auth Feature (Complete) 🎉

- ✅ **Feature: Auth - Domain Layer**
  - `AuthState` sealed class (Initial, Authenticated states)
  - `LoginCredentials` value object
  - `AuthRepository` interface
  - `LoginUseCase` with validation
  - `LogoutUseCase`
  - `ObserveAuthStateUseCase`

- ✅ **Feature: Auth - Data Layer**
  - `AuthRepositoryImpl` with Ktor + TokenStorage
  - `AuthResponse`, `RefreshTokenResponse`, `LoginRequest` DTOs
  - Integration with `safeApiCall` wrapper
  - Automatic token refresh flow
  - `authenticatedApiCall` helper function

- ✅ **Feature: Auth - Presentation Layer**
  - `LoginUiState` (UDF pattern)
  - `LoginScreenModel` (Voyager ScreenModel)
  - `LoginScreen` (Compose UI)
  - Email/Password validation with `EmailValidator`, `PasswordValidator`
  - Error handling with `AppError.toUserMessage()`
  - Loading states and form validation

- ✅ **Feature: Auth - DI Layer**
  - `AuthModule` (Koin module)
  - Repository, UseCases, ScreenModel bindings

#### Core Modules (Complete)

- ✅ **:core:network - 100%**
  - `SafeApiCall.kt` - Error handling wrapper with retry logic
  - `AppError.kt` - Sealed hierarchy for all error types
  - `AuthInterceptor.kt` - Token injection and refresh flow
  - `HttpClientFactory.kt` - Factory function for HttpClient
  - `PlatformEngine.kt` - expect/actual pattern (OkHttp/Darwin)
  - Full HTTP status handling (200, 201, 204, 400, 401, 403, 404, 409, 422, 423, 429, 500)
  - Rate limiting support (X-RateLimit-* headers)

- ✅ **:core:storage - 100%**
  - `TokenStorage` interface
  - `TokenStorageImpl` with DataStore Preferences
  - `createTokenStorage()` expect/actual (Android/iOS)
  - `isAuthenticated()` extension
  - `getAuthHeader()` extension

- ✅ **:core:utils - 100%**
  - `EmailValidator` with regex validation
  - `PasswordValidator` with configurable rules
  - `ValidationResult` sealed class
  - `isValidEmail()`, `isValidPassword()` extensions

- ✅ **:core:navigation - 100%**
  - `Screen` sealed interface (AuthScreen, MainScreen)
  - `Navigator` with Voyager setup
  - `AppNavHost` composable

- ✅ **:core:di - 100%**
  - `CoreModule` - HttpClient, AppConfig
  - `AndroidCoreModule` - TokenStorage with Context

#### Testing

- ✅ **Unit Tests**
  - `SafeApiCallTest` - Mock HTTP responses
  - `SafeApiCallRealTest` - Integration tests
  - `AuthRepositoryImplTest` - Repository tests

#### Documentation Updates

- 🔄 `IMPLEMENTATION_STATUS.md` - Updated to reflect 35% overall progress
- 🔄 `README.md` - Updated versions and structure
- 🔄 `NETWORK_LAYER.md` - Updated to 100% complete status

### Fixed - 2026-03-20

#### Build System

- 🐛 **Fixed duplicate plugin application** in `core-module.gradle.kts`
  - Removed redundant `id("kmp-base")` (already included via `kmp-android`)
  - Fixed potential build issues from plugin ordering

#### Code Quality

- 🐛 **Fixed duplicate `isLenient = true`** in `HttpClientFactory.kt`
- 🐛 **Removed unused import** `@Stable` in `LoginScreenModel.kt`

---

## [0.1.0] - 2026-03-19

### Added - 2026-03-19

#### Build System & Infrastructure

- ✅ **Gradle Version Catalog** (libs.versions.toml)
  - Kotlin 2.2.20
  - Compose Multiplatform 1.10.2
  - Ktor 3.4.1
  - Koin 4.2.0
  - Voyager 1.1.0-beta02
  - Kotlinx Coroutines 1.10.2
  - Kotlinx Serialization 1.10.0
  - DataStore 1.2.1
  - Coil 3.4.0
  - Detekt 1.23.8
  - Ktlint 13.1.0
  - Kover 0.9.7
  - Mockk 1.14.9, Turbine 1.2.1

- ✅ **Convention Plugins** (build-logic/src/main/kotlin/)
  - `kmp-base.gradle.kts` - Base KMP configuration
  - `kmp-android.gradle.kts` - Android target configuration (JVM 21, SDK 36)
  - `kmp-compose.gradle.kts` - Compose Multiplatform setup
  - `core-module.gradle.kts` - Core module convention
  - `feature-module.gradle.kts` - Feature module convention (Clean Architecture)
  - `app-module.gradle.kts` - Application module convention
  - `testing.gradle.kts` - Test dependencies configuration
  - `detekt-configuration.gradle.kts` - Detekt static analysis setup
  - `ktlint-configuration.gradle.kts` - Ktlint linter setup

- ✅ **Code Quality Infrastructure**
  - Detekt 1.23.8 with zero tolerance policy (maxIssues: 0)
  - Ktlint 13.1.0 with auto-formatting
  - .editorconfig with KMP-friendly rules
  - Aggregate tasks: detektAll, ktlintCheckAll, ktlintFormatAll, koverReportAll, koverVerifyAll

- ✅ **Project Structure**
  - Multi-module KMP setup with Feature-First architecture
  - Android application module (`androidApp`)
  - Core modules: network, storage, config, utils, navigation, di
  - Feature modules: auth, catalog, booking, profile, favorites, schedule, reviews

#### Documentation

- ✅ **CODE_QUALITY_GUIDE.md** - Полный гайд по Detekt и Ktlint
- ✅ **TECHNOLOGY_STACK_ANALYSIS.md** - Комплексный анализ технологического стека
- ✅ **IMPLEMENTATION_STATUS.md** - Комплексный трекинг прогресса реализации
- ✅ **NETWORK_LAYER.md** - Документация сетевого слоя
- ✅ **BUILD_LOGIC.md** - Документация convention plugins
- ✅ **CONFIG_MANAGEMENT.md** - Управление конфигурацией и секретами

#### Platform Configuration

- ✅ **Android Target**
  - Compile SDK: 36
  - Min SDK: 24
  - Target SDK: 34
  - JVM Target: 21
  - Namespace: `com.aggregateservice`

- 🟡 **iOS Target** (configured, not tested)
  - Darwin Ktor engine configured
  - Requires macOS environment for testing

### Changed - 2026-03-19

#### Dependencies

- ⬆️ **Upgraded Kotlin**: 1.9.22 → 2.2.20
- ⬆️ **Upgraded Compose Multiplatform**: 1.6.0 → 1.10.2
- ⬆️ **Upgraded Ktor**: 2.3.8 → 3.4.1
- ⬆️ **Upgraded Koin**: 3.5.3 → 4.2.0
- ⬆️ **Upgraded Voyager**: 1.0.0 → 1.1.0-beta02
- ⬆️ **Upgraded Coroutines**: 1.8.0 → 1.10.2
- ⬆️ **Upgraded Serialization**: 1.6.2 → 1.10.0
- ⬆️ **Upgraded DataStore**: 1.1.0-beta01 → 1.2.1
- ⬆️ **Upgraded Coil**: 3.0.0-alpha04 → 3.4.0

#### Build Configuration

- 🔄 **Android Configuration**
  - Compile SDK: 34 → 36
  - JVM Target: 17 → 21
  - Namespace updated to `com.aggregateservice`

### Fixed - 2026-03-19

#### Build System

- 🐛 **Fixed NoSuchElementException** in feature-module.gradle.kts:32
  - Changed `libs.findLibrary("ktor.client.android").get()` to `libs.findLibrary("ktor-client-okhttp").get()`
  - Root cause: Incorrect library name in version catalog

---

## [0.0.1] - 2026-03-18

### Added

- 🎉 **Initial Project Setup**
  - KMP + CMP project initialized
  - Feature-First + Clean Architecture structure
  - Basic build system configuration

### Technical Decisions

- **Architecture**: Feature-First + Clean Architecture
- **UI Framework**: Compose Multiplatform
- **Network**: Ktor 3.x
- **DI**: Koin 4.x
- **Navigation**: Voyager
- **Async**: Kotlinx Coroutines
- **Serialization**: Kotlinx Serialization

---

## Migration Guide

### From 0.1.0 to Unreleased (2026-03-20)

#### Auth Feature Integration

**New dependencies in feature modules:**
```kotlin
// feature/auth/build.gradle.kts
kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)
        }
    }
}
```

**Koin module registration:**
```kotlin
// In app module
initializeKoin {
    modules(coreModule, androidCoreModule, authModule)
}
```

### From 0.0.1 to 0.1.0 (2026-03-19)

#### Ktor 2.x → 3.x Migration

**Before** (Ktor 2.x):
```kotlin
// commonMain
implementation("io.ktor:ktor-client-core:2.3.8")
implementation("io.ktor:ktor-client-android:2.3.8")

// androidMain
implementation(libs.findLibrary("ktor.client.android").get())
```

**After** (Ktor 3.x):
```kotlin
// commonMain
implementation("io.ktor:ktor-client-core:3.4.1")
implementation("io.ktor:ktor-client-auth:3.4.1")
implementation("io.ktor:ktor-client-logging:3.4.1")

// androidMain
implementation(libs.findLibrary("ktor-client-okhttp").get())

// iosMain
implementation(libs.findLibrary("ktor-client-darwin").get())
```

**Breaking Changes**:
- `ktor-client-android` → `ktor-client-okhttp`
- Add `ktor-client-auth` and `ktor-client-logging` explicitly

---

## Future Plans

### [0.2.0] - Planned (Week 4-5)
- 🎯 Catalog feature implementation
- 🎨 Core:theme module (Material 3)
- 🌐 Core:i18n module (Localization)
- 🗺️ Maps integration (Google Maps)

### [0.3.0] - Planned (Week 5-6)
- 🎯 Booking feature implementation
- 📍 Location services

---

## Links

- [Repository](https://github.com/your-org/beauty-service-aggregator-mobile)
- [Issues](https://github.com/your-org/beauty-service-aggregator-mobile/issues)
- [Documentation](docs/)

---

**Changelog Version**: 2.0
**Last Updated**: 2026-03-20
**Maintained By**: Development Team
