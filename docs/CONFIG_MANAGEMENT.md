# 🔐 Централизованное управление конфигурацией - Aggregate Service

**Дата создания**: 2026-03-19
**Статус**: ✅ Complete (100%)
**Версия**: 1.1

---

## 📋 Обзор

Система централизованного управления конфигурацией построена на **expect/actual паттерне** для KMP, обеспечивая type-safe доступ к API ключам, URL и другим настройкам из общего кода.

### 🎯 Ключевые особенности

| Особенность | Описание |
|-------------|----------|
| **Type-Safe** | Компиляция проверяет корректность конфигурации |
| **Platform-Specific** | Android (BuildConfig), iOS (Info.plist/env) |
| **Secure** | API keys НЕ попадают в git |
| **Flexible** | Лёгкое добавление новых параметров |
| **KMP-Native** | 100% общий код для доступа к конфигурации |

---

## 📁 Структура директории config/

Все конфигурационные файлы проекта централизованы в директории `config/`:

```
config/
├── README.md                      # Главный индекс конфигов
├── quality/
│   ├── README.md                  # Документация quality tools
│   ├── detekt.yml                 # Правила Detekt
│   └── .editorconfig              # Справочник ktlint правил
├── logging/
│   ├── README.md                  # Документация логирования
│   └── logback.xml                # Конфигурация Logback
└── secrets/
    ├── README.md                  # Инструкция по секретам
    └── secrets.properties.template # Шаблон секретов
```

### Быстрый справочник

| Что нужно | Где искать |
|-----------|------------|
| Изменить правило Detekt | `config/quality/detekt.yml` |
| Изменить правило ktlint | `.editorconfig` (корень) |
| Настроить логирование | `config/logging/logback.xml` |
| Добавить секрет | `config/secrets/secrets.properties.template` → `secrets.properties` |
| Добавить зависимость | `gradle/libs.versions.toml` |
| Изменить Gradle JVM | `gradle.properties` |

---

## 🏗️ Архитектура

### Структура модуля

```
core/config/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/com/aggregateservice/core/config/
    │   ├── AppConfig.kt              # expect declaration
    │   └── Environment.kt            # Environment enum (DEV, STAGING, PROD)
    │
    ├── androidMain/kotlin/com/aggregateservice/core/config/
    │   └── AppConfig.android.kt      # actual (BuildConfig)
    │
    └── iosMain/kotlin/com/aggregateservice/core/config/
        └── AppConfig.ios.kt          # actual (Info.plist / env)
```

### Поток конфигурации

```
┌─────────────────────────────────────────────────────────────┐
│                     Источники конфигурации                  │
├─────────────────────────────────────────────────────────────┤
│  Android: BuildConfig (Gradle buildConfigField)             │
│  iOS: Info.plist / Environment Variables                    │
│  Local: secrets.properties (только для разработки)          │
│  CI/CD: Environment Variables                               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  AppConfig (expect/actual)                   │
│  - apiBaseUrl: String                                       │
│  - apiKey: String                                           │
│  - environment: Environment                                 │
│  - isDebug: Boolean                                         │
│  - enableLogging: Boolean                                   │
│  - networkTimeoutMs: Long                                   │
│  - apiVersion: String                                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Config Singleton                         │
│  val baseUrl = Config.apiBaseUrl                            │
│  val key = Config.apiKey                                    │
│  if (Config.isDebug) { ... }                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Использование

### Инициализация (Application Layer)

#### Android

```kotlin
// androidApp/src/main/.../MainActivity.kt
package com.aggregateservice.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aggregateservice.core.config.Config

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация конфигурации
        Config.initialize(AppConfig())

        setContent {
            // App UI
        }
    }
}
```

#### iOS

```kotlin
// iosApp/iosApp/.../AppDelegate.swift или в SwiftUI App
import Foundation
import AggregateServiceShared

// В AppDelegate.swift
func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
) -> Bool {
    // Initialize Config
    ConfigKt.configInitialize(appConfig: AppConfig())
    return true
}
```

### Доступ к конфигурации в общем коде

```kotlin
// В любом общем коде (commonMain)
import com.aggregateservice.core.config.Config

class AuthRepository(
    private val httpClient: HttpClient
) {
    suspend fun login(email: String, password: String): AuthResponse {
        return httpClient.post("auth/login") { // URL относительно Config.apiBaseUrl
            setBody(LoginRequest(email, password))
        }.body()
    }

    fun getEnvironmentInfo(): String {
        return """
            Environment: ${Config.environment}
            Debug Mode: ${Config.isDebug}
            API Version: ${Config.apiVersion}
        """.trimIndent()
    }
}
```

### Автоматическая инъекция API ключа

```kotlin
// В network layer (core/network/src/.../PlatformEngine.kt)
fun createHttpClient(): HttpClient {
    return HttpClient(httpClientEngine) {
        defaultRequest {
            url(Config.apiBaseUrl) // Автоматически использует правильный URL

            headers {
                append("X-API-Key", Config.apiKey) // Автоматически добавляет API ключ
                append("X-API-Version", Config.apiVersion)
            }
        }
    }
}
```

---

## 🔐 Управление секретами

### Локальная разработка

#### 1. Создайте `secrets.properties` из template

```bash
cp config/secrets/secrets.properties.template secrets.properties
```

#### 2. Заполните реальные значения

```properties
# secrets.properties
api.key=sk_live_abc123xyz789
map.api.key=AIzaSyBdZr7XQ2Y8WxG5H6J
```

#### 3. `secrets.properties` автоматически загружается Gradle

```kotlin
// build.gradle.kts (корневой)
val secretsFiles = listOf(
    rootProject.file("secrets.properties"),
    rootProject.file("secrets.properties.local"),
    rootProject.file("local.secrets.properties")
)

secretsFiles.forEach { secretsFile ->
    if (secretsFile.exists()) {
        val secrets = java.util.Properties()
        secrets.load(secretsFile.inputStream())

        // Установить project properties
        secrets.forEach { key, value ->
            project.extensions.extraProperties.set(key, value)
        }
    }
}
```

### CI/CD (GitHub Actions пример)

```yaml
# .github/workflows/build.yml
name: Build Android Release

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Build Android Release
        run: ./gradlew assembleRelease
        env:
          # Secrets из GitHub Secrets
          API_KEY: ${{ secrets.API_KEY }}
          MAP_API_KEY: ${{ secrets.MAP_API_KEY }}
```

### Build Config (Android)

```kotlin
// androidApp/build.gradle.kts
android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Читает из secrets.properties или environment variables
        buildConfigField(
            "String",
            "API_KEY",
            "\"${project.findProperty("api.key") ?: System.getenv("API_KEY") ?: ""}\""
        )
        buildConfigField("String", "API_BASE_URL", "\"https://api.dev.aggregateservice.com\"")
        buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
    }

    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.aggregateservice.com\"")
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
        }
    }
}
```

### Info.plist (iOS)

```xml
<!-- iosApp/iosApp/Info.plist -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <!-- API Configuration -->
    <key>ApiBaseUrl</key>
    <string>https://api.aggregateservice.com</string>

    <key>ApiKey</key>
    <string>$(API_KEY)</string> <!-- Из Build Settings -->

    <key>Environment</key>
    <string>PROD</string>

    <key>EnableLogging</key>
    <false/>
</dict>
</plist>
```

---

## 📦 Добавление новых параметров конфигурации

### Шаг 1: Добавьте в `AppConfig.kt` (common)

```kotlin
// core/config/src/commonMain/.../AppConfig.kt
expect class AppConfig {
    // Существующие параметры...
    val apiBaseUrl: String
    val apiKey: String

    // НОВЫЙ ПАРАМЕТР
    val maxRetries: Int
}

// Обновите Config singleton
object Config {
    val maxRetries: Int get() = instance.maxRetries
}
```

### Шаг 2: Реализуйте в Android

```kotlin
// core/config/src/androidMain/.../AppConfig.android.kt
actual class AppConfig actual constructor() {
    // ...

    actual val maxRetries: Int = BuildConfig.MAX_RETRIES
}
```

```kotlin
// androidApp/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("int", "MAX_RETRIES", "3")
    }
}
```

### Шаг 3: Реализуйте в iOS

```kotlin
// core/config/src/iosMain/.../AppConfig.ios.kt
actual class AppConfig actual constructor() {
    // ...

    actual val maxRetries: Int
        get() = readConfigKey(
            envKey = "MAX_RETRIES",
            plistKey = "MaxRetries",
            defaultValue = "3"
        ).toIntOrNull() ?: 3
}
```

```xml
<!-- Info.plist -->
<key>MaxRetries</key>
<integer>3</integer>
```

---

## 🔒 Безопасность

### ✅ DO (Безопасно)

| Практика | Описание |
|----------|----------|
| **Template files** | `config/secrets/secrets.properties.template` с placeholder'ами |
| **Environment variables** | CI/CD secrets через GitHub Secrets |
| **.gitignore** | Игнорировать `secrets.properties`, `local.properties` |
| **BuildConfig** | Генерировать значения через `buildConfigField` |
| **Info.plist** | Использовать переменные `${VARIABLE}` |

### ❌ DON'T (Небезопасно)

| Практика | Почему плохо |
|----------|--------------|
| **Hardcode secrets** | API keys попадают в git |
| **Commit secrets.properties** | Все видят ваши ключи |
| **Print API keys** | Логи могут утечь |
| **URL с keys** | Kibana/GitHub logs видят ключи |

---

## 🧪 Тестирование

### Unit тесты для Config

```kotlin
// core/config/src/commonTest/.../ConfigTest.kt
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigTest {
    @Test
    fun `config should be initialized`() {
        // Arrange
        Config.initialize(AppConfig())

        // Act & Assert
        assertEquals("https://api.dev.aggregateservice.com", Config.apiBaseUrl)
        assertEquals(Environment.DEV, Config.environment)
        assertTrue(Config.isDebug)
    }

    @Test
    fun `production config should not have debug enabled`() {
        // Arrange
        val prodConfig = AppConfig().apply { /* override to PROD */ }
        Config.initialize(prodConfig)

        // Assert
        assertEquals(Environment.PROD, Config.environment)
        assertTrue(!Config.isDebug || Config.environment == Environment.DEV)
    }
}
```

### Интеграционные тесты

```bash
# Проверить, что API ключ не попадает в git
git grep "sk_live_" || echo "✅ No secrets in git"

# Проверить, что .gitignore настроен правильно
git check-ignore -v secrets.properties
# Ожидаемый вывод: .gitignore:1:secrets.properties

# Проверить BuildConfig generation
./gradlew :androidApp:assembleDebug
ls androidApp/build/generated/source/buildConfig/debug/
```

---

## 📊 Best Practices

### 1. Используйте разные окружения

| Окружение | URL | Logging | API Key |
|-----------|-----|---------|---------|
| **DEV** | `api.dev.` | ✅ Вкл | Test key |
| **STAGING** | `api.staging.` | ❌ Выкл | Staging key |
| **PROD** | `api.` | ❌ Выкл | Production key |

### 2. Валидация конфигурации при старте

```kotlin
// В Application.onCreate
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Config.initialize(AppConfig())

        // Валидация
        require(Config.apiKey.isNotEmpty()) {
            "API key is empty! Check secrets.properties or CI/CD configuration."
        }

        require(Config.apiBaseUrl.startsWith("https://")) {
            "API base URL must use HTTPS!"
        }
    }
}
```

### 3. Feature flags через конфигурацию

```kotlin
// AppConfig.kt
expect class AppConfig {
    val featureNewCatalogEnabled: Boolean
    val featureAdvancedFilters: Boolean
}

// Использование
if (Config.featureNewCatalogEnabled) {
    NewCatalogScreen()
} else {
    LegacyCatalogScreen()
}
```

---

## 🐛 Troubleshooting

### Проблема: `Config not initialized`

**Ошибка:**
```
IllegalStateException: Config not initialized. Call Config.initialize() in Application.onCreate()
```

**Решение:**
```kotlin
// Android
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Config.initialize(AppConfig()) // ДОЛЖЕН БЫТЬ ВЫЗВАН ПЕРВЫМ
    }
}
```

### Проблема: API key пустой в release build

**Причина:** BuildConfig не генерируется для release

**Решение:**
```kotlin
// androidApp/build.gradle.kts
android {
    buildTypes {
        release {
            buildConfigField("String", "API_KEY", "\"${System.getenv("API_KEY")}\"")
        }
    }
}
```

### Проблема: iOS не читает Info.plist

**Причина:** Неправильный ключ в Info.plist

**Решение:**
```xml
<!-- Info.plist -->
<key>ApiKey</key> <!-- PascalCase! -->
<string>your_key</string>
```

```kotlin
// AppConfig.ios.kt
NSBundle.mainBundle.objectForInfoDictionaryKey("ApiKey") // "ApiKey" с заглавной
```

---

## 🔗 Связанные документы

- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) - Статус реализации модуля config
- [NETWORK_LAYER.md](NETWORK_LAYER.md) - Интеграция Config с network layer
- [BUILD_LOGIC.md](BUILD_LOGIC.md) - Gradle build configuration
- [CODE_QUALITY_GUIDE.md](CODE_QUALITY_GUIDE.md) - Security best practices

---

## 📈 Status

| Компонент | Статус | Прогресс |
|-----------|--------|----------|
| **expect/actual implementation** | ✅ Complete | 100% |
| **Android BuildConfig** | ✅ Complete | 100% |
| **iOS Info.plist** | ✅ Complete | 100% |
| **Secrets management** | ✅ Complete | 100% |
| **Network layer integration** | ✅ Complete | 100% |
| **Documentation** | ✅ Complete | 100% |

---

**Версия документа**: 1.1
**Last Updated**: 2026-03-20
**Maintainer**: Development Team
