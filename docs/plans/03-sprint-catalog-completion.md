# 🎯 Sprint Plan: Catalog Feature Completion

**Дата создания**: 2026-03-21
**Версия**: 1.0
**Спринт**: Sprint 5 - Catalog Phase 2
**Длительность**: 2 недели
**Статус**: 🔄 ACTIVE

---

## 📊 Executive Summary

### Текущее состояние проекта

| Категория | Статус | Прогресс | Изменение |
|-----------|--------|----------|-----------|
| **Core Infrastructure** | ✅ Complete | 100% | - |
| **Auth Feature** | ✅ Complete | 100% | +Guest Mode |
| **Catalog Feature** | 🟡 In Progress | 70% | Domain+Data done |
| **Test Coverage** | 🟡 Low | 25% | +82 tests |
| **CI/CD** | ⚪ Not Started | 0% | Planned |
| **Общий прогресс** | 🟡 | 35% → 55% (target) |

### Цели спринта

1. **Завершить Catalog Feature** до 100%
2. **Добавить Unit тесты** для Catalog (target: 30+ tests)
3. **Поднять Test Coverage** с 25% до 40%+
4. **Подготовить foundation** для Booking Feature

---

## 🎯 Sprint Goals

### Priority 1: Catalog Presentation Layer (Критический путь)

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| **T1.1** | ProviderDetailScreen - профиль мастера | 4h | ⬜ |
| **T1.2** | ProviderDetailScreenModel | 3h | ⬜ |
| **T1.3** | SearchScreen - поиск с фильтрами | 4h | ⬜ |
| **T1.4** | SearchScreenModel | 3h | ⬜ |
| **T1.5** | CategorySelectionScreen | 3h | ⬜ |
| **T1.6** | UI Components (ProviderCard, RatingStars, etc.) | 4h | ⬜ |

### Priority 2: Catalog Tests (Качество)

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| **T2.1** | SearchProvidersUseCaseTest | 2h | ⬜ |
| **T2.2** | GetCategoriesUseCaseTest | 1h | ⬜ |
| **T2.3** | GetProviderDetailsUseCaseTest | 1h | ⬜ |
| **T2.4** | ProviderMapperTest | 2h | ⬜ |
| **T2.5** | CategoryMapperTest | 1h | ⬜ |
| **T2.6** | CatalogScreenModelTest | 3h | ⬜ |

### Priority 3: Infrastructure (Если останется время)

| Task | Description | Est. Hours | Status |
|------|-------------|------------|--------|
| **T3.1** | GitHub Actions CI workflow | 3h | ⬜ |
| **T3.2** | Kover coverage gates | 1h | ⬜ |

---

## 📅 Week 1: Provider Detail & Search

### Day 1-2: ProviderDetailScreen

**Deliverables:**

```
feature/catalog/src/commonMain/kotlin/presentation/
├── screen/
│   └── ProviderDetailScreen.kt       # NEW
├── screenmodel/
│   └── ProviderDetailScreenModel.kt  # NEW
├── model/
│   └── ProviderDetailUiState.kt      # NEW
└── component/
    ├── ProviderHeader.kt             # NEW
    ├── ServiceListItem.kt            # NEW
    └── WorkingHoursDisplay.kt        # NEW
```

**ProviderDetailScreen Specification:**

```kotlin
// ProviderDetailUiState.kt
@Stable
data class ProviderDetailUiState(
    val provider: Provider? = null,
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
)

// ProviderDetailScreen.kt
class ProviderDetailScreen(private val providerId: String) : Screen {
    @Composable
    override fun Content() {
        // Sections:
        // 1. Header: avatar, name, rating, verified badge
        // 2. Location: address + map preview
        // 3. Working hours: weekly schedule
        // 4. Services: list with prices and duration
        // 5. Reviews: preview + link to all
        // 6. CTA: "Book Now" button (with AuthGuard)
    }
}
```

**Acceptance Criteria:**
- [ ] Provider profile displays all info from domain model
- [ ] Services list with pagination (LazyColumn)
- [ ] "Book Now" button protected with AuthGuard
- [ ] Loading and error states handled
- [ ] Back navigation works correctly

---

### Day 3-4: SearchScreen

**Deliverables:**

```
feature/catalog/src/commonMain/kotlin/presentation/
├── screen/
│   └── SearchScreen.kt               # NEW
├── screenmodel/
│   └── SearchScreenModel.kt          # NEW
├── model/
│   └── SearchUiState.kt              # NEW
└── component/
    ├── SearchBar.kt                  # NEW
    ├── FilterBottomSheet.kt          # NEW
    └── SearchResultsList.kt          # NEW
```

**SearchUiState Specification:**

```kotlin
@Stable
data class SearchUiState(
    val query: String = "",
    val results: List<Provider> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val isSearching: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
)

data class SearchFilters(
    val categoryId: String? = null,
    val minRating: Double? = null,
    val maxPrice: Double? = null,
    val sortBy: SortBy = SortBy.RATING,
    val radiusKm: Double? = null,
)

enum class SortBy {
    RATING, PRICE, DISTANCE, POPULARITY
}
```

**Acceptance Criteria:**
- [ ] Search by provider name/service name
- [ ] Filter by category, rating, price
- [ ] Sort by rating, price, distance
- [ ] Debounced search (300ms)
- [ ] Pagination with infinite scroll
- [ ] Empty state for no results

---

### Day 5: CategorySelectionScreen + Components

**Deliverables:**

```
feature/catalog/src/commonMain/kotlin/presentation/
├── screen/
│   └── CategorySelectionScreen.kt    # NEW
└── component/
    ├── CategoryChip.kt               # NEW
    ├── ProviderCard.kt               # NEW
    └── RatingStars.kt                # NEW
```

**Acceptance Criteria:**
- [ ] Categories displayed in grid/list
- [ ] Category selection navigates to filtered search
- [ ] ProviderCard shows: image, name, rating, services preview
- [ ] RatingStars component reusable

---

## 📅 Week 2: Tests & Quality

### Day 6-7: UseCase Tests

**Test Files:**

```kotlin
// SearchProvidersUseCaseTest.kt
class SearchProvidersUseCaseTest {
    // Tests:
    // - search with empty query returns all
    // - search with filters applies correctly
    // - pagination works
    // - error handling
}

// GetCategoriesUseCaseTest.kt
class GetCategoriesUseCaseTest {
    // Tests:
    // - returns hierarchical categories
    // - caching works
    // - error handling
}

// GetProviderDetailsUseCaseTest.kt
class GetProviderDetailsUseCaseTest {
    // Tests:
    // - returns provider with services
    // - not found handling
    // - error handling
}
```

---

### Day 8-9: Mapper & ScreenModel Tests

**Test Files:**

```kotlin
// ProviderMapperTest.kt
class ProviderMapperTest {
    // Tests:
    // - DTO to Domain mapping
    // - workingHours mapping (edge cases)
    // - null handling
    // - i18n fields flattening
}

// CatalogScreenModelTest.kt
class CatalogScreenModelTest {
    // Tests:
    // - initial state
    // - load providers
    // - load more (pagination)
    // - refresh
    // - error handling
    // - category selection
}
```

---

### Day 10: CI/CD Setup (Optional)

**GitHub Actions Workflow:**

```yaml
# .github/workflows/ci.yml
name: CI
on: [push, pull_request]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: ./gradlew detektAll ktlintCheckAll

  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: ./gradlew allTests koverReportAll
      - uses: codecov/codecov-action@v4

  build:
    runs-on: ubuntu-latest
    needs: [lint, test]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: ./gradlew :androidApp:assembleDebug
```

---

## 📊 Sprint Metrics

### Target Metrics

| Метрика | Start | Target | Current |
|---------|-------|--------|---------|
| Catalog Feature | 70% | 100% | 70% |
| Test Count | 82 | 120+ | 82 |
| Test Coverage | 25% | 40%+ | 25% |
| Detekt Issues | 0 | 0 | 0 |
| Ktlint Violations | 0 | 0 | 0 |

### Estimated Effort

| Category | Hours | Priority |
|----------|-------|----------|
| ProviderDetailScreen | 7h | 🔴 Critical |
| SearchScreen | 7h | 🔴 Critical |
| CategorySelectionScreen | 3h | 🟡 High |
| UI Components | 4h | 🟡 High |
| UseCase Tests | 4h | 🔴 Critical |
| Mapper Tests | 3h | 🟡 High |
| ScreenModel Tests | 3h | 🟡 High |
| CI/CD Setup | 4h | 🟢 Low |
| **Total** | **35h** | - |

---

## 🚀 Definition of Done

### Feature DoD

- [ ] Код написан по Clean Architecture (Domain → Data → Presentation)
- [ ] Voyager ScreenModel с StateFlow
- [ ] Compose UI с Material 3 компонентами
- [ ] Loading/Error/Empty states
- [ ] AuthGuard для write operations
- [ ] Unit tests ≥ 80% coverage для нового кода
- [ ] Detekt: 0 warnings
- [ ] Ktlint: 100% compliance
- [ ] KDoc для публичных API

### Sprint DoD

- [ ] Все Priority 1 tasks завершены
- [ ] Catalog Feature: 100%
- [ ] Test Coverage: ≥ 40%
- [ ] Все тесты проходят
- [ ] IMPLEMENTATION_STATUS.md обновлён
- [ ] CHANGELOG.md обновлён

---

## 🔗 Dependencies

### Internal Dependencies

```
feature:catalog
├── :core:network (safeApiCall, AppError)
├── :core:storage (TokenStorage)
├── :core:navigation (Voyager, AuthGuard)
├── :core:theme (Material 3)
├── :core:i18n (StringKey, I18nProvider)
└── :core:utils (Validators)
```

### External Dependencies

| Library | Version | Usage |
|---------|---------|-------|
| Voyager | 1.1.0-beta02 | Navigation |
| Koin | 4.2.0 | DI |
| Coil | 3.4.0 | Image loading |
| Kotlinx DateTime | 0.6.0 | Date/Time |

---

## ⚠️ Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| API changes required | Medium | High | Mock data for development |
| Maps integration delay | Low | Medium | Defer to next sprint |
| Test writing slower | Medium | Low | Prioritize critical paths |
| iOS compatibility issues | Low | High | Test on iOS early |

---

## 📋 Next Sprint Preview

### Sprint 6: Booking Feature (Week 3-4)

**Planned Scope:**
1. Booking domain entities (Booking, TimeSlot, Schedule)
2. BookingRepository + API service
3. Booking flow screens:
   - SelectServiceScreen
   - SelectDateTimeScreen
   - BookingConfirmationScreen
4. Calendar component
5. Integration with ProviderDetailScreen

**Dependencies:**
- Catalog Feature 100% complete
- AuthGuard working for booking trigger

---

## 🔗 Related Documentation

- [Implementation Status](../IMPLEMENTATION_STATUS.md)
- [Development Roadmap](02-development-roadmap.md)
- [Auth Feature](../features/AUTH_FEATURE.md)
- [Catalog Feature](../features/CATALOG_FEATURE.md)
- [Backend API Reference](../api/BACKEND_API_REFERENCE.md)

---

**Document Owner**: Development Team
**Last Updated**: 2026-03-21
**Next Review**: Daily standup / Weekly sprint review
