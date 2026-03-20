# 🌐 Network Layer Architecture - Ktor 3.4.1

**Дата создания**: 2026-03-19
**Последнее обновление**: 2026-03-20
**Статус**: 🟡 In Progress (60%) - Требуется Auth Interceptor & Error Handling
**Версия Ktor**: 3.4.1 (обновлено с 3.0.3)

---

## 📋 Обзор

Network Layer построен на **Ktor Client 3.0.3** с использованием expect/actual паттерна для платформенно-специфичных HTTP engines.

### 🎯 Архитектурные принципы

| Принцип | Реализация |
|---------|-------------|
| **KMP-Native** | 100% общий код в `commonMain` |
| **Platform-Specific Engines** | OkHttp (Android), Darwin (iOS) |
| **Factory Pattern** | `createHttpClient()` для создания клиентов |
| **Timeout Configuration** | 30 секунд для всех операций |
| **Serialization** | Kotlinx Serialization с JSON |
| **Logging** | Опциональный логгер с custom logger |

---

## 🏗️ Структура модуля

```
core/network/
├── build.gradle.kts                    # Конфигурация зависимостей
├── src/
│   ├── commonMain/kotlin/
│   │   └── com/aggregateservice/core/network/
│   │       ├── PlatformEngine.kt       # expect/actual declaration
│   │       └── (future) safeApiCall.kt # TODO: Error handling wrapper
│   │
│   ├── androidMain/kotlin/
│   │   └── com/aggregateservice/core/network/
│   │       └── PlatformEngine.android.kt  # OkHttp implementation
│   │
│   └── iosMain/kotlin/
│       └── com/aggregateservice/core/network/
│           └── PlatformEngine.ios.kt       # Darwin implementation
```

---

## 🔧 Компоненты

### 1. PlatformEngine (expect/actual)

**Файл**: `src/commonMain/kotlin/com/aggregateservice/core/network/PlatformEngine.kt`

```kotlin
package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TIMEOUT_MS = 30_000L

// expect declaration - будет реализован в каждой платформе
expect val httpClientEngine: HttpClientEngine

// Factory function для создания HTTP клиента
fun createHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    enableLogging: Boolean = false,
): HttpClient =
    HttpClient(engine) {
        // Content Negotiation с JSON сериализацией
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }

        // Timeout configuration
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MS
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
        }

        // Optional logging
        if (enableLogging) {
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            println("AggregateService Log: $message")
                        }
                    }
                level = LogLevel.ALL
            }
        }

        // Default configuration для всех запросов
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }
```

#### 🎯 Конфигурация JSON

| Параметр | Значение | Описание |
|----------|----------|----------|
| `prettyPrint` | `true` | Форматирование JSON (для debug) |
| `isLenient` | `true` | Допускает некорректный JSON |
| `ignoreUnknownKeys` | `true` | Игнорирует неизвестные поля |
| `explicitNulls` | `false` | Не сериализует null значения |

---

### 2. Android Engine (OkHttp)

**Файл**: `src/androidMain/kotlin/com/aggregateservice/core/network/PlatformEngine.android.kt`

```kotlin
package com.aggregateservice.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L

// actual implementation для Android
actual val httpClientEngine: HttpClientEngine
    get() =
        OkHttp.create {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
        }
```

#### 🎯 OkHttp Configuration

| Параметр | Значение | Описание |
|----------|----------|----------|
| `retryOnConnectionFailure` | `true` | Автоматический retry при ошибке |
| `connectTimeout` | 30s | Timeout для соединения |
| `readTimeout` | 30s | Timeout для чтения |
| `writeTimeout` | 30s | Timeout для записи |

---

### 3. iOS Engine (Darwin)

**Файл**: `src/iosMain/kotlin/com/aggregateservice/core/network/PlatformEngine.ios.kt`

```kotlin
package com.aggregateservice.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.timeoutInterval

private const val TIMEOUT_SECONDS = 30.0

// actual implementation для iOS
actual val httpClientEngine: HttpClientEngine
    get() =
        Darwin.create {
            configureSession {
                setTimeoutInterval(TIMEOUT_SECONDS)
            }
        }
```

#### 🎯 Darwin Configuration

| Параметр | Значение | Описание |
|----------|----------|----------|
| `timeoutInterval` | 30.0s | Timeout для всех операций |

---

## 📦 Зависимости

### build.gradle.kts

```kotlin
plugins {
    id("core-module")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        maybeCreate("iosMain").dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
```

### libs.versions.toml

```toml
[versions]
# ⬆️ UPDATED 2026-03-20
ktor = "3.4.1"           # Updated from 3.0.3
serialization = "1.10.0"   # Updated from 1.7.3

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
```

---

## 🚀 Использование

### Создание HTTP клиента

```kotlin
import com.aggregateservice.core.network.createHttpClient
import com.aggregateservice.core.network.httpClientEngine

// Создание клиента с базовым URL
val httpClient = createHttpClient(
    engine = httpClientEngine,
    baseUrl = "https://api.aggregateservice.com",
    enableLogging = BuildConfig.DEBUG // Только для debug builds
)
```

### Пример API запроса (будет реализован в Auth feature)

```kotlin
// TODO: Будет добавлено в feature/auth/data/remote/AuthApiService.kt
suspend fun login(email: String, password: String): LoginResponse {
    return httpClient.post("auth/login") {
        setBody(LoginRequest(email, password))
    }.body()
}
```

---

## 🔮 TODO: Следующие шаги

### Phase 1: Error Handling (Priority: CRITICAL) 🚨

**Обновлено на основе BACKEND_API_REFERENCE.md (2026-03-20)**

1. **safeApiCall wrapper** ⚠️ КРИТИЧНО
   - Обработка всех HTTP кодов (200, 201, 204, 400, 401, 403, 404, 409, 422, 423, 429, 500)
   - Автоматический retry при 500 (max 3 попытки)
   - Маппинг в AppError sealed hierarchy
   - Rate limiting обработка (X-RateLimit-* headers)

2. **AppError sealed hierarchy** ⚠️ КРИТИЧНО
   ```kotlin
   sealed interface AppError {
       // Network errors
       data class NetworkError(
           val code: Int,
           val message: String,
           val detail: String? = null
       ) : AppError

       // Auth errors
       data object Unauthorized : AppError()
       data class AccountLocked(val until: String) : AppError()

       // Business errors
       data class ValidationError(
           val field: String,
           val message: String
       ) : AppError()

       data class SlotNotAvailable(
           val reason: String
       ) : AppError()

       data class RateLimitExceeded(
           val retryAfter: Int  // секунды
       ) : AppError()

       // Unknown
       data class UnknownError(
           val throwable: Throwable
       ) : AppError()
   }
   ```

3. **Auth Interceptor** ⚠️ КРИТИЧНО
   - Automatic token injection (Authorization: Bearer <token>)
   - **Token refresh flow при 401**:
     - POST /auth/refresh с refresh_token из HTTP-only cookie
     - Повторить исходный запрос с новым access token
     - При повторном 401 → logout и перенаправление на login
   - Refresh token rotation (каждый refresh обновляет токен)
   - **Multi-role users**: токен содержит `roles: List<String>` и `currentRole: String?`

### Phase 2: API Services (Priority: MEDIUM)

1. **AuthApiService** (feature/auth)
   - POST /auth/login
   - POST /auth/register
   - POST /auth/refresh
   - POST /auth/logout

2. **CatalogApiService** (feature/catalog)
   - GET /providers
   - GET /providers/{id}
   - GET /services
   - GET /categories

### Phase 3: Testing (Priority: MEDIUM)

1. **Unit Tests**
   - Mock Ktor responses
   - Test error handling
   - Test serialization

2. **Integration Tests**
   - Test against mock server
   - Test timeout behavior

---

## 📊 Статус реализации

| Компонент | Статус | Прогресс | Заметки |
|-----------|--------|----------|---------|
| **Platform Engines** | ✅ Complete | 100% | OkHttp (Android), Darwin (iOS) |
| **HttpClient Factory** | ✅ Complete | 100% | createHttpClient() с Config integration |
| **Content Negotiation** | ✅ Complete | 100% | Kotlinx Serialization JSON |
| **Timeout Configuration** | ✅ Complete | 100% | 30s для всех операций |
| **Logging Plugin** | ✅ Complete | 100% | Custom logger |
| **safeApiCall Wrapper** | ⚠️ IN PROGRESS | 0% | Приоритет: CRITICAL |
| **AppError Hierarchy** | ⚠️ IN PROGRESS | 0% | Приоритет: CRITICAL |
| **Auth Interceptor** | ⚠️ PLANNED | 0% | Приоритет: CRITICAL (требует TokenStorage) |
| **TokenStorage** | ⚠️ PLANNED | 0% | DataStore (core:storage module) |
| **API Services** | ⚪ Not Started | 0% | Auth, Catalog, Booking |

---

## 🔗 Связанные документы

- [BACKEND_API_REFERENCE.md](BACKEND_API_REFERENCE.md) - Детальное описание бэкенд API (обновлено 2026-03-20)
- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) - Общий статус проекта
- [CONFIG_MANAGEMENT.md](CONFIG_MANAGEMENT.md) - Управление конфигурацией и секретами
- [CODE_QUALITY_GUIDE.md](CODE_QUALITY_GUIDE.md) - Детект и Ktlint для network layer
- [plans/01-quality-infrastructure-and-cicd.md](plans/01-quality-infrastructure-and-cicd.md) - План тестирования

---

**Версия документа**: 1.1
**Last Updated**: 2026-03-20
**Maintainer**: Development Team
