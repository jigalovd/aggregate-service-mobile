# Mobile Documentation

Документация мобильного приложения Aggregate Service на базе Kotlin Multiplatform + Compose Multiplatform.

---

## Обзор

Эта папка содержит адаптированную бизнес-документацию для мобильной разработки. Оригинальные документы находятся в `docs/business/` (из бэкенд-проекта).

---

## Документы

### Стратегия и требования

| Документ | Описание | Статус |
|----------|----------|--------|
| [I18N_STRATEGY.md](I18N_STRATEGY.md) | Стратегия локализации (RU/HE/EN) с RTL поддержкой | ✅ Ready |
| [IMPLEMENTATION_GAP.md](IMPLEMENTATION_GAP.md) | Gap analysis: Backend 100% vs Mobile 20% | ✅ Ready |
| [USER_FLOW_UX_COMPLIANCE.md](USER_FLOW_UX_COMPLIANCE.md) | Анализ соответствия USER_FLOW → UX Guidelines | ✅ Ready |
| [UI_FLOWS.md](UI_FLOWS.md) | UI/UX потоки и wireframes | 🔄 Planned |
| [USER_STORIES.md](USER_STORIES.md) | User stories по эпикам | 🔄 Planned |
| [MVP_SCOPE.md](MVP_SCOPE.md) | MVP scope и приоритеты | 🔄 Planned |
| [PERSONAS.md](PERSONAS.md) | Персоны пользователей | 🔄 Planned |

---

## Ключевые решения

### Локализация (i18n)

**Поддерживаемые языки:**

| Код | Язык | RTL | Приоритет |
|-----|------|-----|-----------|
| `ru` | Русский | Нет | P0 (MVP) |
| `he` | Иврит | **Да** | P0 (MVP) |
| `en` | Английский | Нет | P1 |

**Реализация RTL:**

```kotlin
// Theme.kt
CompositionLocalProvider(
    LocalLayoutDirection provides if (locale.isRtl) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
) {
    AppContent()
}
```

### Форматы данных

| Язык | Дата | Валюта | Числа |
|------|------|--------|-------|
| RU | `dd.MM.yyyy` | 1 500 ₽ | 1 234,56 |
| HE | `dd.MM.yyyy` | 1,000 ₪ | 1,234.56 |
| EN | `MM/dd/yyyy` | $1,000.00 | 1,234.56 |

---

## Архитектура

### Feature-First + Clean Architecture

```
feature/
├── domain/      # Entities, UseCases, Repository interfaces
├── data/        # Repository implementations, DTOs, API
└── presentation/ # ScreenModels, UI State, Compose Screens
```

### Core Modules

| Модуль | Назначение |
|--------|------------|
| `core:network` | Ktor client, SafeApiCall, AppError |
| `core:storage` | DataStore, TokenStorage |
| `core:theme` | Material 3 Theme, Colors, Typography |
| `core:i18n` | Localization, AppLocale, StringKey |
| `core:navigation` | Voyager navigation |

---

## Связанные документы

- [Architecture Overview](../01_KMP_CMP_ANALYSIS.md) - KMP/CMP анализ
- [Design System](../04_DESIGN_SYSTEM.md) - Design System
- [UX Guidelines](../05_UX_GUIDELINES.md) - UX Guidelines
- [Implementation Status](../IMPLEMENTATION_STATUS.md) - Статус реализации
- [Backend Business Docs](../business/) - Оригинальные бизнес-документы

---

**Last Updated:** 2026-03-21
