# E3: Booking Engine - Детальный План Реализации

**Дата создания:** 05.03.2026  
**Последнее обновление:** 05.03.2026  
**Приоритет:** P0 (MVP Blocker)  
**Оценка:** 21-28 дней  
**Статус:** 0% → Планирование

---

## 📋 Содержание

1. [Цели и Критерии Успеха](#цели-и-критерии-успеха)
2. [Обзор Блоков](#обзор-блоков)
3. [Блок 1: Workflow & Lifecycle](#блок-1-workflow--lifecycle)
4. [Блок 2: Rescheduling](#блок-2-rescheduling)
5. [Блок 3: Множественные Услуги](#блок-3-множественные-услуги)
6. [Блок 4: No-Show & Penalty Policy](#блок-4-no-show--penalty-policy)
7. [Блок 5: Уведомления & Напоминания](#блок-5-уведомления--напоминания)
8. [Блок 6: График Работы Мастера](#блок-6-график-работы-мастера)
9. [Блок 7: Конфликты & Параллельность](#блок-7-конфликты--параллельность)
10. [Блок 8: Доступность Слотов](#блок-8-доступность-слотов)
11. [Блок 9: История & Аналитика](#блок-9-история--аналитика)
12. [Архитектура](#архитектура)
13. [Database Schema](#database-schema)
14. [API Endpoints](#api-endpoints)
15. [План Реализации](#план-реализации)
16. [Definition of Done](#definition-of-done)

---

## 🎯 Цели и Критерии Успеха

### Бизнес-цели:

- ✅ Клиенты могут бронировать услуги у мастеров
- ✅ Мастера могут подтверждать/отклонять заявки
- ✅ Rescheduling функционал (критичен для MVP)
- ✅ Автоматическое управление слотами времени
- ✅ Защита от double-booking
- ✅ Поддержка множественных услуг в одной записи

### Критерии приемки (Definition of Done):

- [ ] 100% User Stories (US-3.1 to US-3.43) реализованы
- [ ] Test Coverage ≥ 80%
- [ ] API документация (OpenAPI) обновлена
- [ ] Performance: API < 500ms (P95)
- [ ] Security: SEC-001 исправлен (User ID from JWT)
- [ ] Database migrations протестированы
- [ ] Integration tests для критичных сценариев

### Success Metrics:

| Метрика                   | Цель    | Измерение                       |
|---------------------------|---------|---------------------------------|
| Test Coverage             | ≥ 80%   | pytest --cov                    |
| API Response Time (P95)   | < 500ms | Structured logging + monitoring |
| Double-booking Prevention | 100%    | Integration tests               |
| Slot Generation Time      | < 200ms | Performance tests               |
| User Stories Completed    | 100%    | Acceptance criteria             |

---

## 📊 Обзор Блоков

### MVP Phase (Критично для запуска)

| Блок                        | Приоритет   | Оценка | User Stories              |
|-----------------------------|-------------|--------|---------------------------|
| **1. Workflow & Lifecycle** | 🔴 Critical | 2 дня  | US-3.7 - US-3.10, US-3.42 |
| **2. Rescheduling**         | 🔴 Critical | 1 день | US-3.11, US-3.43          |
| **3. Множественные Услуги** | 🔴 Critical | 2 дня  | US-3.14 - US-3.17         |
| **6. График Работы**        | 🔴 Critical | 3 дня  | US-3.25 - US-3.28         |
| **7. Конфликты**            | 🔴 Critical | 2 дня  | US-3.29                   |

**Итого MVP:** 10 дней

### Phase 2 (Важно для retention)

| Блок                      | Приоритет | Оценка  | User Stories               |
|---------------------------|-----------|---------|----------------------------|
| **4. No-Show Policy**     | 🟡 High   | 1.5 дня | US-3.18 - US-3.20 (основа) |
| **5. Уведомления**        | 🟡 High   | 2 дня   | US-3.21 - US-3.24          |
| **8. Доступность Слотов** | 🟡 High   | 1 день  | US-3.34 - US-3.36          |

**Итого Phase 2:** 4.5 дня

### Phase 3 (Nice to Have)

| Блок                       | Приоритет | Оценка | User Stories      |
|----------------------------|-----------|--------|-------------------|
| **7. Waitlist**            | 🟢 Medium | 2 дня  | US-3.30           |
| **9. История & Аналитика** | 🟢 Low    | 2 дня  | US-3.31 - US-3.33 |
| **Модификация Брони**      | 🟢 Low    | 2 дня  | US-3.37 - US-3.39 |
| **Fraud Prevention**       | 🟢 Low    | 1 день | US-3.40 - US-3.41 |

**Итого Phase 3:** 7 дней

**Общая оценка:** 21.5 дня (≈ 3-4 недели)

---

## 🔴 Блок 1: Workflow & Lifecycle (Critical)

### Описание

Управление жизненным циклом бронирования и автоматические переходы между статусами.

### User Stories

#### US-3.7: Автоматический таймаут заявки

**Как** Мастер  
**Хочу** чтобы заявки автоматически отменялись через 24 часа без ответа  
**Чтобы** не висели "висячие" брони

**Acceptance Criteria:**

- [ ] PENDING заявки автоматически переходят в EXPIRED через 24 часа
- [ ] Клиент получает уведомление: "Мастер не ответил на заявку"
- [ ] Слот освобождается в расписании
- [ ] Background task запускается каждые 15 минут

**Technical Implementation:**

```python
# background_tasks/booking_timeout.py
async def expire_pending_bookings():
    """Автоматически отменяет PENDING заявки старше 24 часов."""
    threshold = datetime.now(timezone.utc) - timedelta(hours=24)

    expired_bookings = await db.execute(
        select(Booking)
        .where(Booking.status == BookingStatus.PENDING)
        .where(Booking.created_at < threshold)
    )

    for booking in expired_bookings.scalars().all():
        booking.status = BookingStatus.EXPIRED
        booking.expired_at = datetime.now(timezone.utc)

        await notification_service.send(
            user_id=booking.client_id,
            template="booking_expired",
            context={"provider_name": booking.provider.display_name}
        )

    await db.commit()
```

**Database:**

```sql
ALTER TABLE app.bookings
    ADD COLUMN expired_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX ix_bookings_expired_check ON app.bookings (created_at)
    WHERE status = 'PENDING';
```

---

#### US-3.8: Автоматический переход CONFIRMED → IN_PROGRESS

**Как** Система  
**Хочу** автоматически переводить статус в IN_PROGRESS при наступлении start_time  
**Чтобы** отражать актуальное состояние визита

**Acceptance Criteria:**

- [ ] Trigger: `NOW() >= booking.start_time`
- [ ] Background task каждые 5 минут
- [ ] Логирование изменения статуса
- [ ] Уведомление мастеру (опционально)

**Technical Implementation:**

```python
# background_tasks/booking_status_updater.py
async def update_booking_statuses():
    """Обновляет статусы бронирований на основе времени."""
    now = datetime.now(timezone.utc)

    # CONFIRMED → IN_PROGRESS
    confirmed_bookings = await db.execute(
        select(Booking)
        .where(Booking.status == BookingStatus.CONFIRMED)
        .where(Booking.start_time <= now)
        .where(Booking.end_time > now)
    )

    for booking in confirmed_bookings.scalars().all():
        booking.status = BookingStatus.IN_PROGRESS
        booking.updated_at = now

    await db.commit()
```

---

#### US-3.9: Автоматический переход IN_PROGRESS → COMPLETED

**Как** Система  
**Хочу** автоматически завершать визит после end_time  
**Чтобы** клиент мог оставить отзыв

**Acceptance Criteria:**

- [ ] Trigger: `NOW() >= booking.end_time`
- [ ] Background task каждые 5 минут
- [ ] Уведомление клиенту: "Оставьте отзыв"
- [ ] Отзыв становится доступен

**Technical Implementation:**

```python
# В том же background_tasks/booking_status_updater.py
async def update_booking_statuses():
    now = datetime.now(timezone.utc)

    # ... CONFIRMED → IN_PROGRESS ...

    # IN_PROGRESS → COMPLETED
    in_progress_bookings = await db.execute(
        select(Booking)
        .where(Booking.status == BookingStatus.IN_PROGRESS)
        .where(Booking.end_time <= now)
    )

    for booking in in_progress_bookings.scalars().all():
        booking.status = BookingStatus.COMPLETED
        booking.updated_at = now

        # Trigger notification for review
        await notification_service.send(
            user_id=booking.client_id,
            template="booking_completed",
            context={
                "provider_name": booking.provider.display_name,
                "booking_id": str(booking.id)
            }
        )

    await db.commit()
```

---

#### US-3.10: Мастер завершает запись досрочно

**Как** Мастер  
**Хочу** вручную завершить запись до наступления end_time  
**Чтобы** освободить слот раньше

**Acceptance Criteria:**

- [ ] Только мастер может завершить досрочно
- [ ] Статус: IN_PROGRESS → COMPLETED
- [ ] Уведомление клиенту: "Визит завершен"
- [ ] API endpoint: `PATCH /bookings/{id}/complete`

**API Endpoint:**

```yaml
PATCH /api/v1/bookings/{id}/complete:
  summary: Завершить запись досрочно (Provider only)
  security:
    - BearerAuth: [ ]
  responses:
    200:
      description: Запись завершена
    403:
      description: Только мастер может завершить запись
    422:
      description: Запись не в статусе IN_PROGRESS
```

**Use Case:**

```python
class CompleteBookingUseCase:
    async def execute(self, booking_id: UUID, provider_id: UUID) -> Booking:
        booking = await self._uow.bookings.get_by_id(booking_id)

        # Проверка прав
        if booking.provider_id != provider_id:
            raise PermissionDeniedException()

        # Проверка статуса
        if booking.status != BookingStatus.IN_PROGRESS:
            raise InvalidStatusException()

        # Обновление
        booking.status = BookingStatus.COMPLETED
        booking.updated_at = datetime.now(timezone.utc)

        await self._uow.commit()

        # Уведомление клиенту
        await self._notification_service.send(
            user_id=booking.client_id,
            template="booking_completed_early",
            context={"provider_name": booking.provider.display_name}
        )

        return booking
```

---

#### US-3.42: Auto-Accept режим

**Как** Мастер  
**Хочу** включить автоматическое подтверждение всех заявок  
**Чтобы** не тратить время на ручное подтверждение

**Acceptance Criteria:**

- [ ] Флаг `providers.auto_accept_bookings` (boolean)
- [ ] Если `true`: PENDING → CONFIRMED автоматически
- [ ] Проверка доступности слота перед auto-accept
- [ ] Уведомление клиенту: "Запись подтверждена"
- [ ] API endpoint: `PATCH /providers/settings/auto-accept`

**Database:**

```sql
ALTER TABLE app.providers
    ADD COLUMN auto_accept_bookings BOOLEAN NOT NULL DEFAULT FALSE;
```

**Business Logic:**

```python
# В CreateBookingUseCase
async def execute(self, ...):
    # ... создание брони ...

    # Auto-accept если включено
    if booking.provider.auto_accept_bookings:
        booking.status = BookingStatus.CONFIRMED

        await self._notification_service.send(
            user_id=booking.client_id,
            template="booking_auto_confirmed",
            context={"provider_name": booking.provider.display_name}
        )
    else:
        booking.status = BookingStatus.PENDING

        await self._notification_service.send(
            user_id=booking.provider_id,
            template="new_booking_request",
            context={"client_name": booking.client.display_name}
        )

    await self._uow.commit()
```

---

### API Endpoints Summary (Block 1)

```yaml
# Background Tasks (internal)
POST /internal/cron/expire-pending-bookings:
  summary: Cron job для отмены PENDING заявок
  security: [ InternalAPIKey ]

POST /internal/cron/update-booking-statuses:
  summary: Cron job для обновления статусов
  security: [ InternalAPIKey ]

# Provider Actions
PATCH /api/v1/bookings/{id}/complete:
  summary: Завершить запись досрочно (Provider only)
  security: [ BearerAuth ]

PATCH /api/v1/providers/settings/auto-accept:
  summary: Включить/выключить auto-accept
  security: [ BearerAuth ]
  requestBody:
    content:
      application/json:
        schema:
          type: object
          properties:
            auto_accept_bookings:
              type: boolean
```

---

## 🔄 Блок 2: Rescheduling (Critical)

### Описание

Функционал переноса записей клиентом на свободные слоты мастера.

### Утвержденные требования

1. ✅ **Минимум 2 часа до начала** (как отмена)
2. ❌ **Без ограничений** на количество переносов
3. ✅ **Уведомления** мастеру и клиенту
4. ✅ **История переносов** сохраняется
5. ✅ **Мастер может запретить** переносы (флаг `allow_rescheduling`)

### User Stories

#### US-3.11: Клиент переносит запись

**Как** Клиент  
**Хочу** перенести запись на свободный слот мастера  
**Чтобы** подстроиться под изменившиеся планы

**Acceptance Criteria:**

- [ ] Клиент видит список свободных слотов мастера
- [ ] Может выбрать новый слот
- [ ] Запись автоматически переносится
- [ ] Уведомление мастеру: "Клиент перенес запись"
- [ ] Уведомление клиенту: "Ваша запись перенесена"
- [ ] История переносов сохраняется
- [ ] Если мастер запретил переносы → ошибка

**Business Rules:**

- ✅ Только CONFIRMED записи
- ✅ Минимум 2 часа до начала
- ✅ Новый слот должен быть свободен
- ✅ Мастер может запретить через флаг `allow_rescheduling`
- ✅ Новое время должно быть в будущем
- ✅ Максимум 30 дней вперед

**Database Schema:**

```sql
-- Добавить в bookings
ALTER TABLE app.bookings
    ADD COLUMN reschedule_count INTEGER NOT NULL DEFAULT 0;

-- Добавить в providers
ALTER TABLE app.providers
    ADD COLUMN allow_rescheduling BOOLEAN NOT NULL DEFAULT TRUE;

-- Создать таблицу истории переносов
CREATE TABLE app.booking_reschedule_history
(
    id             UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    booking_id     UUID                     NOT NULL REFERENCES app.bookings (id) ON DELETE CASCADE,
    old_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    old_end_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    new_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    new_end_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    rescheduled_by UUID                     NOT NULL REFERENCES app.users (id),
    reason         TEXT,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX ix_reschedule_history_booking ON app.booking_reschedule_history (booking_id);
CREATE INDEX ix_reschedule_history_created ON app.booking_reschedule_history (created_at DESC);
CREATE INDEX ix_reschedule_history_by_user ON app.booking_reschedule_history (rescheduled_by);
```

**API Endpoint:**

```yaml
PATCH /api/v1/bookings/{booking_id}/reschedule:
  summary: Перенести запись на новое время
  description: |
    Клиент переносит запись на свободный слот мастера.

    **Security:**
    - ✅ Bearer token required
    - ✅ Client only (role check)
    - ✅ User ID from JWT (SEC-001)

    **Business Rules:**
    - Booking must be CONFIRMED
    - Min 2 hours before start
    - New slot must be available
    - Provider must allow rescheduling

  security:
    - BearerAuth: [ ]
  requestBody:
    required: true
    content:
      application/json:
        schema:
          type: object
          required: [ new_start_time ]
          properties:
            new_start_time:
              type: string
              format: date-time
              example: "2025-09-16T14:00:00Z"
            reason:
              type: string
              maxLength: 500
              example: "Опаздываю с работы"

  responses:
    200:
      description: Запись успешно перенесена
    403:
      description: Недостаточно прав или мастер запретил переносы
    409:
      description: Слот занят или слишком поздно
```

**Use Case:**

```python
class RescheduleBookingUseCase:
    async def execute(
            self,
            booking_id: UUID,
            new_start_time: datetime,
            client_id: UUID,
            reason: Optional[str] = None
    ) -> Booking:
        # 1. Получить бронь
        booking = await self._uow.bookings.get_by_id_with_provider(booking_id)

        # 2. Проверка прав (User ID from JWT)
        if booking.client_id != client_id:
            raise PermissionDeniedException()

        # 3. Проверка, что мастер разрешает переносы
        if not booking.provider.allow_rescheduling:
            raise ReschedulingDisabledException()

        # 4. Проверка статуса
        if booking.status != BookingStatus.CONFIRMED:
            raise CannotRescheduleException()

        # 5. Проверка времени (минимум 2 часа)
        time_until_start = booking.start_time - datetime.now(timezone.utc)
        if time_until_start < timedelta(hours=2):
            raise CannotRescheduleException("Перенос возможен не позднее чем за 2 часа")

        # 6. Расчет нового end_time
        duration_minutes = (booking.end_time - booking.start_time).total_seconds() / 60
        new_end_time = new_start_time + timedelta(minutes=duration_minutes)

        # 7. Проверка доступности нового слота
        is_available = await self._slot_availability_service.check(
            provider_id=booking.provider_id,
            start_time=new_start_time,
            end_time=new_end_time,
            exclude_booking_id=booking_id
        )

        if not is_available:
            alternatives = await self._slot_generator.get_alternatives(...)
            raise SlotNotAvailableException(alternatives=alternatives)

        # 8. Сохранить историю
        await self._uow.reschedule_history.create(
            booking_id=booking_id,
            old_start_time=booking.start_time,
            old_end_time=booking.end_time,
            new_start_time=new_start_time,
            new_end_time=new_end_time,
            rescheduled_by=client_id,
            reason=reason
        )

        # 9. Обновить бронь
        booking.start_time = new_start_time
        booking.end_time = new_end_time
        booking.reschedule_count += 1

        await self._uow.commit()

        # 10. Уведомления
        await self._send_notifications(booking, old_start_time, new_start_time)

        return booking
```

---

#### US-3.43: Мастер запрещает переносы

**Как** Мастер  
**Хочу** запретить клиентам переносить записи  
**Чтобы** избежать хаоса в расписании

**Acceptance Criteria:**

- [ ] Флаг в настройках профиля: "Разрешить перенос записей"
- [ ] По умолчанию: `true` (разрешено)
- [ ] Если `false` → клиенты видят ошибку "Мастер не разрешает перенос записей"

**API Endpoint:**

```yaml
PATCH /api/v1/providers/settings/rescheduling:
  summary: Включить/выключить переносы записей
  security:
    - BearerAuth: [ ]
  requestBody:
    content:
      application/json:
        schema:
          type: object
          required: [ allow_rescheduling ]
          properties:
            allow_rescheduling:
              type: boolean
              example: false
  responses:
    200:
      description: Настройка обновлена
```

---

### API Endpoints Summary (Block 2)

```yaml
# Client Actions
PATCH /api/v1/bookings/{id}/reschedule:
  summary: Перенести запись (Client only)
  security: [ BearerAuth ]

# Provider Actions
PATCH /api/v1/providers/settings/rescheduling:
  summary: Включить/выключить переносы
  security: [ BearerAuth ]

# History
GET /api/v1/bookings/{id}/reschedule-history:
  summary: История переносов записи (Provider only)
  security: [ BearerAuth ]
```

---

## 📦 Блок 3: Множественные Услуги (Critical)

### Описание

Поддержка бронирования нескольких услуг в одном визите.

### User Stories

#### US-3.14: Бронирование нескольких услуг

**Как** Клиент  
**Хочу** забронировать несколько услуг за один визит  
**Чтобы** сделать маникюр и педикюр одновременно

**Acceptance Criteria:**

- [ ] Автоматический расчет общей длительности: `sum(services.duration)`
- [ ] Автоматический расчет общей стоимости: `sum(services.price)`
- [ ] Проверка `is_combinable` для каждой услуги
- [ ] Если услуга `is_combinable=false` → только одна услуга

**Database Schema:**

```sql
-- Таблица booking_items (услуги в брони)
CREATE TABLE app.booking_items
(
    id               UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    booking_id       UUID    NOT NULL REFERENCES app.bookings (id) ON DELETE CASCADE,
    service_id       UUID    NOT NULL REFERENCES app.services (id) ON DELETE CASCADE,
    price            INTEGER,
    duration_minutes INTEGER NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX ix_booking_items_booking ON app.booking_items (booking_id);
CREATE INDEX ix_booking_items_service ON app.booking_items (service_id);
```

**API Request:**

```json
{
  "provider_id": "uuid-123",
  "start_time": "2025-09-15T14:00:00Z",
  "service_ids": [
    "service-1",
    "service-2"
  ],
  "comment": "Хочу темный лак"
}
```

**API Response:**

```json
{
  "id": "booking-uuid",
  "status": "PENDING",
  "start_time": "2025-09-15T14:00:00Z",
  "end_time": "2025-09-15T15:30:00Z",
  "total_duration_minutes": 90,
  "total_price": 3500,
  "items": [
    {
      "service_id": "service-1",
      "title": "Маникюр",
      "price": 1500,
      "duration_minutes": 40
    },
    {
      "service_id": "service-2",
      "title": "Педикюр",
      "price": 2000,
      "duration_minutes": 50
    }
  ]
}
```

---

#### US-3.15: Автоматический расчет duration

**Как** Система  
**Хочу** автоматически рассчитывать end_time на основе выбранных услуг  
**Чтобы** избежать ошибок ручного ввода

**Business Rules:**

- `end_time = start_time + sum(service.duration_minutes)`
- Buffer time между услугами: **НЕТ для MVP**

**Implementation:**

```python
# В CreateBookingUseCase
services = await self._uow.services.get_by_ids(service_ids)

# Расчет duration
total_duration = sum(s.duration_minutes for s in services)
end_time = start_time + timedelta(minutes=total_duration)
```

---

#### US-3.16: Автоматический расчет стоимости

**Как** Система  
**Хочу** автоматически рассчитывать total_price  
**Чтобы** клиент видел итоговую сумму

**Business Rules:**

- `total_price = sum(service.base_price)`
- Учитывать `is_price_variable` (показывать "от X ₽")

**Implementation:**

```python
# Расчет price
total_price = sum(s.base_price for s in services if s.base_price)

# Проверка variable price
has_variable_price = any(s.is_price_variable for s in services)
if has_variable_price:
    # В UI показывать "от {total_price} ₽"
    pass
```

---

#### US-3.17: Валидация сочетаемости услуг

**Как** Система  
**Хочу** блокировать несочетаемые услуги  
**Чтобы** предотвратить невозможные комбинации

**Business Rules:**

- Если `service.is_combinable = false` → только одна услуга в заказе
- API возвращает ошибку `INCOMBINABLE_SERVICES`

**Implementation:**

```python
# Валидация сочетаемости
incombinable_services = [s for s in services if not s.is_combinable]

if len(incombinable_services) > 0 and len(services) > 1:
    raise IncombinableServicesException(
        incombinable_service=incombinable_services[0].title,
        message=f"Услуга '{incombinable_services[0].title}' нельзя комбинировать с другими"
    )
```

---

### Use Case (Block 3)

```python
class CreateBookingUseCase:
    async def execute(
            self,
            provider_id: UUID,
            start_time: datetime,
            service_ids: List[UUID],
            client_id: UUID,
            comment: Optional[str] = None
    ) -> Booking:
        # 1. Получить услуги
        services = await self._uow.services.get_by_ids(service_ids)

        # 2. Проверить, что все услуги offered by this provider
        provider_services = await self._uow.provider_services.get_by_provider(provider_id)
        offered_service_ids = {ps.service_id for ps in provider_services}

        if not set(service_ids).issubset(offered_service_ids):
            raise ProviderDoesNotOfferServiceException()

        # 3. Валидация сочетаемости
        incombinable_services = [s for s in services if not s.is_combinable]
        if len(incombinable_services) > 0 and len(services) > 1:
            raise IncombinableServicesException()

        # 4. Расчет duration и price
        total_duration = sum(s.duration_minutes for s in services)
        total_price = sum(s.base_price for s in services if s.base_price)
        end_time = start_time + timedelta(minutes=total_duration)

        # 5. Проверка доступности слота
        is_available = await self._slot_service.check_availability(
            provider_id=provider_id,
            start_time=start_time,
            duration=total_duration
        )

        if not is_available:
            raise SlotNotAvailableException()

        # 6. Создание брони
        booking = Booking(
            client_id=client_id,
            provider_id=provider_id,
            status=BookingStatus.PENDING,
            start_time=start_time,
            end_time=end_time,
            total_price=total_price,
            comment=comment
        )

        await self._uow.bookings.create(booking)

        # 7. Создание BookingItems
        for service in services:
            item = BookingItem(
                booking_id=booking.id,
                service_id=service.id,
                price=service.base_price,
                duration_minutes=service.duration_minutes
            )
            await self._uow.booking_items.create(item)

        await self._uow.commit()

        return booking
```

---

## 🚫 Блок 4: No-Show & Penalty Policy (Техническая основа)

### Описание

Система учета неявок клиентов и подготовка к депозитной системе (Phase 2).

### User Stories

#### US-3.6: Отметка No-Show (уже есть)

**Как** Мастер  
**Хочу** отметить, что клиент не пришел  
**Чтобы** освободить слот

**Acceptance Criteria:**

- [ ] API endpoint: `PATCH /bookings/{id}/no-show`
- [ ] Статус: CONFIRMED/IN_PROGRESS → NO_SHOW
- [ ] Уведомление клиенту: "Вы не явились на запись"

---

#### US-3.18: Расчет No-Show rate

**Как** Система  
**Хочу** автоматически считать процент No-Show для клиента  
**Чтобы** идентифицировать ненадежных клиентов

**Business Rules:**

- `no_show_rate = no_show_count / total_completed_bookings`
- Обновлять после каждого COMPLETED или NO_SHOW
- Хранить в `profiles.no_show_rate`

**Database Schema:**

```sql
-- Добавить в bookings
ALTER TABLE app.bookings
    ADD COLUMN no_show_marked_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN no_show_marked_by UUID REFERENCES app.users (id);

-- Добавить в profiles
ALTER TABLE app.profiles
    ADD COLUMN no_show_count INTEGER       DEFAULT 0,
    ADD COLUMN no_show_rate  DECIMAL(3, 2) DEFAULT 0.00;
```

**Background Task:**

```python
async def update_client_no_show_rate(client_id: UUID):
    """Пересчитать No-Show rate клиента."""
    bookings = await db.execute(
        select(Booking)
        .where(Booking.client_id == client_id)
        .where(Booking.status.in_([BookingStatus.COMPLETED, BookingStatus.NO_SHOW]))
    )

    total = bookings.count()
    no_shows = bookings.filter(Booking.status == BookingStatus.NO_SHOW).count()

    rate = (no_shows / total) if total > 0 else 0.0

    await db.execute(
        update(Profile)
        .where(Profile.user_id == client_id)
        .values(
            no_show_count=no_shows,
            no_show_rate=rate
        )
    )
```

---

#### ~~US-3.19: Deposit для клиентов с высоким No-Show~~ ❌ ОТМЕНЕНО

> **Решение:** Функционал депозита отменён и не планируется к реализации.

---

#### US-3.20: Блокировка клиента (Phase 2)

**Как** Мастер  
**Хочу** заблокировать клиента после 3+ No-Show  
**Чтобы** не работать с ненадежными

**Database Schema:**

```sql
-- Таблица блокировок (для Phase 2)
CREATE TABLE app.provider_client_blocks
(
    id          UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    client_id   UUID NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    reason      TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    UNIQUE (provider_id, client_id)
);

CREATE INDEX ix_provider_client_blocks_provider ON app.provider_client_blocks (provider_id);
CREATE INDEX ix_provider_client_blocks_client ON app.provider_client_blocks (client_id);
```

---

## 🔔 Блок 5: Уведомления & Напоминания

### Описание

Система уведомлений о бронированиях для клиентов и мастеров.

### User Stories

#### US-3.21: Напоминание за 24 часа

**Как** Клиент  
**Хочу** получить напоминание за 24 часа до визита  
**Чтобы** не забыть

**Acceptance Criteria:**

- [ ] Push-уведомление за 24 часа до start_time
- [ ] Background task каждые 15 минут
- [ ] Содержание: мастер, услуги, время, адрес

---

#### US-3.22: Напоминание за 2 часа

**Как** Клиент  
**Хочу** получить напоминание за 2 часа  
**Чтобы** успеть подготовиться

**Acceptance Criteria:**

- [ ] Push-уведомление за 2 часа до start_time
- [ ] Email (опционально)

---

#### US-3.23: Напоминание мастеру за 1 час

**Как** Мастер  
**Хочу** получить напоминание за 1 час до начала записи  
**Чтобы** подготовиться

---

#### US-3.24: Уведомления при изменении статуса

**Как** Клиент/Мастер  
**Хочу** получать уведомления при изменении статуса брони  
**Чтобы** быть в курсе

**Notification Types:**

- `BOOKING_CONFIRMED` → клиенту
- `BOOKING_CANCELLED` → обеим сторонам
- `BOOKING_RESCHEDULED` → мастеру
- `BOOKING_EXPIRED` → клиенту
- `BOOKING_COMPLETED` → клиенту (с призывом оставить отзыв)

**Implementation:**

```python
# background_tasks/send_booking_reminders.py
async def send_booking_reminders():
    """Отправить напоминания о бронированиях."""
    now = datetime.now(timezone.utc)

    # За 24 часа
    bookings_24h = await db.execute(
        select(Booking)
        .where(Booking.status == BookingStatus.CONFIRMED)
        .where(Booking.start_time.between(
            now + timedelta(hours=23),
            now + timedelta(hours=25)
        ))
    )

    for booking in bookings_24h.scalars().all():
        await notification_service.send(
            user_id=booking.client_id,
            template="booking_reminder_24h",
            context={
                "provider_name": booking.provider.display_name,
                "start_time": booking.start_time,
                "services": [item.service.title for item in booking.items]
            }
        )

    # За 2 часа (аналогично)
    # За 1 час мастеру (аналогично)
```

---

## 📅 Блок 6: График Работы Мастера (Critical)

### Описание

Система настройки рабочего расписания мастеров для генерации доступных слотов.

### User Stories

#### US-3.25: Настройка рабочих часов

**Как** Мастер  
**Хочу** настроить график работы по дням недели  
**Чтобы** система генерировала слоты автоматически

**Acceptance Criteria:**

- [ ] Weekly rules: день недели + время начала/окончания
- [ ] API endpoint: `POST /schedule`
- [ ] Автоматическая генерация слотов на основе правил

---

#### US-3.26: Блокировка личного времени

**Как** Мастер  
**Хочу** заблокировать время для отпуска или больничного  
**Чтобы** клиенты не могли записаться

**Acceptance Criteria:**

- [ ] Specific date rules: дата + `is_day_off=true`
- [ ] API endpoint: `POST /schedule` (rule_type: specific_date)

---

#### US-3.27: Buffer time между записями

**Как** Мастер  
**Хочу** настроить перерыв между записями (15 мин)  
**Чтобы** успеть подготовиться

**Acceptance Criteria:**

- [ ] Поле `buffer_after` в schedule_rules
- [ ] Автоматическое добавление buffer к длительности слота

---

#### US-3.28: Перерывы (lunch break)

**Как** Мастер  
**Хочу** настроить обеденный перерыв  
**Чтобы** клиенты не записывались в это время

**Acceptance Criteria:**

- [ ] Поля `break_start_time`, `break_end_time` в schedule_rules
- [ ] Исключение break time при генерации слотов

---

### Database Schema (Block 6)

```sql
CREATE TABLE app.schedule_rules
(
    id               UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    provider_id      UUID        NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    rule_type        VARCHAR(20) NOT NULL, -- 'weekly' или 'specific_date'

    -- Для weekly правил
    day_of_week      INTEGER,              -- 0=Mon, 6=Sun
    start_time       TIME,
    end_time         TIME,
    break_start_time TIME,
    break_end_time   TIME,

    -- Для specific_date правил
    specific_date    DATE,
    is_day_off       BOOLEAN                  DEFAULT FALSE,

    -- Buffer time (в минутах)
    buffer_after     INTEGER                  DEFAULT 0,

    created_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT chk_rule_type CHECK (
        (rule_type = 'weekly' AND day_of_week IS NOT NULL AND start_time IS NOT NULL AND end_time IS NOT NULL) OR
        (rule_type = 'specific_date' AND specific_date IS NOT NULL)
        ),
    CONSTRAINT chk_time_order CHECK (end_time > start_time),
    CONSTRAINT chk_break_time CHECK (
        break_start_time IS NULL OR break_end_time IS NULL OR break_end_time > break_start_time
        )
);

CREATE INDEX ix_schedule_rules_provider ON app.schedule_rules (provider_id);
CREATE INDEX ix_schedule_rules_weekly ON app.schedule_rules (provider_id, day_of_week)
    WHERE rule_type = 'weekly';
CREATE INDEX ix_schedule_rules_specific ON app.schedule_rules (provider_id, specific_date)
    WHERE rule_type = 'specific_date';
```

---

### Slot Generation Algorithm

```python
class SlotGenerator:
    async def generate_available_slots(
            self,
            provider_id: UUID,
            date: date,
            duration_minutes: int
    ) -> List[TimeSlot]:
        """Генерирует доступные слоты на указанную дату."""

        # 1. Получить расписание мастера
        weekly_rule = await self._get_weekly_rule(provider_id, date)
        specific_rule = await self._get_specific_rule(provider_id, date)

        if specific_rule and specific_rule.is_day_off:
            return []  # Выходной

        rule = specific_rule or weekly_rule
        if not rule:
            return []  # Нет расписания

        # 2. Генерация слотов
        slots = []
        current_time = datetime.combine(date, rule.start_time)
        end_time = datetime.combine(date, rule.end_time)

        while current_time + timedelta(minutes=duration_minutes) <= end_time:
            # Пропустить break time
            if rule.break_start_time and rule.break_end_time:
                break_start = datetime.combine(date, rule.break_start_time)
                break_end = datetime.combine(date, rule.break_end_time)

                if current_time >= break_start and current_time < break_end:
                    current_time = break_end
                    continue

            # Проверить доступность
            is_available = await self._check_slot_availability(
                provider_id, current_time, duration_minutes
            )

            if is_available:
                slots.append(TimeSlot(
                    start=current_time,
                    end=current_time + timedelta(minutes=duration_minutes),
                    status=SlotStatus.AVAILABLE
                ))

            # Следующий слот (30-минутные интервалы)
            current_time += timedelta(minutes=30)

        return slots
```

---

## 🔒 Блок 7: Конфликты & Параллельность

### Описание

Защита от double-booking и управление параллельным доступом.

### User Stories

#### US-3.29: Защита от double-booking

**Как** Система  
**Хочу** предотвратить одновременное бронирование одного слота двумя клиентами  
**Чтобы** избежать конфликтов

**Acceptance Criteria:**

- [ ] Optimistic locking на уровне БД
- [ ] Проверка конфликтов перед созданием брони
- [ ] Атомарная операция создания

**Implementation:**

```python
class SqlAlchemyBookingRepository:
    async def create_with_lock(
            self,
            booking: Booking,
            provider_id: UUID,
            start_time: datetime,
            duration_minutes: int
    ) -> Booking:
        """Создает бронь с проверкой конфликтов."""

        # Блокировка строки провайдера (SELECT FOR UPDATE)
        provider = await self._db.execute(
            select(User)
            .where(User.id == provider_id)
            .with_for_update()  # Pessimistic lock
        )

        # Проверка конфликтов
        conflicts = await self._db.execute(
            select(Booking)
            .where(Booking.provider_id == provider_id)
            .where(Booking.status.in_([BookingStatus.CONFIRMED, BookingStatus.PENDING]))
            .where(
                or_(
                    and_(
                        Booking.start_time <= start_time,
                        Booking.end_time > start_time
                    ),
                    and_(
                        Booking.start_time < start_time + timedelta(minutes=duration_minutes),
                        Booking.end_time >= start_time + timedelta(minutes=duration_minutes)
                    )
                )
            )
        )

        if conflicts.scalars().first():
            raise SlotConflictException()

        # Создание брони
        self._db.add(booking)
        await self._db.commit()

        return booking
```

---

#### US-3.30: Waitlist (очередь ожидания) - Phase 3

**Как** Клиент  
**Хочу** встать в очередь, если слот занят  
**Чтобы** получить уведомление, если слот освободится

**Database Schema:**

```sql
CREATE TABLE app.booking_waitlist
(
    id          UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    client_id   UUID      NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    provider_id UUID      NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    date        DATE      NOT NULL,
    time_range  TSTZRANGE NOT NULL,
    service_ids JSONB     NOT NULL,
    status      VARCHAR(20)              DEFAULT 'WAITING',
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX ix_waitlist_provider_date ON app.booking_waitlist (provider_id, date);
```

---

## ⏰ Блок 8: Доступность Слотов

### Описание

Правила ограничения горизонта и времени бронирования.

### User Stories

#### US-3.34: Ограничение горизонта бронирования

**Как** Мастер  
**Хочу** ограничить бронирование 30 днями вперед  
**Чтобы** не планировать слишком далеко

**Business Rules:**

- `start_time <= NOW() + 30 days`
- Настраиваемое: `providers.booking_horizon_days` (default: 30)

**Database:**

```sql
ALTER TABLE app.providers
    ADD COLUMN booking_horizon_days INTEGER DEFAULT 30;
```

---

#### US-3.35: Минимальное время бронирования

**Как** Мастер  
**Хочу** запретить бронирование менее чем за 2 часа  
**Чтобы** иметь время на подготовку

**Business Rules:**

- `start_time >= NOW() + 2 hours`
- Настраиваемое: `providers.min_booking_notice_hours` (default: 2)

**Database:**

```sql
ALTER TABLE app.providers
    ADD COLUMN min_booking_notice_hours INTEGER DEFAULT 2;
```

---

#### US-3.36: Максимум записей в день

**Как** Мастер  
**Хочу** ограничить количество записей в день  
**Чтобы** не перегружаться

**Database:**

```sql
ALTER TABLE app.providers
    ADD COLUMN max_bookings_per_day INTEGER;
```

---

## 📊 Блок 9: История & Аналитика (Phase 3)

### User Stories

#### US-3.31: История записей клиента

**Как** Клиент  
**Хочу** видеть историю своих записей  
**Чтобы** отслеживать посещения

**API Endpoint:**

```yaml
GET /api/v1/bookings/history:
  summary: История записей клиента
  parameters:
    - name: status
      in: query
      schema:
        type: string
        enum: [ COMPLETED, CANCELLED, NO_SHOW ]
    - name: from_date
      in: query
      schema:
        type: string
        format: date
```

---

#### US-3.32: История клиента у мастера

**Как** Мастер  
**Хочу** видеть историю записей конкретного клиента  
**Чтобы** помнить предпочтения

**API Endpoint:**

```yaml
GET /api/v1/clients/{client_id}/bookings:
  summary: История записей клиента у мастера
  security: [ BearerAuth ]
```

---

#### US-3.33: Статистика мастера

**Как** Мастер  
**Хочу** видеть статистику по записям  
**Чтобы** анализировать загрузку

**Metrics:**

- Общее количество записей за период
- Загрузка по дням недели
- Самые популярные услуги
- No-Show rate
- Доход за период

---

## 🏗️ Архитектура

### Feature Structure

```
backend/app/features/booking/
├── domain/
│   ├── entities/
│   │   ├── booking.py
│   │   ├── booking_item.py
│   │   ├── schedule_rule.py
│   │   └── booking_reschedule_history.py
│   ├── value_objects/
│   │   ├── booking_status.py
│   │   ├── time_slot.py
│   │   └── price.py
│   └── exceptions.py
├── application/
│   ├── use_cases/
│   │   ├── create_booking.py
│   │   ├── confirm_booking.py
│   │   ├── cancel_booking.py
│   │   ├── reschedule_booking.py
│   │   ├── complete_booking.py
│   │   ├── mark_no_show.py
│   │   └── get_available_slots.py
│   ├── services/
│   │   ├── slot_generator.py
│   │   ├── slot_availability_service.py
│   │   ├── conflict_checker.py
│   │   └── price_calculator.py
│   ├── repositories.py (protocols)
│   └── unit_of_work.py
├── infrastructure/
│   ├── models/
│   │   ├── booking.py
│   │   ├── booking_item.py
│   │   ├── schedule_rule.py
│   │   └── booking_reschedule_history.py
│   ├── repositories/
│   │   ├── booking_repository.py
│   │   ├── schedule_repository.py
│   │   └── reschedule_history_repository.py
│   ├── mappers/
│   │   ├── booking_mapper.py
│   │   └── schedule_mapper.py
│   └── schemas/
│       ├── booking_schemas.py
│       ├── schedule_schemas.py
│       └── slot_schemas.py
└── presentation/
    ├── controllers/
    │   ├── booking_controller.py
    │   ├── schedule_controller.py
    │   └── slot_controller.py
    └── dependencies.py
```

---

## 🗄️ Database Schema Summary

### Новые таблицы (6 таблиц)

1. ✅ `bookings` - основные бронирования
2. ✅ `booking_items` - услуги в брони
3. ✅ `schedule_rules` - расписание мастеров
4. ✅ `booking_reschedule_history` - история переносов
5. ✅ `booking_waitlist` - очередь ожидания (Phase 3)
6. ✅ `provider_client_blocks` - блокировки клиентов (Phase 2)

### Изменения в существующих таблицах

**bookings:**

- `reschedule_count` (INTEGER)
- `expired_at` (TIMESTAMPTZ)
- `no_show_marked_at` (TIMESTAMPTZ)
- `no_show_marked_by` (UUID)

**profiles:**

- `no_show_count` (INTEGER)
- `no_show_rate` (DECIMAL(3,2))

**providers:**

- `allow_rescheduling` (BOOLEAN, default: TRUE)
- `auto_accept_bookings` (BOOLEAN, default: FALSE)
- `booking_horizon_days` (INTEGER, default: 30)
- `min_booking_notice_hours` (INTEGER, default: 2)
- `max_bookings_per_day` (INTEGER)
- ~~`require_deposit_threshold` (DECIMAL(3,2)) - ОТМЕНЕНО~~
- ~~`deposit_percentage` (INTEGER) - ОТМЕНЕНО~~

---

## 🌐 API Endpoints Summary

### Client Endpoints (9)

1. ✅ `POST /api/v1/bookings` - создать бронь
2. ✅ `GET /api/v1/bookings` - список броней клиента
3. ✅ `GET /api/v1/bookings/{id}` - детали брони
4. ✅ `PATCH /api/v1/bookings/{id}/cancel` - отменить бронь
5. ✅ `PATCH /api/v1/bookings/{id}/reschedule` - перенести бронь
6. ✅ `GET /api/v1/providers/{id}/slots` - получить слоты мастера
7. ✅ `POST /api/v1/waitlist` - встать в очередь (Phase 3)
8. ✅ `GET /api/v1/bookings/history` - история записей (Phase 3)
9. ✅ `GET /api/v1/reschedule-proposals/{id}/accept` - ~~Убрано~~

### Provider Endpoints (8)

1. ✅ `GET /api/v1/bookings` - список броней мастера
2. ✅ `PATCH /api/v1/bookings/{id}/confirm` - подтвердить бронь
3. ✅ `PATCH /api/v1/bookings/{id}/reject` - отклонить бронь
4. ✅ `PATCH /api/v1/bookings/{id}/complete` - завершить досрочно
5. ✅ `PATCH /api/v1/bookings/{id}/no-show` - отметить No-Show
6. ✅ `POST /api/v1/schedule` - создать правило расписания
7. ✅ `GET /api/v1/schedule` - получить расписание
8. ✅ `PATCH /api/v1/providers/settings/rescheduling` - запретить переносы

### Internal Endpoints (3)

1. ✅ `POST /internal/cron/expire-pending-bookings` - cron job
2. ✅ `POST /internal/cron/update-booking-statuses` - cron job
3. ✅ `POST /internal/cron/send-booking-reminders` - cron job

---

## 📅 План Реализации (3-4 недели)

### Week 1: Foundation (Critical Path)

**День 1-2: Database Schema**

- [ ] Создать миграции для всех таблиц
- [ ] Протестировать индексы
- [ ] Добавить CHECK constraints

**День 3-4: Domain Layer**

- [ ] Entities: Booking, BookingItem, ScheduleRule
- [ ] Value Objects: BookingStatus, TimeSlot
- [ ] Exceptions: domain-specific errors
- [ ] Unit tests для domain logic

**День 5: Application Layer (Interfaces)**

- [ ] Repository protocols
- [ ] Unit of Work
- [ ] SlotGenerator service
- [ ] SlotAvailabilityService

### Week 2: Core Booking Flow

**День 6-7: Infrastructure Layer**

- [ ] SQLAlchemy models
- [ ] Repositories implementations
- [ ] Mappers (ORM ↔ Entity)
- [ ] Integration tests для repositories

**День 8-10: Booking CRUD**

- [ ] CreateBookingUseCase
- [ ] ConfirmBookingUseCase
- [ ] CancelBookingUseCase
- [ ] GetAvailableSlotsUseCase
- [ ] API endpoints (POST, GET, PATCH)
- [ ] Integration tests

### Week 3: Advanced Features

**День 11-12: Schedule Management**

- [ ] ScheduleRule CRUD
- [ ] Slot generation algorithm
- [ ] API endpoints
- [ ] Tests

**День 13: Rescheduling**

- [ ] RescheduleBookingUseCase
- [ ] RescheduleHistory entity
- [ ] API endpoints
- [ ] Tests

**День 14-15: Workflow & Lifecycle**

- [ ] Background tasks (cron jobs)
- [ ] Status transitions
- [ ] No-Show marking
- [ ] Tests

### Week 4: Testing & Polish

**День 16-17: Integration Testing**

- [ ] End-to-end scenarios
- [ ] Concurrent booking tests
- [ ] Performance testing
- [ ] Security testing (SEC-001)

**День 18-19: Notifications**

- [ ] Notification templates
- [ ] Reminder cron jobs
- [ ] Tests

**День 20-21: Documentation & Review**

- [ ] OpenAPI specification
- [ ] README for booking feature
- [ ] Code review
- [ ] Bug fixes

---

## ✅ Definition of Done

### Code Quality

- [ ] Type hints (mypy --strict)
- [ ] No linting errors (ruff)
- [ ] Code review passed
- [ ] No N+1 queries (eager loading)
- [ ] Security: User ID from JWT only (SEC-001)

### Testing

- [ ] Unit tests coverage ≥ 80%
- [ ] Integration tests for all endpoints
- [ ] E2E tests for critical scenarios
- [ ] Performance tests (API < 500ms)

### Documentation

- [ ] OpenAPI specification updated
- [ ] Database schema documented
- [ ] README for booking feature
- [ ] Inline docstrings for public APIs

### Deployment

- [ ] Migrations tested
- [ ] Background tasks configured
- [ ] Environment variables documented
- [ ] Monitoring setup

---

## 📊 Итого: User Stories Summary

### MVP Phase (27 US)

**Block 1: Workflow & Lifecycle (5 US)**

- US-3.7: Автоматический таймаут заявки
- US-3.8: Автоматический переход CONFIRMED → IN_PROGRESS
- US-3.9: Автоматический переход IN_PROGRESS → COMPLETED
- US-3.10: Мастер завершает запись досрочно
- US-3.42: Auto-Accept режим

**Block 2: Rescheduling (2 US)**

- US-3.11: Клиент переносит запись
- US-3.43: Мастер запрещает переносы

**Block 3: Множественные Услуги (4 US)**

- US-3.14: Бронирование нескольких услуг
- US-3.15: Автоматический расчет duration
- US-3.16: Автоматический расчет стоимости
- US-3.17: Валидация сочетаемости услуг

**Block 4: No-Show Policy (2 US)**

- US-3.6: Отметка No-Show
- US-3.18: Расчет No-Show rate

**Block 5: Уведомления (4 US)**

- US-3.21: Напоминание за 24 часа
- US-3.22: Напоминание за 2 часа
- US-3.23: Напоминание мастеру за 1 час
- US-3.24: Уведомления при изменении статуса

**Block 6: График Работы (4 US)**

- US-3.25: Настройка рабочих часов
- US-3.26: Блокировка личного времени
- US-3.27: Buffer time между записями
- US-3.28: Перерывы (lunch break)

**Block 7: Конфликты (1 US)**

- US-3.29: Защита от double-booking

**Block 8: Доступность (3 US)**

- US-3.34: Ограничение горизонта бронирования
- US-3.35: Минимальное время бронирования
- US-3.36: Максимум записей в день

**Уже реализовано (2 US)**

- US-3.1: Клиент видит свободные слоты
- US-3.2: Клиент бронирует слот
- US-3.3: Мастер получает уведомление
- US-3.4: Мастер подтверждает/отклоняет
- US-3.5: Клиент отменяет запись

### Phase 2 (5 US)

- US-3.19: Deposit для клиентов с высоким No-Show
- US-3.20: Блокировка клиента
- US-3.31: История записей клиента
- US-3.32: История клиента у мастера
- US-3.33: Статистика мастера

### Phase 3 (6 US)

- US-3.30: Waitlist (очередь ожидания)
- US-3.37: Добавить услугу в существующую бронь
- US-3.38: Удалить услугу из брони
- US-3.39: Изменить цену после оказания
- US-3.40: Ограничение активных записей
- US-3.41: Rate Limiting на бронирование

---

**Всего User Stories: 43**  
**Для MVP (Phase 1): 27**  
**Для Phase 2: 5**  
**Для Phase 3: 6**  
**Уже реализовано: 5**

---

## 📝 Связанные Документы

- [User Stories](../01_USER_STORIES.md) - обновить US для E3
- [Use Cases](../02_USE_CASES.md) - детальные UC для booking
- [Database Schema](../../architecture/backend/02_DATABASE_SCHEMA.md) - таблицы booking
- [API Design](../../architecture/backend/01_API_DESIGN.md) - booking endpoints
- [Implementation Status](../00_IMPLEMENTATION_STATUS.md) - прогресс E3

---

**Последнее обновление:** 05.03.2026  
**Maintained by:** Development Team
