# MVP Scope Definition

**Версия:** 1.2 | **Дата:** 27.02.2026 | **Статус:** Approved

---

## 🎯 MVP Goal

**Цель:** Валидировать core value proposition (multi-role + booking) за 16 недель.

**Принцип:** **Качество > Скорость**

- TDD для всех новых фич
- 1 день/спринт на техдолг
- Mypy ошибки = блокер для PR

**Key Success Metrics:**

- 50 completed bookings/месяц (Month 1)
- 100 registered users
- 50 active providers
- 20% Day-30 retention

---

## ✅ IN Scope

### E1: Onboarding & Identity (95%)

**Что работает:**

- [x] Регистрация с multi-role (client + provider)
- [x] Логин + refresh токены
- [x] Профиль пользователя
- [x] Переключение контекста
- [ ] Email verification (требует SendGrid Spike)

**SendGrid Spike (Week 1):**

- Account setup, SPF/DKIM
- API key + test delivery
- Integration in `main.py`

**Детали:** [00_IMPLEMENTATION_STATUS.md](./00_IMPLEMENTATION_STATUS.md) @./00_IMPLEMENTATION_STATUS.md

---

### E2: Catalog & Geo-Search (MVP)

**Что будет:**

- [ ] Models: Service, Category, Provider (базовый)
- [ ] PostGIS для geo-поиска
- [ ] API: GET /providers, /providers/{id}, /categories
- [ ] Seed data: 50 test providers

**Что НЕ будет (Phase 2):**

- ❌ Portfolio upload
- ❌ Избранное
- ❌ Advanced filters

---

### E3: Booking Engine (MVP)

**Что будет:**

- [ ] Model: Booking (PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW)
- [ ] API: GET /slots, POST /bookings, PATCH /cancel
- [ ] Email notifications (новая бронь, подтверждение, отмена)

**Что НЕ будет (Phase 2):**

- ❌ Advanced scheduling
- ❌ Recurring availability
- ❌ Booking modification
- ❌ Push notifications

---

## ❌ OUT Scope (Phase 2)

| Эпик                   | Причина                     | Timeline  |
|------------------------|-----------------------------|-----------|
| E4: Service Management | Не критично для MVP         | Month 2-3 |
| E5: Reputation System  | Требует critical mass       | Month 2-3 |
| E6: Push Notifications | Email достаточно            | Month 3-4 |
| Phone Verification     | Дополнительная complexity   | Month 2   |
| OAuth Integration      | Не критично для acquisition | Month 4+  |

---

## 📅 Timeline (16 Weeks)

| Period     | Focus                | Deliverable                 |
|------------|----------------------|-----------------------------|
| Week 1-2   | Tech Debt + SendGrid | Email verification работает |
| Week 3-6   | E2 Catalog           | Catalog API deployed        |
| Week 7-10  | E3 Booking           | Booking flow работает       |
| Week 11-14 | Polish               | Production-ready MVP        |
| Week 15-16 | Launch Prep          | MVP в production            |

---

## 🎯 Success Criteria

### Technical

| Metric             | Target        |
|--------------------|---------------|
| Test Coverage      | > 80%         |
| Mypy Errors        | 0             |
| Architecture Score | 9/10          |
| API Response Time  | < 500ms (p95) |
| Uptime             | 99%+          |

### Business (Month 1)

| Metric             | Target |
|--------------------|--------|
| Completed Bookings | 50     |
| Registered Users   | 100    |
| Active Providers   | 50     |
| Day-30 Retention   | 20%    |

---

## 🔗 Связанные документы

| Документ                                                     | Описание            |
|--------------------------------------------------------------|---------------------|
| [00_IMPLEMENTATION_STATUS.md](./00_IMPLEMENTATION_STATUS.md) @./00_IMPLEMENTATION_STATUS.md | Статус реализации   |
| [BACKLOG.md](./BACKLOG.md) @./BACKLOG.md                                   | Задачи и приоритеты |
| [10_KPIS_AND_METRICS.md](./10_KPIS_AND_METRICS.md) @./10_KPIS_AND_METRICS.md           | KPIs и метрики      |
| [01_USER_STORIES.md](./01_USER_STORIES.md) @./01_USER_STORIES.md                   | User Stories        |

---

## 📝 Changelog

| Дата       | Версия | Изменения                                                    |
|------------|--------|--------------------------------------------------------------|
| 27.02.2026 | 1.2    | PM Analysis: E5 → Phase 2, качество > скорость, coverage 80% |
| 23.02.2026 | 1.1    | Email verification decision                                  |
| 23.02.2026 | 1.0    | Initial MVP scope                                            |
