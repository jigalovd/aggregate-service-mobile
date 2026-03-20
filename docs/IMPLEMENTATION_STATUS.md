# Статус реализации проекта - Aggregate Service

## 📊 Executive Summary

| Метрика | Текущее значение | Цель | Прогресс |
|---------|------------------|------|----------|
| **Общий прогресс** | 20% | 100% | ██░░░░░░░░ |
| **Core Infrastructure** | 90% | 100% | █████████░ |
| **Quality Infrastructure** | 80% | 100% | ████████░░ |
| **Features Implemented** | 0/7 | 7 | ░░░░░░░░░░ |
| **Test Coverage** | 0% | 80% | ░░░░░░░░░░ |
| **Documentation** | 90% | 100% | ████████░░ |

**Last Updated**: 2026-03-20
**Project Phase**: Initial Setup & Infrastructure
**Architecture**: Feature-First + Clean Architecture

---

## 🏗️ Core Infrastructure Status

### Build System & Convention Plugins

| Компонент | Статус | Прогресс | Заметки |
|-----------|--------|----------|---------|
| **Gradle Version Catalog** | ✅ Complete | 100% | `libs.versions.toml` настроен |
| **KMP Base Plugin** | ✅ Complete | 100% | `kmp-base.gradle.kts` |
| **KMP Android Plugin** | ✅ Complete | 100% | `kmp-android.gradle.kts` |
| **KMP Compose Plugin** | ✅ Complete | 100% | `kmp-compose.gradle.kts` |
| **Core Module Plugin** | ✅ Complete | 100% | `core-module.gradle.kts` |
| **Feature Module Plugin** | ✅ Complete | 100% | `feature-module.gradle.kts` |
| **App Module Plugin** | ✅ Complete | 100% | `app-module.gradle.kts` |
| **Detekt Configuration** | ✅ Complete | 100% | `detekt-configuration.gradle.kts` |
| **Ktlint Configuration** | ✅ Complete | 100% | `ktlint-configuration.gradle.kts` |

### Technology Stack Versions

```toml
[versions]
# ⬆️ UPDATED 2026-03-20
kotlin = "2.2.20"             # ✅ Latest (from 2.1.0)
agp = "8.12.3"                # ✅ Latest (from 8.7.3)
compose-multiplatform = "1.10.2"  # ✅ Latest (from 1.7.1)
ktor = "3.4.1"                # ✅ Latest (from 3.0.3)
koin = "4.2.0"                # ✅ Latest (from 4.0.2)
voyager = "1.0.1"             # ✅ Stable (from 1.1.0-beta02)
coroutines = "1.10.2"         # ✅ Latest (from 1.9.0)
serialization = "1.10.0"      # ✅ Latest (from 1.7.3)
datastore = "1.2.1"           # ✅ Latest (from 1.1.1)
coil = "3.4.0"                # ✅ Latest (from 3.0.4)

# Code Quality & Testing (UPDATED 2026-03-20)
detekt = "1.23.8"             # ✅ Latest (from 1.23.6)
ktlint = "13.1.0"             # ✅ Latest (from 13.0.0)
kover = "0.9.7"               # ✅ Latest (from 0.8.3)
mockk = "1.14.9"              # ✅ Latest (from 1.13.9)
turbine = "1.2.1"             # ✅ Latest (from 1.1.0)

# Build System
gradle = "8.14.4"             # ✅ Latest wrapper
```

### Core Modules Status

| Модуль | Статус | Прогресс | Описание |
|--------|--------|----------|----------|
| **:core:network** | 🟡 In Progress | 60% | Ktor 3.4.1, OkHttp/Darwin engines, ContentNegotiation, Logging, Auth, Config integration. **ОТсутствует:** safeApiCall, AppError, Auth Interceptor |
| **:core:config** | 🟢 Complete | 100% | Expect/actual pattern, BuildConfig (Android), Info.plist (iOS), Secrets management |
| **:core:storage** | ⚠️ Planned | 0% | DataStore Preferences (приоритет: CRITICAL для TokenStorage) |
| **:core:theme** | ⚪ Not Started | 0% | Material 3 Theme |
| **:core:i18n** | ⚠️ Planned | 0% | Localization (ru, he, en) + i18n helper для flatten `_i18n` полей |
| **:core:utils** | ⚠️ Planned | 0% | Extensions, Validators, i18n helpers |
| **:core:navigation** | ⚪ Not Started | 0% | Voyager setup |
| **:core:di** | ⚪ Not Started | 0% | Koin modules |

### Platform Configuration

| Платформа | Статус | Конфигурация |
|-----------|--------|--------------|
| **Android** | ✅ Configured | compileSdk 36, minSdk 24, targetSdk 34, JVM 21 |
| **iOS** | 🟡 Pending | Xcode setup required |
| **Desktop/Web** | ⚪ Not Planned | Scope: Mobile only |

---

## 📋 Planning & Quality Infrastructure

### Planning Documents Status

| Артефакт | Статус | Прогресс | Описание |
|----------|--------|----------|----------|
| **Quality Infrastructure Plan** | ✅ Created | 100% | [`docs/plans/01-quality-infrastructure-and-cicd.md`](plans/01-quality-infrastructure-and-cicd.md) - 2-week план для Detekt, Ktlint, Kover, CI/CD |
| **Deep Code Review** | ✅ Complete | 100% | [`docs/reviews/2026-03-19-deep-code-review.md`](reviews/2026-03-19-deep-code-review.md) - Zero Tolerance аудит кода |
| **Changelog** | ✅ Complete | 100% | [`CHANGELOG.md`](../CHANGELOG.md) - Changelog проекта и миграции |
| **Code Quality Guide** | ✅ Complete | 100% | [`docs/CODE_QUALITY_GUIDE.md`](CODE_QUALITY_GUIDE.md) - Полный гайд по Detekt и Ktlint |

### Code Quality Tools Status (Day 1-2 Complete / Target: Week 1-2)

| Инструмент | Статус | Прогресс | Заметки |
|------------|--------|----------|---------|
| **Detekt** | ✅ Configured | 100% | Static analysis: `.detekt/config.yml` (zero tolerance: maxIssues: 0) |
| **Ktlint** | ✅ Configured | 100% | Linter + Formatter: `.editorconfig` + `ktlint-configuration.gradle.kts` |
| **Kover** | ✅ Configured | 100% | Test coverage reporting настроен (target: 60%+) |
| **CI/CD** | ⚪ Not Started | 0% | GitHub Actions pipeline (планируется в Week 2) |
| **Unit Tests** | ⚪ Not Started | 0% | Тесты для network слоя (планируются в Week 1) |

### Code Quality Infrastructure Implementation Details

**Day 1-2 Complete (2026-03-19):**

| Компонент | Статус | Детали |
|-----------|--------|---------|
| **Detekt Configuration** | ✅ Complete | - `.detekt/config.yml` (600+ lines)<br>- Zero tolerance policy: maxIssues: 0<br>- All rule sets configured<br>- Auto-correction enabled for formatting |
| **Ktlint Configuration** | ✅ Complete | - `.editorconfig` with KMP-friendly rules<br>- `ktlint-configuration.gradle.kts` (version 13.0.0)<br>- Disabled rules for Compose Resources<br>- Aggregate tasks: `ktlintCheckAll`, `ktlintFormatAll` |
| **Convention Plugins** | ✅ Complete | - `detekt-configuration.gradle.kts`<br>- `ktlint-configuration.gradle.kts`<br>- Applied to all subprojects via `build.gradle.kts` |
| **Aggregate Tasks** | ✅ Complete | - `detektAll` - Check all modules<br>- `ktlintCheckAll` - Lint all modules<br>- `ktlintFormatAll` - Format all modules<br>- `koverReportAll` - Generate coverage reports<br>- `koverVerifyAll` - Verify coverage thresholds |
| **Build-Logic Integration** | ✅ Complete | - Updated `build-logic/build.gradle.kts` with quality plugins<br>- Quality plugins added to `libs.versions.toml`<br>- Gradle Plugin Portal added to `settings.gradle.kts` |
| **Network Layer** | ✅ Complete | - `PlatformEngine.kt` - expect/actual pattern for KMP<br>- `createHttpClient()` - factory function with full Ktor setup<br>- OkHttp engine (Android) with 30s timeout<br>- Darwin engine (iOS) configured<br>- ContentNegotiation with kotlinx.serialization<br>- HTTP timeout configuration (30s)<br>- Logging plugin with custom logger<br>- Default request configuration (baseUrl, JSON content type)<br>- **NEW**: Config integration (automatic API key injection) |
| **Config Management** | ✅ Complete | - `:core:config` module created with expect/actual pattern<br>- `AppConfig.kt` - expect declaration with all config properties<br>- `AppConfig.android.kt` - BuildConfig-based implementation<br>- `AppConfig.ios.kt` - Info.plist + environment variables implementation<br>- `secrets.properties.template` - template for local development<br>- `.gitignore` updated to exclude secret files<br>- `build.gradle.kts` - secrets loading from properties files<br>- Documentation: `CONFIG_MANAGEMENT.md` created |

**Quality Metrics (Current):**

| Метрика | Текущее значение | Цель | Статус |
|---------|------------------|------|--------|
| **Detekt Issues** | 0 | 0 (zero tolerance) | ✅ PASS |
| **Ktlint Violations** | 0 | 0 (all fixable) | ✅ PASS |
| **Kover Coverage** | 0% | 60%+ | ⏳ PENDING |
| **Test Count** | 0 | 100+ | ⏳ PENDING |

### Code Review Findings (2026-03-19)

| Категория | Score | Статус | Critical Issues |
|-----------|-------|--------|-----------------|
| **Build-Logic & Gradle** | 85/100 | ✅ GOOD | 0 critical, 3 minor |
| **Clean Architecture** | N/A | ⚪ EMPTY | No feature code yet |
| **Ktor/Network Layer** | 90/100 | ✅ EXCELLENT | Platform-specific engines configured, factory pattern implemented |
| **Compose/UI** | N/A | ⚪ EMPTY | No UI code yet |
| **Concurrency** | N/A | ⚪ EMPTY | No async code yet |

**КРИТИЧЕСКИЕ ПРОБЛЕМЫ (обновлено 2026-03-20 после анализа BACKEND_API_REFERENCE.md):**
1. ✅ РЕШЕНО: Network layer基础结构完成 (PlatformEngine, createHttpClient)
2. ❌ КРИТИЧНО: Нет `safeApiCall` wrapper для обработки ошибок (4xx, 5xx, rate limiting)
3. ❌ КРИТИЧНО: Нет `AppError` sealed hierarchy (NetworkError, AccountLocked, ValidationError, SlotNotAvailable, RateLimitExceeded)
4. ❌ КРИТИЧНО: Нет Auth Interceptor с автоматическим refresh token flow
5. ❌ КРИТИЧНО: Нет TokenStorage (DataStore) для access/refresh tokens
6. ❌ ВАЖНО: Нет i18n helper для flatten `_i18n` полей (title_i18n: Map<String, String> → title: String)
7. ❌ ВАЖНО: Нет Domain Models (User с roles: List<String>, AuthTokens, Session)
8. ❌ ВАЖНО: Нет Repository interfaces
9. ❌ ВАЖНО: Нет UseCase implementations (SwitchRoleUseCase для multi-role users)

**ПЛАН ИСПРАВЛЕНИЯ:**
1. **Priority 1 (CRITICAL):** safeApiCall → AppError → Auth Interceptor → TokenStorage
2. **Priority 2 (IMPORTANT):** i18n helper → Domain Models (User с multi-role) → UseCases
3. **Priority 3 (DESIRABLE):** Repository interfaces → API Services (Auth, Catalog, Booking)

См. также: [`docs/BACKEND_API_REFERENCE.md`](BACKEND_API_REFERENCE.md) - детальное описание API

### Documentation Coverage

| Категория | Статус | Coverage | Заметки |
|-----------|--------|----------|---------|
| **Architecture** | ✅ Good | 85% | KMP/CMP анализ, Design System, UX Guidelines |
| **Planning** | ✅ Excellent | 100% | Quality Infrastructure план создан |
| **Reviews** | ✅ Excellent | 100% | Deep Code Review проведён |
| **API Docs** | ✅ Complete | 100% | BACKEND_API_REFERENCE.md добавлен (2026-03-20) |
| **KDoc** | ⚪ Missing | 0% | Требуется при написании кода |

---

## 🎯 Feature Implementation Status

### Feature Matrix

| Эпик | Feature | Domain | Data | Presentation | UI | 总 Progress |
|------|---------|--------|------|--------------|-----|-------------|
| **E1** | Auth | ⚪ 0% | ⚪ 0% | ⚪ 0% | ⚪ 0% | ░░░░░░░░░░ 0% |
| **E2** | Catalog | ⚪ 0% | ⚪ 0% | ⚪ 0% | ⚪ 0% | ░░░░░░░░░░ 0% |
| **E3** | Booking | ⚪ 0% | ⚪ 0% | ⚪ 0% | ⚪ 0% | ░░░░░░░░░░ 0% |
| **E4** | Services | ⚪ 0% | ⚪ 0% | ⚪ 0% | ⚪ 0% | ░░░░░░░░░░ 0% |
| **E5** | Profile | ⚪ 0% | ⚪ 0% | ⚪ 0% | ⚪ 0% | ░░░░░░░░░░ 0% |
| **E6** | Favorites | ⚪ 0% | ⚪ 0% | ⚪ 0% | ⚪ 0% | ░░░░░░░░░░ 0% |
| **E7** | Reviews | ⚪ 0% | ⚪ 0% | ⚪ 0% | ⚪ 0% | ░░░░░░░░░░ 0% |

### Feature Details

#### E1: Authentication (Аутентификация)
**Business Value**: Позволяет пользователям регистрироваться и входить в систему

**Components Status**:
- **Domain Layer** ⚪ Not Started
  - [ ] User entity
  - [ ] AuthTokens value object
  - [ ] AuthRepository interface
  - [ ] LoginUseCase
  - [ ] RegisterUseCase
  - [ ] LogoutUseCase

- **Data Layer** ⚪ Not Started
  - [ ] AuthApiService (Ktor)
  - [ ] LoginRequest/Response DTOs
  - [ ] TokenStorage (DataStore)
  - [ ] AuthRepositoryImpl

- **Presentation Layer** ⚪ Not Started
  - [ ] AuthState (sealed interface)
  - [ ] LoginUiState
  - [ ] AuthViewModel/ScreenModel
  - [ ] LoginScreen (Compose)
  - [ ] RegisterScreen (Compose)

**Dependencies**: `:core:network`, `:core:storage`, `:core:di`

---

#### E2: Catalog (Каталог мастеров)
**Business Value**: Позволяет пользователям искать и просматривать мастеров и услуги

**Components Status**:
- **Domain Layer** ⚪ Not Started
  - [ ] Provider entity
  - [ ] Service entity
  - [ ] Category entity
  - [ ] CatalogRepository interface
  - [ ] SearchProvidersUseCase
  - [ ] GetCategoriesUseCase

- **Data Layer** ⚪ Not Started
  - [ ] CatalogApiService (Ktor)
  - [ ] Provider/Service DTOs
  - [ ] RecentSearchStorage (DataStore)
  - [ ] CatalogRepositoryImpl

- **Presentation Layer** ⚪ Not Started
  - [ ] CatalogState
  - [ ] FilterState
  - [ ] CatalogViewModel
  - [ ] SearchScreen
  - [ ] ProviderCard component
  - [ ] FilterBottomSheet

**Special Integration**:
- [ ] Maps integration (Google Maps Android, Mapbox/Google Maps iOS interop)
- [ ] Location services (FusedLocationProvider / CoreLocation)

**Dependencies**: `:core:network`, `:core:storage`, `:core:navigation`

---

#### E3: Booking (Бронирование)
**Business Value**: Позволяет пользователям создавать брони на услуги

**Components Status**:
- **Domain Layer** ⚪ Not Started
  - [ ] Booking entity
  - [ ] TimeSlot entity
  - [ ] BookingRepository interface
  - [ ] CreateBookingUseCase
  - [ ] GetAvailableSlotsUseCase
  - [ ] CancelBookingUseCase

- **Data Layer** ⚪ Not Started
  - [ ] BookingApiService (Ktor)
  - [ ] Booking DTOs
  - [ ] Local booking cache (DataStore)
  - [ ] BookingRepositoryImpl

- **Presentation Layer** ⚪ Not Started
  - [ ] BookingState
  - [ ] CalendarState
  - [ ] BookingViewModel
  - [ ] BookingFlow screens
  - [ ] CalendarPicker component
  - [ ] TimeSlotSelector

**Dependencies**: `:core:network`, `:core:storage`, `:core:navigation`

---

#### E4: Services (Управление услугами - Provider)
**Business Value**: Позволяет мастерам управлять своими услугами

**Components Status**:
- **Domain Layer** ⚪ Not Started
  - [ ] ServiceManagement entity
  - [ ] ServiceProvider entity
  - [ ] ServiceRepository interface
  - [ ] CreateServiceUseCase
  - [ ] UpdateServiceUseCase
  - [ ] DeleteServiceUseCase

- **Data Layer** ⚪ Not Started
  - [ ] ServiceApiService (Ktor)
  - [ ] Service DTOs
  - [ ] ServiceRepositoryImpl

- **Presentation Layer** ⚪ Not Started
  - [ ] ServiceManagementState
  - [ ] ServiceFormState
  - [ ] ServiceManagementViewModel
  - [ ] ServiceListScreen (Provider)
  - [ ] ServiceFormScreen

**Dependencies**: `:core:network`, `:core:di`

---

#### E5: Profile (Профиль пользователя)
**Business Value**: Позволяет пользователям управлять своим профилем

**Components Status**:
- **Domain Layer** ⚪ Not Started
  - [ ] UserProfile entity
  - [ ] ProfileRepository interface
  - [ ] GetProfileUseCase
  - [ ] UpdateProfileUseCase

- **Data Layer** ⚪ Not Started
  - [ ] ProfileApiService (Ktor)
  - [ ] Profile DTOs
  - [ ] ProfileRepositoryImpl

- **Presentation Layer** ⚪ Not Started
  - [ ] ProfileState
  - [ ] ProfileViewModel
  - [ ] ProfileScreen
  - [ ] EditProfileScreen

**Dependencies**: `:core:network`, `:core:storage`, `:core:navigation`

---

#### E6: Favorites (Избранное)
**Business Value**: Позволяет пользователям сохранять любимых мастеров

**Components Status**:
- **Domain Layer** ⚪ Not Started
  - [ ] FavoriteItem entity
  - [ ] FavoritesRepository interface
  - [ ] AddFavoriteUseCase
  - [ ] RemoveFavoriteUseCase
  - [ ] GetFavoritesUseCase

- **Data Layer** ⚪ Not Started
  - [ ] FavoritesApiService (Ktor)
  - [ ] Favorite DTOs
  - [ ] Local favorites cache (DataStore)
  - [ ] FavoritesRepositoryImpl

- **Presentation Layer** ⚪ Not Started
  - [ ] FavoritesState
  - [ ] FavoritesViewModel
  - [ ] FavoritesScreen
  - [ ] FavoriteProviderCard

**Dependencies**: `:core:network`, `:core:storage`, `:core:navigation`

---

#### E7: Reviews (Отзывы)
**Business Value**: Позволяет пользователям оставлять отзывы о мастерах

**Components Status**:
- **Domain Layer** ⚪ Not Started
  - [ ] Review entity
  - [ ] ReviewsRepository interface
  - [ ] CreateReviewUseCase
  - [ ] GetProviderReviewsUseCase

- **Data Layer** ⚪ Not Started
  - [ ] ReviewsApiService (Ktor)
  - [ ] Review DTOs
  - [ ] ReviewsRepositoryImpl

- **Presentation Layer** ⚪ Not Started
  - [ ] ReviewsState
  - [ ] ReviewsViewModel
  - [ ] ReviewsListScreen
  - [ ] CreateReviewScreen
  - [ ] ReviewCard component

**Dependencies**: `:core:network`, `:core:navigation`

---

## 🚀 Current Sprint Focus

### Sprint 1: Infrastructure Setup (Week 1-2)
**Goal**: Настроить базовую инфраструктуру проекта

**Tasks**:
- [x] Setup Gradle convention plugins
- [x] Configure version catalog (libs.versions.toml)
- [x] Setup KMP project structure
- [x] Configure Android target (JVM 21, SDK 36)
- [x] Complete core:network module (base infrastructure)
- [ ] **CRITICAL**: Implement safeApiCall wrapper (Priority 1)
- [ ] **CRITICAL**: Implement AppError sealed hierarchy (Priority 1)
- [ ] **CRITICAL**: Implement Auth Interceptor (Priority 1, после TokenStorage)
- [ ] **CRITICAL**: Implement core:storage module (Priority 1, для TokenStorage)
- [ ] **IMPORTANT**: Implement i18n helper (Priority 2)
- [ ] Implement core:theme module (Priority 3)
- [ ] Implement core:di module (Priority 3)
- [ ] Setup basic navigation (Voyager) (Priority 3)

**Blockers**:
- iOS setup requires macOS environment

---

### Sprint 2: Auth Feature (Week 3-4)
**Goal**: Реализовать фичу аутентификации

**Planned**:
- Domain: User, AuthTokens, AuthRepository, UseCases
- Data: AuthApiService, TokenStorage
- Presentation: LoginScreen, RegisterScreen, AuthViewModel

---

## 📋 Known Issues & Blockers

### Critical Issues
1. **iOS Environment** ⚠️
   - **Issue**: No macOS environment for iOS development
   - **Impact**: Cannot test iOS build, no iOS simulator
   - **Mitigation**: Focus on Android first, iOS will be configured when environment available

### Technical Debt
1. **Missing Core Modules** ⚠️
   - :core:storage not implemented
   - :core:theme not implemented
   - :core:di not implemented
   - :core:navigation not implemented

2. **Test Coverage** ⚠️
   - 0% test coverage
   - No unit tests for commonMain
   - No UI tests for Compose

---

## 🎯 Next Steps (Priority Order - обновлено 2026-03-20)

### Priority 1: CRITICAL (Week 1-2) 🚨
**Блокер для Auth Feature**

1. **Implement safeApiCall wrapper** (core/network/src/commonMain/kotlin/.../safeApiCall.kt)
   - Обработка всех HTTP кодов (200, 201, 204, 400, 401, 403, 404, 409, 422, 423, 429, 500)
   - Автоматический retry при 500 (max 3 попытки)
   - Rate limiting обработка (X-RateLimit-* headers)
   - Возвращает `Result<T>` или `AppError`

2. **Implement AppError sealed hierarchy** (core/network/src/commonMain/kotlin/.../AppError.kt)
   - NetworkError, Unauthorized, AccountLocked, ValidationError
   - SlotNotAvailable, RateLimitExceeded, UnknownError

3. **Implement core:storage module** (core/storage/)
   - DataStore Preferences для token storage
   - TokenStorage interface (get/set/delete access & refresh tokens)

4. **Implement Auth Interceptor** (core/network/src/commonMain/kotlin/.../AuthInterceptor.kt)
   - Automatic token injection (Authorization: Bearer <token>)
   - Token refresh flow при 401
   - Logout при повторном 401

### Priority 2: IMPORTANT (Week 2-3)
**Требуется для Catalog/Booking Features**

5. **Implement i18n helper** (core/utils/src/commonMain/kotlin/.../i18n/I18nExtensions.kt)
   - Map<String, String>.localize(): String?
   - Использование: val title = serviceDto.title_i18n.localize() ?: "Untitled"

6. **Implement Domain Models** (feature/auth/domain/)
   - User entity (roles: List<String>, currentRole: String?)
   - AuthTokens value object
   - UserContext DTO

7. **Implement UseCases** (feature/auth/domain/)
   - LoginUseCase (с AccountLockout handling)
   - RefreshTokenUseCase (с token rotation)
   - SwitchRoleUseCase (для multi-role users)

### Priority 3: DESIRABLE (Week 3-4)

8. **Implement Repository interfaces** (feature/auth/data/)
   - AuthRepository interface
   - AuthRepositoryImpl (Ktor + TokenStorage)

9. **Implement API Services** (feature/auth/data/remote/)
   - AuthApiService (POST /auth/login, /auth/register, /auth/refresh, /auth/logout)

10. **Complete remaining core modules**
    - :core:theme (Material 3)
    - :core:di (Koin modules)
    - :core:navigation (Voyager)

---

## 📈 Progress Tracking

### Weekly Velocity

| Week | Sprint | Planned | Completed | Velocity |
|------|--------|---------|-----------|----------|
| W1-2 | Infrastructure | 8 tasks | 6 tasks | 75% |
| W3-4 | Auth Feature | 12 tasks | 0 tasks | 0% |

### Burndown Chart

```
Total Story Points: ~200 (estimated)
Remaining: 170
Completed: 30
Sprint: 1/12
```

---

## 🔗 Related Documentation

- [Main README](README.md) - Project overview
- [KMP/CMP Analysis](01_KMP_CMP_ANALYSIS.md) - Technology stack analysis
- [Design System](04_DESIGN_SYSTEM.md) - UI/UX guidelines
- [UX Guidelines](05_UX_GUIDELINES.md) - User experience best practices
- [Quality Infrastructure Plan](plans/01-quality-infrastructure-and-cicd.md) - CI/CD roadmap
- [Deep Code Review](reviews/2026-03-19-deep-code-review.md) - Current state analysis

---

**Documentation Version**: 1.1
**Last Sync**: 2026-03-20 (Updated)
**Next Review**: 2026-03-27
