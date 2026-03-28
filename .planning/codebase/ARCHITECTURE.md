# Architecture

**Analysis Date:** 2026-03-28

## Pattern Overview

**Overall:** Clean Architecture with MVVM for Presentation Layer

**Key Characteristics:**
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

**Key Components:**
- `Screen` (Voyager Screen) - Composable UI
- `ScreenModel` - State holder (ViewModel equivalent)
- `*UiState` - Immutable state classes
- `*Intent` - User action events (handled in ScreenModel)

**Data Flow:**
```
UI Events (onClick, onTextChanged)
  -> ScreenModel (processes intent)
  -> UseCase (business logic)
  -> Repository (data access)
  -> StateFlow (new state)
  -> UI recomposition
```

### Domain Layer
- **Purpose:** Business logic and rules, independent of any platform
- **Location:** `feature/*/domain/`
- **Contains:** UseCases, Repository interfaces, Domain models
- **Depends on:** Only core abstractions, no concrete implementations
- **Used by:** Presentation layer

**Key Components:**
- `UseCase` - Single business operation
- `*Repository` - Interface defining data operations
- `*Model` - Domain entities (AuthState, Booking, etc.)
- `*Credentials` / `*Request` - Input models

**Rules:**
- No imports from `io.ktor.*`
- No Android/iOS platform imports
- No Compose UI code

### Data Layer
- **Purpose:** Concrete data operations and external integrations
- **Location:** `feature/*/data/`
- **Contains:** Repository implementations, DTOs, API services, mappers
- **Depends on:** Domain layer (implements interfaces), Core modules (network, storage)
- **Used by:** Domain layer (via injected implementations)

**Key Components:**
- `*RepositoryImpl` - Implements domain repository interface
- `*Dto` - Data transfer objects for API
- `*ApiService` - API definitions
- `*Mapper` - DTO to domain model conversion

## Module Architecture

### Core Modules
Shared infrastructure modules in `core/` directory:

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
Domain-specific modules in `feature/` directory:

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

**Core modules** can depend on other core modules but NOT on feature modules.

**Feature modules** can depend on core modules but NOT on other feature modules (prevents circular dependencies).

**Platform apps** depend on:
- Core modules
- Feature modules (via `app` aggregator)
- Platform-specific implementations

## Key Design Patterns

### Repository Pattern
Domain layer defines repository interfaces; Data layer provides implementations.

**Example:**
```
AuthRepository (interface in domain)
  <- AuthRepositoryImpl (in data)
```

### UseCase Pattern
Single-responsibility business operations. Each UseCase does one thing.

**Example:**
```
LoginUseCase
LogoutUseCase
ObserveAuthStateUseCase
RegisterUseCase
```

### State Management
ScreenModels hold UI state as `StateFlow<UiState>`. UI observes state and emits events.

**Example in** `feature/auth/presentation/screenmodel/LoginScreenModel.kt`:
```kotlin
class LoginScreenModel(...) : ScreenModel {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
}
```

### Dependency Injection
Koin for DI. Core module provides base modules; feature modules provide feature-specific modules.

**Initialization:**
```kotlin
// In Application
initializeKoin {
    modules(authModule, catalogModule, /* other feature modules */)
}
```

### Navigation
Voyager library for navigation with `Navigator` composable and `Screen` interface.

**Example:**
```kotlin
class LoginScreen : Screen {
    @Composable
    override fun Content() { ... }
}
```

## Cross-Cutting Concerns

**Error Handling:** `core:network/AppError` sealed class with typed errors (Unauthorized, NetworkError, ValidationError, etc.)

**Authentication:** `AuthState` sealed class (Guest, Authenticated) observed via `StateFlow`. `AuthGuard` interface for protected routes.

**Internationalization:** `I18nProvider` abstraction with `StringKey` enum for type-safe string keys.

---

*Architecture analysis: 2026-03-28*
