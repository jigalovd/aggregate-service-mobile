# Changelog - Aggregate Service Mobile

All notable changes to the Aggregate Service mobile application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added - 2026-03-19

#### Build System & Infrastructure
- ✅ **Gradle Version Catalog** (libs.versions.toml)
  - Kotlin 2.1.0
  - Compose Multiplatform 1.7.1
  - Ktor 3.0.3
  - Koin 4.0.2
  - Voyager 1.1.0-beta02
  - Kotlinx Coroutines 1.9.0
  - Kotlinx Serialization 1.7.3
  - DataStore 1.1.1
  - Coil 3.0.4
  - Detekt 1.23.6 (static analysis)
  - Ktlint 13.0.0 (linter + formatter)
  - Kover 0.8.3 (test coverage)
  - Mockk 1.13.9, Turbine 1.1.0 (testing)

- ✅ **Convention Plugins** (build-logic/src/main/kotlin/)
  - `kmp-base.gradle.kts` - Base KMP configuration
  - `kmp-android.gradle.kts` - Android target configuration (JVM 21, SDK 36)
  - `kmp-compose.gradle.kts` - Compose Multiplatform setup
  - `core-module.gradle.kts` - Core module convention
  - `feature-module.gradle.kts` - Feature module convention (Clean Architecture)
  - `app-module.gradle.kts` - Application module convention
  - `detekt-configuration.gradle.kts` - Detekt static analysis setup
  - `ktlint-configuration.gradle.kts` - Ktlint linter setup

- ✅ **Code Quality Infrastructure** (Day 1-2 Complete)
  - Detekt 1.23.6 with zero tolerance policy (maxIssues: 0)
  - Ktlint 13.0.0 with auto-formatting
  - .editorconfig with KMP-friendly rules
  - Aggregate tasks: detektAll, ktlintCheckAll, ktlintFormatAll, koverReportAll, koverVerifyAll
  - Configured for all subprojects via build.gradle.kts

- ✅ **Project Structure**
  - Multi-module KMP setup with Feature-First architecture
  - Android application module (`androidApp`)
  - Core modules: network, storage, theme, i18n, utils, navigation, di
  - Feature modules (planned): auth, catalog, booking, services, profile, favorites, reviews

#### Documentation
- ✅ **CODE_QUALITY_GUIDE.md** - Полный гайд по Detekt и Ktlint
  - Инструкции по запуску и конфигурации
  - Работа с baseline и Suppress
  - Интеграция с IDE и CI/CD
  - Troubleshooting и FAQ

#### Core Modules
- ✅ **:core:network** (60% complete)
  - Ktor 3.0.3 client setup
  - Platform-specific engines: OkHttp (Android), Darwin (iOS)
  - Content negotiation, auth, logging plugins
  - Kotlinx Serialization integration
  - Updated package: `com.aggregateservice.core.network`

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

#### Documentation (New)
- ✅ **Project Tracking**
  - `IMPLEMENTATION_STATUS.md` - Комплексный трекинг прогресса реализации (15% complete)
  - `CHANGELOG.md` - Changelog проекта с гидами миграции

- ✅ **Planning Documents**
  - `docs/plans/01-quality-infrastructure-and-cicd.md` - 2-недельный план внедрения Detekt, Ktlint, Kover, CI/CD
  - Покрывает: 6 фаз, 2700+ строк, детальный timeline, митигация рисков
  - Target: Detekt 0 warnings, Ktlint 100% compliance, Kover 60%+ coverage

- ✅ **Code Review**
  - `docs/reviews/2026-03-19-deep-code-review.md` - Deep Code Review (Zero Tolerance стандарт)
  - Build-Logic: 85/100 (✅ GOOD)
  - Overall Health: 65/100 (⚠️ NEEDS ATTENTION)
  - Выявлено 5 критических проблем: safeApiCall, Domain models, Repository interfaces, UseCase implementations, DI modules

#### Documentation Updates
- 🔄 `docs/README.md` - Обновлён: добавлены секции "Трекинг и статус", "Планирование и ревью", ссылка на TECHNOLOGY_STACK_ANALYSIS
- 🔄 `docs/IMPLEMENTATION_STATUS.md` - Обновлён: добавлены секции "Planning & Quality Infrastructure" и "Code Review Findings"
- 🔄 `docs/01_KMP_CMP_ANALYSIS.md` - Обновлён: версии библиотек синхронизированы с `libs.versions.toml`
- ✅ **NEW: Technology Stack Analysis**
  - `docs/TECHNOLOGY_STACK_ANALYSIS.md` - 🔬 Комплексный анализ технологического стека (500+ строк)
  - Краткий обзор всех технологий (14 tech stack items)
  - Детальный анализ плюсов/минусов для каждой технологии
  - Сравнительные таблицы: KMP vs Flutter vs RN, Ktor vs Retrofit, Koin vs Dagger
  - Альтернативы рассмотрены: почему НЕ выбрали Flutter, React Native, Native
  - Technology Maturity Assessment: все технологии production-ready (Low Risk)

#### Development Tools
- ✅ **Build Configuration**
  - Gradle 8.x with Kotlin DSL
  - Version catalog for dependency management
  - Convention plugins for consistent module configuration
  - JVM 21 target for Android

### Changed - 2026-03-19

#### Dependencies
- ⬆️ **Upgraded Kotlin**: 1.9.22 → 2.1.0
- ⬆️ **Upgraded Compose Multiplatform**: 1.6.0 → 1.7.1
- ⬆️ **Upgraded Ktor**: 2.3.8 → 3.0.3
- ⬆️ **Upgraded Koin**: 3.5.3 → 4.0.2
- ⬆️ **Upgraded Voyager**: 1.0.0 → 1.1.0-beta02
- ⬆️ **Upgraded Coroutines**: 1.8.0 → 1.9.0
- ⬆️ **Upgraded Serialization**: 1.6.2 → 1.7.3
- ⬆️ **Upgraded DataStore**: 1.1.0-beta01 → 1.1.1
- ⬆️ **Upgraded Coil**: 3.0.0-alpha04 → 3.0.4

#### Build Configuration
- 🔄 **Android Configuration**
  - Compile SDK: 34 → 36
  - JVM Target: 17 → 21
  - Namespace updated to `com.aggregateservice`

#### Network Layer
- 🔄 **Ktor Client Configuration**
  - Added `ktor-client-auth` dependency
  - Added `ktor-client-logging` dependency
  - Updated to Ktor 3.x APIs
  - Fixed Android client: `ktor.client.android` → `ktor-client-okhttp`

### Fixed - 2026-03-19

#### Build System
- 🐛 **Fixed NoSuchElementException** in feature-module.gradle.kts:32
  - Changed `libs.findLibrary("ktor.client.android").get()` to `libs.findLibrary("ktor-client-okhttp").get()`
  - Root cause: Incorrect library name in version catalog

#### Package Structure
- 🐛 **Removed deprecated file**: `core/network/src/commonMain/kotlin/com/aggregate/core/network/PlatformEngine.kt`
  - Replaced with proper Ktor platform-specific configuration

### Deprecated - 2026-03-19

#### Dependencies
- ⚠️ **Deprecated**: `ktor.client.android` (replaced by `ktor-client-okhttp`)
- ⚠️ **Deprecated**: `coil-network-ktor` (replaced by `coil-network-ktor3`)

---

## [0.1.0] - 2026-03-19

### Added
- 🎉 **Initial Project Setup**
  - KMP + CMP project initialized
  - Feature-First + Clean Architecture structure
  - Basic build system configuration
  - Convention plugins for module management

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
implementation("io.ktor:ktor-client-core:3.0.3")
implementation("io.ktor:ktor-client-auth:3.0.3")
implementation("io.ktor:ktor-client-logging:3.0.3")

// androidMain
implementation(libs.findLibrary("ktor-client-okhttp").get())

// iosMain
implementation(libs.findLibrary("ktor-client-darwin").get())
```

**Breaking Changes**:
- `ktor-client-android` → `ktor-client-okhttp`
- Add `ktor-client-auth` and `ktor-client-logging` explicitly
- Update package imports if using internal Ktor APIs

#### Koin 3.x → 4.x Migration

**Before** (Koin 3.x):
```kotlin
// Not yet implemented - placeholder for future migration
```

**After** (Koin 4.x):
```kotlin
// To be implemented when DI module is created
```

#### Kotlin 1.9.x → 2.1.0 Migration

**Key Changes**:
- JVM target 17 → 21
- Updated Compose Compiler plugin integration
- New K2 compiler (more stable, faster compilation)

**Gradle Configuration**:
```kotlin
// Before
jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)

// After
jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
```

---

## Future Plans

### [0.2.0] - Planned (Week 3-4)
- 🎯 Auth feature implementation
- 📦 Core:storage module (DataStore)
- 📦 Core:theme module (Material 3)
- 📦 Core:di module (Koin setup)

### [0.3.0] - Planned (Week 5-6)
- 🎯 Catalog feature implementation
- 🗺️ Maps integration (Google Maps)
- 📍 Location services

---

## Links

- [Repository](https://github.com/your-org/beauty-service-aggregator-mobile)
- [Issues](https://github.com/your-org/beauty-service-aggregator-mobile/issues)
- [Documentation](docs/)

---

**Changelog Version**: 1.0
**Last Updated**: 2026-03-19
**Maintained By**: Development Team
