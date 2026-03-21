# Beauty Service Aggregator - Backlog

**Версия:** 0.7.4 | **Дата:** 12.03.2026

---

## 🎯 MVP Scope

**Подробно:** [11_MVP_SCOPE.md](./11_MVP_SCOPE.md) @./11_MVP_SCOPE.md

### IN Scope (Must Have)

| Эпик                     | Статус | Описание                                                         |
|--------------------------|--------|------------------------------------------------------------------|
| E1: Auth & Identity      | 98% ✅  | Email verification требует SendGrid Spike, Resend verification ✅ |
| E2: Catalog & Geo-Search | 100% ✅ | ЗАВЕРШЁН 03.03.2026 (5 дней)                                     |
| E3: Booking Engine       | 85% 🟡 | Domain/App/Infra/Pres layers ✅, Tests 85%, E2E pending           |

### OUT Scope (Phase 2)

| Эпик                      | Причина                        |
|---------------------------|--------------------------------|
| E4: Service Management    | Не критично для MVP            |
| E5: Reputation System     | Требует critical mass bookings |
| E6: Notifications         | Email достаточно для MVP       |
| Phone verification, OAuth | Phase 2+                       |

---

## ✅ P0 - Security Audit (ИСПРАВЛЕНО)

| ID      | Задача                       | Оценка | Статус           |
|---------|------------------------------|--------|------------------|
| SEC-001 | Fix User ID Injection        | 1 день | ✅ **ИСПРАВЛЕНО** |
| SEC-003 | Fix Debug Mode Default       | 1 час  | ✅ **ИСПРАВЛЕНО** |
| DB-001  | Fix Broken Migration Chain   | 1 час  | ✅ **ИСПРАВЛЕНО** |
| DB-002  | Fix Duplicate profiles Table | 1 час  | ✅ **ИСПРАВЛЕНО** |
| DB-003  | Fix Wrong PostGIS Type       | 4 часа | ✅ **ИСПРАВЛЕНО** |

**Всего P0:** 1 день (план: 6.5 часов)  
**Дата завершения:** 04.03.2026

**Подробнее:**

- [Security Improvements](../architecture/backend/15_SECURITY_IMPROVEMENTS.md)
  @../architecture/backend/15_SECURITY_IMPROVEMENTS.md
- [Performance Improvements](../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md)
  @../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md
- [Business Logic Analysis](./BUSINESS_LOGIC_ANALYSIS.md) @./BUSINESS_LOGIC_ANALYSIS.md

---

## 🟠 P1 - High Priority (Next Sprint)

**Запланировано:** После E3 Booking MVP завершения (2-3 дня)

### Security (2 issues)

| ID      | Задача                    | Оценка | Приоритет | Статус           |
|---------|---------------------------|--------|-----------|------------------|
| SEC-002 | Add JWT iss/aud claims    | 1 день | High      | ✅ **ИСПРАВЛЕНО** |
| SEC-005 | Implement Account Lockout | 2 дня  | High      | ⚠️ Pending       |

### Performance (3 issues)

| ID       | Задача                       | Оценка | Impact         | Статус           |
|----------|------------------------------|--------|----------------|------------------|
| PERF-001 | Fix N+1 in get_favorites     | 2 дня  | 51 queries → 2 | ✅ **ИСПРАВЛЕНО** |
| PERF-002 | Fix N+1 in token cleanup     | 1 день | N+1 DELETE     | ⚠️ Pending       |
| PERF-003 | Add Missing Database Indexes | 1 день | 2-50x speedup  | ⚠️ Pending       |

### Architecture (2 issues)

| ID       | Задача                          | Оценка | Статус           |
|----------|---------------------------------|--------|------------------|
| ARCH-002 | Create Application DTOs         | 2 дня  | ✅ **ИСПРАВЛЕНО** |
| ARCH-003 | Fix Private Method from Outside | 1 день | ✅ **ИСПРАВЛЕНО** |

### Database (3 issues)

| ID     | Задача                    | Оценка | Статус     |
|--------|---------------------------|--------|------------|
| DB-004 | Add Composite Indexes (5) | 1 день | ⚠️ Pending |
| DB-005 | Add CHECK Constraints (4) | 4 часа | ⚠️ Pending |
| DB-006 | Add FK Indexes            | 4 часа | ⚠️ Pending |

**Всего P1:** 11 дней (2 недели)

---

## 📋 Current Sprint: E3 Booking Engine (85% 🟡)

**Цель:** End-to-end booking flow  
**Оценка:** 21-28 дней (фактически: ~14 дней, 85% завершено)  
**Приоритет:** High | **MVP почти готов**

### Sprint Progress

- [x] Database Schema Design (2-3 дня) ✅
    - Booking, BookingItem, ScheduleRule, BookingRescheduleHistory models
    - Conflict detection indexes
- [x] Domain Layer (3-4 дня) ✅
    - Value Objects: BookingStatus
    - Entities: Booking, BookingItem, ScheduleRule, BookingRescheduleHistory
    - Services: SlotGenerator
    - Exceptions: 10+ domain-specific
- [x] Application Layer (3-4 дня) ✅
    - Protocols: BookingRepository, ScheduleRuleRepository, BookingUnitOfWork
    - Use Cases: Create, Cancel, Confirm, Complete, Reschedule, GetSlots (6 use cases)
- [x] Infrastructure Layer (4-5 дней) ✅
    - SQLAlchemy models (4 models)
    - Repositories: BookingRepository, ScheduleRuleRepository
    - Mappers: Domain ↔ ORM conversion
- [x] Presentation Layer (2-3 дня) ✅
    - FastAPI controllers: BookingController, ScheduleController
    - API endpoints: 15 endpoints (8 booking + 6 schedule + 1 slots)
- [ ] Tests (85% завершено)
    - [x] Unit tests: Domain entities, use cases (34 tests) ✅
    - [x] Integration tests: API endpoints (41 tests) ✅
    - [ ] E2E tests (критичные сценарии)
    - [ ] Performance tests (concurrent bookings)

**API Endpoints (реализовано 15):**

- ✅ POST /api/v1/bookings - Create booking
- ✅ GET /api/v1/bookings/{id} - Get booking details
- ✅ GET /api/v1/bookings/provider/{id} - List provider bookings
- ✅ GET /api/v1/bookings/client/{id} - List client bookings
- ✅ PATCH /api/v1/bookings/{id}/confirm - Confirm booking (Provider)
- ✅ PATCH /api/v1/bookings/{id}/cancel - Cancel booking
- ✅ PATCH /api/v1/bookings/{id}/reschedule - Reschedule booking
- ✅ PATCH /api/v1/bookings/{id}/complete - Complete booking (Provider)
- ✅ GET /api/v1/bookings/slots - Get available slots
- ✅ GET/POST/PATCH/DELETE /api/v1/schedule/* - Schedule management (6 endpoints)

**Tech:** Slot generation ✅, Conflict detection ✅, Timezone handling ✅

**Оценка до завершения:** 2-3 дня (E2E tests, Performance tests)

---

## 📋 E2: Catalog & Geo-Search ✅ ЗАВЕРШЁН

**Статус:** 100% ✅  
**Дата завершения:** 03.03.2026  
**Время:** 5 дней (план: 14-21 день)  
**Эффективность:** 🚀 47% быстрее плана

**Реализовано:**

- ✅ Database schema (5 таблиц + PostGIS)
- ✅ Domain layer (entities, value objects)
- ✅ Application layer (protocols, UoW)
- ✅ Infrastructure layer (models, repositories)
- ✅ Presentation layer (controllers)
- ✅ Tests (unit + integration)

**API Endpoints:** 9 endpoints реализовано

**Детали:** [Epics/E2_IMPLEMENTATION_STATUS.md](./Epics/E2_IMPLEMENTATION_STATUS.md)
@./Epics/E2_IMPLEMENTATION_STATUS.md

---

## 📊 Gap Analysis

| Метрика            | Текущее | Цель (MVP) | Gap | Статус       |
|--------------------|---------|------------|-----|--------------|
| Test Coverage      | ~88%    | 80%        | +8% | ✅ Превышен   |
| Mypy Errors        | **0**   | 0          | 0   | ✅ **FIXED**  |
| Architecture Score | 9/10    | 9/10       | 0   | ✅ **TARGET** |

---

## 🟡 P2 - Medium Priority (Phase 2)

### Security (5 issues)

| ID      | Задача                      | Оценка |
|---------|-----------------------------|--------|
| SEC-004 | Password Reset Token in URL | 1 день |
| SEC-006 | Timing Attack Protection    | 4 часа |
| SEC-007 | Email Logging Plaintext     | 2 часа |
| SEC-008 | CORS Restrictions           | 1 час  |
| SEC-009 | Catalog Auth Requirements   | 1 день |

### Architecture (2 issues)

| ID       | Задача                          | Оценка |
|----------|---------------------------------|--------|
| ARCH-004 | Remove Cross-Feature Coupling   | 2 дня  |
| ARCH-005 | Split Services Responsibilities | 3 дня  |

### Performance (3 issues)

| ID       | Задача                          | Оценка | Статус           |
|----------|---------------------------------|--------|------------------|
| PERF-004 | Redis Connection Pool           | 1 день | ✅ **ИСПРАВЛЕНО** |
| PERF-005 | Increase DB Pool Size           | 1 час  | ⚠️ Pending       |
| PERF-007 | Integrate Catalog Redis Cache   | 2 часа | ⚠️ Pending       |

**PERF-007 Детали:**
- **Файлы:** `core/dependencies.py`, `features/catalog/presentation/controllers.py`
- **Задача:** Подключить `CachedCategoryRepository` и `CachedServiceRepository` к DI
- **Условие:** Кеш используется только если Redis доступен (fail-open)
- **TTL:** 5 минут (уже настроено в `cached_repositories.py`)
- **Создано:** `infrastructure/cached_repositories.py` ✅

---

## 🟢 P3 - Low Priority (Future)

| ID       | Задача                          | Оценка |
|----------|---------------------------------|--------|
| SEC-011  | Security Headers Middleware     | 2 часа |
| PERF-006 | PostGIS Bounding Box Pre-Filter | 4 часа |
| SEC-010  | Improve Error Messages          | 2 часа |

---

## 🧪 Testing Gaps

| Проблема                          | Приоритет | Оценка | Статус     |
|-----------------------------------|-----------|--------|------------|
| Fix failed test (profiles)        | P1        | 1 час  | ⚠️ Pending |
| Add catalog API integration tests | P1        | 2 дня  | ⚠️ Pending |
| Add E2E tests (0 tests)           | P2        | 3 дня  | ⚠️ Pending |
| Add security audit tests          | P1        | 4 часа | ⚠️ Pending |

---

## 🎯 Target Metrics (Month 1)

| Метрика            | Цель |
|--------------------|------|
| Completed Bookings | 50   |
| Registered Users   | 100  |
| Active Providers   | 50   |
| Day-30 Retention   | 20%  |

---

## 🔗 Связанные документы

| Документ                                                                                                                                   | Описание                  |
|--------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|
| [00_IMPLEMENTATION_STATUS.md](./00_IMPLEMENTATION_STATUS.md) @./00_IMPLEMENTATION_STATUS.md                                                | Статус реализации         |
| [Security Improvements](../architecture/backend/15_SECURITY_IMPROVEMENTS.md) @../architecture/backend/15_SECURITY_IMPROVEMENTS.md          | Security audit results    |
| [Performance Improvements](../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md) @../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md | Performance audit results |
| [Business Logic Analysis](./BUSINESS_LOGIC_ANALYSIS.md) @./BUSINESS_LOGIC_ANALYSIS.md                                                      | Business logic issues     |
| [11_MVP_SCOPE.md](./11_MVP_SCOPE.md) @./11_MVP_SCOPE.md                                                                                    | MVP определение           |
| [01_USER_STORIES.md](./01_USER_STORIES.md) @./01_USER_STORIES.md                                                                           | User Stories              |
| [10_KPIS_AND_METRICS.md](./10_KPIS_AND_METRICS.md) @./10_KPIS_AND_METRICS.md                                                               | KPIs                      |

---

## 📝 Changelog

| Дата       | Версия | Изменения                                                                                                |
|------------|--------|----------------------------------------------------------------------------------------------------------|
| 12.03.2026 | 0.7.4  | **Code Review + Performance**: DB pagination, Redis cache layer (PERF-007 pending DI integration)        |
| 07.03.2026 | 0.7.2  | **Resend verification email**, TRUNCATE test isolation, frontend stub, 1016 tests (+122), E3 Booking 85% |
| 06.03.2026 | 0.7.1  | **All 40 mypy errors fixed** - Booking schemas, type annotations, Pool monitor, cleanup tasks            |
| 04.03.2026 | 0.7.0  | **E2 Catalog 100%** - All P0 issues fixed, E3 Booking started (Domain layer)                             |
| 04.03.2026 | 0.6.0  | **P1 Security/Performance fixes** - JWT claims, N+1 queries, Redis pool, Layer boundaries                |
| 03.03.2026 | 0.5.0  | **Security audit** - 29 issues found (5 CRITICAL fixed)                                                  |
| 27.02.2026 | 0.4.0  | PM Analysis: E5 → Phase 2, качество > скорость                                                           |
| 23.02.2026 | 0.3.0  | Email verification decision                                                                              |
| 21.02.2026 | 0.2.0  | Multi-role system complete                                                                               |
