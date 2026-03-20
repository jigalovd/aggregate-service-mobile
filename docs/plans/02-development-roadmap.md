# 🚀 План развития проекта Aggregate Service Mobile

**Дата создания**: 2026-03-21
**Версия**: 1.0
**Базируется на**: Deep Code Review (2026-03-20), Implementation Status
**Текущий прогресс**: 35%
**Целевой прогресс к концу плана**: 100%

---

## 📊 Executive Summary

### Текущее состояние проекта

| Категория | Статус | Прогресс | Критичность |
|-----------|--------|----------|-------------|
| **Core Infrastructure** | ✅ Complete | 100% | - |
| **Quality Infrastructure** | ✅ Complete | 100% | - |
| **Auth Feature** | ✅ Complete | 100% | - |
| **Core:Theme** | ❌ Missing | 0% | 🔴 HIGH |
| **Core:I18n** | ❌ Missing | 0% | 🔴 HIGH |
| **Catalog Feature** | ❌ Not Started | 0% | 🔴 CRITICAL |
| **Booking Feature** | ❌ Not Started | 0% | 🔴 CRITICAL |
| **Profile Feature** | ❌ Not Started | 0% | 🟡 MEDIUM |
| **Favorites Feature** | ❌ Not Started | 0% | 🟢 LOW |
| **Reviews Feature** | ❌ Not Started | 0% | 🟢 LOW |
| **Services Feature** | ❌ Not Started | 0% | 🟡 MEDIUM |
| **Test Coverage** | 🟡 Low | 15% | 🔴 HIGH |
| **CI/CD Pipeline** | ❌ Not Started | 0% | 🟡 MEDIUM |
| **iOS Platform** | 🟡 Configured | 30% | 🟡 MEDIUM |

### Ключевые риски

1. **🔴 CRITICAL**: Отсутствие Catalog и Booking блокирует основной пользовательский сценарий
2. **🔴 HIGH**: Низкое тестовое покрытие (15% vs цель 60%+)
3. **🔴 HIGH**: Отсутствие theme и i18n блокирует UI разработку
4. **🟡 MEDIUM**: iOS не протестирован на реальном устройстве
5. **🟡 MEDIUM**: Отсутствие CI/CD замедляет разработку

---

## 🎯 Стратегические цели

### MVP Goals (Week 1-8)

1. **Завершить Core модули**: theme, i18n
2. **Реализовать Catalog Feature**: поиск и просмотр мастеров
3. **Реализовать Booking Feature**: бронирование услуг
4. **Достичь 60%+ test coverage**
5. **Настроить CI/CD pipeline**

### Production Goals (Week 9-12)

6. **Реализовать Profile Feature**: управление профилем
7. **Реализовать Favorites Feature**: избранное
8. **Реализовать Reviews Feature**: отзывы
9. **iOS testing & fixes**
10. **Performance optimization**

---

## 📅 PHASE 1: Core Foundation (Week 1-2)

**Цель**: Завершить базовые модули для UI разработки

### 1.1 Core:Theme Implementation (Days 1-3)

**Приоритет**: 🔴 HIGH
**Зависимости**: нет

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T1.1.1 | Создать модуль `:core:theme` | 1h | ⬜ |
| T1.1.2 | Определить ColorScheme (light/dark) | 2h | ⬜ |
| T1.1.3 | Определить Typography | 2h | ⬜ |
| T1.1.4 | Создать Theme composable | 2h | ⬜ |
| T1.1.5 | Добавить Shape definitions | 1h | ⬜ |
| T1.1.6 | Создать предустановленные стили компонентов | 3h | ⬜ |
| T1.1.7 | Написать unit тесты | 2h | ⬜ |
| T1.1.8 | Документация модуля | 1h | ⬜ |

**Deliverables**:
- [ ] `core/theme/build.gradle.kts`
- [ ] `core/theme/src/commonMain/kotlin/.../Color.kt`
- [ ] `core/theme/src/commonMain/kotlin/.../Typography.kt`
- [ ] `core/theme/src/commonMain/kotlin/.../Theme.kt`
- [ ] `core/theme/src/commonMain/kotlin/.../Shape.kt`
- [ ] `core/theme/src/commonMain/kotlin/.../Theme.kt`

**Acceptance Criteria**:
- ✅ Material 3 theme поддерживает light/dark режимы
- ✅ Цвета соответствуют бренду (определить палитру)
- ✅ Typography включает все необходимые стили
- ✅ Theme интегрирован в androidApp

---

### 1.2 Core:I18n Implementation (Days 3-5)

**Приоритет**: 🔴 HIGH
**Зависимости**: нет

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T1.2.1 | Создать модуль `:core:i18n` | 1h | ⬜ |
| T1.2.2 | Определить интерфейс I18nProvider | 2h | ⬜ |
| T1.2.3 | Создать ресурсы для ru (русский) | 3h | ⬜ |
| T1.2.4 | Создать ресурсы для he (иврит) | 3h | ⬜ |
| T1.2.5 | Создать ресурсы для en (английский) | 2h | ⬜ |
| T1.2.6 | Реализовать flatten helper для `_i18n` полей | 2h | ⬜ |
| T1.2.7 | Интегрировать с androidApp | 2h | ⬜ |
| T1.2.8 | Написать unit тесты | 2h | ⬜ |

**Deliverables**:
- [ ] `core/i18n/build.gradle.kts`
- [ ] `core/i18n/src/commonMain/kotlin/.../I18nProvider.kt`
- [ ] `core/i18n/src/commonMain/resources/strings/strings-ru.xml`
- [ ] `core/i18n/src/commonMain/resources/strings/strings-he.xml`
- [ ] `core/i18n/src/commonMain/resources/strings/strings-en.xml`
- [ ] `core/i18n/src/commonMain/kotlin/.../FlattenI18n.kt`

**Acceptance Criteria**:
- ✅ Поддержка 3 языков (ru, he, en)
- ✅ Автоматическое определение языка системы
- ✅ Возможность ручного переключения языка
- ✅ Flatten helper корректно обрабатывает `_i18n` поля из API

---

### 1.3 Test Infrastructure Enhancement (Days 5-7)

**Приоритет**: 🟡 MEDIUM
**Зависимости**: 1.1, 1.2

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T1.3.1 | Настроить Kover отчёты для всех модулей | 2h | ⬜ |
| T1.3.2 | Добавить тесты для core:theme | 2h | ⬜ |
| T1.3.3 | Добавить тесты для core:i18n | 2h | ⬜ |
| T1.3.4 | Создать test-utils модуль с общими утилитами | 3h | ⬜ |
| T1.3.5 | Настроить Compose UI testing | 2h | ⬜ |
| T1.3.6 | Добавить UI тесты для LoginScreen | 3h | ⬜ |

**Deliverables**:
- [ ] Test coverage ≥ 40% после Phase 1
- [ ] UI тесты для LoginScreen
- [ ] Shared test utilities

---

## 📅 PHASE 2: Catalog Feature (Week 3-4)

**Цель**: Реализовать основной пользовательский сценарий - поиск мастеров

### 2.1 Domain Layer (Days 1-2)

**Приоритет**: 🔴 CRITICAL
**Зависимости**: Phase 1

**Entities**:

```kotlin
// Provider (Мастер)
data class Provider(
    val id: String,
    val businessName: String,
    val description: String?,          // _i18n
    val rating: Double,
    val reviewCount: Int,
    val location: Location,
    val photos: List<String>,
    val services: List<ServiceSummary>,
    val workingHours: WorkingHours,
    val isVerified: Boolean,
)

// Service (Услуга)
data class Service(
    val id: String,
    val providerId: String,
    val categoryId: String,
    val name: String,                   // _i18n
    val description: String?,           // _i18n
    val price: Price,
    val durationMinutes: Int,
    val isActive: Boolean,
)

// Category (Категория)
data class Category(
    val id: String,
    val name: String,                   // _i18n
    val icon: String?,
    val parentId: String?,
)

// Location
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
)
```

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T2.1.1 | Создать Provider entity | 1h | ⬜ |
| T2.1.2 | Создать Service entity | 1h | ⬜ |
| T2.1.3 | Создать Category entity | 1h | ⬜ |
| T2.1.4 | Создать Location value object | 1h | ⬜ |
| T2.1.5 | Создать SearchFilters value object | 2h | ⬜ |
| T2.1.6 | Определить CatalogRepository interface | 2h | ⬜ |
| T2.1.7 | Создать SearchProvidersUseCase | 2h | ⬜ |
| T2.1.8 | Создать GetProviderDetailsUseCase | 1h | ⬜ |
| T2.1.9 | Создать GetCategoriesUseCase | 1h | ⬜ |
| T2.1.10 | Написать unit тесты для domain | 3h | ⬜ |

**Deliverables**:
- [ ] `feature/catalog/src/commonMain/kotlin/domain/model/`
- [ ] `feature/catalog/src/commonMain/kotlin/domain/repository/CatalogRepository.kt`
- [ ] `feature/catalog/src/commonMain/kotlin/domain/usecase/`

---

### 2.2 Data Layer (Days 2-4)

**Приоритет**: 🔴 CRITICAL
**Зависимости**: 2.1

**API Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/providers` | Список мастеров с фильтрами |
| GET | `/providers/{id}` | Детали мастера |
| GET | `/providers/{id}/services` | Услуги мастера |
| GET | `/categories` | Список категорий |
| GET | `/services/search` | Поиск услуг |

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T2.2.1 | Создать DTOs (ProviderDto, ServiceDto, CategoryDto) | 3h | ⬜ |
| T2.2.2 | Создать mappers (DTO → Domain) | 2h | ⬜ |
| T2.2.3 | Создать CatalogApiService (Ktor) | 3h | ⬜ |
| T2.2.4 | Реализовать CatalogRepositoryImpl | 4h | ⬜ |
| T2.2.5 | Добавить кэширование категорий | 2h | ⬜ |
| T2.2.6 | Написать unit тесты для data layer | 4h | ⬜ |

**Deliverables**:
- [ ] `feature/catalog/src/commonMain/kotlin/data/dto/`
- [ ] `feature/catalog/src/commonMain/kotlin/data/mapper/`
- [ ] `feature/catalog/src/commonMain/kotlin/data/api/CatalogApiService.kt`
- [ ] `feature/catalog/src/commonMain/kotlin/data/repository/CatalogRepositoryImpl.kt`

---

### 2.3 Presentation Layer (Days 4-7)

**Приоритет**: 🔴 CRITICAL
**Зависимости**: 2.2

**Screens**:

1. **CatalogScreen** - Главный экран каталога
2. **SearchScreen** - Поиск с фильтрами
3. **ProviderDetailScreen** - Профиль мастера
4. **CategorySelectionScreen** - Выбор категории

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T2.3.1 | Создать CatalogUiState | 2h | ⬜ |
| T2.3.2 | Создать CatalogScreenModel | 3h | ⬜ |
| T2.3.3 | Создать CatalogScreen UI | 4h | ⬜ |
| T2.3.4 | Создать SearchUiState | 2h | ⬜ |
| T2.3.5 | Создать SearchScreenModel | 3h | ⬜ |
| T2.3.6 | Создать SearchScreen UI | 4h | ⬜ |
| T2.3.7 | Создать ProviderDetailScreen | 4h | ⬜ |
| T2.3.8 | Создать CategorySelectionScreen | 3h | ⬜ |
| T2.3.9 | Создать CatalogModule (Koin) | 1h | ⬜ |
| T2.3.10 | Написать UI тесты | 4h | ⬜ |

**UI Components**:

| Component | Description | Status |
|-----------|-------------|--------|
| ProviderCard | Карточка мастера в списке | ⬜ |
| ServiceListItem | Элемент услуги | ⬜ |
| CategoryChip | Чип категории | ⬜ |
| SearchBar | Поле поиска | ⬜ |
| FilterBottomSheet | Панель фильтров | ⬜ |
| RatingStars | Звёзды рейтинга | ⬜ |

**Deliverables**:
- [ ] `feature/catalog/src/commonMain/kotlin/presentation/model/`
- [ ] `feature/catalog/src/commonMain/kotlin/presentation/screenmodel/`
- [ ] `feature/catalog/src/commonMain/kotlin/presentation/screen/`
- [ ] `feature/catalog/src/commonMain/kotlin/presentation/components/`
- [ ] `feature/catalog/src/commonMain/kotlin/di/CatalogModule.kt`

---

### 2.4 Maps Integration (Days 7-10)

**Приоритет**: 🟡 MEDIUM
**Зависимости**: 2.3

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T2.4.1 | Исследовать maps решения (Google Maps vs Mapbox) | 2h | ⬜ |
| T2.4.2 | Создать `:core:maps` модуль | 1h | ⬜ |
| T2.4.3 | Определить expect/actual интерфейс для карт | 2h | ⬜ |
| T2.4.4 | Реализовать Google Maps для Android | 4h | ⬜ |
| T2.4.5 | Добавить ProviderMapScreen | 3h | ⬜ |
| T2.4.6 | Интегрировать с CatalogScreen | 2h | ⬜ |

**Deliverables**:
- [ ] `core/maps/build.gradle.kts`
- [ ] `core/maps/src/commonMain/kotlin/.../MapProvider.kt`
- [ ] `core/maps/src/androidMain/kotlin/.../GoogleMapsProvider.kt`
- [ ] ProviderMapScreen с маркерами мастеров

---

## 📅 PHASE 3: Booking Feature (Week 5-6)

**Цель**: Реализовать полный цикл бронирования услуг

### 3.1 Domain Layer (Days 1-2)

**Entities**:

```kotlin
// Booking
data class Booking(
    val id: String,
    val providerId: String,
    val serviceId: String,
    val clientId: String,
    val dateTime: Instant,
    val durationMinutes: Int,
    val status: BookingStatus,
    val totalPrice: Price,
    val notes: String?,
    val createdAt: Instant,
)

enum class BookingStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
}

// TimeSlot
data class TimeSlot(
    val startTime: Instant,
    val endTime: Instant,
    val isAvailable: Boolean,
)

// Schedule
data class ProviderSchedule(
    val providerId: String,
    val date: LocalDate,
    val slots: List<TimeSlot>,
)
```

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T3.1.1 | Создать Booking entity | 1h | ⬜ |
| T3.1.2 | Создать TimeSlot value object | 1h | ⬜ |
| T3.1.3 | Создать ProviderSchedule entity | 1h | ⬜ |
| T3.1.4 | Определить BookingRepository interface | 2h | ⬜ |
| T3.1.5 | Создать CreateBookingUseCase | 2h | ⬜ |
| T3.1.6 | Создать CancelBookingUseCase | 1h | ⬜ |
| T3.1.7 | Создать GetAvailableSlotsUseCase | 2h | ⬜ |
| T3.1.8 | Создать GetClientBookingsUseCase | 1h | ⬜ |
| T3.1.9 | Написать unit тесты | 3h | ⬜ |

---

### 3.2 Data Layer (Days 2-4)

**API Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/bookings` | Создать бронирование |
| GET | `/bookings/{id}` | Детали бронирования |
| PUT | `/bookings/{id}/cancel` | Отменить бронирование |
| GET | `/bookings/client/{clientId}` | Бронирования клиента |
| GET | `/providers/{id}/schedule` | Расписание мастера |

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T3.2.1 | Создать DTOs (BookingDto, TimeSlotDto) | 2h | ⬜ |
| T3.2.2 | Создать mappers | 2h | ⬜ |
| T3.2.3 | Создать BookingApiService | 3h | ⬜ |
| T3.2.4 | Реализовать BookingRepositoryImpl | 4h | ⬜ |
| T3.2.5 | Добавить локальное кэширование | 2h | ⬜ |
| T3.2.6 | Написать unit тесты | 3h | ⬜ |

---

### 3.3 Presentation Layer (Days 4-7)

**Screens**:

1. **BookingFlowScreen** - Контейнер flow
2. **SelectServiceScreen** - Выбор услуги
3. **SelectDateTimeScreen** - Выбор даты и времени
4. **BookingConfirmationScreen** - Подтверждение
5. **BookingDetailsScreen** - Детали брони
6. **MyBookingsScreen** - Список бронирований

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T3.3.1 | Создать BookingUiState | 2h | ⬜ |
| T3.3.2 | Создать BookingScreenModel | 4h | ⬜ |
| T3.3.3 | Создать SelectServiceScreen | 3h | ⬜ |
| T3.3.4 | Создать SelectDateTimeScreen (Calendar) | 5h | ⬜ |
| T3.3.5 | Создать BookingConfirmationScreen | 3h | ⬜ |
| T3.3.6 | Создать BookingDetailsScreen | 3h | ⬜ |
| T3.3.7 | Создать MyBookingsScreen | 4h | ⬜ |
| T3.3.8 | Создать BookingModule (Koin) | 1h | ⬜ |
| T3.3.9 | Написать UI тесты | 4h | ⬜ |

**UI Components**:

| Component | Description | Status |
|-----------|-------------|--------|
| CalendarPicker | Календарь с доступными датами | ⬜ |
| TimeSlotGrid | Сетка временных слотов | ⬜ |
| BookingCard | Карточка бронирования | ⬜ |
| BookingStatusChip | Статус брони | ⬜ |

---

## 📅 PHASE 4: Profile & User Management (Week 7)

**Цель**: Управление профилем пользователя

### 4.1 Profile Feature Implementation

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T4.1.1 | Domain: User entity, ProfileRepository | 3h | ⬜ |
| T4.1.2 | Data: ProfileApiService, DTOs | 3h | ⬜ |
| T4.1.3 | Presentation: ProfileScreen | 4h | ⬜ |
| T4.1.4 | Presentation: EditProfileScreen | 3h | ⬜ |
| T4.1.5 | DI: ProfileModule | 1h | ⬜ |
| T4.1.6 | Unit тесты | 2h | ⬜ |

---

## 📅 PHASE 5: Additional Features (Week 8)

**Цель**: Завершить второстепенные фичи

### 5.1 Favorites Feature

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T5.1.1 | Domain: Favorite entity, UseCases | 2h | ⬜ |
| T5.1.2 | Data: FavoriteRepository, API | 2h | ⬜ |
| T5.1.3 | Presentation: FavoritesScreen | 3h | ⬜ |
| T5.1.4 | Интеграция с ProviderDetailScreen | 1h | ⬜ |

### 5.2 Reviews Feature

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T5.2.1 | Domain: Review entity, UseCases | 2h | ⬜ |
| T5.2.2 | Data: ReviewRepository, API | 2h | ⬜ |
| T5.2.3 | Presentation: ReviewsScreen | 3h | ⬜ |
| T5.2.4 | Presentation: WriteReviewScreen | 3h | ⬜ |

---

## 📅 PHASE 6: Quality & Production Readiness (Week 9-10)

### 6.1 CI/CD Pipeline Setup

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T6.1.1 | Создать GitHub Actions workflow | 3h | ⬜ |
| T6.1.2 | Настроить lint job (detekt + ktlint) | 2h | ⬜ |
| T6.1.3 | Настроить test job с coverage | 2h | ⬜ |
| T6.1.4 | Настроить build job (debug/release) | 2h | ⬜ |
| T6.1.5 | Добавить PR checks | 2h | ⬜ |

**GitHub Actions Workflow**:

```yaml
name: CI
on: [push, pull_request]
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
      - run: ./gradlew detektAll ktlintCheckAll

  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
      - run: ./gradlew allTests koverReportAll
      - uses: codecov/codecov-action@v4

  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
      - run: ./gradlew :androidApp:assembleDebug
```

### 6.2 Test Coverage Target

**Цель**: 60%+ coverage для всех feature модулей

| Module | Current | Target | Status |
|--------|---------|--------|--------|
| core:network | 40% | 70% | 🟡 |
| core:storage | 20% | 60% | 🟡 |
| feature:auth | 30% | 60% | 🟡 |
| feature:catalog | 0% | 60% | ⬜ |
| feature:booking | 0% | 60% | ⬜ |

### 6.3 Performance Optimization

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T6.3.1 | Profile startup time | 2h | ⬜ |
| T6.3.2 | Optimize image loading (Coil) | 2h | ⬜ |
| T6.3.3 | Add pagination to lists | 3h | ⬜ |
| T6.3.4 | Memory leak analysis | 3h | ⬜ |

---

## 📅 PHASE 7: iOS Platform Support (Week 11-12)

**Цель**: Полная поддержка iOS

### 7.1 iOS Project Setup

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T7.1.1 | Создать Xcode project | 2h | ⬜ |
| T7.1.2 | Настроить Kotlin Framework | 2h | ⬜ |
| T7.1.3 | Настроить Info.plist | 1h | ⬜ |
| T7.1.4 | Настроить signing & capabilities | 1h | ⬜ |

### 7.2 iOS Testing

**Tasks**:

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| T7.2.1 | Тестирование на симуляторе | 4h | ⬜ |
| T7.2.2 | Тестирование на реальном устройстве | 4h | ⬜ |
| T7.2.3 | Исправление iOS-specific багов | 8h | ⬜ |
| T7.2.4 | Тестирование maps на iOS | 3h | ⬜ |

---

## 📊 Milestones & Deliverables

### Milestone M1: Core Foundation Complete (End of Week 2)

- [ ] core:theme module implemented
- [ ] core:i18n module implemented
- [ ] Test coverage ≥ 40%
- [ ] UI components styled with theme

### Milestone M2: Catalog Feature Complete (End of Week 4)

- [ ] Catalog feature fully implemented (Domain + Data + Presentation)
- [ ] Maps integration working
- [ ] User can search and browse providers
- [ ] Test coverage ≥ 50%

### Milestone M3: Booking Feature Complete (End of Week 6)

- [ ] Booking flow fully functional
- [ ] Calendar and time slot selection working
- [ ] User can create and cancel bookings
- [ ] Test coverage ≥ 55%

### Milestone M4: MVP Features Complete (End of Week 8)

- [ ] Profile feature implemented
- [ ] Favorites feature implemented
- [ ] Reviews feature implemented
- [ ] Test coverage ≥ 60%

### Milestone M5: Production Ready (End of Week 10)

- [ ] CI/CD pipeline operational
- [ ] Performance optimized
- [ ] All critical bugs fixed
- [ ] Documentation complete

### Milestone M6: iOS Support (End of Week 12)

- [ ] iOS build successful
- [ ] All features tested on iOS
- [ ] iOS-specific issues resolved

---

## 📈 Resource Estimation

### Total Effort by Phase

| Phase | Duration | Est. Hours | Complexity |
|-------|----------|------------|------------|
| Phase 1: Core Foundation | 2 weeks | 50h | 🟡 Medium |
| Phase 2: Catalog Feature | 2 weeks | 80h | 🔴 High |
| Phase 3: Booking Feature | 2 weeks | 70h | 🔴 High |
| Phase 4: Profile Feature | 1 week | 16h | 🟢 Low |
| Phase 5: Additional Features | 1 week | 20h | 🟢 Low |
| Phase 6: Quality & CI/CD | 2 weeks | 30h | 🟡 Medium |
| Phase 7: iOS Support | 2 weeks | 25h | 🟡 Medium |
| **Total** | **12 weeks** | **~291h** | - |

### Risk Buffer

- Add 20% buffer for unexpected issues: ~60h
- **Total with buffer**: ~350h (~9 weeks full-time)

---

## 🔗 Related Documentation

- [Implementation Status](../IMPLEMENTATION_STATUS.md)
- [API Reference](../BACKEND_API_REFERENCE.md)
- [User Stories](../business/USER_STORIES.md)
- [Design System](../04_DESIGN_SYSTEM.md)
- [UX Guidelines](../05_UX_GUIDELINES.md)
- [Network Layer](../NETWORK_LAYER.md)
- [Testing Infrastructure](../TESTING_INFRASTRUCTURE.md)

---

## 📝 Change Log

| Date | Version | Changes |
|------|---------|---------|
| 2026-03-21 | 1.0 | Initial roadmap creation |

---

**Document Owner**: Development Team
**Next Review**: Weekly during sprint planning
**Last Updated**: 2026-03-21
