# Customer Journey Maps: Beauty Service Aggregator

**Дата создания:** 23.02.2026
**Версия:** 1.1
**Based on Personas:** [`07_USER_PERSONAS.md`](./07_USER_PERSONAS.md) @./07_USER_PERSONAS.md

---

## ⚠️ Important Note: Email Verification Status (MVP)

**Current Behavior:**

- Email verification API endpoints работают (`/verify-email`, `/resend-verification`)
- **ОДНАКО** verification НЕ enforced для protected endpoints
- RequireVerifiedMiddleware НЕ активирован в `app/main.py`

**Impact on Customer Journey:**

- ✅ Registration flow работает без verification
- ✅ Booking flow доступен сразу после регистрации
- ⚠️ **Phase 4 (Booking):** Шаг "Email verification" может быть пропущен

**For MVP:**

- Email verification **OPTIONAL** (Option B: speed > security)
- Phase 2: Phone verification + enforce email verification

**See Also:**

- [`00_IMPLEMENTATION_STATUS.md`](./00_IMPLEMENTATION_STATUS.md) @./00_IMPLEMENTATION_STATUS.md - technical details
- [`11_MVP_SCOPE.md`](./11_MVP_SCOPE.md) @./11_MVP_SCOPE.md - MVP decision rationale

---

## 🗺️ Journey Map 1: Мария (Quality-seeker) - Поиск нового мастера

### Persona Context

- **Персона:** Мария, 28 лет, маркетолог
- **Цель:** Найти надежного мастера по маникюру рядом с офисом
- **Начальная точка:** Осознание потребности (нужен маникюр)
- **Конечная точка:** Повторная запись (становится лояльным клиентом)

---

### Phase 1: Awareness (Осознание потребности)

**Timing:** Вторник, 10:00 (на работе)

| Touchpoint             | Action                                | Thought                           | Emotion             | Pain Point     | Opportunity                    |
|------------------------|---------------------------------------|-----------------------------------|---------------------|----------------|--------------------------------|
| **Внутренний триггер** | Мария смотрит на свои ногти           | "Пора к мастеру, выглядят ужасно" | 😐 Frustration (-2) | -              | -                              |
| **Instagram**          | Листает ленту, видит рекламу мастеров | "Не знаю, кому довериться"        | 😕 Confused (-1)    | Сложно выбрать | Рейтинги и отзывы в приложении |

**Key Insights:**

- ❌ **Текущая проблема:** Нет trusted source для поиска мастеров
- ✅ **Решение:** Beauty Service станет trusted source с рейтингами

---

### Phase 2: Research (Поиск мастера)

**Timing:** Вторник, 12:30 (обеденный перерыв)

| Touchpoint      | Action                                                 | Thought                                                | Emotion            | Pain Point                 | Opportunity                     |
|-----------------|--------------------------------------------------------|--------------------------------------------------------|--------------------|----------------------------|---------------------------------|
| **Google Maps** | Ищет "маникюр Тель-Авив"                               | "Список салонов, но нет информации о мастерах"         | 😐 Neutral (0)     | Нет transparent информации | Показывать частных мастеров     |
| **Instagram**   | Проводит 2 часа, изучая портфолио                      | "У этого хорошие работы, но не понятно, сколько стоит" | 😕 Confused (-1)   | Нет transparent цен        | Цены upfront в приложении       |
| **WhatsApp**    | Пишет 3 мастерам: "Есть ли время на следующей неделе?" | "Жду ответов уже 2 часа"                               | 😤 Frustrated (-2) | Медленная коммуникация     | Моментальная бронь в приложении |

**Key Insights:**

- ❌ **Текущая проблема:** 2+ часа на research, нет информации
- ✅ **Решение:** Все в одном месте (рейтинги + цены + портфолио + бронь)

---

### Phase 3: Decision (Принятие решения)

**Timing:** Вторник, 19:00 (дома)

| Touchpoint          | Action                             | Thought                                          | Emotion          | Pain Point                 | Opportunity                         |
|---------------------|------------------------------------|--------------------------------------------------|------------------|----------------------------|-------------------------------------|
| **Сравнение**       | Сравнивает 3 мастеров              | "Этот 400 ₪, тот 200 ₪. В чем разница?"          | 😕 Confused (-1) | Не понятно, за что платишь | Детальная информация: опыт, рейтинг |
| **Отзывы подруги**  | Звонит подруге: "Кому ты ходишь?"  | "Подруга recomends Анну, но она в другом районе" | 😊 Relief (+1)   | Доверие > реклама          | Социальные proof (реальные отзывы)  |
| **Финальный выбор** | Выбирает мастера по отзыву подруги | "Надеюсь, не ошибусь"                            | 😰 Anxious (-1)  | Нет гарантии качества      | Гарантия возврата денег             |

**Current State:**

- 😰 **Emotion:** Negative (-1) - anxious, не уверена
- ⏱️ **Time spent:** 3+ часа
- 💰 **Cost:** 0 ₪ (только time)

**Future State (with Beauty Service):**

- 😊 **Emotion:** Positive (+2) - уверена
- ⏱️ **Time spent:** 10 минут
- 💰 **Cost:** 0 ₪

**Improvement:** 🚀 18x быстрее, позитивные эмоции

---

### Phase 4: Booking (Бронирование)

**Timing:** Вторник, 20:00

| Touchpoint             | Action                                         | Thought                  | Emotion              | Pain Point          | Opportunity                  |
|------------------------|------------------------------------------------|--------------------------|----------------------|---------------------|------------------------------|
| **WhatsApp**           | Пишет мастеру: "Здравствуйте, хочу записаться" | "Жду реакции..."         | 😐 Neutral (0)       | Медленно            | Автоматическая бронь         |
| **Обсуждение деталей** | Мастер: "Какие услуги? Когда удобно?"          | "Я же не знаю, что есть" | 😤 Frustrated (-2)   | Не знает прайс-лист | Выбрать услуги из списка     |
| **Подтверждение**      | Мастер: "Есть время в пятницу в 14:00"         | "Не подходит, я работаю" | 😞 Disappointed (-2) | Не подходит время   | Видеть все слоты в календаре |

**Current State:**

- 😞 **Emotion:** Negative (-2) - disappointed, frustrated
- ⏱️ **Time spent:** 30+ минут (обратная связь)
- 🔄 **Touchpoints:** 5+ (WhatsApp сообщения)

**Future State (with Beauty Service):**

- 😊 **Emotion:** Positive (+2) - satisfied
- ⏱️ **Time spent:** 2 минуты
- 🔄 **Touchpoints:** 1 (в приложении)

**Improvement:** 🚀 15x быстрее, 5x меньше touchpoints

---

### Phase 5: Service Delivery (Получение услуги)

**Timing:** Пятница, 14:00 (запись)

| Touchpoint  | Action                | Thought                         | Emotion          | Pain Point         | Opportunity                 |
|-------------|-----------------------|---------------------------------|------------------|--------------------|-----------------------------|
| **Arrival** | Приходит в салон      | "Мастер опаздывает на 10 минут" | 😒 Annoyed (-1)  | Потерянное время   | Push-уведомление о задержке |
| **Service** | Мастер делает маникюр | "Неплохо, но не идеально"       | 😐 Neutral (0)   | Не тем мастер      | Matching algorithm          |
| **Payment** | Платит наличными      | "Забыла карточку, была наличка" | 😰 Stressed (-1) | Нет удобной оплаты | Онлайн-оплата               |

**Key Insights:**

- ❌ **Текущая проблема:** Нет прозрачности, неоптимальный match
- ✅ **Решение:** Matching algorithm по предпочтениям, online payment

---

### Phase 6: Post-Service (После услуги)

**Timing:** Пятница, 15:00 (после визита)

| Touchpoint   | Action                                    | Thought                          | Emotion         | Pain Point          | Opportunity             |
|--------------|-------------------------------------------|----------------------------------|-----------------|---------------------|-------------------------|
| **Review**   | Мастер: "Оставьте отзыв, пожалуйста"      | "Не хочу, чувствую себя неловко" | 😒 Awkward (-1) | Социальное давление | Необязательный отзыв    |
| **Reminder** | Мастер: "Приходите через 2-3 недели"      | "Забуду, нужно записаться"       | 😐 Neutral (0)  | Легко забыть        | Автоматический reminder |
| **Loyalty**  | Мастер: "Вам скидка 10% за привод друзей" | "Не знаю, кого привести"         | 😐 Neutral (0)  | Нет мотивации       | Система лояльности      |

**Key Insights:**

- ❌ **Текущая проблема:** Нет retention механизма
- ✅ **Решение:** Автоматические reminders, система лояльности

---

### Phase 7: Retention (Возврат клиента)

**Timing:** Через 3 недели

| Touchpoint     | Action                                | Thought                     | Emotion            | Pain Point              | Opportunity                      |
|----------------|---------------------------------------|-----------------------------|--------------------|-------------------------|----------------------------------|
| **Reminder**   | (Текущее) Напоминает себе в календаре | "О, нужно записаться"       | 😐 Neutral (0)     | Легко забыть            | Push-reminder от мастера         |
| **Re-booking** | Опять через WhatsApp                  | "Опять этот долгий процесс" | 😒 Frustrated (-1) | Нет удобного re-booking | One-click re-booking             |
| **Decision**   | Решает попробовать другого мастера    | "Может есть лучше?"         | 😕 Confused (-1)   | Нет лояльности          | Profile мастера, история визитов |

**Current State:**

- 😕 **Emotion:** Negative (-1) - frustrated, confused
- 🔄 **Retention:** 40% (теряет 60% клиентов)
- ⏱️ **Time to re-book:** 30+ минут

**Future State (with Beauty Service):**

- 😊 **Emotion:** Positive (+2) - satisfied
- 🔄 **Retention:** 80% (теряет только 20%)
- ⏱️ **Time to re-book:** 30 секунд

**Improvement:** 🚀 60x быстрее, 2x выше retention

---

## 📊 Journey Metrics Summary

| Metric                  | Current State   | Future State    | Improvement  |
|-------------------------|-----------------|-----------------|--------------|
| **Time to find master** | 3+ часов        | 10 минут        | 🚀 18x       |
| **Time to book**        | 30+ минут       | 2 минуты        | 🚀 15x       |
| **Touchpoints**         | 10+             | 2-3             | 🚀 3x меньше |
| **Emotion score**       | -1.2 (Negative) | +1.8 (Positive) | 🚀 +3.0      |
| **Retention rate**      | 40%             | 80%             | 🚀 2x        |
| **Re-book time**        | 30+ минут       | 30 секунд       | 🚀 60x       |

---

## 🗺️ Journey Map 2: Елена ("Busy") - "Здесь и сейчас"

### Persona Context

- **Персона:** Елена, 35 лет, HR Manager + мама
- **Цель:** Записаться на маникюр в свободный час
- **Сценарий:** "Удалось найти слот на сегодня" vs "Не нашлось, расстроена"

---

### Phase 1: Trigger (Осознание - срочно)

**Timing:** Среда, 12:00 (на работе)

| Touchpoint    | Action                   | Thought                                      | Emotion         | Pain Point             | Opportunity                      |
|---------------|--------------------------|----------------------------------------------|-----------------|------------------------|----------------------------------|
| **Free slot** | Елену отменили встречу   | "У меня есть свободный час (13:00-14:00)!"   | 😊 Excited (+2) | -                      | "Быстрая запись" feature         |
| **Decision**  | "Хочу успеть на маникюр" | "Но кого спросить? Есть ли свободное время?" | 😕 Worried (-1) | Не знает, у кого слоты | Geo-search + availability filter |

**Current State:** 😕 Confused, не знает куда пойти
**Future State:** 😊 Excited, открывает приложение, видит доступные слоты

---

### Phase 2: Search (Поиск - срочно)

**Timing:** Среда, 12:05

| Touchpoint      | Action                           | Thought                                     | Emotion              | Pain Point              | Opportunity             |
|-----------------|----------------------------------|---------------------------------------------|----------------------|-------------------------|-------------------------|
| **Google Maps** | Ищет "маникюр рядом"             | "Салоны есть, но не понятно, есть ли время" | 😒 Frustrated (-1)   | Нет информации о слотах | Real-time availability  |
| **Calling**     | Звонит 3 салонам                 | "У всех запись на неделю вперед"            | 😞 Disappointed (-3) | Не успеть сегодня       | Waitlist feature        |
| **Desperate**   | "Ладно, поеду в тот, что дальше" | "Трафиг, не успею"                          | 😤 Angry (-2)        | Потерянное время        | Фильтр "сегодня, рядом" |

**Current State:** 😞 Disappointed (-3), не успевает
**Future State:** 😊 Satisfied (+2), нашла слот за 2 минуты

---

### Phase 3: Booking (Бронь - срочно)

**Timing:** Среда, 12:10

| Touchpoint     | Action                                       | Thought                    | Emotion         | Pain Point | Opportunity             |
|----------------|----------------------------------------------|----------------------------|-----------------|------------|-------------------------|
| **Found!**     | (Future State) Нашла мастера со слотом 13:00 | "Ура, успеваю!"            | 😊 Excited (+3) | -          | One-tap booking         |
| **Navigation** | Открывает Waze                               | "10 минут ехать"           | 😊 Calm (+1)    | -          | Deep link to Waze       |
| **Arrival**    | Приезжает к 12:50                            | "Успела, могу выпить кофе" | 😊 Relaxed (+2) | -          | Push reminder за 10 мин |

**Future State:** 😊 Positive (+2), все гладко

---

## 🗺️ Journey Map 3: Анна (Provider) - Управление расписанием

### Persona Context

- **Персона:** Анна, 32 года, самозанятый мастер
- **Цель:** Заполнить расписание, избежать простоев

---

### Phase 1: Morning (Начало дня)

**Timing:** Понедельник, 08:00

| Touchpoint         | Action                        | Thought                                         | Emotion              | Pain Point               | Opportunity                        |
|--------------------|-------------------------------|-------------------------------------------------|----------------------|--------------------------|------------------------------------|
| **Wake up**        | Просыпается, берет телефон    | "10 новых сообщений в WhatsApp"                 | 😒 Overwhelmed (-1)  | Хаос с записью           | Автоматическая запись              |
| **Check schedule** | Открывает бумажный ежедневник | "Сегодня только 2 записи. Буду сидеть без дела" | 😞 Disappointed (-2) | Незаполненное расписание | Smart recommendations для клиентов |

**Current State:** 😞 Disappointed (-2), пустое расписание
**Future State:** 😊 Satisfied (+1), система подсказала заполнить слоты

---

### Phase 2: Client No-Show (Клиент не пришел)

**Timing:** Понедельник, 14:00

| Touchpoint             | Action                                   | Thought                             | Emotion              | Pain Point               | Opportunity              |
|------------------------|------------------------------------------|-------------------------------------|----------------------|--------------------------|--------------------------|
| **Waiting**            | Ждет клиента (Мария)                     | "Мария не пришла и не предупредила" | 😤 Angry (-2)        | Потерянное время + доход | Late cancellation policy |
| **Attempt to contact** | Пишет в WhatsApp                         | "Мария не отвечает"                 | 😡 Frustrated (-3)   | Невозможно переехать     | Auto-reminders за 2 часа |
| **Reschedule?**        | "Не могу заполнить это слот за 10 минут" | "Потеряла 400 ₪"                    | 😞 Disappointed (-3) | Нет резервного времени   | Waitlist для слотов      |

**Current State:** 😞 Disappointed (-3), потерянный доход
**Future State:** 😌 Neutral (0), система пометила как no-show, клиент оплатил штраф

---

### Phase 3: Analytics (Аналитика)

**Timing:** Воскресенье, 20:00 (конец дня)

| Touchpoint           | Action                           | Thought                                | Emotion          | Pain Point             | Opportunity                   |
|----------------------|----------------------------------|----------------------------------------|------------------|------------------------|-------------------------------|
| **Calculate income** | Считает в Excel                  | "Сколько я заработала? Не могу понять" | 😕 Confused (-1) | Нет аналитики          | Dashboard с метриками         |
| **Popular services** | "Какие услуги самые популярные?" | "Не знаю, нет данных"                  | 😐 Neutral (0)   | Нет данных для решений | Analytics dashboard           |
| **Marketing**        | "Как привлечь новых клиентов?"   | "Не знаю, откуда они приходят"         | 😕 Worried (-1)  | Нет marketing insights | Acquisition channels tracking |

**Future State:** 😊 Satisfied (+2), видит все метрики в dashboard

---

## 🎯 Key Opportunities Identified

### 1. **For Clients (Мария, Елена)**

- ✅ Transparent информация (рейтинги, цены, портфолио)
- ✅ Быстрая запись (2 минуты vs 3 часов)
- ✅ Real-time availability (видеть слоты "здесь и сейчас")
- ✅ Автоматические reminders (не забыть о записи)
- ✅ Re-booking в один клик

### 2. **For Providers (Анна)**

- ✅ Автоматизация записи (без хаоса WhatsApp)
- ✅ Analytics dashboard (доход, популярные услуги)
- ✅ Reminders для клиентов (снизить no-show)
- ✅ Waitlist для слотов (заполнить пустые места)
- ✅ Late cancellation policy (защита от отмен)

### 3. **Cross-Cutting**

- ✅ Геолокация (найти рядом)
- ✅ Push-уведомления (оповещения в реальном времени)
- ✅ Система рейтингов (доверие + качество)
- ✅ Онлайн-оплата (удобство)

---

## 📈 Next Steps

1. **Validate hypotheses** - интервью с реальными пользователями
2. **Create wireframes** - визуализировать touchpoints
3. **Prioritize features** - основываясь на pain points
4. **Measure baseline** - замерить текущие метрики (если есть)

---

## 🔗 Related Documents

- **User Personas:** [`07_USER_PERSONAS.md`](./07_USER_PERSONAS.md) @./07_USER_PERSONAS.md
- **User Stories:** [`01_USER_STORIES.md`](./01_USER_STORIES.md) @./01_USER_STORIES.md
- **KPIs:** [`10_KPIS_AND_METRICS.md`](./10_KPIS_AND_METRICS.md) @./10_KPIS_AND_METRICS.md (создать)

---

**Последнее обновление:** 23.02.2026
**Автор:** Product Analyst
**Статус:** Draft - требует валидации с реальными пользователями
