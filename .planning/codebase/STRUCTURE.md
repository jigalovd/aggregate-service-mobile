# Codebase Structure

**Analysis Date:** 2026-03-28

## Directory Layout

```
aggregate-mobile/
├── .planning/codebase/     # GSD planning documents
├── androidApp/              # Android application entry point
│   └── src/
│       └── androidMain/kotlin/com/aggregateservice/androidApp/
├── app/                    # Aggregator module (no sources)
├── build-logic/            # Gradle build plugins
├── config/                 # Configuration files
│   ├── logging/
│   ├── quality/
│   └── secrets/
├── core/                   # Core shared modules
│   ├── config/
│   ├── di/
│   ├── firebase-auth/
│   ├── i18n/
│   ├── navigation/
│   ├── network/
│   ├── storage/
│   ├── test-utils/
│   ├── theme/
│   ├── ui/
│   └── utils/
├── docs/                   # Documentation
├── feature/                 # Feature modules
│   ├── auth/
│   ├── booking/
│   ├── catalog/
│   ├── favorites/
│   ├── profile/
│   ├── reviews/
│   ├── schedule/
│   └── services/
├── iosApp/                  # iOS application entry point
├── build.gradle.kts         # Root build configuration
└── settings.gradle.kts      # Module includes
```

## Directory Purposes

### Core Modules (`core/`)

Each core module follows KMP structure with `commonMain`, `androidMain`, `iosMain`:

**`core/config/`**
- Purpose: App configuration and platform-specific settings
- Key files: `AppConfig.kt`, `AppConfig.android.kt`, `AppConfig.ios.kt`

**`core/di/`**
- Purpose: Koin dependency injection setup
- Key files: `AppModule.kt`, `CoreModule.kt`, `AndroidCoreModule.kt`

**`core/firebase-auth/`**
- Purpose: Firebase authentication abstraction for Google, Apple, Phone auth
- Key files: `FirebaseAuthApi.kt`, `FirebaseAuthApiAndroid.kt`, `FirebaseAuthApiIos.kt`

**`core/i18n/`**
- Purpose: Internationalization support
- Key files: `I18nProvider.kt`, `Strings.kt`, `Locale.kt`

**`core/navigation/`**
- Purpose: Navigation infrastructure (Voyager-based)
- Key files: `Navigator.kt`, `AppNavHost.kt`, `Screen.kt`, `AuthGuard.kt`

**`core/network/`**
- Purpose: HTTP client, API error handling, interceptors
- Key files: `HttpClientFactory.kt`, `SafeApiCall.kt`, `AppError.kt`, `AuthInterceptor.kt`

**`core/storage/`**
- Purpose: Token and data persistence
- Key files: `TokenStorage.kt`, `TokenStorage.android.kt`, `TokenStorage.ios.kt`

**`core/theme/`**
- Purpose: Material3 theming
- Key files: `Theme.kt`, `AppColors.kt`, `Typography.kt`, `Dimensions.kt`, `Shape.kt`

**`core/ui/`**
- Purpose: Shared Compose UI components
- Key files: `foundation/button/AppButton.kt`, `foundation/input/AppTextField.kt`

**`core/utils/`**
- Purpose: Utility extensions and validators
- Key files: `ValidationUtils.kt`, `I18nExtensions.kt`

### Feature Modules (`feature/`)

Each feature module follows Clean Architecture with:

```
feature:<name>/
├── src/
│   ├── commonMain/kotlin/com/aggregateservice/feature/<name>/
│   │   ├── di/                    # Feature DI module
│   │   ├── domain/
│   │   │   ├── model/              # Domain models
│   │   │   ├── repository/         # Repository interfaces
│   │   │   └── usecase/            # UseCases
│   │   ├── data/
│   │   │   ├── api/                # API service definitions
│   │   │   ├── dto/                # Data transfer objects
│   │   │   ├── mapper/             # DTO to domain mappers
│   │   │   └── repository/        # Repository implementations
│   │   └── presentation/
│   │       ├── component/          # Compose UI components
│   │       ├── model/              # UI state models
│   │       ├── screen/             # Compose screens
│   │       └── screenmodel/        # ScreenModels
│   ├── commonTest/                 # Shared tests
│   ├── androidMain/                # Android-specific code
│   └── iosMain/                    # iOS-specific code
└── build.gradle.kts
```

**Example: `feature/auth/` structure:**
```
feature/auth/
├── src/
│   ├── commonMain/kotlin/com/aggregateservice/feature/auth/
│   │   ├── di/AuthModule.kt
│   │   ├── domain/
│   │   │   ├── model/AuthState.kt, LoginCredentials.kt, RegistrationRequest.kt
│   │   │   ├── repository/AuthRepository.kt
│   │   │   └── usecase/LoginUseCase.kt, LogoutUseCase.kt, etc.
│   │   ├── data/
│   │   │   ├── dto/AuthResponse.kt, LoginRequest.kt, etc.
│   │   │   └── repository/AuthRepositoryImpl.kt
│   │   └── presentation/
│   │       ├── component/AuthPromptDialog.kt, LinkAccountDialog.kt
│   │       ├── model/LoginUiState.kt, RegistrationUiState.kt
│   │       ├── screen/LoginScreen.kt, RegistrationScreen.kt
│   │       └── screenmodel/LoginScreenModel.kt, RegistrationScreenModel.kt
│   └── commonTest/... (tests)
└── build.gradle.kts
```

## Key File Locations

### Entry Points

| Platform | File |
|----------|------|
| Android | `androidApp/src/androidMain/kotlin/com/aggregateservice/androidApp/MainActivity.kt` |
| iOS | `iosApp/iosApp/AppDelegate.swift` |

### Configuration

| Purpose | File |
|---------|------|
| Root build | `build.gradle.kts` |
| Module includes | `settings.gradle.kts` |
| Android app | `androidApp/build.gradle.kts` |
| App module | `app/build.gradle.kts` |
| Build plugins | `build-logic/src/main/kotlin/*.gradle.kts` |

### Core Infrastructure

| Component | Location |
|-----------|----------|
| DI setup | `core/di/src/commonMain/kotlin/com/aggregateservice/core/di/` |
| Navigation | `core/navigation/src/commonMain/kotlin/com/aggregateservice/core/navigation/` |
| Network | `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/` |
| Theme | `core/theme/src/commonMain/kotlin/com/aggregateservice/core/theme/` |

## Naming Conventions

### Files
- Kotlin source files: `PascalCase.kt` (e.g., `LoginScreen.kt`, `AuthState.kt`)
- Test files: `PascalCaseTest.kt` or `PascalCaseSpec.kt`
- Build scripts: `kebab-case.gradle.kts`

### Directories
- Module directories: `kebab-case/` (e.g., `core:network`, `feature:auth`)
- Package directories: `lowercase/` matching package names
- Source sets: `camelCase/` (`commonMain`, `androidMain`, `iosMain`)

### Classes
- ScreenModels: `PascalCaseScreenModel.kt`
- Screens: `PascalCaseScreen.kt`
- UseCases: `PascalCaseUseCase.kt`
- Repositories: `PascalCaseRepository.kt` / `PascalCaseRepositoryImpl.kt`
- DTOs: `PascalCaseDto.kt`
- UI State: `PascalCaseUiState.kt`

## Where to Add New Code

### New Feature Module
1. Create `feature:<name>/` directory
2. Create `feature/<name>/build.gradle.kts` using `feature-module.gradle.kts` plugin
3. Add `include(":feature:<name>")` to `settings.gradle.kts`
4. Implement Clean Architecture layers:
   - `domain/model/` - Domain entities
   - `domain/repository/` - Repository interface
   - `domain/usecase/` - Business logic UseCases
   - `data/dto/` - API data transfer objects
   - `data/repository/` - Repository implementation
   - `presentation/screen/` - Compose UI
   - `presentation/screenmodel/` - State management
   - `presentation/component/` - Reusable UI components
   - `di/` - Koin module for the feature

### New Core Module
1. Create `core:<name>/` directory
2. Create `core/<name>/build.gradle.kts` using `core-module.gradle.kts` plugin
3. Add `include(":core:<name>")` to `settings.gradle.kts`
4. Implement module with `commonMain`, platform-specific sources

### New Screen (within existing feature)
1. Add screen file in `feature/<name>/presentation/screen/`
2. Add ScreenModel in `feature/<name>/presentation/screenmodel/`
3. Add UI state model in `feature/<name>/presentation/model/`
4. Register in feature DI module

### New UseCase
1. Add in `feature/<name>/domain/usecase/`
2. Inject repository interface in constructor
3. Register in feature DI module

## Special Directories

### `build-logic/`
- Purpose: Gradle build convention plugins
- Generated: No (committed)
- Contains: Reusable Gradle configurations for KMP modules

### `config/`
- Purpose: Quality and logging configurations
- Subdirectories:
  - `config/quality/` - Detekt, Ktlint rules
  - `config/logging/` - Logging configuration
  - `config/secrets/` - (gitignored) Local secrets

### `docs/`
- Purpose: Project documentation

### `.planning/`
- Purpose: GSD planning documents (where this file lives)
- Generated: Yes (by GSD commands)

---

*Structure analysis: 2026-03-28*
