# Planning Documents - Aggregate Service

**Last Updated**: 2026-03-19
**Status**: Active (1 plan in progress)
**Architecture**: Feature-First + Clean Architecture

---

## 📋 Available Plans

### 01. Quality Infrastructure & CI/CD (Week 1-2)

**Status:** 📝 PLANNED - Ready for execution
**Created:** 2026-03-19
**Timeline:** 2 weeks (10 business days)
**Priority:** 🔴 CRITICAL

**Scope:**
- **Phase 1**: Code Quality Infrastructure (Days 1-3)
  - Detekt (static analysis) - .detekt/config.yml
  - Ktlint (code formatting) - .editorconfig
  - Kover (code coverage) - 60%+ target

- **Phase 2**: Testing Infrastructure (Days 3-5)
  - Mockk, Turbine, Coroutines Test
  - Test structure: commonTest/androidTest/iosTest

- **Phase 3**: Error Handling Foundation (Days 4-5)
  - `AppError.kt` (sealed hierarchy, 5 error types)
  - `safeApiCall.kt` (Ktor wrapper)
  - Unit tests for error mapping

- **Phase 4**: CI/CD Pipeline (Week 2, Days 1-3)
  - GitHub Actions (4 jobs: lint, test, coverage, build)
  - Lefthook (pre-commit hooks)

- **Phase 5**: Domain Models & Repository (Days 3-5)
  - User, AuthTokens, Session (Domain entities)
  - AuthRepository (interface)
  - LoginUseCase, RegisterUseCase

- **Phase 6**: Dependency Injection (Days 4-5)
  - Koin modules (NetworkModule, AuthModule)
  - Koin init in Android app

**Deliverables:**
- ✅ Detekt: 0 warnings baseline
- ✅ Ktlint: 100% style compliance
- ✅ Kover: 60%+ coverage (network layer)
- ✅ CI/CD: Automated checks on every PR
- ✅ Tests: 100% network functions covered

**Details:** [01-quality-infrastructure-and-cicd.md](01-quality-infrastructure-and-cicd.md)

---

## 🎯 Upcoming Plans (Planned)

### 02. Auth Feature Implementation (Week 3-4)
**Status:** ⏳ Planned
**Timeline:** 2 weeks
**Scope:**
- Domain Layer: User, AuthTokens, Session entities
- Data Layer: AuthApiService (Ktor), DTOs, TokenStorage
- Presentation Layer: AuthState, LoginScreenModel, LoginScreen (Compose)

### 03. Catalog Feature Implementation (Week 5-6)
**Status:** ⏳ Planned
**Timeline:** 2 weeks
**Scope:**
- Domain Layer: Provider, Service, Category entities
- Data Layer: CatalogApiService, SearchStorage
- Presentation Layer: CatalogState, SearchScreen, ProviderCard
- **Integration**: Maps (Google Maps Android, Mapbox iOS)

### 04. Booking Feature Implementation (Week 7-8)
**Status:** ⏳ Planned
**Timeline:** 2 weeks
**Scope:**
- Domain Layer: Booking, TimeSlot entities
- Data Layer: BookingApiService, Local cache
- Presentation Layer: BookingFlow, CalendarPicker, TimeSlotSelector

### 05. Core:Storage & Core:DI Implementation (Week 2-3)
**Status:** ⏳ Planned
**Timeline:** 1 week
**Scope:**
- DataStore Preferences setup
- Koin 4.0.2 modules configuration
- Platform-specific storage (expect/actual)

---

## 📊 Planning Statistics

| Metric | Value |
|--------|-------|
| **Total Active Plans** | 1 (Quality Infrastructure) |
| **Total Phases** | 6 (plan 01) |
| **Estimated Duration** | 2 weeks (plan 01) |
| **Files to Create** | 15+ (configs, tests, domain models) |
| **Files to Modify** | 10+ (gradle, docs, build-logic) |

---

## 🔗 Related Documentation

### Project Tracking
- [Implementation Status](../IMPLEMENTATION_STATUS.md) - Progress tracking (15% complete)
- [Deep Code Review](../reviews/2026-03-19-deep-code-review.md) - Current state analysis (65/100)

### Architecture
- [KMP/CMP Analysis](../01_KMP_CMP_ANALYSIS.md) - Technology stack analysis
- [Design System](../04_DESIGN_SYSTEM.md) - UI/UX guidelines
- [UX Guidelines](../05_UX_GUIDELINES.md) - User experience best practices

### Project
- [Main README](../README.md) - Project overview
- [Changelog](../../CHANGELOG.md) - Version history and migrations

---

## 📈 Planning Process

### How Plans Are Created

1. **Trigger**: Deep Code Review выявляет критические проблемы
2. **Analysis**: Senior Architect анализирует gap-ы
3. **Planning**: Создаётся детальный план с phases, deliverables, timeline
4. **Review**: План обсуждается с командой
5. **Execution**: План внедряется (см. Timeline)
6. **Validation**: Detekt/Ktlint/Kover валидируют качество
7. **Completion**: IMPLEMENTATION_STATUS обновляется

### Planning Principles

- **Evidence-Based**: План базируется на Deep Code Review findings
- **Measurable**: Each phase имеет concrete deliverables
- **Time-Boxed**: Жёсткие timeline (1-2 недели на план)
- **Risk-Aware**: Митигация рисков документирована
- **Tested**: Code examples в плане проверены

---

## 🎯 Success Criteria

### Plan Completion Checklist

- [ ] Все phases выполнены в timeline
- [ ] Detekt: 0 warnings
- [ ] Ktlint: 100% compliance
- [ ] Kover: 60%+ coverage
- [ ] CI/CD: Автоматические проверки работают
- [ ] IMPLEMENTATION_STATUS обновлён
- [ ] CHANGELOG включает изменения

---

## 🚀 Next Steps

**Immediate (This Week):**
1. ✅ Start Phase 1: Detekt + Ktlint + Kover setup
2. ✅ Create `.detekt/config.yml`
3. ✅ Create `.editorconfig`
4. ✅ Update `gradle/libs.versions.toml`

**Upcoming (Week 2):**
1. ⏳ Phase 4: CI/CD pipeline
2. ⏳ Phase 5: Domain models (Auth feature)
3. ⏳ Phase 6: Koin DI setup

---

**Last Updated**: 2026-03-19
**Next Review**: 2026-04-02 (after plan 01 completion)
**Owner**: Development Team
**Maintained By**: Senior Documentation Sync Engineer
