# Mobile Application (KMP + CMP)

Мобильное приложение для Aggregate Service, построенное на базе Kotlin Multiplatform (KMP) и Compose Multiplatform (CMP).

## Структура

```
├── app/                    # Главный модуль приложения
├── androidApp/             # Android application entry point
├── core/
│   ├── network/            # Ktor client, SafeApiCall, AppError, AuthInterceptor
│   ├── storage/            # DataStore Preferences, TokenStorage
│   ├── config/             # AppConfig (expect/actual pattern)
│   ├── utils/              # Validators, Extensions
│   ├── navigation/         # Voyager navigation setup
│   ├── di/                 # Koin modules
│   ├── theme/              # Material 3 Theme (planned)
│   └── i18n/               # Localization (planned)
├── feature/
│   ├── auth/               # ✅ Authentication feature (Clean Architecture)
│   ├── catalog/            # Catalog feature (planned)
│   ├── booking/            # Booking feature (planned)
│   ├── profile/            # Profile feature (planned)
│   ├── favorites/          # Favorites feature (planned)
│   ├── schedule/           # Schedule feature (planned)
│   └── reviews/            # Reviews feature (planned)
└── build-logic/            # Gradle convention plugins
```

## Технологический стек

| Категория | Технология | Версия |
|-----------|------------|--------|
| **Language** | Kotlin | 2.2.20 |
| **UI** | Compose Multiplatform | 1.10.2 |
| **Build** | AGP | 8.12.3 |
| **Network** | Ktor Client | 3.4.1 |
| **DI** | Koin | 4.2.0 |
| **Navigation** | Voyager | 1.1.0-beta02 |
| **Storage** | DataStore Preferences | 1.2.1 |
| **Serialization** | Kotlinx Serialization | 1.10.0 |
| **Coroutines** | Kotlinx Coroutines | 1.10.2 |
| **Images** | Coil 3 | 3.4.0 |
| **Quality** | Detekt | 1.23.8 |
| **Quality** | Ktlint | 13.1.0 |
| **Coverage** | Kover | 0.9.7 |

## Архитектура

- **Feature-First Modularization** - Каждая фича в отдельном модуле
- **Clean Architecture** - Разделение на Domain/Data/Presentation слои
- **UDF Pattern** - Unidirectional Data Flow в UI

## Статус реализации

| Компонент | Статус |
|-----------|--------|
| Core Infrastructure | ✅ 100% |
| Auth Feature | ✅ 100% |
| Catalog Feature | ⚪ Planned |
| Booking Feature | ⚪ Planned |

Подробности: [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md)

## Сборка

```bash
# Сборка проекта
./gradlew assembleDebug

# Проверка кода
./gradlew detektAll ktlintCheckAll

# Тесты
./gradlew allTests

# Coverage report
./gradlew koverReportAll
```

## Документация

### Quick Links

- [Implementation Status](docs/IMPLEMENTATION_STATUS.md) - Текущий прогресс реализации
- [Auth Feature](docs/features/AUTH_FEATURE.md) - Документация Auth Feature (100% complete)
- [API Reference](docs/BACKEND_API_REFERENCE.md) - Backend API документация

### Architecture & Infrastructure

- [Network Layer](docs/NETWORK_LAYER.md) - Ktor client, safeApiCall, AppError
- [Build Logic](docs/BUILD_LOGIC.md) - Gradle convention plugins
- [Config Management](docs/CONFIG_MANAGEMENT.md) - Управление конфигурацией и секретами

### Quality

- [Code Quality Guide](docs/CODE_QUALITY_GUIDE.md) - Detekt и Ktlint
- [Testing Infrastructure](docs/TESTING_INFRASTRUCTURE.md) - Инфраструктура тестирования

### Technology Stack

- [KMP/CMP Analysis](docs/01_KMP_CMP_ANALYSIS.md) - Анализ технологического стека
- [Technology Stack](docs/TECHNOLOGY_STACK_ANALYSIS.md) - Детальный анализ технологий

## Требования

- JDK 21
- Android SDK 36
- Gradle 8.14.4
- macOS (для iOS сборки)
