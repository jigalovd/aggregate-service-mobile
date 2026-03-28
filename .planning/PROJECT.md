# Aggregate Service Mobile

## What This Is

Kotlin Multiplatform Mobile (KMP) приложение для бронирования услуг мастеров (b2c marketplace). Пользователи могут искать мастеров, просматривать профили, бронировать услуги и управлять своими записями. Поддерживается Android и iOS.

**Core Value:** Пользователь может найти и забронировать услугу мастера за 3 клика

## Requirements

### Validated

- ✓ **Auth Feature** — Firebase Auth (Google, Apple, Phone), Guest mode, JWT tokens — sprint 2
- ✓ **Catalog Feature** — Domain/Data/Presentation complete, UI pending — sprint 4-5
- ✓ **Booking Feature** — Domain/Data/Presentation complete — sprint 6
- ✓ **Services Feature (Provider)** — CRUD для услуг мастера — sprint 7
- ✓ **Profile Feature** — Просмотр/редактирование профиля — sprint 8
- ✓ **Favorites Feature** — Domain/Data/Presentation complete, UI pending — sprint 9
- ✓ **Reviews Feature** — Domain/Data/Presentation complete, UI pending — sprint 10
- ✓ **Core Infrastructure** — KMP structure, convention plugins, Koin DI, Ktor networking, Voyager navigation, DataStore — sprint 1
- ✓ **Core:Theme** — Material 3 theme, light/dark, RTL support — sprint 3
- ✓ **Core:I18n** — ru, he, en localization — sprint 3

### Active

- [ ] **UI Integration** — Подключить Catalog/Booking/Profile/Favorites/Reviews screens к реальным данным
- [ ] **Registration Flow** — Email/password registration (пока есть только login)
- [ ] **Maps Integration** — Показать мастеров на карте (Google Maps Android, MapKit iOS)
- [ ] **Test Coverage** — Довести до 80% (сейчас 55%)
- [ ] **CI/CD** — GitHub Actions pipeline

### Out of Scope

- [Desktop/Web] — Mobile-only
- [Real-time chat] — Не требуется для MVP
- [Video calls] — Not core to booking value
- [Push notifications] — Отложено на v2

## Context

**Текущий прогресс:** 86% (по IMPLEMENTATION_STATUS.md)

Структура:
- Feature-first Clean Architecture
- 7 feature modules: auth, catalog, booking, services, profile, favorites, reviews
- 8 core modules: network, config, storage, utils, navigation, di, theme, i18n
- KMP с Android + iOS targets

**Проблемы:**
- UI слои фичей не подключены к реальным screens (заглушки)
- 7 technical debt items в CONCERNS.md
- Auth race conditions при token refresh
- Нет pagination в списках
- Нет offline mode

**Tech Stack:**
- Kotlin 2.2.20, Compose Multiplatform 1.10.2
- Ktor 3.4.1, Koin 4.2.0, Voyager 1.1.0-beta02
- Firebase Auth (Android only), DataStore, Coil 3.4.0
- Detekt + Ktlint + Kover для quality

## Constraints

- **[Platform]**: Android + iOS only, Kotlin Multiplatform — no Desktop/Web
- **[Language]**: Kotlin 2.2.20, Jetpack Compose for UI — no SwiftUI
- **[Auth]**: Firebase Auth — Google/Apple/Phone, no email/password yet
- **[API]**: REST backend — dev/staging URLs configured
- **[Timeline]**: MVP должен быть готов к production
- **[Quality]**: Zero tolerance на detekt/ktlint violations, 80% test coverage цель

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Feature-first architecture | Feature isolation, better than layer-first for this scale | ✓ Good |
| KMP over separate native | Code sharing Android/iOS, proven for this use case | ✓ Good |
| Voyager navigation | Compose-native, simple Screen sealed class pattern | ✓ Good |
| Firebase Auth | Google/Apple/Phone SSO, Android-only для Firebase | ⚠️ Revisit (iOS?) |
| Ktor over Retrofit | Multiplatform, lighter, Kotlin-first | ✓ Good |
| Feature isolation (Booking/Catalog) | Booking не зависит от Catalog напрямую | ✓ Good |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-03-28 after GSD initialization from IMPLEMENTATION_STATUS.md*
