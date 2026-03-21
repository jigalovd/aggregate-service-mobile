# KPIs & Metrics: Beauty Service Aggregator

**Дата создания:** 23.02.2026
**Версия:** 1.1
**Последнее обновление:** 27.02.2026 (PM + Tech Lead Analysis)

---

## 🎯 North Star Metric

### Definition

**"Completed Bookings per Month"** - количество завершенных бронирований в месяц

### Почему это North Star?

- ✅ Прямое отражение ценности для обеих сторон (клиенты записались, мастера оказали услугу)
- ✅ Ведущий индикатор бизнес-здоровья
- ✅ Фокус на core value proposition (соединить клиентов и мастеров)

**Target (MVP - 6 месяцев):**

- Phase 1 (месяц 1-2): 50 bookings/месяц
- Phase 2 (месяц 3-4): 200 bookings/месяц
- Phase 3 (месяц 5-6): 500 bookings/месяц

---

## 📊 Когортные метрики (Funnel Metrics)

### 1. Acquisition (Привлечение)

**Воронка: Visitor → Registered User → First Booking**

| Metric                 | Definition                             | Target (MVP) | Current | Status         |
|------------------------|----------------------------------------|--------------|---------|----------------|
| **Visitors**           | Уникальные посетители приложения/сайта | 1,000/месяц  | 0       | 🔴 Not started |
| **Sign-up Rate**       | Registration / Visitors                | 15%          | 0%      | 🔴 Not started |
| **First Booking Rate** | First booking / Registered             | 40%          | 0%      | 🔴 Not started |

**Benchmarks:**

- Industry average Sign-up Rate: 10-20%
- Industry average First Booking Rate: 30-50%

**Key Drivers:**

- 🚀 Marketing channels (Instagram, Google Ads)
- 🚀 Onboarding UX (простота регистрации)
- 🚀 Initial catalog size (количество мастеров)

---

### 2. Activation (Активация)

**Definition:** Пользователь совершил первое бронирование

| Metric                       | Definition                                   | Target (MVP) | Current | Status         |
|------------------------------|----------------------------------------------|--------------|---------|----------------|
| **Time to First Booking**    | Время от регистрации до первого бронирования | < 24 часов   | -       | 🔴 Not started |
| **First Booking Completion** | % завершенных первых бронирований            | 90%          | -       | 🔴 Not started |
| **Provider First Booking**   | % мастеров, получивших первую бронь          | 60%          | -       | 🔴 Not started |

**Key Insights:**

- Если Time to First Booking > 24 часа → проблема с supply (мало мастеров)
- Если First Booking Completion < 90% → проблема с качеством match

---

### 3. Engagement (Вовлеченность)

**Definition:** Пользователи регулярно используют приложение

| Metric                   | Definition                                       | Target (MVP) | Current | Status         |
|--------------------------|--------------------------------------------------|--------------|---------|----------------|
| **DAU/MAU**              | Daily Active Users / Monthly Active Users        | > 20%        | -       | 🔴 Not started |
| **Booking Frequency**    | Среднее количество бронирований на клиента/месяц | 1.5          | -       | 🔴 Not started |
| **Provider Utilization** | Средняя заполняемость графика мастера            | 60%          | -       | 🔴 Not started |

**Benchmarks:**

- Healthy DAU/MAU: > 20% (excellent: > 30%)
- Healthy Booking Frequency: 1.5-2.5 bookings/месяц
- Healthy Provider Utilization: 60-80%

---

### 4. Retention (Удержание)

**Definition:** Пользователи возвращаются

| Metric               | Definition                                 | Target (MVP) | Current | Status         |
|----------------------|--------------------------------------------|--------------|---------|----------------|
| **Day-7 Retention**  | % пользователей, вернувшихся через 7 дней  | 40%          | -       | 🔴 Not started |
| **Day-30 Retention** | % пользователей, вернувшихся через 30 дней | 25%          | -       | 🔴 Not started |
| **Cohort Retention** | Удержание по когортам (месяц 1, 2, 3...)   | > 60% (M3)   | -       | 🔴 Not started |

**Benchmarks:**

- Marketplace average Day-7: 30-40%
- Marketplace average Day-30: 20-30%
- Excellent retention: M3 > 60%

**Key Drivers:**

- 🚀 Quality of matches (клиенты довольны мастерами)
- 🚀 Reminders (не забыть о повторной записи)
- 🚀 Loyalty programs (мотивация возвращаться)

---

### 5. Revenue (Выручка)

**Definition:** Денежные метрики (Phase 2 - Monetization)

| Metric            | Definition                         | Target (Phase 2) | Current | Status         |
|-------------------|------------------------------------|------------------|---------|----------------|
| **ARPU**          | Average Revenue Per User (в месяц) | $5-10            | $0      | 🔴 Not started |
| **CAC**           | Customer Acquisition Cost          | < $10            | -       | 🔴 Not started |
| **LTV**           | Lifetime Value (за 12 месяцев)     | > $50            | -       | 🔴 Not started |
| **LTV/CAC Ratio** | LTV / CAC                          | > 3:1            | -       | 🔴 Not started |

**Benchmarks:**

- Healthy marketplace LTV/CAC: > 3:1
- Excellent: > 5:1

**Monetization Models (Phase 2):**

- Option A: Commission per booking (10-15%)
- Option B: Subscription for providers ($20-50/месяц)
- Option C: Hybrid (комиссия + Pro features)

---

### 6. Referral (Рекомендации)

**Definition:** Пользователи рекомендуют приложение

| Metric                | Definition                                             | Target (MVP) | Current | Status         |
|-----------------------|--------------------------------------------------------|--------------|---------|----------------|
| **NPS**               | Net Promoter Score (-100 to +100)                      | > 40         | -       | 🔴 Not started |
| **Viral Coefficient** | Сколько новых пользователей приводит один существующий | > 1.0        | -       | 🔴 Not started |
| **Share Rate**        | % пользователей, поделившихся приложением              | 15%          | 0%      | 🔴 Not started |

**Benchmarks:**

- Excellent NPS: > 50 (Good: 30-50, Average: 0-30)
- Viral Coefficient > 1.0 = exponential growth

**Key Drivers:**

- 🚀 Exceptional user experience
- 🚀 Referral program (бонусы за рекомендации)
- 🚀 Social sharing (share master profile)

---

## 🎯 Segment-Specific Metrics

### For Clients (Клиенты)

| Metric                         | Definition                                      | Target     |
|--------------------------------|-------------------------------------------------|------------|
| **Search Success Rate**        | % успешных поисков (нашли мастера и записались) | > 80%      |
| **Booking Cancellation Rate**  | % отмененных бронирований                       | < 15%      |
| **Average Booking Time**       | Время от начала поиска до бронирования          | < 10 минут |
| **Client Satisfaction (CSAT)** | Удовлетворенность после визита (1-5)            | > 4.5      |

### For Providers (Мастера)

| Metric                    | Definition                                           | Target |
|---------------------------|------------------------------------------------------|--------|
| **Provider Activation**   | % мастеров, получивших первую бронь в течение 7 дней | > 60%  |
| **Schedule Utilization**  | Средняя заполняемость графика                        | 60-80% |
| **No-Show Rate**          | % клиентов, не пришедших на запись                   | < 10%  |
| **Provider Churn**        | % мастеров, покинувших платформу за месяц            | < 5%   |
| **Provider Satisfaction** | Удовлетворенность мастеров (1-5)                     | > 4.0  |

---

## 📈 Health Score Metrics

### System Health (Здоровье системы)

| Metric                   | Definition                          | Healthy   | Warning         | Critical         |
|--------------------------|-------------------------------------|-----------|-----------------|------------------|
| **Supply/Demand Ratio**  | Мастера / Клиенты (в радиусе)       | 1:3 - 1:5 | < 1:5 или > 1:2 | < 1:10 или > 1:1 |
| **Match Rate**           | % успешных matches (запрос → бронь) | > 70%     | 50-70%          | < 50%            |
| **Payment Success Rate** | % успешных платежей                 | > 98%     | 95-98%          | < 95%            |
| **API Response Time**    | Среднее время ответа API            | < 200ms   | 200-500ms       | > 500ms          |
| **App Crash Rate**       | % крашей приложения                 | < 0.5%    | 0.5-2%          | > 2%             |

**Key Insights:**

- Supply/Demand Ratio < 1:10 → мало мастеров (нужен supply-side growth)
- Supply/Demand Ratio > 1:1 → мало клиентов (нужен demand-side growth)
- Match Rate < 50% → проблема с качеством алгоритмов

---

## 🛠️ Technical Health Metrics (Code Quality)

**Дата анализа:** 27.02.2026

### Code Quality Metrics

| Metric                 | Current | Target (MVP) | Gap  | Status      |
|------------------------|---------|--------------|------|-------------|
| **Test Coverage**      | ~40%    | 80%          | -40% | 🔴 Critical |
| **Mypy Errors**        | 43      | 0            | -43  | 🔴 Critical |
| **Architecture Score** | 7/10    | 9/10         | -2   | 🟡 Warning  |
| **Tech Debt Score**    | 7/10    | 9/10         | -2   | 🟡 Warning  |
| **E2E Tests**          | 0       | 20%          | -20% | 🟡 Warning  |

### Test Coverage by Feature

| Feature  | Unit | Integration | E2E | Overall        |
|----------|------|-------------|-----|----------------|
| Auth     | 88%  | 85%         | 0%  | 🟢 Good        |
| Profiles | 94%  | 70%         | 0%  | 🟢 Good        |
| Booking  | 0%   | 0%          | 0%  | 🔴 Not Started |
| Search   | 0%   | 0%          | 0%  | 🔴 Not Started |
| Reviews  | 0%   | 0%          | 0%  | 🔴 Not Started |

### Technical Debt Tasks (P0)

| Task                     | Effort   | Blocker            |
|--------------------------|----------|--------------------|
| SendGrid Spike           | 2-3 days | Email verification |
| Mypy fixes (43 errors)   | 1 day    | Code quality       |
| Profiles JWT integration | 1 day    | Security           |

---

## 🎯 MVP Success Criteria

### Phase 1 (Months 1-2): Launch

**Goal:** Запуск MVP, первичные пользователи

| Criterion                       | Target            | Success/Fail    |
|---------------------------------|-------------------|-----------------|
| Launch app (Backend + Frontend) | ✅ Working MVP     | ✅ Complete      |
| 50+ registered providers        | 50+ мастеров      | ❌ Not met       |
| 200+ registered clients         | 200+ клиентов     | ❌ Not met       |
| 100+ total bookings             | 100+ бронирований | ❌ Not met       |
| < 5% crash rate                 | Stable app        | ⚠️ Need testing |

**Status:** 🔴 Need to launch

---

### Phase 2 (Months 3-4): Growth

**Goal:** Рост пользовательской базы

| Criterion              | Target                       | Success/Fail |
|------------------------|------------------------------|--------------|
| 200+ bookings/month    | 200+ бронирований/месяц      | ❌ Not met    |
| 40% sign-up rate       | 40% регистрация/посетители   | ❌ Not met    |
| 40% first booking rate | 40% первая бронь/регистрация | ❌ Not met    |
| 25% Day-30 retention   | 25% удержание (день 30)      | ❌ Not met    |

**Status:** 🔴 Not started

---

### Phase 3 (Months 5-6): Scale

**Goal:** Масштабирование

| Criterion                 | Target                     | Success/Fail |
|---------------------------|----------------------------|--------------|
| 500+ bookings/month       | 500+ бронирований/месяц    | ❌ Not met    |
| 60% provider utilization  | 60% заполняемость мастеров | ❌ Not met    |
| 1.5 bookings/client/month | 1.5 бронь/клиент/месяц     | ❌ Not met    |
| NPS > 40                  | Net Promoter Score > 40    | ❌ Not met    |

**Status:** 🔴 Not started

---

## 🔍 Tracking & Analytics

### Tools to Implement

1. **Backend Analytics**
    - **Mixpanel / Amplitude** - product analytics (funnels, cohorts)
    - **PostgreSQL** - raw data storage
    - **Metabase / Grafana** - dashboards

2. **Frontend Analytics**
    - **Firebase Analytics** - mobile app events
    - **Crashlytics** - crash reporting

3. **Business Intelligence**
    - **Google Sheets** - initial MVP tracking
    - **Looker / Tableau** - advanced BI (Phase 2)

### Key Events to Track

**Client-side:**

- `app_open` - открытие приложения
- `search_started` - начало поиска
- `master_profile_viewed` - просмотр мастера
- `booking_initiated` - начало бронирования
- `booking_completed` - бронь завершена
- `booking_cancelled` - бронь отменена
- `review_submitted` - отзыв оставлен
- `app_shared` - приложение поделились

**Provider-side:**

- `provider_registered` - регистрация мастера
- `schedule_updated` - обновление графика
- `booking_received` - получение новой брони
- `booking_confirmed` - подтверждение брони
- `booking_completed` - завершение услуги
- `analytics_viewed` - просмотр аналитики

---

## 🎯 Dashboard Requirements

### Executive Dashboard (CEO/Product)

**Real-time:**

- Today's bookings (vs yesterday, vs last week)
- Active users (DAU/MAU)
- Conversion funnel (Visitor → Sign-up → Booking)

**Weekly:**

- Booking growth rate
- Retention curves
- Supply/Demand ratio by city

**Monthly:**

- Revenue (Phase 2)
- CAC, LTV, LTV/CAC
- Cohort analysis

### Provider Dashboard (Master)

**Real-time:**

- Today's schedule
- Upcoming bookings
- New booking requests

**Weekly:**

- Earnings
- Utilization rate
- Client satisfaction

**Monthly:**

- Popular services
- No-show rate
- Reviews received

---

## 🔗 Related Documents

- **Customer Journey:** [`08_CUSTOMER_JOURNEY.md`](./08_CUSTOMER_JOURNEY.md) @./08_CUSTOMER_JOURNEY.md
- **User Personas:** [`07_USER_PERSONAS.md`](./07_USER_PERSONAS.md) @./07_USER_PERSONAS.md
- **Implementation Status:** [`00_IMPLEMENTATION_STATUS.md`](./00_IMPLEMENTATION_STATUS.md) @./00_IMPLEMENTATION_STATUS.md

---

## 📝 Next Steps

1. **Implement analytics SDK** (Mixpanel/Firebase)
2. **Define event taxonomy** (согласовать команды)
3. **Create dashboards** (Metabase/Grafana)
4. **Set up alerts** (critical metrics threshold)
5. **Baseline measurement** (замерить стартовые метрики)

---

**Последнее обновление:** 27.02.2026
**Автор:** Product Analyst + Tech Lead
**Статус:** Updated with Technical Metrics
