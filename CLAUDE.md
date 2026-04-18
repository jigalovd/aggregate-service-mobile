# CLAUDE.md

### Architecture
The repo is a Kotlin-centric project focused on building a cross-platform application, likely leveraging Kotlin Multiplatform (KMP). With over 52,000 lines of code across 357 files, the project is structured to support both Android and potentially other platforms through shared logic. The codebase is actively maintained, with significant recent development activity in authentication and catalog modules, indicating a robust, feature-rich application. The project is primarily built using **Kotlin**, which accounts for over 80% of the codebase.
### Key Modules
| Module | Purpose | Owner |
|--------|---------|-------|
| `feature` | The feature module is a comprehensive collection of domain-specific feature sets | — |
| `core` | The core module serves as the foundational bedrock of the aggregateservice appli | — |
| `iosApp` | The iosApp module serves as the primary iOS client implementation for the projec | — |
| `androidApp` | The androidApp module serves as the primary Android-specific entry point for the | — |
| `app` | The app module serves as the primary entry point and navigation orchestration la | — |
### Tech Stack
**Languages:** Kotlin


**Infra:** Gradle### Hotspots (High Churn)
| File | Churn | 90d Commits | Owner |
|------|-------|-------------|-------|
| `core/i18n/src/commonMain/kotlin/com/aggregateservice/core/i18n/Strings.kt` | 99.7th %ile | 11 | Dima |
| `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/presentation/screen/ProviderDetailScreen.kt` | 99.4th %ile | 18 | Dima |
| `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt` | 99.3th %ile | 11 | Dima |
| `feature/catalog/src/commonTest/kotlin/com/aggregateservice/feature/catalog/presentation/screenmodel/ProviderDetailScreenModelTest.kt` | 99.1th %ile | 8 | Dima |
| `core/network/src/commonTest/kotlin/com/aggregateservice/core/network/SafeApiCallRealTest.kt` | 98.8th %ile | 6 | Dima |

## Project

**Aggregate Service Mobile**

Kotlin Multiplatform Mobile (KMP) приложение для бронирования услуг мастеров (b2c marketplace). Пользователи могут искать мастеров, просматривать профили, бронировать услуги и управлять своими записями. Поддерживается Android и iOS.

**Core Value:** Пользователь может найти и забронировать услугу мастера за 3 клика

### Constraints

- **[Platform]**: Android + iOS only, Kotlin Multiplatform — no Desktop/Web
- **[Language]**: Kotlin 2.2.20, Jetpack Compose for UI — no SwiftUI
- **[Auth]**: Firebase Auth — Google/Apple/Phone, no email/password yet
- **[API]**: REST backend — dev/staging URLs configured
- **[Timeline]**: MVP должен быть готов к production
- **[Quality]**: Zero tolerance на detekt/ktlint violations, 80% test coverage цель


## Technology Stack

## Languages
- Kotlin 2.2.20 - All shared code (Android, iOS), core modules, and feature modules
- Swift - iOS-specific entry point (`iosApp/iosApp/AppDelegate.swift`)
- XML - Android resources (minimal)
## Runtime
- Android: minSdk 24, targetSdk 34, compileSdk 36
- iOS: Xcode/Apple toolchain (Kotlin/Native compilation)
- Java: JDK 21 (required for Gradle build)
- Gradle 8.13+ with Kotlin DSL
- Gradle Wrapper: `gradlew` / `gradlew.bat`
- Configuration cache: enabled
## Frameworks
- Jetpack Compose 1.10.2 (BOM) - Primary UI framework for Android and iOS
- Compose Material3 - Material Design 3 components
- Compose Foundation & UI - Core compose libraries
- Compose Icons Extended - Icon set
- Voyager 1.1.0-beta02 - Screen navigation with ScreenModel integration
- Koin 4.2.0 - Dependency injection framework
- Ktor 3.4.1 - HTTP client framework
- Kotlinx Serialization 1.10.0 - JSON parsing
- Kotlinx DateTime 0.7.1-0.6.x-compat - Date/time handling
- AndroidX DataStore 1.2.1 - Preferences storage
- Coil 3.4.0 - Image loading for Compose
## Build Tools & Plugins
- `kmp-base` - Kotlin Multiplatform base configuration (iOS targets: x64, arm64, simulator arm64)
- `kmp-android` - Android-specific KMP configuration
- `kmp-compose` - Jetpack Compose configuration
- `core-module` - Base configuration for core modules
- `feature-module` - Base configuration for feature modules
- `app-module` - Application module configuration
- Detekt 1.23.8 - Static code analysis (config: `config/quality/detekt.yml`)
- Ktlint 13.1.0 - Kotlin linting (config: `.editorconfig`)
- Kover 0.9.7 - Code coverage
- JUnit 5.11.4 - Testing framework
- MockK 1.14.9 - Mocking library for Kotlin
- Turbine 1.2.1 - Flow testing library
- Robolectric 4.14.1 - Android unit testing
- Ktor Client Mock 3.4.1 - HTTP client mocking
## Project Structure
## Configuration Files
- `settings.gradle.kts` - Module includes
- `build.gradle.kts` - Root build configuration
- `gradle.properties` - Gradle and Kotlin settings
- `gradle/libs.versions.toml` - Version catalog
- `config/quality/detekt.yml` - Detekt rules
- `.editorconfig` - Ktlint/editor settings
- `local.properties` - SDK/NDK paths
- `secrets.properties*` - Local secrets (not committed)
## Key Dependencies Summary
| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.2.20 | Language |
| Jetpack Compose | 1.10.2 | UI Framework |
| Ktor | 3.4.1 | HTTP Client |
| Koin | 4.2.0 | Dependency Injection |
| Voyager | 1.1.0-beta02 | Navigation |
| Kotlinx Serialization | 1.10.0 | JSON Parsing |
| Coil | 3.4.0 | Image Loading |
| Firebase Auth | 23.2.0 | Authentication |
| DataStore | 1.2.1 | Local Storage |
| Detekt | 1.23.8 | Static Analysis |
| Ktlint | 13.1.0 | Linting |
| Kover | 0.9.7 | Code Coverage |


## Conventions

## Naming Conventions
- Kotlin source files: `PascalCase.kt` - e.g., `LoginScreenModel.kt`, `AuthRepository.kt`
- Kotlin script files: `kebab-case.kts`
- Test files: `<ClassUnderTest>Test.kt` - e.g., `LoginScreenModelTest.kt`
- `PascalCase` - e.g., `LoginScreenModel`, `AuthRepository`, `AppError`
- `camelCase` - e.g., `onLoginClick()`, `validate()`, `searchProviders()`
- `camelCase` - e.g., `uiState`, `isLoading`, `accessToken`
- `SCREAMING_SNAKE_CASE` - e.g., `MAX_RETRIES`, `NETWORK_TIMEOUT_MS`
- `com.aggregateservice.{feature|core}.{module}.{layer}` - e.g., `com.aggregateservice.feature.auth.domain.usecase`
- Suffix `Dto` optional, use `@Serializable` annotation
## Code Style
- Indent: 4 spaces (not tabs)
- Max line length: 120 characters (enforced by Detekt)
- Line endings: LF (Unix style)
- Trailing commas: enabled (`ij_kotlin_allow_trailing_comma = true`)
- Final newline: required
- Trim trailing whitespace: enabled
- NO wildcard imports
- Order:
- Prefer `val` over `var`
- Use `StateFlow<T>` for UI state (NOT `LiveData`)
- Use `Result<T>` for operations that can fail
- Prefer sealed interfaces for state/error modeling
- Use `object` for singletons, `class` for instances
## Linting & Static Analysis
- **Ktlint** (`org.jlleitschuh.gradle.ktlint`) - Code style enforcement
- **Detekt** (`io.gitlab.arturbosch.detekt`) - Static analysis
- Ktlint rules: `config/quality/.editorconfig` (supplements root `.editorconfig`)
- Detekt rules: `config/quality/detekt.yml`
- `MaxLineLength`: 120 characters
- `CyclomaticComplexMethod`: threshold 15
- `LongMethod`: threshold 60 lines
- `LongParameterList`: function 6, constructor 7
- `NestedBlockDepth`: threshold 4
- `TooManyFunctions`: 20 per file/class
- `WildcardImport`: prohibited (except `java.util.*`, `kotlinx.android.synthetic.*`)
- `MagicNumber`: ignore -1, 0, 1, 2
## Architecture Patterns
## Error Handling
- Use `AppError` sealed interface from `:core:network`
- NEVER leak Ktor exceptions to Domain/Presentation layers
- Use `safeApiCall { }` wrapper for all network calls
- Map errors to user messages in ScreenModel
## Dependency Rules
## Git Workflow
- Uses standard git commit message format (no specific convention enforced)
- Sample commit hooks exist in `.git/hooks/` but are not actively enforced
- `main` - production-ready code
- `develop` - integration branch


## Architecture

## Pattern Overview
- **Multi-module Kotlin Multiplatform Mobile (KMM)** project targeting Android and iOS
- **Clean Architecture** with strict layer separation: Presentation, Domain, Data
- **MVVM** pattern using Voyager ScreenModels for state management
- **Unidirectional Data Flow (UDF)** for UI state management
- **Dependency Injection** via Koin
- **Reactive Programming** using Kotlin Flows and StateFlow
## Layers
### Presentation Layer
- **Purpose:** UI rendering and user interaction handling
- **Location:** `feature/*/presentation/`
- **Contains:** Compose screens, ScreenModels, UI state models, components
- **Depends on:** Domain layer (UseCases, Repository interfaces)
- **Used by:** Platform entry points (MainActivity, AppDelegate)
- `Screen` (Voyager Screen) - Composable UI
- `ScreenModel` - State holder (ViewModel equivalent)
- `*UiState` - Immutable state classes
- `*Intent` - User action events (handled in ScreenModel)
```
```
### Domain Layer
- **Purpose:** Business logic and rules, independent of any platform
- **Location:** `feature/*/domain/`
- **Contains:** UseCases, Repository interfaces, Domain models
- **Depends on:** Only core abstractions, no concrete implementations
- **Used by:** Presentation layer
- `UseCase` - Single business operation
- `*Repository` - Interface defining data operations
- `*Model` - Domain entities (AuthState, Booking, etc.)
- `*Credentials` / `*Request` - Input models
- No imports from `io.ktor.*`
- No Android/iOS platform imports
- No Compose UI code
### Data Layer
- **Purpose:** Concrete data operations and external integrations
- **Location:** `feature/*/data/`
- **Contains:** Repository implementations, DTOs, API services, mappers
- **Depends on:** Domain layer (implements interfaces), Core modules (network, storage)
- **Used by:** Domain layer (via injected implementations)
- `*RepositoryImpl` - Implements domain repository interface
- `*Dto` - Data transfer objects for API
- `*ApiService` - API definitions
- `*Mapper` - DTO to domain model conversion
## Module Architecture
### Core Modules
| Module | Purpose |
|--------|---------|
| `core:network` | HTTP client (Ktor), API error handling, interceptors |
| `core:storage` | Token storage (DataStore), secure preferences |
| `core:di` | Koin DI configuration |
| `core:navigation` | Voyager navigation, AuthGuard, Navigator |
| `core:theme` | Material3 theme, colors, typography, shapes |
| `core:i18n` | Internationalization, string resources |
| `core:ui` | Shared UI components (buttons, inputs) |
| `core:utils` | Utility extensions, validators |
| `core:config` | App configuration, platform-specific settings |
| `core:firebase-auth` | Firebase authentication abstraction |
| `core:test-utils` | Shared test utilities |
### Feature Modules
| Module | Purpose |
|--------|---------|
| `feature:auth` | Authentication, registration, social login |
| `feature:catalog` | Service catalog browsing |
| `feature:booking` | Booking creation, management |
| `feature:services` | Service details and providers |
| `feature:profile` | User profile management |
| `feature:favorites` | Favorites/bookmarks |
| `feature:schedule` | Scheduling and availability |
| `feature:reviews` | Reviews and ratings |
### App Module
- **Location:** `app/`
- **Purpose:** Aggregator module that combines all features via `api` dependencies
- **Contains:** Only `build.gradle.kts` (no source code)
### Platform Applications
- **Android:** `androidApp/` - Android entry point (MainActivity, MainApplication)
- **iOS:** `iosApp/` - iOS entry point (AppDelegate)
## Dependency Rules
- Core modules
- Feature modules (via `app` aggregator)
- Platform-specific implementations
## Key Design Patterns
### Repository Pattern
```
```
### UseCase Pattern
```
```
### State Management
```kotlin
```
### Dependency Injection
```kotlin
```
### Navigation
```kotlin
```
## Cross-Cutting Concerns
