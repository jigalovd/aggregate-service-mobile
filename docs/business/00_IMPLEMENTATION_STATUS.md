# Статус Реализации Бэкенда

**Дата:** 15.03.2026 | **Версия:** 0.8.2 | **Прогресс:** 100% ✅

---

## 📊 Executive Summary

### Ключевые метрики

| Метрика            | Текущее  | Цель (MVP) | Gap  | Статус          |
|--------------------|----------|------------|------|-----------------|
| Общий прогресс     | **100%** | 100%       | 0%   | ✅ **MVP READY** |
| Эпиков реализовано | 4/6      | 4/6 (MVP)  | 0    | ✅ Готово        |
| Test Coverage      | ~88%     | 80%        | +8%  | ✅ Готово        |
| Mypy Errors        | **0**    | 0          | 0    | ✅ **FIXED**     |
| Architecture Score | 9.5/10   | 9/10       | +0.5 | ✅ **EXCEEDED**  |

### Gap Analysis: Бизнес vs Реализация

| Эпик                     | Бизнес-требования | Реализация | Gap     | Блокер MVP   |
|--------------------------|-------------------|------------|---------|--------------|
| **E1: Auth & Identity**  | US-1.1 to US-1.5  | **100%** ✅ | 0%      | ✅ **Готово** |
| **E2: Catalog & Search** | US-2.1 to US-2.4  | **100%** ✅ | 0%      | ✅ **Готово** |
| **E3: Booking Engine**   | US-3.1 to US-3.6  | **100%** ✅ | 0%      | ✅ **Готово** |
| **E5: Reviews**          | US-5.1 to US-5.7  | 0%         | Phase 2 | Нет          |
| **E7: i18n**             | US-7.1 to US-7.8  | **100%** ✅ | 0%      | ✅ **Готово** |

### Принятые решения (15.03.2026)

| Решение                                  | Обоснование                             | Влияние          |
|------------------------------------------|-----------------------------------------|------------------|
| **SECURITY-002: Constant-Time Response** | Защита от timing attacks в email verify | +Security        |
| **SECURITY-003: Logout Error Logging**   | Логирование ошибок при отзыве токенов   | +Observability   |
| **SECURITY-004: Dynamic Dummy Hash**     | Генерация dummy bcrypt hash при запуске | +Security        |
| **PERF-002/003: Query Optimization**     | Eager loading + duplicate JOINs fix     | +Performance     |
| **ARCH-001: Explicit Lazy Loading**      | Явный lazy="selectin" для relationships | +Clarity         |
| **CODE-001: Config-Driven Constants**    | Magic numbers в Settings                | +Maintainability |

### Принятые решения (14.03.2026)

| Решение                                   | Обоснование                             | Влияние      |
|-------------------------------------------|-----------------------------------------|--------------|
| **Account Lockout Reset Fix**             | Сброс expired lockout перед инкрементом | +Security    |
| **Email Verification Token Blacklisting** | Предотвращение повторного использования | +Security    |
| **Redis Graceful Degradation**            | Fallback при недоступности Redis        | +Reliability |
| **GAP-002: Service Management CRUD**      | Provider services management API        | +Provider UX |
| **GAP-005: Cancellation Reason**          | Already implemented - verified          | +Analytics   |
| **MVP → 100% Complete**                   | All P0 gaps closed                      | ✅ **READY**  |

### ✅ MVP ГОТОВ К РЕЛИЗУ

**Все критичные gaps закрыты**

---

### ✅ P0 - Security Audit (ИСПРАВЛЕНО)

| Задача                         | Оценка     | Статус           |
|--------------------------------|------------|------------------|
| ~~SEC-001: User ID Injection~~ | ~~1 день~~ | ✅ **ИСПРАВЛЕНО** |
| ~~SEC-003: Debug Mode~~        | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** |
| ~~DB-001: Migration Chain~~    | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** |
| ~~DB-002: Duplicate Tables~~   | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** |
| ~~DB-003: PostGIS Type~~       | ~~4 часа~~ | ✅ **ИСПРАВЛЕНО** |

**Подробнее:**

- [Security Improvements](../architecture/backend/15_SECURITY_IMPROVEMENTS.md)
  @../architecture/backend/15_SECURITY_IMPROVEMENTS.md
- [Performance Improvements](../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md)
  @../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md
- [Business Logic Analysis](./BUSINESS_LOGIC_ANALYSIS.md) @./BUSINESS_LOGIC_ANALYSIS.md

### ✅ P1 - Security & Performance (ИСПРАВЛЕНО)

| Задача                         | Оценка     | Статус           |
|--------------------------------|------------|------------------|
| ~~SEC-002: JWT Claims~~        | ~~2 часа~~ | ✅ **ИСПРАВЛЕНО** |
| ~~PERF-001: N+1 Favorites~~    | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** |
| ~~PERF-004: Redis Pool~~       | ~~3 часа~~ | ✅ **ИСПРАВЛЕНО** |
| ~~ARCH-001: Layer Boundaries~~ | ~~4 часа~~ | ✅ **ИСПРАВЛЕНО** |
| ~~ARCH-003: Private Methods~~  | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** |
| ~~Mypy Errors~~                | ~~2 часа~~ | ✅ **0 ошибок**   |

### ✅ P2 - Code Quality & Security Hardening (ИСПРАВЛЕНО 15.03.2026)

| Задача                               | Оценка     | Статус           | Файл                |
|--------------------------------------|------------|------------------|---------------------|
| ~~SECURITY-002: Timing Attack~~      | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** | controllers.py      |
| ~~SECURITY-003: Logout Logging~~     | ~~30 мин~~ | ✅ **ИСПРАВЛЕНО** | controllers.py      |
| ~~SECURITY-004: Dynamic Dummy Hash~~ | ~~30 мин~~ | ✅ **ИСПРАВЛЕНО** | login.py            |
| ~~SECURITY-005: Password Strength~~  | ~~0 мин~~  | ✅ **Уже было**   | schemas.py          |
| ~~PERF-002: Provider Search N+1~~    | ~~2 часа~~ | ✅ **ИСПРАВЛЕНО** | repositories.py     |
| ~~PERF-003: Duplicate JOINs~~        | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** | repositories.py     |
| ~~ARCH-001: Explicit Lazy Loading~~  | ~~30 мин~~ | ✅ **ИСПРАВЛЕНО** | auth/models.py      |
| ~~CODE-001: Magic Numbers~~          | ~~1 час~~  | ✅ **ИСПРАВЛЕНО** | config.py, login.py |

**Детали исправлений:**

1. **SECURITY-002: Constant-Time Email Verification**
    - Добавлен `min_response_time = 0.15` для защиты от timing attacks
    - Гарантирует минимальное время ответа 150ms
    - Затрудняет email enumeration через время отклика

2. **SECURITY-003: Logout Error Logging**
    - Добавлено логирование ошибок при отзыве refresh_token
    - Добавлено логирование ошибок при blacklist access_token
    - Улучшает observability и debugging

3. **SECURITY-004: Dynamic Dummy Bcrypt Hash**
    - Dummy hash генерируется динамически при запуске приложения
    - Использует `get_password_hash(str(uuid.uuid4()))`
    - Устраняет возможность timing analysis через hardcoded hash

4. **PERF-002/003: Query Optimization**
    - Добавлен опциональный `include_services` параметр с eager loading
    - Устранены duplicate JOINs через флаг `joined_provider_service`
    - Оптимизация: N+1 запросов → 1 запрос при `include_services=True`

5. **ARCH-001: Explicit Lazy Loading**
    - Добавлен явный `lazy="selectin"` для всех relationships
    - Устраняет неоднозначность в загрузке связанных сущностей
    - Улучшает предсказуемость поведения ORM

6. **CODE-001: Config-Driven Constants**
    - `MAX_FAILED_ATTEMPTS` → `settings.max_failed_login_attempts`
    - `LOCKOUT_DURATION_MINUTES` → `settings.lockout_duration_minutes`
    - Magic numbers теперь в `app/config/config.py`

### 🟡 P1 - Следующий спринт

| Задача            | Оценка  | Приоритет | Статус                 |
|-------------------|---------|-----------|------------------------|
| Integration тесты | 2-3 дня | High      | ✅ **~266 тестов**      |
| E2E тесты         | 1-2 дня | Medium    | ✅ **9 тестов создано** |
| Performance тесты | 1 день  | High      | ✅ **3 теста создано**  |

---

## ✅ Реализовано

### E1: Onboarding & Identity (100% ✅)

**Auth API:**

- [x] POST `/api/v1/auth/register` - регистрация с multi-role
- [x] POST `/api/v1/auth/login` - логин
- [x] POST `/api/v1/auth/refresh` - refresh токены
- [x] GET `/api/v1/auth/me` - текущий пользователь
- [x] Email verification (API готов, middleware отключён)
- [x] Password reset flow
- [x] Profile management (GET/PATCH `/api/v1/profiles/me`)
- [x] Multi-role system (client + provider в одном аккаунте)

**Новые возможности (27.02-14.03.2026):**

- [x] HTTP-only cookies для refresh токенов (безопасность)
- [x] Rate limiting с SlowAPI (защита от брутфорса)
- [x] HTML email templates с локализацией (EN/RU/HE)
- [x] Scheduler для периодических задач
- [x] Типизированные dataclasses для токенов (`RefreshTokenData`, `PasswordResetTokenData`)
- [x] **Account Lockout Mechanism (улучшен)**
    - Reset expired lockout перед инкрементом failed attempts
    - Поля: `failed_login_attempts`, `locked_until`
    - Exception: `AccountLockedError` с `locked_until`
- [x] **Email Verification Token Blacklisting**
    - `jti` claim для уникальной идентификации токенов
    - Redis-based blacklist с TTL = token expiration
    - Graceful degradation при недоступности Redis
    - Exception: `TokenAlreadyUsedError`
- [x] **Redis Graceful Degradation**
    - `RedisUnavailableError` exception
    - Warning логирование при недоступности
    - Fallback поведение (skip blacklist check)
- [x] **Comprehensive test coverage (~88%)**
    - **Unit tests (~560)**: Domain, Application, Infrastructure, Core layers
    - **Integration tests (~539)**: API endpoints, database interactions, security scenarios
    - **Total: ~1099 tests** covering auth flow, password reset, token management, rate limiting, account lockout, token
      blacklisting
- [x] Integration tests для auth endpoints:
    - Token refresh flow
    - Password reset flow
    - Rate limiting scenarios
    - Security scenarios (token expiration, invalid tokens)
    - Logout flow
    - Token blacklist
    - JWT boundary cases
- [x] **Resend verification email** (без утечки информации о существовании аккаунта)
- [x] **SendGrid Spike** (Sprint 2) - документация и тестирование
- [x] **Robust SendGrid test script** - `scripts/test_sendgrid.py`
- [x] **Production enablement guide** - документация в `docs/sprints/SPRINT_2_SENDGRID_SPIKE.md`

**✅ Sprint 2 Complete (10.03.2026):**

- SendGrid integration tested and validated
- Email verification flow documented
- Production enablement guide created
- 32 verification-related tests passing

**Детали:
** [03_AUTH.md](../architecture/backend/03_AUTH.md)
@../architecture/backend/03_AUTH.md, [09_MULTI_ROLE_SYSTEM.md](../architecture/backend/09_MULTI_ROLE_SYSTEM.md)
@../architecture/backend/09_MULTI_ROLE_SYSTEM.md

---

### E2: Catalog & Geo-Search (100% ✅)

**Дата завершения:** 03.03.2026  
**Время выполнения:** 5 дней (план: 14-21 день)  
**Эффективность:** 🚀 47% быстрее плана

**Реализовано:**

- [x] **Database Schema (5 таблиц + PostGIS)**
    - `categories` - иерархия категорий с i18n
    - `services` - услуги с ценами и duration
    - `providers` - мастера с GEOMETRY location
    - `provider_services` - M2M связь мастер-услуга
    - `favorites` - избранное пользователей
- [x] **PostGIS Integration**
    - GEOMETRY(POINT, 4326) для координат
    - GiST index на `providers.location`
    - Helper functions: km_to_degrees(), degrees_to_km()
- [x] **Domain Layer**
    - Value Objects: Location, PriceRange, I18nString
    - Entities: Category, Service, Provider, Favorite
    - Exceptions: domain-specific errors
- [x] **Application Layer**
    - Protocols: Repository & UoW interfaces
    - Unit of Work pattern
- [x] **Infrastructure Layer**
    - SQLAlchemy models with PostGIS
    - Repositories with geo-search
    - Mappers (ORM ↔ Entity)
    - Pydantic v2 schemas
- [x] **Presentation Layer**
    - FastAPI controllers
    - Dependency injection
- [x] **Tests**
    - Unit tests (domain, value objects)
    - Integration tests (repositories, PostGIS)

**API Endpoints (9):**

- [x] `GET /api/v1/catalog/categories` - список категорий
- [x] `GET /api/v1/catalog/categories/{id}` - детали категории
- [x] `GET /api/v1/catalog/services` - список услуг
- [x] `GET /api/v1/catalog/services/{id}` - детали услуги
- [x] `POST /api/v1/catalog/providers/search` - поиск по геолокации
- [x] `GET /api/v1/catalog/providers/{id}` - детали провайдера
- [x] `POST /api/v1/catalog/favorites` - добавить в избранное
- [x] `GET /api/v1/catalog/favorites` - список избранного
- [x] `DELETE /api/v1/catalog/favorites/{provider_id}` - удалить из избранного

**PostGIS Features:**

- ✅ GEOMETRY(POINT, 4326) для координат
- ✅ GiST index для O(log N) geo-поиска
- ✅ KNN operator `<->` для сортировки по расстоянию
- ✅ Optimized for Israel (±0.05% error)

**Детали:**

- [Epics/E2_IMPLEMENTATION_STATUS.md](./Epics/E2_IMPLEMENTATION_STATUS.md) @./Epics/E2_IMPLEMENTATION_STATUS.md
- [Epics/E2_CATALOG_PLAN_GEOMETRY.md](./Epics/E2_CATALOG_PLAN_GEOMETRY.md) @./Epics/E2_CATALOG_PLAN_GEOMETRY.md

---

### E7: Internationalization (100%)

- [x] Middleware для автоопределения языка
- [x] Gettext система (RU/HE/EN)
- [x] Locale файлы (.po)
- [x] Локализованные исключения

**Детали:** [04_I18N_IMPLEMENTATION.md](../architecture/backend/04_I18N_IMPLEMENTATION.md)
@../architecture/backend/04_I18N_IMPLEMENTATION.md

---

### Infrastructure (Ready)

| Компонент                            | Статус    |
|--------------------------------------|-----------|
| PostgreSQL + SQLAlchemy 2.0 (async)  | ✅         |
| PostGIS Extension                    | ✅         |
| Alembic миграции                     | ✅         |
| Connection pooling                   | ✅         |
| Unit of Work pattern                 | ✅         |
| Clean Architecture слои              | ✅         |
| Structured logging                   | ✅         |
| Error handling                       | ✅         |
| Background Task Scheduler            | ✅         |
| Rate Limiting (SlowAPI)              | ✅         |
| Architecture Diagrams (Mermaid)      | ✅         |
| SharedKernel (UserContext)           | ✅         |
| **Redis Cache with Connection Pool** | ✅ **NEW** |
| **JWT Claims (jti, iat)**            | ✅ **NEW** |
| **Eager Loading (N+1 fixed)**        | ✅ **NEW** |

---

## 🟡 В процессе

### E3: Booking Engine (100% ✅)

**Статус:** MVP Complete - All layers implemented

**Реализовано:**

- [x] **Domain Layer** (100%)
    - Entities: Booking, BookingItem, ScheduleRule, BookingRescheduleHistory
    - Value Objects: BookingStatus
    - Services: SlotGenerator
    - Exceptions: 10+ domain-specific exceptions
    - **Enhanced:** Added `completed_at`, `completed_by`, `no_show_marked_at`, `no_show_marked_by` fields
- [x] **Application Layer** (100%)
    - Use Cases: Create, Cancel, Confirm, Complete, Reschedule, GetSlots, MarkNoShow, ValidateServices (8 use cases)
        - Protocols: BookingRepository, ScheduleRuleRepository
        - Unit of Work: BookingUnitOfWork
    - **NEW:** MarkNoShowUseCase (GAP-004)
    - **NEW:** ValidateServicesUseCase (GAP-003)
- [x] **Infrastructure Layer** (100%)
    - Models: Booking, BookingItem, ScheduleRule, BookingRescheduleHistory (4 models)
    - Repositories: BookingRepository, ScheduleRuleRepository
    - Mappers: Domain ↔ ORM conversion
    - Schemas: Pydantic v2 request/response
    - **Enhanced:** Added `completed_at`, `completed_by`, `no_show_marked_at`, `no_show_marked_by` to models
- [x] **Presentation Layer** (100%)
    - Controllers: BookingController, ScheduleController
    - Endpoints: 16 API endpoints (8 booking + 6 schedule + 1 slots + 1 no-show)
        - Dependencies: DI configuration
    - **NEW:** `PATCH /bookings/{id}/no-show` endpoint (GAP-004)
    - **NEW:** Date range filters for booking history (GAP-007)
- [x] **Tests** (90%)
    - Unit tests: Domain entities, use cases (34 tests)
    - Integration tests: API endpoints (41 tests) ✅
    - **E2E tests:** 9 critical scenarios ✅ **NEW**
        - **Performance tests:** 3 benchmarks ✅ **NEW**
    - Coverage: ~88% ✅

**✅ Sprint 4 Complete (12.03.2026):**

- [x] **GAP-001**: Provider Schedule Management - Router registered (5 endpoints)
- [x] **GAP-002**: Service Management CRUD - 5 endpoints for provider services ✅ **NEW**
- [x] **GAP-003**: Multi-Service Validation - Use case created
- [x] **GAP-004**: No-Show Tracking API - Endpoint created
- [x] **GAP-005**: Booking Cancellation Reason - Already implemented ✅ **VERIFIED**
- [x] **GAP-006**: Profile No-Show Fields - Fields added
- [x] **GAP-007**: Booking History Enhancement - Date range filters added

**✅ E3 Booking Engine: 100% Complete**

**API Endpoints (21):**

- Booking: 8 endpoints (create, get, cancel, confirm, complete, reschedule, no-show, client/provider history)
- Schedule: 5 endpoints (CRUD for schedule rules)
- Slots: 1 endpoint (get available slots)
- Provider Services: 5 endpoints (CRUD for provider services) ✅ **NEW**

**Оценка:** MVP Complete | **Приоритет:** P0 | **Статус:** ✅ **READY**

---

### Phase 2 (Не MVP)

| Эпик                   | Описание            | Оценка     |
|------------------------|---------------------|------------|
| E4: Service Management | CRUD услуг мастеров | 10-14 дней |
| E5: Reputation System  | Отзывы и рейтинги   | 14-21 день |
| E6: Notifications      | Push-уведомления    | 14-21 день |

---

## 🗄️ База данных

**Созданные таблицы:**

- Auth: `users`, `roles`, `user_roles`, `user_contexts`, `profiles`, `password_reset_tokens`, `refresh_tokens`
- Catalog: `categories`, `services`, `providers`, `provider_services`, `favorites`
- Booking: `bookings`, `booking_items`, `schedule_rules`, `booking_reschedule_history`

**Profiles Table Enhancement (GAP-006):**

- `no_show_count` INTEGER DEFAULT 0 - количество пропущенных бронирований
- `no_show_rate` NUMERIC(3,2) DEFAULT 0.00 - соотношение no-show к общему количеству бронирований

**PostGIS Features:**

- ✅ Extension installed
- ✅ GEOMETRY columns
- ✅ GiST indexes

**Детальная схема:** [02_DATABASE_SCHEMA.md](../architecture/backend/02_DATABASE_SCHEMA.md)
@../architecture/backend/02_DATABASE_SCHEMA.md

---

## 📁 Структура проекта

**Feature-First:**

- `features/auth/` - ✅ 100%
- `features/profiles/` - ✅ 100%
- `features/catalog/` - ✅ 100% (includes provider services CRUD)
- `features/booking/` - ✅ 100%
- `features/reviews/` - ❌ 0% (Phase 2)
- `shared_kernel/` - ✅ UserContext

**Детали:** [00_ARCHITECTURE_OVERVIEW.md](../architecture/backend/00_ARCHITECTURE_OVERVIEW.md)
@../architecture/backend/00_ARCHITECTURE_OVERVIEW.md

---

## 🔗 Связанные документы

| Документ                                                                                                                                   | Описание                  |
|--------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|
| [BACKLOG.md](./BACKLOG.md) @./BACKLOG.md                                                                                                   | Задачи и приоритеты       |
| [Security Improvements](../architecture/backend/15_SECURITY_IMPROVEMENTS.md) @../architecture/backend/15_SECURITY_IMPROVEMENTS.md          | Security audit results    |
| [Performance Improvements](../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md) @../architecture/backend/16_PERFORMANCE_IMPROVEMENTS.md | Performance audit results |
| [Business Logic Analysis](./BUSINESS_LOGIC_ANALYSIS.md) @./BUSINESS_LOGIC_ANALYSIS.md                                                      | Business logic issues     |
| [11_MVP_SCOPE.md](./11_MVP_SCOPE.md) @./11_MVP_SCOPE.md                                                                                    | MVP определение           |
| [01_USER_STORIES.md](./01_USER_STORIES.md) @./01_USER_STORIES.md                                                                           | User Stories              |
| [10_KPIS_AND_METRICS.md](./10_KPIS_AND_METRICS.md) @./10_KPIS_AND_METRICS.md                                                               | KPIs и метрики            |
| [backend/README.md](../architecture/backend/README.md) @../architecture/backend/README.md                                                  | Backend архитектура       |
| [API Gaps Plan](../plans/API_GAPS_CLOSURE_PLAN.md) @../plans/API_GAPS_CLOSURE_PLAN.md                                                      | P0/P1/P2 API gaps plan    |
| [Sprint 3 Status](../sprints/SPRINT_3_P0_GAPS_STATUS.md) @../sprints/SPRINT_3_P0_GAPS_STATUS.md                                            | Sprint 3 progress         |

---

## 📝 Changelog

| Дата       | Версия | Изменения                                                                                                             |
|------------|--------|-----------------------------------------------------------------------------------------------------------------------|
| 14.03.2026 | 0.8.1  | **Security Hardening** - Account lockout reset fix, Email verification token blacklisting, Redis graceful degradation |
| 12.03.2026 | 0.8.0  | **MVP COMPLETE** - GAP-002 (Provider Services CRUD), GAP-005 verified, E3 Booking 100%, Total 100% ✅                  |
| 11.03.2026 | 0.7.5  | **Sprint 3 Progress** - 5/7 P0 gaps completed (GAP-001, GAP-003, GAP-004, GAP-006, GAP-007), Security hardening       |
| 10.03.2026 | 0.7.4  | **Sprint 1 Complete** - E2E tests (9), Performance tests (3), Data model enhancements, Coverage 88%                   |
| 09.03.2026 | 0.7.3  | **Dependencies update**, bcrypt 5.x, SQLAlchemy relationships fixes, code formatting                                  |
| 07.03.2026 | 0.7.2  | **Resend verification email**, TRUNCATE test isolation, frontend stub, 1016 tests (+122)                              |
| 06.03.2026 | 0.7.1  | **All 40 mypy errors fixed** - Booking schemas, type annotations, Pool monitor, cleanup tasks                         |
| 04.03.2026 | 0.6.0  | **P1 Security/Performance fixes** - JWT claims, N+1 queries, Redis pool, Layer boundaries                             |
| 04.03.2026 | 0.5.0  | **E2 Catalog 100%** - Database, Domain, Application, Infrastructure, Presentation, Tests                              |
| 04.03.2026 | 0.4.7  | **P0 Audit Issues Fixed** - SEC-001, SEC-003, DB-001, DB-002, DB-003, ARCH-001                                        |
| 03.03.2026 | 0.4.6  | **Security audit** - 29 issues found (5 CRITICAL, 11 HIGH, 10 MEDIUM, 3 LOW)                                          |
| 02.03.2026 | 0.4.5  | **Integration tests (139)** - Token refresh, password reset, security scenarios, rate limiting                        |
| 01.03.2026 | 0.4.4  | **Unit tests coverage 93% (571 tests)** - Domain, Application, Infrastructure, Core layers                            |
| 01.03.2026 | 0.4.3  | Email templates, HTTP-only cookies, rate limiting, scheduler, diagrams, mypy=0                                        |
| 27.02.2026 | 0.4.2  | PM Analysis: E5 → Phase 2, качество > скорость                                                                        |
| 27.02.2026 | 0.4.1  | DB Subsystem refactoring (UoW, Protocols)                                                                             |
| 24.02.2026 | 0.4.0  | Clean Architecture migration                                                                                          |
| 23.02.2026 | 0.3.0  | Email verification decision                                                                                           |
