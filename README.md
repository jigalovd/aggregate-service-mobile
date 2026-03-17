# Mobile Application (KMP + CMP)

Это мобильное приложение для Beauty Service Aggregator, построенное на базе Kotlin Multiplatform (KMP) и Compose
Multiplatform (CMP).

## Структура

- `shared/` - Общий код бизнес-логики и UI (Compose Multiplatform).
- `androidApp/` - Нативное приложение для Android.
- `iosApp/` - Нативное приложение для iOS.

## Технологический стек

- **Kotlin Multiplatform 1.9.22+**
- **Compose Multiplatform 1.6.0+**
- **Ktor Client** (сетевые запросы)
- **Koin** (Dependency Injection)
- **Voyager** (Навигация)
- **DataStore** (Локальное хранение)
- **Coil 3** (Изображения)

Подробный анализ и планы внедрения см. в `docs/mobile/docs/01_KMP_CMP_ANALYSIS.md`.
