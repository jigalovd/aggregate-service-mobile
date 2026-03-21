# Beauty Service Aggregator - Backend API Reference

**Версия:** 0.8.2
**Дата:** Март 2026
**Статус:** MVP Phase
**Базовый URL:** `https://api.beauty-service.com/api/v1`

---

## Содержание

1. [Обзор архитектуры](#1-обзор-архитектуры)
2. [Технический стек](#2-технический-стек)
3. [Аутентификация и безопасность](#3-аутентификация-и-безопасность)
4. [Модели данных](#4-модели-данных)
5. [API Endpoints](#5-api-endpoints)
6. [Статусы и коды ответов](#6-статусы-и-коды-ответов)
7. [Бизнес-правила](#7-бизнес-правила)
8. [Локализация](#8-локализация)
9. [Rate Limiting](#9-rate-limiting)
10. [WebSocket и Real-time](#10-websocket-и-real-time)

---

## 1. Обзор архитектуры

### 1.1 Архитектурный стиль

**Feature-First + Clean Architecture + DDD**

```
backend/app/features/
├── auth/                    # Аутентификация и пользователи
│   ├── domain/              # Сущности, Value Objects, исключения
│   ├── application/         # Use Cases, протоколы, Unit of Work
│   ├── infrastructure/      # SQLAlchemy модели, Pydantic схемы
│   └── presentation/        # FastAPI контроллеры
├── booking/                 # Бронирования
├── catalog/                 # Каталог услуг
├── profiles/                # Профили пользователей
└── ...
```

### 1.2 Слои и зависимости

**Правило:** Зависимости направлены внутрь (Dependency Inversion)

```
┌─────────────────────────────────────┐
│  Presentation (FastAPI Routers)     │  ← Внешний слой
├─────────────────────────────────────┤
│  Application (Use Cases, Protocols) │
├─────────────────────────────────────┤
│  Infrastructure (ORM, Schemas)      │
├─────────────────────────────────────┤
│  Domain (Entities, Business Logic)  │  ← Внутренний слой
└─────────────────────────────────────┘
```

### 1.3 Ключевые паттерны

- **Repository Pattern** - Протоколы в application, реализации в infrastructure
- **Unit of Work** - Управление транзакциями на feature
- **UserContext** - DTO для авторизованного пользователя (из JWT)
- **Shared Kernel** - Общие DTOs для cross-feature коммуникации

### 1.4 База данных

**PostgreSQL 15+ + PostGIS**

- **Schema:** `app`
- **Migration strategy:** Единая миграция для MVP (`20260304_1000_v1_initial_schema.py`)
- **Connection pooling:** Async SQLAlchemy с pool size 5-15
- **Indexing:** GiST для гео-запросов, B-tree для UUID lookup

---

## 2. Технический стек

### 2.1 Backend Framework

| Компонент | Версия | Назначение |
|-----------|--------|------------|
| **Python** | 3.12+ | Runtime |
| **FastAPI** | 0.109+ | Web framework |
| **Uvicorn** | 0.27+ | ASGI server |
| **Pydantic** | 2.6+ | Validation, serialization |

### 2.2 Database & ORM

| Компонент | Версия | Назначение |
|-----------|--------|------------|
| **SQLAlchemy** | 2.0.25+ | Async ORM |
| **asyncpg** | 0.30+ | Async PostgreSQL driver |
| **Alembic** | 1.13+ | Migrations |
| **GeoAlchemy2** | 0.14+ | PostGIS integration |

### 2.3 Security & Auth

| Компонент | Версия | Назначение |
|-----------|--------|------------|
| **python-jose** | 3.3+ | JWT (cryptography) |
| **bcrypt** | 5.0+ | Password hashing |
| **slowapi** | 0.1.9+ | Rate limiting |

### 2.4 Infrastructure

| Компонент | Версия | Назначение |
|-----------|--------|------------|
| **Redis** | 5.0+ | Token blacklist, cache |
| **SendGrid** | 6.11+ | Email service |
| **Babel** | 2.14+ | i18n |

---

## 3. Аутентификация и безопасность

### 3.1 JWT токены

#### Access Token

```json
{
  "sub": "user-uuid",
  "exp": 1741234567,
  "iat": 1741233667,
  "type": "access",
  "jti": "token-uuid",
  "iss": "beauty-service",
  "aud": "beauty-service-clients",
  "current_role": "client"  // Для multi-role users
}
```

**Характеристики:**
- **Алгоритм:** HS256 (whitelisted: HS256, HS384, HS512)
- **Срок действия:** 15 минут (конфигурируемо)
- **Передача:** Authorization: Bearer `<token>`
- **Blacklist:** Redis (ключ: `blacklist:{jti}`)

#### Refresh Token

```json
{
  "sub": "user-uuid",
  "exp": 1743912000,
  "type": "refresh",
  "jti": "token-uuid"
}
```

**Характеристики:**
- **Срок действия:** 30 дней
- **Хранение:** HTTP-only cookie (secure, samesite=strict)
- **Rotatable:** Каждый refresh обновляет токен
- **DB storage:** Хэш в таблице `app.refresh_tokens`

### 3.2 Auth Flow

#### Регистрация

```
POST /api/v1/auth/register
Request:
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "roles": ["client"],
  "phone": "+972501234567",
  "language_code": "ru"
}

Response 201:
{
  "access_token": "eyJ...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "is_active": true,
    "is_verified": false,
    "roles": ["client"],
    "current_role": "client",
    "created_at": "2026-03-20T10:00:00Z"
  }
}

Set-Cookie: refresh_token=eyJ...; HttpOnly; Secure; SameSite=Strict; Max-Age=2592000; Path=/api/v1/auth
```

#### Вход

```
POST /api/v1/auth/login
Request:
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

Response 200:
{
  "access_token": "eyJ...",
  "user": { ... }
}

Set-Cookie: refresh_token=eyJ...
```

#### Обновление токена

```
POST /api/v1/auth/refresh
Cookie: refresh_token=eyJ...

Response 200:
{
  "access_token": "eyJ...",
  "user": { ... }
}

Set-Cookie: refresh_token=eyJ...  // Новый refresh token
```

#### Выход

```
POST /api/v1/auth/logout
Authorization: Bearer <access_token>
Cookie: refresh_token=eyJ...

Response 200:
{
  "message": "Logged out successfully"
}

// Backend:
// 1. Ревокет refresh token в БД
// 2. Добавляет access token jti в blacklist (Redis, TTL=15min)
// 3. Удаляет cookie
```

### 3.3 UserContext

**Критически важный DTO** для cross-feature коммуникации:

```python
@dataclass(frozen=True)
class UserContext:
    user_id: UUID
    email: str
    roles: list[str]           # ["client"], ["provider"], или ["client", "provider"]
    current_role: str | None   # Выбранная роль для multi-role users
    is_active: bool
    is_verified: bool
    created_at: datetime | None
```

**Использование в контроллерах:**

```python
@router.post("/bookings")
async def create_booking(
    current_user: Annotated[UserContext, Depends(get_current_user_context)],
    ...
):
    # user_id ВСЕГДА берется из JWT, НЕ из request body!
    client_id = current_user.user_id
```

### 3.4 Multi-Role Users

**Варианты реализации:**

#### Вариант A: Single User, Multiple Roles (Текущая реализация)

```
User: uuid-123
├── Roles: ["client", "provider"]
└── Current Role: "provider" (switchable)

POST /api/v1/users/me/roles
{
  "role": "provider"  // Switch to provider context
}
```

**Плюсы:**
- Единый аккаунт для клиента и мастера
- Удобная смена роли

**Минусы:**
- Сложности с permissions
- Может запутать UX

#### Вариант B: Separate Accounts (Альтернатива)

```
Client Account: uuid-123 (client role only)
Provider Account: uuid-456 (provider role only)
Linked Accounts: {uuid-123 ↔ uuid-456}
```

**Плюсы:**
- Четкое разделение прав
- Проще permissions

**Минусы:**
- Два аккаунта для одного пользователя
- Сложнее linking

**Рекомендация:** Оставить Вариант A для MVP, рассмотреть B для Phase 2.

### 3.5 Account Lockout (SEC-001, CVSS 9.1)

**Политика:**
- **Max attempts:** 5
- **Lockout duration:** 15 минут
- **Reset:** После успешного логина

**Flow:**
```
1. Failed login → failed_login_attempts++
2. failed_login_attempts >= 5 → locked_until = now() + 15min
3. Successful login → failed_login_attempts = 0, locked_until = NULL
```

**Response 423 Locked:**
```json
{
  "detail": "Account locked until 2026-03-20T10:15:00Z. Reason: Too many failed login attempts."
}
```

---

## 4. Модели данных

### 4.1 Core Tables

#### Users

```sql
CREATE TABLE app.users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMPTZ,
    language_code   VARCHAR(5) NOT NULL DEFAULT 'ru',
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT check_language_code CHECK (language_code IN ('ru', 'he', 'en'))
);

CREATE INDEX ix_users_email ON app.users(email);
CREATE INDEX ix_users_locked_until ON app.users(locked_until) WHERE locked_until IS NOT NULL;
```

#### Roles

```sql
CREATE TABLE app.roles (
    id          INTEGER PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed data:
INSERT INTO app.roles (id, name, description) VALUES
(1, 'client', 'Клиент, который бронирует услуги'),
(2, 'provider', 'Поставщик услуг, мастер или салон');
```

#### User Roles (Many-to-Many)

```sql
CREATE TABLE app.user_roles (
    id        UUID PRIMARY KEY,
    user_id   UUID NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
    role_id   INTEGER NOT NULL REFERENCES app.roles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, role_id)
);
```

#### User Contexts (Current Role)

```sql
CREATE TABLE app.user_contexts (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL UNIQUE REFERENCES app.users(id) ON DELETE CASCADE,
    current_role_id INTEGER NOT NULL REFERENCES app.roles(id) ON DELETE CASCADE,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### Profiles

```sql
CREATE TABLE app.profiles (
    id             UUID PRIMARY KEY,
    user_id        UUID NOT NULL UNIQUE REFERENCES app.users(id) ON DELETE CASCADE,
    full_name      VARCHAR(255),
    phone          VARCHAR(20) UNIQUE,
    avatar_url     TEXT,
    no_show_count  INTEGER NOT NULL DEFAULT 0,
    no_show_rate   NUMERIC(3,2) NOT NULL DEFAULT 0.00 CHECK (no_show_rate >= 0 AND no_show_rate <= 1),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### Refresh Tokens

```sql
CREATE TABLE app.refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) UNIQUE NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    is_revoked  BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Composite index для active token lookup
CREATE INDEX ix_refresh_tokens_active_lookup
ON app.refresh_tokens(user_id, is_revoked, expires_at)
WHERE is_revoked = FALSE;
```

### 4.2 Catalog Tables

#### Categories

```sql
CREATE TABLE app.categories (
    id          UUID PRIMARY KEY,
    parent_id   UUID REFERENCES app.categories(id) ON DELETE SET NULL,
    name_i18n   JSONB NOT NULL,  -- {"ru": "Стрижка", "he": "תספורת", "en": "Haircut"}
    icon_url    TEXT,
    sort_order  INTEGER,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### Services

```sql
CREATE TABLE app.services (
    id                  UUID PRIMARY KEY,
    category_id         UUID NOT NULL REFERENCES app.categories(id) ON DELETE RESTRICT,
    title_i18n          JSONB NOT NULL,
    description_i18n    JSONB,
    base_price          INTEGER,
    price_range_id      UUID REFERENCES app.price_ranges(id) ON DELETE SET NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'ILS',
    duration_minutes    INTEGER NOT NULL,
    is_combinable       BOOLEAN NOT NULL DEFAULT TRUE,
    is_price_variable   BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### Providers

```sql
CREATE TABLE app.providers (
    id              UUID PRIMARY KEY,
    user_id         UUID UNIQUE REFERENCES app.users(id) ON DELETE SET NULL,
    display_name    VARCHAR(255) NOT NULL,
    bio             TEXT,
    avatar_url      TEXT,
    phone           VARCHAR(20),
    location        GEOGRAPHY(POINT, 4326),  -- PostGIS
    address         TEXT,
    rating_cached   NUMERIC(3,2),
    reviews_count   INTEGER NOT NULL DEFAULT 0,
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- GiST index для geo search
CREATE INDEX ix_providers_location_gist
ON app.providers USING GIST(location);
```

#### Favorites

```sql
CREATE TABLE app.favorites (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
    provider_id  UUID NOT NULL REFERENCES app.providers(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, provider_id)
);
```

### 4.3 Booking Tables

#### Bookings

```sql
CREATE TABLE app.bookings (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES app.users(id) ON DELETE RESTRICT,
    client_id           UUID NOT NULL REFERENCES app.users(id) ON DELETE RESTRICT,
    start_time          TIMESTAMPTZ NOT NULL,
    end_time            TIMESTAMPTZ NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_price         INTEGER NOT NULL,
    total_duration_minutes INTEGER NOT NULL,
    notes               TEXT,
    confirmed_at        TIMESTAMPTZ,
    confirmed_by        UUID REFERENCES app.users(id),
    cancelled_at        TIMESTAMPTZ,
    cancelled_by        UUID REFERENCES app.users(id),
    cancellation_reason TEXT,
    completed_at        TIMESTAMPTZ,
    no_show_marked_at   TIMESTAMPTZ,
    no_show_marked_by   UUID REFERENCES app.users(id),
    reschedule_count    INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT check_status CHECK (
        status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'EXPIRED', 'NO_SHOW')
    )
);

-- Index для provider bookings
CREATE INDEX ix_bookings_provider_id ON app.bookings(provider_id, start_time DESC);

-- Index для client bookings
CREATE INDEX ix_bookings_client_id ON app.bookings(client_id, start_time DESC);
```

#### Booking Items

```sql
CREATE TABLE app.booking_items (
    id                UUID PRIMARY KEY,
    booking_id        UUID NOT NULL REFERENCES app.bookings(id) ON DELETE CASCADE,
    service_id        UUID NOT NULL REFERENCES app.services(id) ON DELETE RESTRICT,
    price             INTEGER NOT NULL,
    duration_minutes  INTEGER NOT NULL
);
```

#### Booking Reschedule History

```sql
CREATE TABLE app.booking_reschedule_history (
    id                UUID PRIMARY KEY,
    booking_id        UUID NOT NULL REFERENCES app.bookings(id) ON DELETE CASCADE,
    old_start_time    TIMESTAMPTZ NOT NULL,
    new_start_time    TIMESTAMPTZ NOT NULL,
    rescheduled_by    UUID NOT NULL REFERENCES app.users(id),
    reason            TEXT,
    rescheduled_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### Schedule Rules

```sql
CREATE TABLE app.schedule_rules (
    id              UUID PRIMARY KEY,
    provider_id     UUID NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
    weekday         SMALLINT NOT NULL CHECK (weekday BETWEEN 1 AND 7),  -- 1=Mon, 7=Sun
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, weekday, start_time, end_time)
);
```

### 4.4 Provider Services (Portfolio)

```sql
CREATE TABLE app.provider_services (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES app.providers(id) ON DELETE CASCADE,
    service_id          UUID NOT NULL REFERENCES app.services(id) ON DELETE RESTRICT,
    price               INTEGER NOT NULL,
    duration_minutes    INTEGER NOT NULL,
    description_i18n    JSONB,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, service_id)
);
```

#### Portfolio Items

```sql
CREATE TABLE app.portfolio_items (
    id              UUID PRIMARY KEY,
    provider_id     UUID NOT NULL REFERENCES app.providers(id) ON DELETE CASCADE,
    title_i18n      JSONB NOT NULL,
    description_i18n JSONB,
    image_url       TEXT NOT NULL,
    service_id      UUID REFERENCES app.services(id) ON DELETE SET NULL,
    sort_order      INTEGER,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 5. API Endpoints

### 5.1 Аутентификация (`/api/v1/auth`)

#### POST /register

**Регистрация нового пользователя**

```http
POST /api/v1/auth/register
Content-Type: application/json
Accept-Language: ru

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "roles": ["client"],
  "phone": "+972501234567",
  "language_code": "ru"
}
```

**Validation Rules:**
- `email`: Валидный email, уникальный
- `password`: Минимум 8 символов
- `roles`: Массив, валидные значения `["client"]`, `["provider"]`, или `["client", "provider"]`
- `phone`: Опционально, формат `+XXXXXXXXXXX`
- `language_code`: `ru`, `he`, или `en`

**Response 201:**
```json
{
  "access_token": "eyJ...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "is_active": true,
    "is_verified": false,
    "roles": ["client"],
    "current_role": "client",
    "created_at": "2026-03-20T10:00:00Z"
  }
}
```

**Errors:**
- `409 Conflict`: Email уже существует
- `422 Unprocessable Entity`: Validation error

**Rate Limit:** 5/hour per IP

---

#### POST /login

**Вход в систему**

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response 200:**
```json
{
  "access_token": "eyJ...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "is_active": true,
    "is_verified": true,
    "roles": ["client", "provider"],
    "current_role": "client",
    "created_at": "2026-03-20T10:00:00Z"
  }
}
```

**Set-Cookie:**
```
refresh_token=eyJ...; HttpOnly; Secure; SameSite=Strict; Max-Age=2592000; Path=/api/v1/auth
```

**Errors:**
- `401 Unauthorized`: Неверный email/пароль
- `423 Locked`: Аккаунт заблокирован (см. Account Lockout)
- `401 Unauthorized`: Пользователь неактивен

**Rate Limit:** 10/minute per IP

---

#### GET /me

**Получить текущего пользователя**

```http
GET /api/v1/auth/me
Authorization: Bearer <access_token>
```

**Response 200:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "is_active": true,
  "is_verified": true,
  "roles": ["client", "provider"],
  "current_role": "provider",
  "created_at": "2026-03-20T10:00:00Z"
}
```

**Errors:**
- `401 Unauthorized`: Не авторизован

---

#### POST /verify-email

**Подтвердить email**

```http
POST /api/v1/auth/verify-email
Content-Type: application/json

{
  "token": "uuid-token-from-email"
}
```

**Response 200:**
```json
{
  "message": "Email verified successfully"
}
```

**Errors:**
- `400 Bad Request`: Токен недействителен, истек, или уже использован

**Rate Limit:** 5/hour per IP

---

#### POST /resend-verification

**Отправить повторное письмо подтверждения**

```http
POST /api/v1/auth/resend-verification
Content-Type: application/json
Accept-Language: ru

{
  "email": "user@example.com"
}
```

**Response 200:**
```json
{
  "message": "If the email exists and is not verified, a verification email has been sent"
}
```

**Security:** Всегда возвращает success (не раскрывает существование email)

**Rate Limit:** 5/hour per IP

---

#### POST /forgot-password

**Запрос на сброс пароля**

```http
POST /api/v1/auth/forgot-password
Content-Type: application/json
Accept-Language: ru

{
  "email": "user@example.com"
}
```

**Response 200:**
```json
{
  "message": "If the email exists, a password reset link has been sent"
}
```

**Security:** Всегда возвращает success

**Rate Limit:** 5/hour per IP

---

#### POST /reset-password

**Сбросить пароль с токеном**

```http
POST /api/v1/auth/reset-password
Content-Type: application/json

{
  "token": "uuid-token-from-email",
  "new_password": "NewSecurePass123!"
}
```

**Response 200:**
```json
{
  "message": "Password reset successfully"
}
```

**Side Effects:**
- Все refresh tokens revoked
- Все access tokens добавлены в blacklist

**Rate Limit:** 10/hour per IP

---

#### POST /refresh

**Обновить access token**

```http
POST /api/v1/auth/refresh
Cookie: refresh_token=eyJ...
```

Или:

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJ..."
}
```

**Response 200:**
```json
{
  "access_token": "eyJ...",
  "user": { ... }
}
```

**Set-Cookie:** Новый refresh token

**Rate Limit:** 20/minute per IP

---

#### POST /logout

**Выход из системы**

```http
POST /api/v1/auth/logout
Authorization: Bearer <access_token>
Cookie: refresh_token=eyJ...
```

**Response 200:**
```json
{
  "message": "Logged out successfully"
}
```

**Side Effects:**
- Refresh token revoked в БД
- Access token jti добавлен в blacklist (TTL=15min)
- Cookie удалена

---

### 5.2 User Roles (`/api/v1/users`)

#### GET /me/roles

**Получить роли пользователя**

```http
GET /api/v1/users/me/roles
Authorization: Bearer <access_token>
```

**Response 200:**
```json
{
  "roles": ["client", "provider"],
  "current_role": "client"
}
```

---

#### POST /me/roles

**Добавить роль пользователю**

```http
POST /api/v1/users/me/roles
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "role": "provider"
}
```

**Response 201:**
```json
{
  "message": "Role 'provider' added successfully"
}
```

**Errors:**
- `400 Bad Request`: Роль уже существует
- `422 Unprocessable Entity`: Неверная роль

---

#### DELETE /me/roles

**Удалить роль пользователя**

```http
DELETE /api/v1/users/me/roles?role=provider
Authorization: Bearer <access_token>
```

**Response 200:**
```json
{
  "message": "Role 'provider' removed successfully"
}
```

**Business Rule:** Нельзя удалить последнюю роль

---

#### PUT /me/context

**Переключить текущую роль**

```http
PUT /api/v1/users/me/context
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "current_role": "provider"
}
```

**Response 200:**
```json
{
  "message": "Context switched to 'provider'",
  "roles": ["client", "provider"],
  "current_role": "provider"
}
```

**Errors:**
- `400 Bad Request`: Роль не назначена пользователю

---

### 5.3 Профили (`/api/v1/profiles`)

#### GET /me

**Получить профиль текущего пользователя**

```http
GET /api/v1/profiles/me
Authorization: Bearer <access_token>
```

**Response 200:**
```json
{
  "id": "uuid",
  "user_id": "uuid",
  "full_name": "Иван Иванов",
  "phone": "+972501234567",
  "avatar_url": "https://cdn.beauty-service.com/avatars/uuid.jpg",
  "no_show_count": 0,
  "no_show_rate": 0.0
}
```

---

#### PATCH /me

**Обновить профиль**

```http
PATCH /api/v1/profiles/me
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "full_name": "Петр Петров",
  "phone": "+972507654321"
}
```

**Response 200:**
```json
{
  "id": "uuid",
  "user_id": "uuid",
  "full_name": "Петр Петров",
  "phone": "+972507654321",
  "avatar_url": "https://cdn.beauty-service.com/avatars/uuid.jpg",
  "no_show_count": 0,
  "no_show_rate": 0.0
}
```

---

### 5.4 Каталог (`/api/v1/catalog`)

#### GET /categories

**Получить категории**

```http
GET /api/v1/catalog/categories?parent_id=<uuid>&limit=100&offset=0
Accept-Language: ru
```

**Query Parameters:**
- `parent_id` (optional): UUID родительской категории для подкатегорий
- `limit` (default: 100, max: 100)
- `offset` (default: 0)

**Response 200:**
```json
{
  "categories": [
    {
      "id": "uuid",
      "parent_id": null,
      "name": {
        "ru": "Стрижка",
        "he": "תספורת",
        "en": "Haircut"
      },
      "icon_url": "https://cdn.beauty-service.com/icons/haircut.png",
      "sort_order": 1,
      "is_active": true,
      "created_at": "2026-03-20T10:00:00Z",
      "updated_at": "2026-03-20T10:00:00Z"
    }
  ],
  "total": 15
}
```

---

#### GET /categories/{category_id}

**Получить категорию по ID**

```http
GET /api/v1/catalog/categories/{category_id}
Accept-Language: ru
```

**Response 200:** (см. выше)

**Errors:**
- `404 Not Found`: Категория не найдена

---

#### GET /services

**Получить услуги**

```http
GET /api/v1/catalog/services?category_id=<uuid>&limit=100&offset=0
Accept-Language: ru
```

**Query Parameters:**
- `category_id` (optional): Фильтр по категории
- `limit` (default: 100, max: 100)
- `offset` (default: 0)

**Response 200:**
```json
{
  "services": [
    {
      "id": "uuid",
      "category_id": "uuid",
      "title": {
        "ru": "Женская стрижка",
        "he": "תספורת נשית",
        "en": "Women's haircut"
      },
      "description": {
        "ru": "Классическая стрижка",
        "he": "תספורת קלאסית",
        "en": "Classic haircut"
      },
      "base_price": 15000,
      "price_min": 10000,
      "price_max": 20000,
      "currency": "ILS",
      "duration_minutes": 60,
      "is_combinable": true,
      "is_price_variable": true,
      "is_active": true,
      "created_at": "2026-03-20T10:00:00Z",
      "updated_at": "2026-03-20T10:00:00Z"
    }
  ],
  "total": 45
}
```

**Note:** `base_price`, `price_min`, `price_max` в агоротах (1/100 shekel)

---

#### GET /services/{service_id}

**Получить услугу по ID**

```http
GET /api/v1/catalog/services/{service_id}
Accept-Language: ru
```

**Response 200:** (см. выше)

**Errors:**
- `404 Not Found`: Услуга не найдена

---

#### POST /providers/search

**Поиск поставщиков услуг**

```http
POST /api/v1/catalog/providers/search
Content-Type: application/json
Accept-Language: ru

{
  "lat": 32.0853,
  "lon": 34.7818,
  "radius_km": 5,
  "category_id": null,
  "service_id": null,
  "price_min": null,
  "price_max": null,
  "sort_by": "distance",
  "limit": 20,
  "offset": 0
}
```

**Request Body:**
- `lat`, `lon` (required): Координаты пользователя (WGS84)
- `radius_km` (default: 5): Радиус поиска
- `category_id` (optional): Фильтр по категории
- `service_id` (optional): Фильтр по услуге
- `price_min`, `price_max` (optional): Фильтр по цене
- `sort_by`: `distance`, `rating`, `price_asc`, `price_desc`
- `limit` (default: 20, max: 100)
- `offset` (default: 0)

**Response 200:**
```json
{
  "providers": [
    {
      "id": "uuid",
      "user_id": "uuid",
      "display_name": "Салон красоты 'Ева'",
      "bio": "Лучший салон в Тель-Авиве",
      "avatar_url": "https://cdn.beauty-service.com/avatars/uuid.jpg",
      "phone": "+972501234567",
      "location": {
        "lat": 32.0853,
        "lon": 34.7818
      },
      "address": "ул. Дизенгоф, 123, Тель-Авив",
      "rating_cached": 4.5,
      "reviews_count": 120,
      "is_verified": true,
      "is_active": true,
      "distance_km": 0.8,
      "created_at": "2026-03-20T10:00:00Z",
      "updated_at": "2026-03-20T10:00:00Z"
    }
  ],
  "total": 25,
  "limit": 20,
  "offset": 0
}
```

---

#### GET /providers/{provider_id}

**Получить поставщика по ID**

```http
GET /api/v1/catalog/providers/{provider_id}
Accept-Language: ru
```

**Response 200:** (см. выше, без `distance_km`)

**Errors:**
- `404 Not Found`: Поставщик не найден

---

#### POST /favorites

**Добавить в избранное**

```http
POST /api/v1/catalog/favorites
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "provider_id": "uuid"
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "user_id": "uuid",
  "provider_id": "uuid",
  "provider": {
    "id": "uuid",
    "display_name": "Салон красоты 'Ева'",
    ...
  },
  "created_at": "2026-03-20T10:00:00Z"
}
```

**Errors:**
- `404 Not Found`: Поставщик не найден
- `409 Conflict`: Уже в избранном

---

#### GET /favorites

**Получить избранные**

```http
GET /api/v1/catalog/favorites?limit=50&offset=0
Authorization: Bearer <access_token>
Accept-Language: ru
```

**Response 200:**
```json
{
  "favorites": [
    {
      "id": "uuid",
      "user_id": "uuid",
      "provider_id": "uuid",
      "provider": { ... },
      "created_at": "2026-03-20T10:00:00Z"
    }
  ],
  "total": 12,
  "limit": 50,
  "offset": 0
}
```

---

#### DELETE /favorites/{provider_id}

**Удалить из избранного**

```http
DELETE /api/v1/catalog/favorites/{provider_id}
Authorization: Bearer <access_token>
```

**Response 204:** No Content

**Errors:**
- `404 Not Found`: Не в избранном

---

### 5.5 Бронирования (`/api/v1/bookings`)

#### POST /bookings

**Создать бронирование**

```http
POST /api/v1/bookings
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "provider_id": "uuid",
  "service_ids": ["uuid-1", "uuid-2"],
  "start_time": "2026-03-25T14:00:00Z",
  "notes": "Женская стрижка + укладка"
}
```

**Request Body:**
- `provider_id`: UUID поставщика (user_id из таблицы providers)
- `service_ids`: Массив UUID услуг (1-10 услуг)
- `start_time`: ISO 8601 datetime, в будущем, не более 30 дней
- `notes` (optional): Заметки

**Response 201:**
```json
{
  "id": "uuid",
  "provider_id": "uuid",
  "client_id": "uuid",
  "start_time": "2026-03-25T14:00:00Z",
  "end_time": "2026-03-25T15:30:00Z",
  "status": "PENDING",
  "total_price": 30000,
  "total_duration_minutes": 90,
  "notes": "Женская стрижка + укладка",
  "confirmed_at": null,
  "confirmed_by": null,
  "cancelled_at": null,
  "cancelled_by": null,
  "cancellation_reason": null,
  "completed_at": null,
  "reschedule_count": 0,
  "items": [
    {
      "id": "uuid",
      "service_id": "uuid-1",
      "price": 15000,
      "duration_minutes": 60
    },
    {
      "id": "uuid",
      "service_id": "uuid-2",
      "price": 15000,
      "duration_minutes": 30
    }
  ],
  "created_at": "2026-03-20T10:00:00Z",
  "updated_at": "2026-03-20T10:00:00Z"
}
```

**Errors:**
- `403 Forbidden`: Только client может создавать бронирования
- `400 Bad Request`: Поставщик не найден / неактивен
- `409 Conflict`: Слот недоступен
- `422 Unprocessable Entity`: Business validation error

**Business Rules:**
- User ID берется из JWT (SEC-001)
- Client role required
- Start time в будущем, не более 30 дней
- Min 1 услуга, max 10 услуг
- Все услуги должны принадлежать поставщику
- Слот должен быть доступен (нет конфликтов)

---

#### GET /bookings/{booking_id}

**Получить бронирование**

```http
GET /api/v1/bookings/{booking_id}
Authorization: Bearer <access_token>
```

**Response 200:** (см. выше)

**Errors:**
- `403 Forbidden`: Access denied (не client/provider брони)
- `404 Not Found`: Бронирование не найдено

---

#### GET /bookings/provider/{provider_id}

**Получить бронирования поставщика**

```http
GET /api/v1/bookings/provider/{provider_id}?status=CONFIRMED&from_date=2026-03-20&to_date=2026-03-27&limit=20&offset=0
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `status` (optional): Фильтр по статусу
- `from_date`, `to_date` (optional): Фильтр по дате
- `limit` (default: 20, max: 100)
- `offset` (default: 0)

**Response 200:**
```json
[
  {
    "id": "uuid",
    "provider_id": "uuid",
    "client_id": "uuid",
    ...
  }
]
```

**Errors:**
- `403 Forbidden`: Access denied (не admin и не сам provider)

---

#### GET /bookings/client/{client_id}

**Получить бронирования клиента**

```http
GET /api/v1/bookings/client/{client_id}?status=PENDING&limit=20&offset=0
Authorization: Bearer <access_token>
```

**Response 200:** (см. выше)

**Errors:**
- `403 Forbidden`: Access denied (не admin и не сам client)

---

#### PATCH /bookings/{booking_id}/confirm

**Подтвердить бронирование (Provider only)**

```http
PATCH /api/v1/bookings/{booking_id}/confirm
Authorization: Bearer <access_token>
```

**Response 204:** No Content

**Errors:**
- `403 Forbidden`: Только provider может подтверждать
- `422 Unprocessable Entity`: Неверный статус (не PENDING)

**Business Rules:**
- Provider role required
- Status must be PENDING
- Sets confirmed_at, confirmed_by

---

#### PATCH /bookings/{booking_id}/cancel

**Отменить бронирование**

```http
PATCH /api/v1/bookings/{booking_id}/cancel
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "cancellation_reason": "Не смог прийти"
}
```

**Response 204:** No Content

**Errors:**
- `403 Forbidden`: Client отменяет слишком поздно (<2 часа)
- `409 Conflict`: Уже отменено
- `422 Unprocessable Entity`: Неверный статус

**Business Rules:**
- **Client:** минимум 2 часа до start_time
- **Provider:** может отменить в любое время
- Status must be PENDING или CONFIRMED
- Sets cancelled_at, cancelled_by, cancellation_reason

---

#### PATCH /bookings/{booking_id}/reschedule

**Перенести бронирование**

```http
PATCH /api/v1/bookings/{booking_id}/reschedule
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "new_start_time": "2026-03-25T16:00:00Z",
  "reason": "Не могу прийти в 14:00"
}
```

**Response 204:** No Content

**Errors:**
- `403 Forbidden`: Client переносит слишком поздно
- `409 Conflict`: Новый слот недоступен
- `422 Unprocessable Entity`: Max reschedules exceeded (3)

**Business Rules:**
- **Client:** минимум 2 часа до start_time
- **Provider:** может переносить в любое время
- Status must be CONFIRMED
- Max 3 reschedules per booking
- New slot must be available
- Creates record in booking_reschedule_history
- Increments reschedule_count

---

#### PATCH /bookings/{booking_id}/complete

**Завершить бронирование (Provider only)**

```http
PATCH /api/v1/bookings/{booking_id}/complete
Authorization: Bearer <access_token>
```

**Response 204:** No Content

**Errors:**
- `403 Forbidden`: Только provider может завершать
- `422 Unprocessable Entity`: Неверный статус

**Business Rules:**
- Provider role required
- Status must be CONFIRMED
- Только после end_time
- Enables review creation for client

---

#### PATCH /bookings/{booking_id}/no-show

**Отметить как неявку (Provider only)**

```http
PATCH /api/v1/bookings/{booking_id}/no-show
Authorization: Bearer <access_token>
```

**Response 204:** No Content

**Errors:**
- `403 Forbidden`: Только provider может отмечать
- `422 Unprocessable Entity`: Неверный статус

**Business Rules:**
- Provider role required
- Status must be CONFIRMED или IN_PROGRESS
- Sets no_show_marked_at, no_show_marked_by
- Client's no_show_count incremented (Phase 2)

---

#### GET /bookings/slots

**Получить доступные слоты**

```http
GET /api/v1/bookings/slots?provider_id=uuid&date=2026-03-25&duration_minutes=60
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `provider_id` (required): UUID поставщика
- `date` (required): ISO 8601 date
- `duration_minutes` (default: 30, min: 15): Минимальная длительность слота

**Response 200:**
```json
[
  {
    "start_time": "2026-03-25T09:00:00Z",
    "end_time": "2026-03-25T10:00:00Z",
    "is_available": true
  },
  {
    "start_time": "2026-03-25T10:00:00Z",
    "end_time": "2026-03-25T11:00:00Z",
    "is_available": false
  },
  {
    "start_time": "2026-03-25T11:00:00Z",
    "end_time": "2026-03-25T12:00:00Z",
    "is_available": true
  }
]
```

**Business Rules:**
- Slot generation based on provider's schedule rules
- Buffer time: 15 минут между бронированиями
- Excludes: past slots, booked slots, non-working days

---

### 5.6 Schedule Rules (`/api/v1`)

#### POST /schedule-rules

**Создать правило расписания (Provider only)**

```http
POST /api/v1/schedule-rules
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "weekday": 1,
  "start_time": "09:00",
  "end_time": "18:00"
}
```

**Request Body:**
- `weekday`: 1-7 (1=Monday, 7=Sunday)
- `start_time`: HH:MM format
- `end_time`: HH:MM format

**Response 201:**
```json
{
  "id": "uuid",
  "provider_id": "uuid",
  "weekday": 1,
  "start_time": "09:00",
  "end_time": "18:00",
  "is_active": true,
  "created_at": "2026-03-20T10:00:00Z",
  "updated_at": "2026-03-20T10:00:00Z"
}
```

---

#### GET /schedule-rules/{rule_id}

**Получить правило расписания**

```http
GET /api/v1/schedule-rules/{rule_id}
Authorization: Bearer <access_token>
```

**Response 200:** (см. выше)

---

#### GET /schedule-rules/provider/{provider_id}

**Получить правила расписания поставщика**

```http
GET /api/v1/schedule-rules/provider/{provider_id}
Authorization: Bearer <access_token>
```

**Response 200:**
```json
[
  {
    "id": "uuid",
    "provider_id": "uuid",
    "weekday": 1,
    "start_time": "09:00",
    "end_time": "18:00",
    "is_active": true,
    ...
  },
  {
    "id": "uuid",
    "provider_id": "uuid",
    "weekday": 2,
    "start_time": "09:00",
    "end_time": "18:00",
    "is_active": true,
    ...
  }
]
```

---

#### PUT /schedule-rules/{rule_id}

**Обновить правило расписания (Owner only)**

```http
PUT /api/v1/schedule-rules/{rule_id}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "weekday": 1,
  "start_time": "10:00",
  "end_time": "19:00"
}
```

**Response 200:** (см. выше)

---

#### DELETE /schedule-rules/{rule_id}

**Удалить правило расписания (Owner only)**

```http
DELETE /api/v1/schedule-rules/{rule_id}
Authorization: Bearer <access_token>
```

**Response 204:** No Content

---

### 5.7 Provider Services (`/api/v1`)

#### POST /provider-services

**Создать услугу поставщика (Provider only)**

```http
POST /api/v1/provider-services
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "service_id": "uuid",
  "price": 15000,
  "duration_minutes": 60,
  "description_i18n": {
    "ru": "Женская стрижка",
    "he": "תספורת נשית",
    "en": "Women's haircut"
  }
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "provider_id": "uuid",
  "service_id": "uuid",
  "price": 15000,
  "duration_minutes": 60,
  "description_i18n": {
    "ru": "Женская стрижка",
    "he": "תספורת נשית",
    "en": "Women's haircut"
  },
  "is_active": true,
  "created_at": "2026-03-20T10:00:00Z",
  "updated_at": "2026-03-20T10:00:00Z"
}
```

---

#### GET /provider-services

**Получить услуги поставщика**

```http
GET /api/v1/provider-services?provider_id=uuid&is_active=true
Authorization: Bearer <access_token>
```

**Response 200:**
```json
{
  "provider_services": [
    {
      "id": "uuid",
      "provider_id": "uuid",
      "service_id": "uuid",
      "service": {
        "id": "uuid",
        "title_i18n": {
          "ru": "Стрижка",
          "he": "תספורת",
          "en": "Haircut"
        },
        "category_id": "uuid"
      },
      "price": 15000,
      "duration_minutes": 60,
      "description_i18n": { ... },
      "is_active": true,
      ...
    }
  ],
  "total": 10
}
```

---

#### GET /provider-services/{provider_service_id}

**Получить услугу поставщика**

```http
GET /api/v1/provider-services/{provider_service_id}
Authorization: Bearer <access_token>
```

**Response 200:** (см. выше)

---

#### PATCH /provider-services/{provider_service_id}

**Обновить услугу поставщика (Owner only)**

```http
PATCH /api/v1/provider-services/{provider_service_id}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "price": 20000,
  "duration_minutes": 90
}
```

**Response 200:** (см. выше)

---

#### DELETE /provider-services/{provider_service_id}

**Удалить услугу поставщика (Owner only)**

```http
DELETE /api/v1/provider-services/{provider_service_id}
Authorization: Bearer <access_token>
```

**Response 204:** No Content

---

#### POST /portfolio

**Добавить работу в портфолио (Provider only)**

```http
POST /api/v1/portfolio
Authorization: Bearer <access_token>
Content-Type: multipart/form-data

title_i18n='{"ru": "Работа 1", "he": "עבודה 1", "en": "Work 1"}'
description_i18n='{"ru": "Описание", "he": "תיאור", "en": "Description"}'
service_id=uuid
image=@photo.jpg
sort_order=1
```

**Response 201:**
```json
{
  "id": "uuid",
  "provider_id": "uuid",
  "title_i18n": { ... },
  "description_i18n": { ... },
  "image_url": "https://cdn.beauty-service.com/portfolio/uuid.jpg",
  "service_id": "uuid",
  "sort_order": 1,
  "is_active": true,
  "created_at": "2026-03-20T10:00:00Z",
  "updated_at": "2026-03-20T10:00:00Z"
}
```

---

#### GET /portfolio

**Получить портфолио**

```http
GET /api/v1/portfolio?provider_id=uuid&is_active=true
Authorization: Bearer <access_token>
Accept-Language: ru
```

**Response 200:**
```json
{
  "portfolio_items": [
    {
      "id": "uuid",
      "provider_id": "uuid",
      "title": "Работа 1",
      "description": "Описание",
      "image_url": "https://cdn.beauty-service.com/portfolio/uuid.jpg",
      "service_id": "uuid",
      "service": {
        "id": "uuid",
        "title": "Стрижка"
      },
      "sort_order": 1,
      "is_active": true,
      ...
    }
  ],
  "total": 15
}
```

---

#### GET /portfolio/{portfolio_item_id}

**Получить работу портфолио**

```http
GET /api/v1/portfolio/{portfolio_item_id}
Authorization: Bearer <access_token>
Accept-Language: ru
```

**Response 200:** (см. выше)

---

#### DELETE /portfolio/{portfolio_item_id}

**Удалить работу портфолио (Owner only)**

```http
DELETE /api/v1/portfolio/{portfolio_item_id}
Authorization: Bearer <access_token>
```

**Response 204:** No Content

---

## 6. Статусы и коды ответов

### 6.1 HTTP Status Codes

| Код | Описание | Использование |
|-----|----------|---------------|
| **200** | OK | Успешный GET, PATCH |
| **201** | Created | Успешный POST |
| **204** | No Content | Успешный DELETE, PATCH без body |
| **400** | Bad Request | Validation error, неверный запрос |
| **401** | Unauthorized | Не авторизован, токен недействителен |
| **403** | Forbidden | Нет прав, ownership validation failed |
| **404** | Not Found | Ресурс не найден |
| **409** | Conflict | Конфликт (уже существует, слот занят) |
| **422** | Unprocessable Entity | Business validation error |
| **423** | Locked | Аккаунт заблокирован |
| **429** | Too Many Requests | Rate limit exceeded |
| **500** | Internal Server Error | Ошибка сервера |

### 6.2 Error Response Format

```json
{
  "detail": "Error message in user's language (ru/he/en)"
}
```

**Validation Errors (422):**
```json
{
  "detail": [
    {
      "loc": ["body", "email"],
      "msg": "field required",
      "type": "value_error.missing"
    }
  ]
}
```

---

## 7. Бизнес-правила

### 7.1 Бронирования

#### Статусы

```
PENDING → CONFIRMED → COMPLETED
   ↓         ↓
CANCELLED  CANCELLED
   ↓
EXPIRED (автоматически через 24ч)
```

**Дополнительные статусы:**
- `IN_PROGRESS`: В процессе выполнения
- `NO_SHOW`: Клиент не явился

#### Правила создания

- Client role required
- Start time в будущем
- Max 30 дней вперед
- Min 1 услуга, max 10 услуг
- Слот должен быть доступен

#### Правила отмены

| Роль | Минимальное время | Примечание |
|------|-------------------|------------|
| Client | 2 часа до start_time | 否则 403 |
| Provider | Любое время | Логируется причина |

#### Правила переноса

- Max 3 reschedules per booking
- Client: минимум 2 часа до start_time
- Provider: любое время
- Новый слот должен быть доступен

#### Истечение срока (Expiration)

- Автоматически через 24 часа если PENDING
- Background task проверяет каждую минуту

### 7.2 Поставщики (Providers)

#### Верификация

- `is_verified`: Флаг верификации
- Устанавливается admin-ом
- Обязателен для Phase 2

#### Рейтинг

- `rating_cached`: Кэшированный рейтинг (0-5)
- `reviews_count`: Количество отзывов
- Обновляется при создании/удалении отзыва (Phase 2)

#### График работы

- Schedule rules: weekday + time range
- Нет правил = выходной
- Можно добавить несколько правил на один день

### 7.3 Избранное (Favorites)

- Уникальность: (user_id, provider_id)
- Добавление: 201 или 409 если уже существует
- Удаление: 204 или 404 если не существует

### 7.4 Язык (i18n)

- Поддерживаемые языки: `ru`, `he`, `en`
- Accept-Language header определяет язык
- Поля с суффиксом `_i18n` хранят JSONB: `{"ru": "...", "he": "...", "en": "..."}`

---

## 8. Локализация

### 8.1 Поддерживаемые языки

- `ru` - Русский (дефолт)
- `he` - Иврит
- `en` - Английский

### 8.2 Определение языка

**Приоритет:**
1. `Accept-Language` header
2. `language_code` из user profile
3. `ru` (дефолт)

**Пример:**
```http
GET /api/v1/catalog/categories
Accept-Language: ru
```

### 8.3 i18n Поля

**Request/Response:**
```json
{
  "title_i18n": {
    "ru": "Стрижка",
    "he": "תספורת",
    "en": "Haircut"
  }
}
```

**Flat response (клиентская сторона):**
```json
{
  "title": "Стрижка"  // На основе Accept-Language
}
```

**Backend decision:** MVP возвращает `_i18n` поля. KMP app должен плоскать их на стороне клиента.

---

## 9. Rate Limiting

### 9.1 Стратегия

**SlowAPI** с IP-based limiting.

### 9.2 Лимиты

| Endpoint | Лимит |
|----------|-------|
| POST /auth/register | 5/hour |
| POST /auth/login | 10/minute |
| POST /auth/verify-email | 5/hour |
| POST /auth/resend-verification | 5/hour |
| POST /auth/forgot-password | 5/hour |
| POST /auth/reset-password | 10/hour |
| POST /auth/refresh | 20/minute |
| Other endpoints | 60/minute (дефолт) |

### 9.3 Response 429

```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
Retry-After: 60

{
  "detail": "Rate limit exceeded"
}
```

---

## 10. WebSocket и Real-time

### 10.1 Статус MVP Phase

**WebSocket НЕ реализован в MVP.**

### 10.2 Варианты для Phase 2

#### Вариант A: WebSocket (FastAPI + WebSockets)

**Плюсы:**
- Нативная поддержка в FastAPI
- Bidirectional communication
- Легкий для simple notifications

**Минусы:**
- Масштабирование (сложно horizontally)
- Connection management

#### Вариант B: Server-Sent Events (SSE)

**Плюсы:**
- Простой для unidirectional push
- HTTP-based (легче масштабировать)
- Auto-reconnect

**Минусы:**
- Только server → client
- No binary data

#### Вариант C: Push Notifications (Firebase + APNs)

**Плюсы:**
- Native mobile experience
- Работает когда app в background
- Масштабируется

**Минусы:**
- Требует Firebase/APNs setup
- Не realtime (задержки)

#### Вариант D: Polling (Last Resort)

**Плюсы:**
- Простейший для реализации
- Работает везде

**Минусы:**
- Не realtime
- Батарея draining
- Server load

**Рекомендация для Phase 2:**
- **Primary:** Push Notifications (Firebase + APNs) для critical updates
- **Secondary:** WebSocket для in-app realtime (chat, live booking updates)
- **Fallback:** Polling для legacy compatibility

---

## Дополнительно

### Webhooks (Phase 2)

Для integration с payment systems, external services:

```http
POST /webhooks/payment-provider
{
  "event": "payment.success",
  "booking_id": "uuid",
  "amount": 15000,
  "currency": "ILS"
}
```

### Background Jobs

**Scheduler:** APScheduler (in-memory) для MVP.

**Jobs:**
- Expire pending bookings (каждую минуту)
- Cleanup expired tokens (каждый час)
- Send booking reminders (Phase 2)

---

## Приложение A: User Flow Examples

### A.1 Client Registration & Booking Flow

```
1. POST /auth/register
   → 201 Created, access_token, refresh_token cookie

2. GET /auth/me
   → 200 OK, user data

3. GET /catalog/providers/search (geo search)
   → 200 OK, providers list

4. GET /providers/{provider_id}
   → 200 OK, provider details

5. GET /bookings/slots (check availability)
   → 200 OK, available slots

6. POST /bookings (create booking)
   → 201 Created, booking PENDING

7. [Provider receives notification (Phase 2)]

8. PATCH /bookings/{id}/confirm (provider confirms)
   → 204 No Content, booking CONFIRMED

9. [Client receives notification (Phase 2)]

10. PATCH /bookings/{id}/complete (provider completes)
    → 204 No Content, booking COMPLETED
```

### A.2 Provider Onboarding Flow

```
1. POST /auth/register
   Request: { "roles": ["client", "provider"] }
   → 201 Created

2. POST /users/me/roles (add provider role)
   → 201 Created

3. PUT /users/me/context (switch to provider)
   → 200 OK

4. POST /profiles (create provider profile)
   → 201 Created

5. POST /schedule-rules (set working hours)
   → 201 Created

6. POST /provider-services (add services)
   → 201 Created

7. POST /portfolio (add photos)
   → 201 Created
```

---

## Приложение B: Migration Strategy

### B.1 MVP Phase

**Единая миграция:**
```
backend/alembic/versions/20260304_1000_v1_initial_schema.py
```

**Правила:**
- НЕ создавать новые миграции
- Редактировать существующую
- После изменений: `dropdb && createdb && alembic upgrade head`

### B.2 Production Phase

**Отдельные миграции:**
```
alembic revision --autogenerate -m "add reviews table"
```

**Rollback strategy:**
```bash
alembic downgrade -1
```

---

**Документ подготовлен для команды KMP mobile development.**

**Вопросы и уточнения:** @backend-team
