# Эпики и Функциональные Требования

> **📊 Текущий статус реализации:** См. [`00_IMPLEMENTATION_STATUS.md`](./00_IMPLEMENTATION_STATUS.md)
> @./00_IMPLEMENTATION_STATUS.md
>
> **Общий прогресс:** ~85% (MVP: E1 + E2 + E3 + E7 complete)
>
> **Frontend Ready:** 12.03.2026 - Auth, Catalog, Booking API готовы
>
> **PM Analysis:** 27.02.2026 - E5 Reviews → Phase 2 (подтверждено)

---

## E1: Onboarding & Identity (Регистрация и Профиль)

### Статус: 🟢 95% - MULTI-ROLE AUTH COMPLETE

### Описание

Первый контакт пользователя с приложением. **Уникальная фича:** пользователь может иметь множество ролей (клиент,
мастер) и переключаться между ними.

### Реализовано

- [x] Модели User и Profile
- [x] **Множественные роли** (Role, UserRole, UserContext)
- [x] **Переключение контекста** между ролями
- [x] Password hashing (bcrypt)
- [x] JWT токены с current_role (python-jose)
- [x] Поле language_code для i18n
- [x] **API endpoints для регистрации/логина с множественными ролями**
- [x] **API endpoints для управления ролями и контекстом**
- [x] **Dependencies для проверки ролей (require_role, require_any_role)**
- [x] Верификация email
- [x] Сброс пароля
- [x] Refresh токены
- [x] Profile Management API

### Не реализовано

- [ ] Верификация телефона
- [ ] OAuth интеграция

### User Stories

* **US-1.1**: ✅ Я как Гость хочу быстро зарегистрироваться через email, указав одну или несколько ролей (клиент,
  мастер), чтобы начать пользоваться сервисом.
* **US-1.2**: ✅ Я как Мастер хочу иметь возможность добавить роль клиента, чтобы записываться к другим мастерам.
* **US-1.3**: ✅ Я как Пользователь хочу переключаться между ролями (клиент/мастер) в интерфейсе, чтобы получить доступ к
  соответствующим функциям.
* **US-1.4**: 🟡 Я как Мастер хочу заполнить свой профиль (фото работ, опыт), чтобы привлечь клиентов. (API готово)
* **US-1.5**: ❌ Я как Клиент хочу видеть историю своих записей и избранных мастеров.

**Технические детали:**

- Множественные роли реализованы через many-to-many связь `user_roles`
- Текущий контекст хранится в `user_contexts` и включается в JWT токен
- API: `GET/POST/DELETE /me/roles` для управления ролями
- API: `GET/PUT /me/context` для переключения контекста
- Middleware: `require_role(role)`, `require_any_role(*roles)` для защиты endpoints

## E2: Catalog & Geo-Search (Каталог и Поиск)

### Статус: ✅ 100% - MVP COMPLETE

**Frontend Ready:** Все API endpoints реализованы и протестированы.

### Описание

Основной сценарий поиска услуги. Пользователь ищет "здесь и сейчас" или планирует визит.

### Реализовано

- [x] Модели: Category, Service, ProviderService, Favorite
- [x] PostGIS для геолокации (GEOMETRY POINT, GiST indexes)
- [x] Geo-поиск по радиусу (ST_DWithin)
- [x] Фильтрация по категории, цене
- [x] Сортировка по расстоянию, рейтингу
- [x] Избранное (CRUD)

### API Endpoints

| Method | Endpoint | Описание |
|--------|----------|----------|
| GET | `/api/v1/catalog/categories` | Список категорий |
| GET | `/api/v1/catalog/categories/{id}` | Детали категории |
| GET | `/api/v1/catalog/services` | Список услуг (фильтрация по категории) |
| GET | `/api/v1/catalog/services/{id}` | Детали услуги |
| POST | `/api/v1/catalog/providers/search` | Geo-поиск мастеров |
| GET | `/api/v1/catalog/providers/{id}` | Профиль мастера |
| POST | `/api/v1/catalog/favorites` | Добавить в избранное |
| GET | `/api/v1/catalog/favorites` | Список избранного |
| DELETE | `/api/v1/catalog/favorites/{provider_id}` | Удалить из избранного |

### User Stories

* **US-2.1**: ✅ Я как Гость хочу видеть карту с точками мастеров вокруг меня (радиус поиска).
* **US-2.2**: ✅ Я как Гость хочу фильтровать мастеров по типу услуги (стрижка, маникюр) и цене.
* **US-2.3**: ✅ Я как Гость хочу видеть список мастеров списком, отсортированным по расстоянию или рейтингу.
* **US-2.4**: ✅ Я как Клиент хочу сохранить понравившегося мастера в "Избранное".

## E3: Booking Engine (Бронирование)

### Статус: ✅ 100% - MVP COMPLETE

**Frontend Ready:** 21+ API endpoints реализованы и протестированы.

### Описание

Ядро системы. Управление слотами времени, предотвращение овербукинга, поддержка множественных услуг, автоматические статусы.

### Детальный план

См. [`Epics/E3_BOOKING_ENGINE_PLAN.md`](./Epics/E3_BOOKING_ENGINE_PLAN.md) @./Epics/E3_BOOKING_ENGINE_PLAN.md

### Реализовано

- [x] Модели: Booking, BookingItem, ScheduleRule, BookingRescheduleHistory
- [x] Логика генерации слотов на основе ScheduleRule
- [x] Проверка конфликтов (optimistic locking)
- [x] Множественные услуги (BookingItem)
- [x] Автоматические переходы статусов
- [x] Reschedule с историей
- [x] No-Show tracking
- [x] Provider Services (CRUD)

### API Endpoints

| Method | Endpoint | Описание |
|--------|----------|----------|
| POST | `/api/v1/bookings` | Создать бронь |
| GET | `/api/v1/bookings` | Список бронирований |
| GET | `/api/v1/bookings/{id}` | Детали брони |
| PATCH | `/api/v1/bookings/{id}/confirm` | Подтвердить (provider) |
| PATCH | `/api/v1/bookings/{id}/cancel` | Отменить |
| PATCH | `/api/v1/bookings/{id}/complete` | Завершить |
| PATCH | `/api/v1/bookings/{id}/reschedule` | Перенести |
| PATCH | `/api/v1/bookings/{id}/no-show` | No-Show |
| GET | `/api/v1/bookings/slots` | Свободные слоты |
| GET | `/api/v1/schedule/rules` | Правила расписания |
| POST | `/api/v1/schedule/rules` | Создать правило |
| PATCH | `/api/v1/schedule/rules/{id}` | Обновить правило |
| DELETE | `/api/v1/schedule/rules/{id}` | Удалить правило |
| GET | `/api/v1/provider-services` | Услуги мастера |
| POST | `/api/v1/provider-services` | Добавить услугу |
| PATCH | `/api/v1/provider-services/{id}` | Обновить услугу |
| DELETE | `/api/v1/provider-services/{id}` | Удалить услугу |

### User Stories - MVP Phase

#### Базовые сценарии

* **US-3.1**: ✅ Я как Клиент хочу видеть свободные слоты у выбранного мастера на конкретную дату.
* **US-3.2**: ✅ Я как Клиент хочу забронировать слот, выбрав услугу и время.
* **US-3.3**: 🟡 Я как Мастер хочу получать уведомление о новой заявке (Push). **(E6 pending)**
* **US-3.4**: ✅ Я как Мастер хочу подтвердить или отклонить заявку (с указанием причины).
* **US-3.5**: ✅ Я как Клиент хочу отменить запись (за 2 часа до начала), если планы изменились.
* **US-3.6**: ✅ Я как Мастер хочу отметить, что клиент не пришел (No-Show).

#### Workflow & Lifecycle

* **US-3.7**: ✅ Я как Система хочу автоматически отменять PENDING заявки через 24 часа без ответа мастера (EXPIRED).
* **US-3.8**: ✅ Я как Система хочу автоматически переводить статус в IN_PROGRESS при наступлении start_time.
* **US-3.9**: ✅ Я как Система хочу автоматически завершать визит (COMPLETED) после end_time.
* **US-3.10**: ✅ Я как Мастер хочу вручную завершить запись до наступления end_time.
* **US-3.42**: ✅ Я как Мастер хочу включить автоматическое подтверждение всех заявок (auto-accept mode).

#### Rescheduling

* **US-3.11**: ✅ Я как Клиент хочу перенести запись на свободный слот мастера (минимум за 2 часа до начала).
* **US-3.43**: ✅ Я как Мастер хочу запретить клиентам переносить записи через настройки профиля.

#### Множественные Услуги

* **US-3.14**: ✅ Я как Клиент хочу забронировать несколько услуг за один визит (маникюр + педикюр).
* **US-3.15**: ✅ Я как Система хочу автоматически рассчитывать end_time = start_time + sum(services.duration).
* **US-3.16**: ✅ Я как Система хочу автоматически рассчитывать total_price = sum(services.price).
* **US-3.17**: ✅ Я как Система хочу блокировать несочетаемые услуги (is_combinable=false).

#### No-Show & Penalty Policy

* **US-3.18**: ✅ Я как Система хочу автоматически считать No-Show rate для каждого клиента.
* **US-3.19**: ❌ (Отменено) ~~Я как Мастер хочу требовать депозит от клиентов с высоким No-Show rate.~~
* **US-3.20**: 🟡 (Phase 2) Я как Мастер хочу заблокировать клиента после 3+ No-Show.

#### Уведомления & Напоминания

* **US-3.21**: 🟡 Я как Клиент хочу получать напоминание за 24 часа до визита. **(E6 pending)**
* **US-3.22**: 🟡 Я как Клиент хочу получать напоминание за 2 часа до визита. **(E6 pending)**
* **US-3.23**: 🟡 Я как Мастер хочу получать напоминание за 1 час до начала записи. **(E6 pending)**
* **US-3.24**: 🟡 Я как Клиент/Мастер хочу получать уведомления при изменении статуса брони. **(E6 pending)**

#### График Работы Мастера

* **US-3.25**: ✅ Я как Мастер хочу настроить график работы по дням недели (Mon-Fri 9-18).
* **US-3.26**: ✅ Я как Мастер хочу заблокировать время для отпуска или больничного (specific_date rules).
* **US-3.27**: ✅ Я как Мастер хочу настроить buffer time между записями (15 мин).
* **US-3.28**: ✅ Я как Мастер хочу настроить обеденный перерыв (lunch break 13:00-14:00).

#### Конфликты & Параллельность

* **US-3.29**: ✅ Я как Система хочу предотвратить double-booking через optimistic locking.
* **US-3.30**: 🟡 (Phase 2) Я как Клиент хочу встать в очередь, если слот занят (waitlist).

#### Доступность Слотов

* **US-3.34**: ✅ Я как Мастер хочу ограничить бронирование 30 днями вперед (booking_horizon).
* **US-3.35**: ✅ Я как Мастер хочу запретить бронирование менее чем за 2 часа (min_booking_notice).
* **US-3.36**: 🟡 (Phase 2) Я как Мастер хочу ограничить количество записей в день (max_bookings_per_day).

### User Stories - Phase 2

#### История & Аналитика

* **US-3.31**: 🟡 Я как Клиент хочу видеть историю своих записей.
* **US-3.32**: 🟡 Я как Мастер хочу видеть историю записей конкретного клиента.
* **US-3.33**: 🟡 Я как Мастер хочу видеть статистику по записям (загрузка, доход, No-Show rate).

#### Модификация Бронирования

* **US-3.37**: 🟡 Я как Клиент хочу добавить услугу к существующей записи (если слот позволяет).
* **US-3.38**: 🟡 Я как Клиент хочу удалить услугу из записи (с пересчетом времени).
* **US-3.39**: 🟡 Я как Мастер хочу изменить итоговую цену после оказания услуги (доп. услуги).

#### Fraud Prevention

* **US-3.40**: 🟡 Я как Система хочу ограничить клиента максимум 5 активными записями.
* **US-3.41**: 🟡 Я как Система хочу ограничить количество запросов на бронирование (rate limiting).

**Итого:** 43 User Stories  
**MVP Phase:** 27 US (✅ 21 complete, 🟡 6 pending E6 Notifications)  
**Phase 2:** 16 US

## E4: Service Management (Управление услугами - Мастер)

### Статус: 🟢 80% - PARTIALLY COMPLETE

**Frontend Ready:** Provider Services CRUD API готов.

### Описание

Инструменты для мастера по настройке своего бизнеса.

### Реализовано

- [x] Provider Services API (CRUD)
- [x] Связь услуг с мастером (provider_services table)
- [x] Кастомные цены для мастера
- [x] График работы (E3: ScheduleRule)

### Не реализовано

- [ ] Портфолио (фото работ)
- [ ] Временное скрытие услуги

### API Endpoints

| Method | Endpoint | Описание |
|--------|----------|----------|
| GET | `/api/v1/provider-services` | Услуги мастера |
| POST | `/api/v1/provider-services` | Добавить услугу |
| PATCH | `/api/v1/provider-services/{id}` | Обновить услугу |
| DELETE | `/api/v1/provider-services/{id}` | Удалить услугу |

### User Stories

* **US-4.1**: ✅ Я как Мастер хочу создать список своих услуг с ценами и длительностью.
* **US-4.2**: ✅ Я как Мастер хочу настроить свой график работы (дни недели, часы работы, перерывы). **(E3: ScheduleRule)**
* **US-4.3**: ❌ Я как Мастер хочу добавить фото в портфолио.
* **US-4.4**: ❌ Я как Мастер хочу временно скрыть услугу, если не могу ее оказывать.

---

## E5: Reputation System (Рейтинг и Отзывы)

### Статус: 🔴 0% → Phase 2 (подтверждено 27.02.2026)

### Описание

Система доверия с защитой от накруток. Отзывы только по факту оказания услуги, взвешенный рейтинг с Trust Score.

**Почему Phase 2:**

- Требует "critical mass" bookings (20-30+ минимум)
- Для MVP достаточно manual quality control
- **Подтверждено PM + Tech Lead Analysis (27.02.2026)**

### Не реализовано

- [ ] Модели: Review, ReputationEvent
- [ ] Trust Score система
- [ ] Взвешенный рейтинг
- [ ] Anti-fraud детекция

### User Stories

* **US-5.1**: ❌ Я как Клиент хочу видеть рейтинг мастера (звезды) и читать отзывы других, чтобы принять решение о
  записи.
* **US-5.2**: ❌ Я как Клиент могу оставить отзыв только после того, как статус записи перешел в `COMPLETED`.
* **US-5.3**: ❌ Я как Клиент хочу иметь возможность отредактировать свой отзыв в течение 24 часов.
* **US-5.4**: ❌ Я как Клиент хочу фильтровать отзывы по оценке, языку и наличию текста.
* **US-5.5**: ❌ Я как Мастер хочу получать push-уведомление о каждом новом отзыве.
* **US-5.6**: ❌ Я как Администратор хочу видеть queue подозрительных отзывов для модерации.
* **US-5.7**: ❌ (Future) Я как Мастер хочу отвечать на отзывы клиентов.

**Детали реализации:** См. `06_REPUTATION_STRATEGY.md`

---

## E7: Internationalization (Интернационализация)

### Статус: ✅ 100% - PRODUCTION READY

### Описание

Поддержка трех языков (Русский, Иврит, Английский) с учетом RTL для Иврита.

### Реализовано

- [x] Middleware для автоопределения языка
- [x] Gettext система (RU/HE/EN)
- [x] Locale файлы (.po)
- [x] Локализованные исключения
- [x] Поле language_code в User
- [x] Тестовые API endpoints

### User Stories

* **US-7.1**: ✅ Я как Пользователь хочу выбрать язык приложения при первом запуске (RU/HE/EN).
* **US-7.2**: ✅ Я как Пользователь хочу изменить язык в настройках в любое время.
* **US-7.3**: ✅ Я как Пользователь хочу видеть весь интерфейс на выбранном языке (кнопки, меню, заголовки).
* **US-7.4**: ✅ Я как Пользователь хочу видеть категории услуг и названия услуг на моем языке.
* **US-7.5**: ✅ Я как Пользователь (Иврит) хочу видеть зеркальный интерфейс (RTL) - текст справа налево.
* **US-7.6**: ✅ Я как Пользователь хочу видеть даты, время и валюты в формате моего региона.
    * RU: 14.09.2025, 1 500 ₽
    * HE: 14.09.2025, 1,500 ₪
    * EN: Sep 14, 2025, $1,500.00
* **US-7.7**: ✅ Я как Клиент хочу получать push-уведомления на моем языке.
* **US-7.8**: ✅ Я как Мастер хочу заполнять профили услуг на нескольких языках (опционально).

**Детали реализации:** См. `05_I18N_STRATEGY.md`

---

## E6: Notifications (Уведомления)

### Статус: 🔴 0% - NOT STARTED

### Описание

Транзакционные уведомления для удержания пользователей.

### Не реализовано

- [ ] Email сервис (SendGrid не настроен)
- [ ] Push-уведомления
- [ ] Шаблоны уведомлений
- [ ] Очередь отправки

### User Stories

* **US-6.1**: ❌ Пуш-уведомление клиенту: "Ваша запись подтверждена".
* **US-6.2**: ❌ Пуш-уведомление мастеру: "Новая запись на [Дата/Время]".
* **US-6.3**: ❌ Напоминание клиенту за 2 часа до визита.

---

## 🎯 Frontend Ready Summary

### Полностью готовы к разработке (100%)

| Эпик | Описание | API Endpoints | Use Cases |
|------|----------|---------------|-----------|
| **E1** | Auth & Identity | 8+ | [Auth Use Cases](./02a_AUTH_USE_CASES.md) |
| **E2** | Catalog & Geo-Search | 9 | [Search Use Cases](./02_USE_CASES.md) |
| **E3** | Booking Engine | 21+ | [Search Use Cases](./02_USE_CASES.md) |
| **E7** | i18n | Middleware | [I18n Strategy](./05_I18N_STRATEGY.md) |

### Частично готовы (80%)

| Эпик | Описание | Что готово | Что нужно |
|------|----------|------------|-----------|
| **E4** | Service Management | Provider Services CRUD | Portfolio UI |

### Phase 2 (после MVP)

| Эпик | Описание | Статус |
|------|----------|--------|
| **E5** | Reputation System | Blocked: требует 20-30+ bookings |
| **E6** | Notifications | Blocked: требует SendGrid/Firebase |

---

## 📱 Рекомендуемый порядок разработки Frontend

### Week 1: Auth + Infrastructure
1. Flutter project setup
2. API client generation (из OpenAPI)
3. Auth flow (Login, Register, Password Reset)
4. Role Switcher component

### Week 2: Catalog + Search
1. Home screen с картой
2. Search filters (категория, цена, расстояние)
3. Provider profile screen
4. Favorites functionality

### Week 3: Booking
1. Service selection (корзина)
2. Calendar + Time slots
3. Booking confirmation
4. Booking details screen

### Week 4: Provider Dashboard
1. Provider schedule management
2. Booking management (confirm/cancel/complete)
3. Provider services CRUD
4. Profile editing

---

## 📚 Связанные документы

- **API Design:** [architecture/backend/01_API_DESIGN.md](../architecture/backend/01_API_DESIGN.md)
- **Auth Use Cases:** [frontend/AUTH_USE_CASES.md](../frontend/AUTH_USE_CASES.md)
- **Booking Use Cases:** [frontend/BOOKING_USE_CASES.md](../frontend/BOOKING_USE_CASES.md)
- **Profile Use Cases:** [frontend/PROFILE_USE_CASES.md](../frontend/PROFILE_USE_CASES.md)
- **Search Use Cases:** [business/02_USE_CASES.md](./02_USE_CASES.md)
- **Implementation Status:** [business/00_IMPLEMENTATION_STATUS.md](./00_IMPLEMENTATION_STATUS.md)
- **Multi-Role System:** [architecture/backend/09_MULTI_ROLE_SYSTEM.md](../architecture/backend/09_MULTI_ROLE_SYSTEM.md)
- **Design System:** [frontend/DESIGN_SYSTEM.md](../frontend/DESIGN_SYSTEM.md)
- **UI States:** [frontend/UI_STATES.md](../frontend/UI_STATES.md)
- **Frontend Docs:** [frontend/README.md](../frontend/README.md)

---

**Last Updated:** 2026-03-12  
**Status:** Ready for Frontend Development
