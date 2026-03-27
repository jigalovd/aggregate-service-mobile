# Firebase Authentication Integration Design

**Created:** 2026-03-26
**Status:** Approved

---

## 1. Overview

Интеграция Firebase Authentication для авторизации через Google/Apple/Phone провайдеров.

**Tech Stack:** Python / FastAPI / firebase-admin SDK / PostgreSQL

**Architecture:**

```
Android → Firebase Auth (Google/Apple/Phone) → получает Firebase Token
Android → отправляет Firebase Token → Backend
Backend → верифицирует через firebase-admin SDK → создаёт/линкует пользователя
```

**Key Differences from Direct OAuth:**

- Backend не работает с OAuth напрямую
- Firebase SDK делает всю работу с провайдерами
- Backend только верифицирует Firebase tokens

---

## 2. Supported Providers

- Google Sign-In
- Apple Sign-In
- Phone Number Authentication

---

## 3. Database Schema

### 3.1 New Table: `firebase_accounts`

```sql
CREATE TABLE firebase_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    firebase_uid VARCHAR(128) NOT NULL,
    provider VARCHAR(20) NOT NULL,    -- 'google', 'apple', 'phone'
    email VARCHAR(255),               -- from Firebase token
    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ NULL,      -- soft delete for re-linking
    
    CONSTRAINT firebase_accounts_firebase_uid_unique UNIQUE (firebase_uid),
    CONSTRAINT firebase_accounts_user_provider_unique UNIQUE (user_id, provider)
);

CREATE INDEX idx_firebase_accounts_user_id ON firebase_accounts(user_id);
CREATE INDEX idx_firebase_accounts_firebase_uid ON firebase_accounts(firebase_uid);
```

### 3.2 Domain Entity

```python
@dataclass(frozen=True)
class FirebaseAccountEntity:
    id: UUID | None
    user_id: UUID
    firebase_uid: str
    provider: str                    # "google", "apple", "phone"
    email: str | None
    linked_at: datetime | None
    deleted_at: datetime | None
```

---

## 4. API Endpoints

### 4.1 POST /auth/provider/verify

Основной endpoint для Firebase authentication.

**Request:**

```json
{
  "firebase_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Responses:**

| Case                                           | HTTP | Body                                                                                   |
|------------------------------------------------|------|----------------------------------------------------------------------------------------|
| Новый пользователь                             | 201  | `AuthResponse` + `{"is_new_user": true}`                                               |
| Существующий, Firebase привязан                | 200  | `AuthResponse`                                                                         |
| Существующий, email есть, Firebase не привязан | 200  | `{"error": "link_required", "email": "...", "firebase_uid": "...", "provider": "..."}` |
| Firebase UID привязан к ДРУГОМУ аккаунту       | 409  | `{"error": "firebase_already_linked", "message": "..."}`                               |

### 4.2 POST /auth/provider/link

Привязка Firebase аккаунта к существующему.

**Request:**

```json
{
  "firebase_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "password": "user_password"
}
```

**Responses:**

| Case                                         | HTTP | Body                                        |
|----------------------------------------------|------|---------------------------------------------|
| Успех                                        | 200  | `AuthResponse`                              |
| Неверный пароль                              | 401  | `{"error": "invalid_credentials", ...}`     |
| Firebase UID уже привязан к другому аккаунту | 409  | `{"error": "firebase_already_linked", ...}` |

### 4.3 DELETE /auth/provider/unlink

Отвязка Firebase аккаунта.

**Требует:** Bearer token

**Request:**

```json
{
  "provider": "google"
}
```

**Responses:**

| Case                                       | HTTP | Body                                                    |
|--------------------------------------------|------|---------------------------------------------------------|
| Успех                                      | 200  | `{"message": "Firebase account unlinked successfully"}` |
| Нет привязанного Firebase этого провайдера | 404  | `{"error": "firebase_not_linked", ...}`                 |
| Последний способ входа                     | 400  | `{"error": "cannot_unlink_last_auth_method", ...}`      |

### 4.4 GET /auth/provider/accounts

Список привязанных Firebase аккаунтов.

**Требует:** Bearer token

**Response:**

```json
{
  "accounts": [
    {
      "provider": "google",
      "email": "user@gmail.com",
      "linked_at": "2026-03-26T10:00:00Z"
    },
    {
      "provider": "apple",
      "email": "user@icloud.com",
      "linked_at": "2026-03-26T11:00:00Z"
    }
  ]
}
```

---

## 5. Firebase Token Verification

### 5.1 Backend Verification

```python
import firebase_admin
from firebase_admin import credentials, auth

# Инициализация (один раз при старте)
cred = credentials.Certificate("path/to/serviceAccountKey.json")
firebase_admin.initialize_app(cred)

# Верификация токена
def verify_firebase_token(token: str) -> dict:
    decoded_token = auth.verify_id_token(token)
    return {
        "uid": decoded_token["uid"],
        "email": decoded_token.get("email"),
        "email_verified": decoded_token.get("email_verified", False),
        "name": decoded_token.get("name"),
        "provider": decoded_token["firebase"]["identities"]["sign_in_provider"][0],
    }
```

### 5.2 Firebase Admin SDK Setup

**Environment Variables:**

```bash
FIREBASE_SERVICE_ACCOUNT_JSON="..."  # JSON string of service account
# OR
FIREBASE_SERVICE_ACCOUNT_PATH="/path/to/serviceAccountKey.json"
```

---

## 6. Account Resolution Flow

### 6.1 New User Flow

```
1. Android: Firebase Sign-In (Google/Apple/Phone)
2. Android: sends {firebase_token} to POST /auth/provider/verify
3. Backend: verify_firebase_token(token)
4. Backend: firebase_uid = decoded["uid"], email = decoded["email"]
5. Backend: lookup firebase_accounts WHERE firebase_uid = uid
   → NOT FOUND
6. Backend: lookup users WHERE email = email
   → NOT FOUND
7. Backend: CREATE user (email, has_password=FALSE, is_verified=TRUE)
8. Backend: CREATE firebase_account (user_id, firebase_uid, provider, email)
9. Backend: CREATE default role "client"
10. Backend: return AuthResponse (access + refresh tokens)
```

### 6.2 Existing User Flow (Firebase already linked)

```
1-4: Same as above
5: Backend: lookup firebase_accounts WHERE firebase_uid = uid
   → FOUND (user_id = existing_user_id)
6: Backend: fetch user by user_id
7: Backend: return AuthResponse
```

### 6.3 Email Conflict Flow (link_required)

```
1-4: Same as above
5: Backend: lookup firebase_accounts WHERE firebase_uid = uid
   → NOT FOUND
6: Backend: lookup users WHERE email = email
   → FOUND (existing_user_id)
7: Backend: Check if this user has password (has_password = TRUE)
   → YES (registered via email/password)
8: Return: {"error": "link_required", "email": "...", "firebase_uid": "...", "provider": "..."}
```

### 6.4 Link Flow (Account Linking)

```
1. Android: shows "Enter password to link Firebase"
2. Android: sends {firebase_token, password} to POST /auth/provider/link
3. Backend: verify_firebase_token(token) → firebase_uid, email
4. Backend: verify password for user with this email
   → FAIL: return 401
5. Backend: check firebase_accounts WHERE firebase_uid = firebase_uid
   → FOUND linked to ANOTHER user: return 409 "firebase_already_linked"
6. Backend: CREATE firebase_account for this user
7. Backend: return AuthResponse
```

### 6.5 Conflict Detection (Firebase UID already linked elsewhere)

```
1. Android: sends {firebase_token} to POST /auth/provider/verify
2. Backend: verify_firebase_token → firebase_uid, email
3. Backend: lookup firebase_accounts WHERE firebase_uid = uid
   → FOUND (but linked to DIFFERENT user_id)
4. Return 409: {"error": "firebase_already_linked", "message": "..."}
```

---

## 7. Unlink Validation

**Cannot unlink if:**

- `user.has_password = FALSE` AND
- `user.firebase_accounts` (excluding the one being unlinked) is empty

**Result:** User must have at least one authentication method (password OR Firebase account).

---

## 8. User Creation Defaults

| Field           | Value        | Note                            |
|-----------------|--------------|---------------------------------|
| `has_password`  | `FALSE`      | Firebase-only user              |
| `is_verified`   | `TRUE`       | Firebase already verified email |
| `roles`         | `["client"]` | Default role                    |
| `language_code` | `"ru"`       | Default                         |

---

## 9. Security

### 9.1 Rate Limiting

| Endpoint           | Limit             |
|--------------------|-------------------|
| `/firebase/verify` | 10/minute per IP  |
| `/firebase/link`   | 5/minute per user |
| `/firebase/unlink` | 5/minute per user |

### 9.2 Error Responses (RFC 7807)

```json
{
  "type": "https://api.example.com/errors/firebase-already-linked",
  "title": "Firebase account already linked",
  "status": 409,
  "detail": "This Firebase account is already linked to another user.",
  "instance": "/auth/provider/verify"
}
```

### 9.3 Security Checklist

- [x] Firebase token verification via firebase-admin SDK
- [x] Constant-time response for `/link` (password verification)
- [x] Rate limiting on all endpoints
- [x] Account lockout (inherited from existing auth)
- [x] Refresh token rotation
- [x] Soft delete for firebase_accounts

---

## 10. Files to Create/Modify

### 10.1 New Files

```
backend/app/features/firebase_auth/
├── __init__.py
├── domain/
│   ├── __init__.py
│   ├── entities.py              # FirebaseAccountEntity
│   └── exceptions.py           # FirebaseError, LinkRequiredError, FirebaseAlreadyLinkedError
├── application/
│   ├── __init__.py
│   ├── dto/
│   │   ├── __init__.py
│   │   └── firebase.py        # FirebaseVerifyDTO, FirebaseLinkDTO
│   ├── use_cases/
│   │   ├── __init__.py
│   │   ├── verify_firebase_token.py
│   │   └── link_firebase_account.py
│   └── protocols/
│       ├── __init__.py
│       └── unit_of_work.py     # FirebaseAuthUnitOfWorkProtocol
├── infrastructure/
│   ├── __init__.py
│   ├── models.py              # FirebaseAccount (SQLAlchemy)
│   ├── repositories.py        # FirebaseAccountRepository
│   ├── firebase_client.py     # Firebase token verification wrapper
│   └── unit_of_work.py        # FirebaseAuthUnitOfWork
└── presentation/
    ├── __init__.py
    ├── controllers.py          # routers
    └── schemas.py             # Pydantic schemas
```

### 10.2 Modifications

| File                                                          | Change                                                      |
|---------------------------------------------------------------|-------------------------------------------------------------|
| `backend/app/features/auth/domain/entities.py`                | Add `firebase_accounts` relationship                        |
| `backend/app/features/auth/infrastructure/models.py`          | Add `has_password` column, `firebase_accounts` relationship |
| `backend/app/features/auth/infrastructure/repositories.py`    | Add firebase_account methods                                |
| `backend/app/core/dependencies.py`                            | Add `get_firebase_auth_uow`, use case dependencies          |
| `backend/app/main.py`                                         | Register `firebase_auth_router`                             |
| `backend/app/config/config.py`                                | Add Firebase configuration                                  |
| `backend/alembic/versions/20260304_1000_v1_initial_schema.py` | Add `firebase_accounts` table, `has_password` column        |

### 10.3 Dependencies

```toml
# pyproject.toml
firebase-admin = "^6.0.0"
```

---

## 11. Configuration

### 11.1 Environment Variables

```bash
# Firebase Admin SDK
FIREBASE_SERVICE_ACCOUNT_JSON='{"type":"service_account","project_id":"..."}'
# OR
FIREBASE_SERVICE_ACCOUNT_PATH="/path/to/serviceAccountKey.json"

# Optional
FIREBASE_PROJECT_ID="your-project-id"
```

### 11.2 Config Model

```python
class FirebaseSettings(BaseSettings):
    service_account_json: str | None = None
    service_account_path: str | None = None
    project_id: str | None = None
    
    @property
    def use_emulator(self) -> bool:
        return self.service_account_json is None and self.service_account_path is None
```

---

## 12. Testing Requirements

- Unit tests for use cases (80%+ coverage)
- Integration tests for Firebase token verification (mock firebase-admin)
- Integration tests for account linking flow
- Security tests for rate limiting
