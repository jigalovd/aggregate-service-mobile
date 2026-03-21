# Mobile Documentation - Kotlin Multiplatform

Aggregate Service Mobile App на базе Kotlin Multiplatform и Compose Multiplatform.

**👉 [Полный индекс документации](00_INDEX.md)**

---

## 📊 Статус проекта

| Метрика | Значение |
|---------|----------|
| **Общий прогресс** | 45% |
| **Core Infrastructure** | 100% |
| **Features Implemented** | 1/7 (Auth) |
| **Architecture** | Feature-First + Clean Architecture |

---

## 📁 Структура документации

```
docs/
├── 00_INDEX.md           # 📚 Полный индекс документации
├── README.md             # Этот файл
├── IMPLEMENTATION_STATUS.md  # 📊 Статус реализации
│
├── architecture/         # 🏗️ Архитектура и технологии
├── design/               # 🎨 UI/UX и Design System
├── quality/              # ✅ Качество кода и тестирование
├── business/             # 💼 Бизнес-требования (reference)
├── mobile/               # 📱 Мобильная разработка KMP/CMP
├── features/             # 🔐 Документация по фичам
├── plans/                # 📋 Планы развития
├── reports/              # 📊 Ревью и отчёты
├── reviews/              # 📋 Code reviews
└── api/                  # 🔗 Backend API
```

---

## 🚀 Быстрый старт

### Ключевые документы

| Документ | Описание |
|----------|----------|
| [00_INDEX.md](00_INDEX.md) | 📚 Полный индекс всей документации |
| [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) | 📊 Статус реализации (45% complete) |
| [Development Roadmap](plans/02-development-roadmap.md) | 🗺️ Roadmap на 12 недель |

### Архитектура

| Документ | Описание |
|----------|----------|
| [KMP/CMP Analysis](architecture/01_KMP_CMP_ANALYSIS.md) | Анализ Kotlin Multiplatform + Compose Multiplatform |
| [Network Layer](architecture/NETWORK_LAYER.md) | Network Layer архитектура (Ktor 3.4.1) |
| [Build Logic](architecture/BUILD_LOGIC.md) | Build Logic & Convention Plugins |

### UI/UX

| Документ | Описание |
|----------|----------|
| [Design System](design/04_DESIGN_SYSTEM.md) | Design System базового уровня |
| [UX Guidelines](design/05_UX_GUIDELINES.md) | UX Guidelines |

### Качество

| Документ | Описание |
|----------|----------|
| [Code Quality Guide](quality/CODE_QUALITY_GUIDE.md) | Гайд по Detekt и Ktlint |
| [Testing Infrastructure](quality/TESTING_INFRASTRUCTURE.md) | Инфраструктура тестирования |

---

## 🏗️ Архитектура проекта

### Feature-First + Clean Architecture

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
│   │       ├── auth/                 # Фича "Аутентификация" ✅
│   │       ├── catalog/              # Фича "Каталог" 🔄
│   │       ├── booking/              # Фича "Бронирование"
│   │       ├── profile/              # Фича "Профиль"
│   │       └── favorites/            # Фича "Избранное"
│   │
│   ├── androidMain/kotlin/           # Android-specific
│   └── iosMain/kotlin/               # iOS-specific
│
├── androidApp/                       # Android Application
└── iosApp/                           # iOS Application
```

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

---

## 🔧 Технологический стек

### Core

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Kotlin Multiplatform** | 2.2.20 | Кроссплатформенная разработка |
| **Compose Multiplatform** | 1.10.2 | Декларативный UI |
| **Ktor Client** | 3.4.1 | Сетевые запросы |
| **Koin** | 4.2.0 | Dependency Injection |
| **Voyager** | 1.1.0-beta02 | Navigation |
| **DataStore** | 1.2.1 | Локальное хранение |
| **Coil** | 3.4.0 | Загрузка изображений |

### Code Quality

| Инструмент | Версия | Назначение |
|------------|--------|------------|
| **Detekt** | 1.23.8 | Static analysis (zero tolerance) |
| **Ktlint** | 13.1.0 | Linter + Formatter |
| **Kover** | 0.9.7 | Test coverage (target: 60%+) |

---

## 📋 Текущий спринт

**Phase 2: Catalog Feature (Week 3-4)**

- [ ] Domain: Provider, Service, Category entities
- [ ] Data: CatalogApiService, DTOs, Repository
- [ ] Presentation: CatalogScreen, SearchScreen

---

## 🔗 Связанные ресурсы

- [Backend API Reference](api/BACKEND_API_REFERENCE.md) - API документация
- [Changelog](../CHANGELOG.md) - История изменений
- [Business Requirements](business/) - Бизнес-требования

---

**Last Updated:** 2026-03-21
**Architecture:** Feature-First + Clean Architecture
**Phase:** Phase 2 - Catalog Feature
