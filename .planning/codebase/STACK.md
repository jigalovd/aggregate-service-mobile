# Technology Stack

**Analysis Date:** 2026-03-28

## Languages

**Primary:**
- Kotlin 2.2.20 - All shared code (Android, iOS), core modules, and feature modules

**Secondary:**
- Swift - iOS-specific entry point (`iosApp/iosApp/AppDelegate.swift`)
- XML - Android resources (minimal)

## Runtime

**Environment:**
- Android: minSdk 24, targetSdk 34, compileSdk 36
- iOS: Xcode/Apple toolchain (Kotlin/Native compilation)
- Java: JDK 21 (required for Gradle build)

**Build System:**
- Gradle 8.13+ with Kotlin DSL
- Gradle Wrapper: `gradlew` / `gradlew.bat`
- Configuration cache: enabled

## Frameworks

**Core - UI:**
- Jetpack Compose 1.10.2 (BOM) - Primary UI framework for Android and iOS
- Compose Material3 - Material Design 3 components
- Compose Foundation & UI - Core compose libraries
- Compose Icons Extended - Icon set

**Core - Navigation:**
- Voyager 1.1.0-beta02 - Screen navigation with ScreenModel integration
  - `voyager-navigator` - Navigation container
  - `voyager-screenmodel` - ScreenModel support
  - `voyager-koin` - Koin integration
  - `voyager-transitions` - Transition animations

**Core - Dependency Injection:**
- Koin 4.2.0 - Dependency injection framework
  - `koin-core` - Core DI (shared)
  - `koin-android` - Android-specific DI
  - `koin-compose` - Compose integration for `koinInject()`

**Core - Networking:**
- Ktor 3.4.1 - HTTP client framework
  - `ktor-client-core` - Core client
  - `ktor-client-okhttp` - Android engine
  - `ktor-client-darwin` - iOS engine
  - `ktor-client-content-negotiation` - Content handling
  - `ktor-client-auth` - Authentication support
  - `ktor-client-logging` - HTTP logging
  - `ktor-serialization-kotlinx-json` - Kotlinx Serialization integration

**Core - Serialization:**
- Kotlinx Serialization 1.10.0 - JSON parsing
- Kotlinx DateTime 0.7.1-0.6.x-compat - Date/time handling

**Core - Local Storage:**
- AndroidX DataStore 1.2.1 - Preferences storage
  - `androidx-datastore-preferences` - Core
  - `androidx-datastore-preferences-android` - Android implementation

**Core - Image Loading:**
- Coil 3.4.0 - Image loading for Compose
  - `coil-compose` - Compose integration
  - `coil-network-ktor3` - Ktor network support
  - `coil-network-okhttp` - OkHttp network support

## Build Tools & Plugins

**Gradle Plugins (via convention plugins in `build-logic/`):**
- `kmp-base` - Kotlin Multiplatform base configuration (iOS targets: x64, arm64, simulator arm64)
- `kmp-android` - Android-specific KMP configuration
- `kmp-compose` - Jetpack Compose configuration
- `core-module` - Base configuration for core modules
- `feature-module` - Base configuration for feature modules
- `app-module` - Application module configuration

**Code Quality Tools:**
- Detekt 1.23.8 - Static code analysis (config: `config/quality/detekt.yml`)
- Ktlint 13.1.0 - Kotlin linting (config: `.editorconfig`)
- Kover 0.9.7 - Code coverage

**Testing:**
- JUnit 5.11.4 - Testing framework
- MockK 1.14.9 - Mocking library for Kotlin
- Turbine 1.2.1 - Flow testing library
- Robolectric 4.14.1 - Android unit testing
- Ktor Client Mock 3.4.1 - HTTP client mocking

## Project Structure

```
aggregate-service-mobile/
├── app/                    # Aggregator module (depends on all features)
├── androidApp/            # Android application entry point
├── iosApp/                # iOS application entry point
├── core/                  # Core modules
│   ├── network/          # Ktor HTTP client setup
│   ├── storage/         # DataStore preferences
│   ├── di/              # Koin DI configuration
│   ├── config/          # App configuration
│   ├── navigation/      # Navigation abstractions
│   ├── theme/           # Compose theming
│   ├── i18n/            # Internationalization
│   ├── utils/           # Utilities
│   ├── firebase-auth/   # Firebase authentication (Android)
│   ├── test-utils/      # Testing utilities
│   └── ui/              # Shared UI components
├── feature/              # Feature modules
│   ├── auth/            # Authentication
│   ├── catalog/         # Service catalog
│   ├── booking/        # Booking system
│   ├── services/       # Services feature
│   ├── profile/        # User profile
│   ├── favorites/      # Favorites management
│   ├── schedule/       # Scheduling
│   └── reviews/        # Reviews/ratings
├── build-logic/        # Gradle convention plugins
└── config/             # Build configurations
    ├── logging/        # Logback configuration
    └── quality/        # Detekt configuration
```

## Configuration Files

**Gradle:**
- `settings.gradle.kts` - Module includes
- `build.gradle.kts` - Root build configuration
- `gradle.properties` - Gradle and Kotlin settings
- `gradle/libs.versions.toml` - Version catalog

**Code Quality:**
- `config/quality/detekt.yml` - Detekt rules
- `.editorconfig` - Ktlint/editor settings

**Android:**
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

---

*Stack analysis: 2026-03-28*
