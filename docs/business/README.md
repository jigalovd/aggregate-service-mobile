# Business Documentation

**Важное:** Эти документы из бэкенд-проекта и содержат примеры на Flutter/Dart. Для Kotlin Multiplatform версий см. [docs/mobile/](../mobile/)

---

## Справочные материалы (Reference Only)

| Документ | Описание |
|----------|----------|
| [01_USER_STORIES.md](01_USER_STORIES.md) | User stories + API endpoints |
| [02_USE_CASES.md](02_USE_CASES.md) | Детальные сценарии использования |
| [03_USER_FLOW.md](03_USER_FLOW.md) | UI/UX потоки (wireframes) |
| [05_I18N_STRATEGY.md](05_I18N_STRATEGY.md) | i18n стратегия (Flutter examples) |
| [07_USER_PERSONAS.md](07_USER_PERSONAS.md) | Персоны пользователей |
| [08_CUSTOMER_JOURNEY.md](08_CUSTOMER_JOURNEY.md) | Customer Journey Maps |
| [11_MVP_SCOPE.md](11_MVP_SCOPE.md) | MVP scope definition |

---

## Адаптированные документы (KMP/CMP)

| Документ | Расположение | Описание |
|----------|--------------|----------|
| I18N_STRATEGY.md | [mobile/I18N_STRATEGY.md](../mobile/I18N_STRATEGY.md) | Адаптировано под KMP/CMP |
| IMPLEMENTATION_GAP.md | [mobile/IMPLEMENTATION_GAP.md](../mobile/IMPLEMENTATION_GAP.md) | Gap analysis backend vs mobile |
| USER_FLOW_UX_COMPLIANCE.md | [mobile/USER_FLOW_UX_COMPLIANCE.md](../mobile/USER_FLOW_UX_COMPLIANCE.md) | UX compliance report |

---

## API Reference

| Endpoint | Описание |
|----------|----------|
| `POST /auth/login` | Авторизация |
| `POST /auth/register` | Регистрация |
| `POST /auth/refresh` | Обновление токена |
| `GET /providers` | Поиск мастеров |
| `GET /providers/{id}` | Профиль мастера |
| `GET /providers/{id}/services` | Услуги мастера |
| `GET /slots` | Доступные слоты |
| `POST /bookings` | Создание записи |
| `PATCH /bookings/{id}/cancel` | Отмена записи |

Полный API reference: [BACKEND_API_REFERENCE.md](../BACKEND_API_REFERENCE.md)

---

**Last Updated:** 2026-03-21
