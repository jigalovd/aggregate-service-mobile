# Mobile Documentation - Kotlin Multiplatform

Aggregate Service Mobile App на базе Kotlin Multiplatform и Compose Multiplatform.

## Обзор документов

### Архитектура и анализ

| Документ                                                     | Описание                                            |
|--------------------------------------------------------------|-----------------------------------------------------|
| [01_KMP_CMP_ANALYSIS.md](01_KMP_CMP_ANALYSIS.md)             | Анализ Kotlin Multiplatform и Compose Multiplatform |
| [TECHNOLOGY_STACK_ANALYSIS.md](TECHNOLOGY_STACK_ANALYSIS.md) | 🔬 Анализ технологического стека (плюсы/минусы)    |
| [NETWORK_LAYER.md](NETWORK_LAYER.md)                         | 🌐 Network Layer архитектура (Ktor 3.0.3)           |
| [BUILD_LOGIC.md](BUILD_LOGIC.md)                             | 🔧 Build Logic & Convention Plugins                  |
| [CONFIG_MANAGEMENT.md](CONFIG_MANAGEMENT.md)                 | 🔐 Централизованное управление конфигурацией         |
| [02_MAP_PROVIDERS_ANALYSIS.md](02_MAP_PROVIDERS_ANALYSIS.md) | Сравнительный анализ поставщиков карт               |
| [04_DESIGN_SYSTEM.md](04_DESIGN_SYSTEM.md)                   | Design System базового уровня                       |
| [05_UX_GUIDELINES.md](05_UX_GUIDELINES.md)                   | UX Guidelines                                       |
| [CODE_QUALITY_GUIDE.md](CODE_QUALITY_GUIDE.md)               | 📚 Гайд по Detekt и Ktlint                          |

### Трекинг и статус

| Документ                                                     | Описание                                            |
|--------------------------------------------------------------|-----------------------------------------------------|
| [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)         | 📊 Статус реализации проекта (20% complete)         |
| [CHANGELOG.md](../CHANGELOG.md)                             | 📝 Changelog проекта и миграции                      |

### Планирование и ревью

| Документ                                                     | Описание                                            |
|--------------------------------------------------------------|-----------------------------------------------------|
| [plans/01-quality-infrastructure-and-cicd.md](plans/01-quality-infrastructure-and-cicd.md) | 🎯 План внедрения качества кода и CI/CD (2 weeks) |
| [reviews/2026-03-19-deep-code-review.md](reviews/2026-03-19-deep-code-review.md) | 🚨 Deep Code Review (Zero Tolerance Audit)         |

## Технологический стек

### Core

- **Kotlin Multiplatform** - кроссплатформенная разработка
- **Compose Multiplatform** - декларативный UI
- **Coroutines** - асинхронное программирование
- **Ktor Client** - сетевые запросы
- **Kotlinx Serialization** - сериализация JSON

### Platform-specific

- **Android**: Jetpack Compose, Material 3
- **iOS**: Compose UI (Skia rendering)

### Infrastructure

- **Koin** - Dependency Injection
- **Voyager / Decompose** - Navigation
- **DataStore** - локальное хранение
- **Coil** - загрузка изображений

## Архитектура: Feature-First + Clean Architecture

```
mobile/
├── shared/                           # KMP Shared Module
│   ├── commonMain/kotlin/
│   │   ├── core/                     # Ядро (переиспользуемый код)
│   │   │   ├── network/              # HTTP client, interceptors
│   │   │   ├── storage/              # DataStore, cache
│   │   │   ├── theme/                # Theme, colors, typography
│   │   │   ├── i18n/                 # Локализация
│   │   │   └── utils/                # Utilities
│   │   │
│   │   └── features/                 # 🎯 Feature-First: Бизнес-фичи
│   │       ├── auth/                 # Фича "Аутентификация"
│   │       │   ├── domain/           # Domain layer (100% shared)
│   │       │   │   ├── model/        # User, AuthTokens
│   │       │   │   ├── repository/   # AuthRepository (interface)
│   │       │   │   └── usecase/      # LoginUseCase, RegisterUseCase
│   │       │   │
│   │       │   ├── data/             # Data layer (100% shared)
│   │       │   │   ├── remote/       # AuthApiService
│   │       │   │   ├── local/        # TokenStorage
│   │       │   │   └── repository/   # AuthRepositoryImpl
│   │       │   │
│   │       │   └── presentation/     # Presentation layer (100% shared)
│   │       │       ├── model/        # UI State models
│   │       │       ├── viewmodel/    # AuthViewModel
│   │       │       └── ui/           # LoginScreen, RegisterScreen
│   │       │
│   │       ├── catalog/              # Фича "Каталог"
│   │       │   ├── domain/
│   │       │   ├── data/
│   │       │   └── presentation/
│   │       │
│   │       ├── booking/              # Фича "Бронирование"
│   │       │   ├── domain/
│   │       │   ├── data/
│   │       │   └── presentation/
│   │       │
│   │       ├── profile/              # Фича "Профиль"
│   │       │   ├── domain/
│   │       │   ├── data/
│   │       │   └── presentation/
│   │       │
│   │       └── favorites/            # Фича "Избранное"
│   │           ├── domain/
│   │           ├── data/
│   │           └── presentation/
│   │
│   ├── androidMain/kotlin/           # Android-specific
│   │   └── platform/
│   │
│   └── iosMain/kotlin/               # iOS-specific
│       └── platform/
│
├── androidApp/                       # Android Application
│   └── src/main/
│
└── iosApp/                           # iOS Application
    └── iosApp/
```

## Feature-First принципы

### Правила организации кода

| Правило                | Описание                                            |
|------------------------|-----------------------------------------------------|
| **Feature = папка**    | Каждая фича в отдельной папке `features/{feature}/` |
| **3 слоя внутри**      | domain, data, presentation внутри каждой фичи       |
| **Domain независим**   | Domain layer не зависит от фреймворков              |
| **Zero cross-imports** | Фичи не импортируют друг друга напрямую             |
| **Shared Kernel**      | Общий код в `core/`, не в фичах                     |

### Зависимости слоев

```
┌─────────────────────────────────────────────────┐
│                 presentation                     │
│  (UI, ViewModels, Navigation)                   │
│                      ↓                           │
├─────────────────────────────────────────────────┤
│                   domain                         │
│  (Entities, Use Cases, Repository interfaces)   │
│                      ↓                           │
├─────────────────────────────────────────────────┤
│                    data                          │
│  (Repository implementations, API, Storage)     │
└─────────────────────────────────────────────────┘
```

## Быстрый старт

```bash
# Клонировать репозиторий
git clone <repo-url>
cd beauty-service/mobile

# Сборка Android
./gradlew :androidApp:assembleDebug

# Сборка iOS (требуется macOS)
./gradlew :shared:linkDebugFrameworkIos
```

## Связанные документы

- [Backend Architecture](../architecture/backend/README.md) - архитектура backend
- [Coding Standards](../architecture/CODING_STANDARDS.md) - стандарты кода
- [API Design](../architecture/backend/01_API_DESIGN.md) - API спецификация

## Статус проекта

**Phase**: Infrastructure Setup (80% complete)
**Architecture**: Feature-First + Clean Architecture
**Last Updated**: 2026-03-19

### Достижения (2026-03-19)

- ✅ **Core Infrastructure**: Gradle convention plugins, version catalog, KMP structure
- ✅ **Quality Infrastructure**: Detekt (0 warnings), Ktlint (100% compliance), Kover configured
- ✅ **Network Layer**: Ktor 3.0.3 с platform-specific engines (OkHttp/Darwin)
- ✅ **Config Management**: Expect/actual pattern для централизованной конфигурации (API keys, environments)
- 🔄 **Feature Development**: Auth feature запланирован (Week 3-4)
