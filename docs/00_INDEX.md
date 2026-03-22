# 📚 Документация Aggregate Service Mobile

**Последнее обновление:** 2026-03-22
**Версия:** 2.2
**Прогресс проекта:** 62%

---

## 🎯 Быстрая навигация

| Домен | Описание | Документы |
|-------|----------|-----------|
| [🏗️ Architecture](architecture/) | Технологический стек и архитектура | 7 |
| [🎨 Design](design/) | UI/UX и Design System | 3 |
| [✅ Quality](quality/) | Качество кода и тестирование | 3 |
| [💼 Business](business/) | Бизнес-требования (reference) | 8 |
| [📱 Mobile](mobile/) | Мобильная разработка KMP/CMP | 8 |
| [🔐 Features](features/) | Документация по фичам | 2 |
| [📋 Plans](plans/) | Планы развития | 3 |
| [📊 Reports](reports/) | Ревью и отчёты | 5 |
| [🔗 API](api/) | Backend API | 1 |

---

## 🏗️ Architecture

Технологический стек и архитектура проекта.

| Документ | Описание |
|----------|----------|
| [KMP_CMP_ANALYSIS.md](architecture/01_KMP_CMP_ANALYSIS.md) | Анализ Kotlin Multiplatform + Compose Multiplatform |
| [TECHNOLOGY_STACK_ANALYSIS.md](architecture/TECHNOLOGY_STACK_ANALYSIS.md) | Анализ технологического стека (плюсы/минусы) |
| [NETWORK_LAYER.md](architecture/NETWORK_LAYER.md) | Network Layer архитектура (Ktor 3.4.1) |
| [BUILD_LOGIC.md](architecture/BUILD_LOGIC.md) | Build Logic & Convention Plugins |
| [CONFIG_MANAGEMENT.md](architecture/CONFIG_MANAGEMENT.md) | Централизованное управление конфигурацией |
| [CONFIG_IMPLEMENTATION_SUMMARY.md](architecture/CONFIG_IMPLEMENTATION_SUMMARY.md) | Сводка по конфигурации |
| [Анализ подходов к разработке интерфейсов.md](architecture/Анализ_подходов_к_разработке_интерфейсов.md) | Анализ подходов к разработке UI |

---

## 🎨 Design

UI/UX дизайн и Design System.

| Документ | Описание |
|----------|----------|
| [DESIGN_SYSTEM.md](design/04_DESIGN_SYSTEM.md) | Design System базового уровня |
| [UX_GUIDELINES.md](design/05_UX_GUIDELINES.md) | UX Guidelines |
| [MAP_PROVIDERS_ANALYSIS.md](design/02_MAP_PROVIDERS_ANALYSIS.md) | Сравнительный анализ поставщиков карт |

---

## ✅ Quality

Качество кода и тестирование.

| Документ | Описание |
|----------|----------|
| [CODE_QUALITY_GUIDE.md](quality/CODE_QUALITY_GUIDE.md) | Гайд по Detekt и Ktlint |
| [TESTING_INFRASTRUCTURE.md](quality/TESTING_INFRASTRUCTURE.md) | Инфраструктура тестирования |
| [TESTING_QUICK_START.md](quality/TESTING_QUICK_START.md) | Быстрый старт по тестированию |

---

## 💼 Business

Бизнес-требования (Reference Only — из бэкенд-проекта).

| Документ | Описание |
|----------|----------|
| [USER_STORIES.md](business/01_USER_STORIES.md) | User stories + API endpoints |
| [USE_CASES.md](business/02_USE_CASES.md) | Детальные сценарии использования |
| [USER_FLOW.md](business/03_USER_FLOW.md) | UI/UX потоки (wireframes) |
| [I18N_STRATEGY.md](business/05_I18N_STRATEGY.md) | i18n стратегия (Flutter examples) |
| [USER_PERSONAS.md](business/07_USER_PERSONAS.md) | Персоны пользователей |
| [CUSTOMER_JOURNEY.md](business/08_CUSTOMER_JOURNEY.md) | Customer Journey Maps |
| [MVP_SCOPE.md](business/11_MVP_SCOPE.md) | MVP scope definition |
| [README.md](business/README.md) | Обзор бизнес-документации |

---

## 📱 Mobile

Мобильная разработка на Kotlin Multiplatform + Compose Multiplatform.

| Документ | Описание | Статус |
|----------|----------|--------|
| [I18N_STRATEGY.md](mobile/I18N_STRATEGY.md) | i18n стратегия (KMP/CMP адаптация) | ✅ Ready |
| [IMPLEMENTATION_GAP.md](mobile/IMPLEMENTATION_GAP.md) | Gap analysis: Backend vs Mobile | ✅ Ready |
| [USER_FLOW_UX_COMPLIANCE.md](mobile/USER_FLOW_UX_COMPLIANCE.md) | UX compliance report | ✅ Ready |
| [README.md](mobile/README.md) | Обзор мобильной документации | ✅ Ready |
| UI_FLOWS.md | UI/UX потоки | 🔄 Planned |
| USER_STORIES.md | User stories по эпикам | 🔄 Planned |
| MVP_SCOPE.md | MVP scope | 🔄 Planned |
| PERSONAS.md | Персоны | 🔄 Planned |

---

## 🔐 Features

Документация по фичам.

| Документ | Описание | Статус |
|----------|----------|--------|
| [AUTH_FEATURE.md](features/AUTH_FEATURE.md) | Auth Feature (Login, Logout, Refresh, Guest Mode) | ✅ Complete (100%) |
| [CATALOG_FEATURE.md](features/CATALOG_FEATURE.md) | Catalog Feature (Providers, Services, Categories, Search) | ✅ Complete (95%) |
| [BOOKING_FEATURE.md](features/BOOKING_FEATURE.md) | Booking Feature (Create, Cancel, Reschedule, Slots) | ✅ Complete (100%) |
| [SERVICES_FEATURE.md](features/SERVICES_FEATURE.md) | Services Feature (Provider CRUD operations) | ✅ Complete (100%) |

---

## 📋 Plans

Планы развития проекта.

| Документ | Описание | Статус |
|----------|----------|--------|
| [01-quality-infrastructure-and-cicd.md](plans/01-quality-infrastructure-and-cicd.md) | План качества и CI/CD | ✅ Complete |
| [02-development-roadmap.md](plans/02-development-roadmap.md) | Roadmap разработки (12 weeks) | 🔄 Active |
| [README.md](plans/README.md) | Обзор планов | ✅ Ready |

---

## 📊 Reports

Ревью и отчёты.

| Документ | Описание |
|----------|----------|
| [2026-03-18-deep-review.md](reviews/2026-03-18-deep-review.md) | Deep Code Review |
| [2026-03-19-deep-code-review.md](reviews/2026-03-19-deep-code-review.md) | Deep Code Review (Zero Tolerance) |
| [DEEP_CODE_REVIEW_2026-03-20.md](reports/DEEP_CODE_REVIEW_2026-03-20.md) | Deep Code Review |
| [CORE_THEME_AND_I18N_IMPLEMENTATION.md](reports/CORE_THEME_AND_I18N_IMPLEMENTATION.md) | Детали имплементации core:theme и core:i18n |
| [UX_GUIDELINES_COMPLIANCE_REPORT.md](reports/UX_GUIDELINES_COMPLIANCE_REPORT.md) | UX Compliance Report |

---

## 🔗 API

Backend API документация.

| Документ | Описание |
|----------|----------|
| [BACKEND_API_REFERENCE.md](api/BACKEND_API_REFERENCE.md) | Полный справочник по Backend API |

---

## 📈 Статус проекта

| Метрика | Значение |
|---------|----------|
| **Общий прогресс** | 62% |
| **Core Infrastructure** | 100% |
| **Quality Infrastructure** | 100% |
| **Features Implemented** | 4/7 (Auth 100%, Catalog 95%, Booking 100%, Services 100%) |
| **Test Coverage** | 45% |

---

## 🚀 Текущий спринт

**Sprint 7: Services Feature ✅ COMPLETE**

- [x] Domain: ProviderService entity, CreateServiceRequest, UpdateServiceRequest
- [x] Domain: ServicesRepository interface, 5 UseCases (CRUD)
- [x] Data: ServicesApiService with safeApiCall, ServiceMapper
- [x] Presentation: ServicesListScreen, ServiceFormScreen
- [x] DI: ServicesModule registered in MainApplication

**Next Sprint: Profile Feature**

---

## 📁 Структура папок

```
docs/
├── 00_INDEX.md           # Этот файл
├── README.md             # Обзор проекта
├── IMPLEMENTATION_STATUS.md  # Статус реализации
│
├── architecture/         # 🏗️ Архитектура и технологии (7 docs)
├── design/               # 🎨 UI/UX и Design System (3 docs)
├── quality/              # ✅ Качество кода и тестирование (3 docs)
├── business/             # 💼 Бизнес-требования (8 docs)
├── mobile/               # 📱 Мобильная разработка (8 docs)
├── features/             # 🔐 Документация по фичам (1 doc)
├── plans/                # 📋 Планы развития (3 docs)
├── reports/              # 📊 Ревью и отчёты (5 docs)
├── reviews/              # 📋 Code reviews (2 docs)
└── api/                  # 🔗 Backend API (1 doc)
```

---

**Поддерживается:** Development Team
**Контакт:** см. README.md
