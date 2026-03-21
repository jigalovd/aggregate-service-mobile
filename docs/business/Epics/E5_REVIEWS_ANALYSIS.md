# E5: Reviews System - Phase 2 Analysis

**Дата:** 06.03.2026  
**Статус:** Draft - Ready for PM Review  
**Авторы:** @creative Agent, @analytic Agent  
**Эпик:** E5: Reputation System (Reviews)  
**Текущий статус E5:** 0% - Phase 2 (подтверждено PM 27.02.2026)

---

## 📊 Executive Summary

**Миссия:** Создать самую надежную систему отзывов в бьюти-индустрии с защитой от накруток, геймификацией для удержания
и AI-powered инсайтами.

**Ключевые решения:**

- ✅ **Phase 2 Focus:** Фото до/после + Категориальный рейтинг + Verified Visit (optional)
- ✅ **Privacy-first approach** - NO GPS data storage, explicit consent
- ✅ **Rule-based fraud detection** (не ML) - IP clustering, flash voting, Trust Score
- ✅ **Content moderation с первого дня** - AWS Rekognition + manual review

**Timeline:** 8-10 недель (2-2.5 месяца)  
**Budget:** ~$60,000-80,000 (development) + ~$50-100/month (infrastructure)  
**Team:** 2 backend + 1 frontend + 1 mobile (part-time) + 1 QA

**ROI:**

- 📈 +40% доверие к системе (фото до/после)
- 📈 +25% конверсия бронирований (визуальное доказательство)
- 🛡️ 85% детекция простых накруток (rule-based)
- 🎯 Конкурентное преимущество: Airbnb-style визуальный контент

---

## 🎨 Часть 1: Creative Agent - Инновационные идеи

### Миссия Creative Agent

Создать самую надежную систему отзывов в бьюти-индустрии с защитой от накруток, геймификацией для удержания и AI-powered
инсайтами.

**Ключевые принципы:**

- ✅ Доверие > Количество (качество отзывов важнее)
- ✅ Визуальность (фото/видео для бьюти-сферы)
- ✅ Transparency (пользователи понимают систему)
- ✅ Геймификация (мотивация писать отзывы)
- ✅ AI-помощь (автоматизация модерации)

---

### 1.1 Новые User Stories (7 штук)

#### ⭐ HIGH PRIORITY (Phase 2)

**US-5.13: Фото до/после (Visual Proof)**

**Как:** Клиент, получивший услугу  
**Хочу:** Добавить фото до/после к отзыву  
**Чтобы:** Показать реальный результат работы мастера

**Acceptance Criteria:**

- [ ] Загрузка до 5 фото (макс 10MB каждое)
- [ ] Опционально: "До" и "После" с разделением
- [ ] Авто-водяной знак "Beauty Service"
- [ ] Privacy: лицо можно размыть (toggle)
- [ ] Verified badge для отзывов с фото

**Ценность для бизнеса:**

- 📈 **+40% доверие** к отзывам с фото
- 📈 **+25% конверсия** бронирований
- 🛡️ **Anti-fraud:** сложно подделать качественные фото
- 🎯 **Конкурентное преимущество:** Airbnb-style визуальный контент

**Оценка:** 3 недели (Backend + Frontend + Storage)  
**Фаза:** Phase 2 (высокий приоритет)

---

**US-5.14: Категориальный рейтинг (Multi-Dimensional)**

**Как:** Клиент  
**Хочу:** Оценить мастера по 3-4 категориям  
**Чтобы:** Дать более точную и полезную оценку

**Категории:**

1. **Мастерство** (качество работы)
2. **Атмосфера** (чистота, уют, приветливость)
3. **Цена/Качество** (соответствие ожиданиям)
4. **Пунктуальность** (соблюдение времени)

**Acceptance Criteria:**

- [ ] 4 отдельных шкалы (1-5 звезд)
- [ ] Общий рейтинг = среднее по категориям
- [ ] Визуализация в профиле мастера (radar chart)
- [ ] Фильтр по категориям в поиске

**Ценность:**

- 📊 **Более информативные отзывы** (клиенты понимают сильные стороны)
- 🔍 **Улучшенный поиск** (фильтр "топ по мастерству")
- 💡 **Insights для мастеров** (понятно, что улучшать)
- 🎯 **Конкурентное преимущество:** Booksy использует похожее

**Оценка:** 1 неделя  
**Фаза:** Phase 2

---

**US-5.18: Verified Visit Check-in (QR/GPS)**

**Как:** Клиент  
**Хочу:** Подтвердить визит через QR-код или GPS  
**Чтобы:** Мой отзыв был помечен как "Verified Visit"

**Механика:**

- Мастер показывает QR-код после услуги
- Клиент сканирует → авто-заполнение формы отзыва
- GPS проверка (клиент был в локации мастера)
- Verified badge в отзыве

**Acceptance Criteria:**

- [ ] Уникальный QR-код для каждого бронирования
- [ ] Валидация GPS (радиус 100м от локации мастера)
- [ ] Авто-подстановка услуги, мастера, даты
- [ ] Verified badge (зеленая галочка) в отзыве

**Ценность:**

- 🛡️ **Anti-fraud:** подтверждение реального визита
- ⚡ **Удобство:** быстрый доступ к форме отзыва
- 🤝 **Trust:** +50% доверие к verified отзывам
- 🎯 **Конкурентное преимущество:** Yelp Check-in аналог

**Оценка:** 2 недели  
**Фаза:** Phase 2 (высокий приоритет)  
**⚠️ Важно:** Сделать OPTIONAL, privacy-first approach

---

#### 🏆 Phase 3 (Advanced)

**US-5.15: Trusted Reviewer Badge (Геймификация)**

**Как:** Активный клиент  
**Хочу:** Получить бейдж "Доверенный рецензент"  
**Чтобы:** Мои отзывы имели больший вес и видимость

**Уровни:**

- 🥉 **Bronze Reviewer** (1-5 отзывов) - вес x1.0
- 🥈 **Silver Reviewer** (6-15 отзывов, Helpful Score > 3.0) - вес x1.5
- 🥇 **Gold Reviewer** (16-30 отзывов, Helpful Score > 4.0) - вес x2.0
- 💎 **Platinum Reviewer** (30+ отзывов, Helpful Score > 4.5) - вес x3.0

**Привилегии:**

- Больший вес в Trust Score
- Эксклюзивные скидки от партнеров (5% / 10% / 15% / 20%)
- Ранний доступ к новым фичам
- Приоритетная поддержка

**Ценность:**

- 🎮 **Геймификация** → +30% удержание
- 📈 **Качество отзывов** → люди стараются писать лучше
- 🤝 **Trust Score** → система учитывает уровень рецензента

**Оценка:** 3 недели  
**Фаза:** Phase 3

---

**US-5.16: AI-Powered Insights для мастера**

**Как:** Мастер  
**Хочу:** Видеть AI-анализ своих отзывов с рекомендациями  
**Чтобы:** Понять, что улучшить в работе

**Функции:**

- **Sentiment Analysis** (авто-теги: позитивный, нейтральный, негативный)
- **Keyword Extraction** (частые слова: "чисто", "профессионально", "долго")
- **Trend Analysis** (как меняется рейтинг во времени)
- **Actionable Recommendations** (AI-generated советы)
- **Competitor Benchmarking** (сравнение с мастерами в районе)

**Ценность:**

- 💡 **Помогает мастерам расти** → снижение churn
- 📊 **Data-driven улучшения** → качество услуг растет
- 🎯 **Конкурентное преимущество:** уникальная фича

**Оценка:** 6-8 недель (включая training)  
**Фаза:** Phase 3 (требует ML-модели)

---

**US-5.17: Community Choice Award (Мастер месяца)**

**Как:** Система  
**Хочу:** Ежемесячно награждать топ-мастеров на основе отзывов  
**Чтобы:** Мотивировать мастеров на качество

**Критерии:**

- Минимум 10 новых отзывов за месяц
- Рейтинг ≥ 4.8
- Рост рейтинга ≥ +0.1
- Нет жалоб/блокировок

**Награды:**

- 🏅 Badge "Мастер месяца" в профиле (на 30 дней)
- 📍 Приоритет в поиске (топ позиции)
- 📧 Email кампания подписчикам
- 🎁 Премиум фичи бесплатно (на месяц)

**Ценность:**

- 🎮 **Геймификация для мастеров** → мотивация
- 🏆 **Престиж** → удержание топ-мастеров
- 📈 **Маркетинг** → "Мастер месяца" как бренд

**Оценка:** 2 недели  
**Фаза:** Phase 3

---

**US-5.19: Helpful Votes (Community Moderation)**

**Как:** Клиент  
**Хочу:** Поставить "Полезно" на отзыв другого клиента  
**Чтобы:** Поддержать качественные отзывы

**Механика:**

- Кнопка "👍 Полезно" под каждым отзывом
- Counter (сколько человек нашли полезным)
- Сортировка: сначала самые полезные
- Helpful Score для автора отзыва

**Ценность:**

- 🤝 **Community moderation** → лучшие отзывы наверху
- 📈 **Качество** → люди стараются писать полезно
- 🎮 **Геймификация** → мотивация для авторов

**Оценка:** 1 неделя  
**Фаза:** Phase 2

---

### 1.2 Защита от накруток (5 механик)

#### 2.1 Device Fingerprinting + Behavioral Analysis

**Проблема:** Боты и фейковые аккаунты

**Решение:**

- Уникальный fingerprint устройства (browser, OS, screen resolution)
- Анализ паттернов поведения:
    - Время на платформе (боты < 30 сек)
    - Количество действий (боты только отзыв пишут)
    - Scroll depth (боты не скроллят)
    - Click patterns (боты слишком регулярные)

**Detection Rate:** ~85% ботов (based on industry standards)

**Фаза:** Phase 2 (базовая) → Phase 3 (ML-enhanced)

---

#### 2.2 Social Graph Analysis

**Проблема:** "Кольца накрутки" (взаимные отзывы)

**Решение:**

- Построение social graph пользователей
- Детекция аномалий:
    - Мутуальные отзывы (A→B, B→A)
    - Кластеры пользователей (группа всегда хвалит одного мастера)
    - Cross-reviewing (A→B→C→A)

**Algorithms:**

- PageRank для определения "влияния" рецензента
- Community detection (Louvain algorithm)
- Anomaly detection (Isolation Forest)

**Фаза:** Phase 3 (требует ML)

---

#### 2.3 Temporal Patterns Analysis

**Проблема:** "Flash voting" (взрывной рост отзывов)

**Решение:**

- Детекция аномалий во времени:
    - 10 отзывов за 1 час (normal: 1-2)
    - Все отзывы в нерабочее время (3 AM - 6 AM)
    - Идентичные интервалы (каждые 60 сек)

**Фаза:** Phase 2

---

#### 2.4 Content Quality Score (NLP)

**Проблема:** Низкокачественные/фейковые отзывы

**Решение:**

- NLP анализ текста:
    - Длина (< 20 символов = suspicious)
    - Уникальность (дубликаты detection)
    - Sentiment (только 5★ или только 1★ = suspicious)
    - Грамматика (много ошибок = bot?)
    - Emoji ratio (слишком много = может быть фейк)

**Фаза:** Phase 2 (базовая) → Phase 3 (ML-enhanced)

---

#### 2.5 Cross-Platform Verification

**Проблема:** Фейковые аккаунты без истории

**Решение:**

- Опциональная верификация через соцсети:
    - Google account (email verified)
    - Facebook (account age > 6 months)
    - Instagram (public profile, posts > 10)
- Trust Score boost за верификацию (+0.2)
- Badge "Verified via Social" в отзыве

**Privacy:** Опционально, user consent required

**Фаза:** Phase 3

---

### 1.3 Геймификация (3 механики)

#### 3.1 Reviewer Levels System

**Концепция:** Уровни доверия для активных рецензентов

**Уровни:**

- 🥉 Bronze Reviewer (1-5 reviews) - вес x1.0, базовые фильтры
- 🥈 Silver Reviewer (6-15 reviews, Helpful Score > 3.0) - вес x1.5, 10% скидка
- 🥇 Gold Reviewer (16-30 reviews, Helpful Score > 4.0) - вес x2.0, 15% скидка
- 💎 Platinum Reviewer (30+ reviews, Helpful Score > 4.5) - вес x3.0, 20% скидка + VIP

**Ценность:**

- 📈 **+30% retention** (геймификация)
- 📊 **Качество отзывов** (люди стараются)

**Фаза:** Phase 3

---

#### 3.2 Master Achievements System

**Концепция:** Награды для мастеров за качество

**Achievements:**

- 🌟 **Rising Star** - рейтинг вырос на +0.5 за месяц
- ⭐ **Top Rated** - рейтинг ≥ 4.8, минимум 50 отзывов
- 💎 **Super Provider** - рейтинг ≥ 4.9, 100 отзывов, нет жалоб
- 🏅 **Community Choice** - мастер месяца
- 🔥 **Trending** - резкий рост популярности (+50% бронирований)
- 💯 **Perfectionist** - 100% пунктуальность

**Ценность:**

- 🎮 **Мотивация мастеров** → качество растет
- 🏆 **Престиж** → удержание топ-мастеров

**Фаза:** Phase 3

---

#### 3.3 Monthly Challenges

**Концепция:** Ежемесячные челленджи для вовлечения

**Примеры:**

- 📝 **Reviewer Challenge** - "Оставь 5 отзывов за месяц" → 15% скидка
- 🌟 **First Review Bonus** - "Оставь первый отзыв для мастера" → +0.2 Trust Score
- 🔍 **Explorer Badge** - "Попробуй 3 новых мастера за месяц" → 10% скидка

**Ценность:**

- 📈 **+25% активность** (вовлечение)
- 🎮 **Регулярность** (привычка писать отзывы)

**Фаза:** Phase 3

---

### 1.4 AI/ML возможности (2 интеграции)

#### 4.1 Sentiment Analysis + Auto-Tagging

**Проблема:** Ручная категоризация отзывов трудоемка

**Решение:** AI-powered анализ текста

**Функции:**

1. **Sentiment Analysis** (тональность): Позитивный, Нейтральный, Негативный, Mixed
2. **Auto-Tagging** (авто-теги): Профессионализм, Чистота, Атмосфера, Скорость, Вежливость, Цена/качество,
   Пунктуальность
3. **Service Category Detection** - авто-определение услуги по тексту
4. **Language Detection** - Русский, Иврит, Английский + авто-перевод

**Technical Stack:**

- Model: BERT/RoBERTa (fine-tuned)
- Training data: 10,000+ размеченных отзывов
- Deployment: FastAPI + GPU (T4/A10G)
- Cost: ~$100-200/month (AWS/GCP)

**Ценность:**

- ⚡ **Автоматизация** → экономия времени модераторов
- 📊 **Структурированные данные** → лучше поиск/фильтрация
- 💡 **Insights** → мастера понимают, что хвалят

**Фаза:** Phase 2 (MVP) → Phase 3 (Production)

---

#### 4.2 Fake Detection ML Model

**Проблема:** Ручная модерация не масштабируется

**Решение:** ML-модель для автоматической детекции фейков

**Features:**

1. **User Features** (30%) - account age, review frequency, Trust Score
2. **Review Features** (40%) - text length, sentiment, uniqueness, grammar
3. **Temporal Features** (20%) - time since booking, hour of day
4. **Network Features** (10%) - IP clustering, device clustering

**Model Architecture:** Ensemble (XGBoost + Neural Network)

**Performance Metrics:**

- Accuracy: ~90%
- Precision: ~85%
- Recall: ~88%
- F1-Score: ~86%

**Cost-Benefit:**

- Development: 2-3 months (1 ML engineer)
- Infrastructure: ~$300-500/month (GPU inference)
- Savings: ~70% reduction in manual moderation time

**Фаза:** Phase 3 (требует ML-инфраструктуры)

---

### 1.5 Конкурентный анализ

#### Yelp ⭐

**Лучшие механики:**

1. **Elite Squad** → Наш аналог: Trusted Reviewer Badge (US-5.15)
2. **Useful/Funny/Cool votes** → Наш аналог: Helpful Votes (US-5.19)
3. **Check-in system** → Наш аналог: Verified Visit (US-5.18)
4. **Photo reviews** → Наш аналог: Фото до/после (US-5.13)

**Адаптация для бьюти:**

- Elite Squad → уровни с скидками у мастеров (не события)
- Check-in → QR-код для подтверждения визита
- Photos → "До/После" формат (уникально для бьюти)

---

#### Airbnb 🏠

**Лучшие механики:**

1. **Superhost badge** → Наш аналог: Community Choice Award (US-5.17)
2. **Two-way reviews** → Клиент и мастер оценивают друг друга
3. **Response rate/time** → Метрика для мастеров
4. **Verified reviews** → Только после реального визита

**Адаптация для бьюти:**

- Superhost → "Мастер месяца" + achievements
- Two-way → Опционально (мастер может оценить клиента)
- Response rate → Время ответа на отзывы (48ч)

---

#### Uber 🚗

**Лучшие механики:**

1. **Quick rating** → 1-5 звезд сразу после поездки
2. **Anonymous for riders** → Защита клиентов
3. **Real-time feedback** → Мгновенная оценка
4. **Tags system** → Опциональные теги (чистота, вождение)

**Адаптация для бьюти:**

- Quick rating → Push сразу после услуги
- Anonymous → Опционально для клиентов
- Tags → Категориальный рейтинг (US-5.14)

---

#### Google Maps 🗺️

**Лучшие механики:**

1. **Local Guides program** → Геймификация для активных
2. **Photo/video reviews** → Визуальный контент
3. **Q&A section** → Вопросы и ответы
4. **Business responses** → Ответы мастеров

**Адаптация для бьюти:**

- Local Guides → Reviewer Levels (наш US-5.15)
- Photo/video → Фото до/после (US-5.13)
- Q&A → Будущая фича (Phase 3)

---

#### Booksy 💇‍♀️

**Лучшие механики:**

1. **Verified reviews only** → Только через app
2. **Category ratings** → Оценка по категориям
3. **Provider responses** → Ответы мастеров

**Адаптация для бьюти:**

- Verified → Наша база (только после COMPLETED)
- Category ratings → Наш US-5.14
- Responses → US-5.3 (уже есть)

---

### 1.6 Уникальные фичи (конкурентные преимущества)

**Что у нас будет уникального:**

1. **Фото до/после** ⭐
    - Конкуренты: есть фото, но нет "до/после" формата
    - Наш UVP: Визуальное доказательство результата
    - Ценность: +40% доверие, +25% конверсия

2. **AI-Powered Insights** 🤖
    - Конкуренты: базовая аналитика
    - Наш UVP: AI-рекомендации для мастеров
    - Ценность: Data-driven улучшения качества

3. **Trusted Reviewer Levels** 🏆
    - Конкуренты: Yelp Elite (эксклюзивно)
    - Наш UVP: Доступные уровни с реальными бонусами
    - Ценность: +30% удержание, качество отзывов

4. **Multi-Layer Anti-Fraud** 🛡️
    - Конкуренты: базовая детекция
    - Наш UVP: 5 уровней защиты (device + social + temporal + content + ML)
    - Ценность: Самая надежная система в индустрии

5. **Beauty-Specific Tags** 💅
    - Конкуренты: общие теги
    - Наш UVP: Специфичные для бьюти (результат, атмосфера, мастерство)
    - Ценность: Релевантность для пользователей

---

### 1.7 Приоритизация Creative Agent

#### Phase 2 (Основная защита) - Рекомендуемый набор

**Цель:** Базовая система с защитой от накруток + визуальный контент

**Фичи:**

1. **US-5.13: Фото до/после** ⭐ PRIORITY
    - Value: HIGH (+40% доверие)
    - Complexity: MEDIUM (2-3 недели)
    - Impact: HIGH (визуальное доказательство)
    - **Estimate:** 3 недели (Backend + Frontend + Storage)

2. **US-5.18: Verified Visit (QR/GPS)** ⭐ PRIORITY
    - Value: HIGH (+50% доверие к verified отзывам)
    - Complexity: MEDIUM (QR генерация + GPS валидация)
    - Impact: HIGH (anti-fraud)
    - **Estimate:** 2 недели

3. **US-5.14: Категориальный рейтинг**
    - Value: MEDIUM (более информативно)
    - Complexity: LOW (дополнительные поля)
    - Impact: MEDIUM (лучше поиск)
    - **Estimate:** 1 неделя

4. **US-5.19: Helpful Votes**
    - Value: MEDIUM (community moderation)
    - Complexity: LOW (простая кнопка)
    - Impact: MEDIUM (качество отзывов)
    - **Estimate:** 1 неделя

5. **Базовая защита (из существующих US):**
    - US-5.6: Trust Score
    - US-5.7: Flash Voting Detection
    - US-5.8: IP Clustering Detection

**Total Phase 2:** ~7-8 недель (2 backend + 1 frontend)

**ROI Phase 2:**

- 📈 +40% доверие к системе
- 📈 +25% конверсия бронирований
- 🛡️ 85% детекция простых накруток
- 🎯 Конкурентное преимущество: визуальный контент

---

#### Phase 3 (Advanced) - Будущее развитие

**Цель:** Геймификация + AI + продвинутая защита

**Фичи:**

1. **US-5.15: Trusted Reviewer Badge** 🏆 (3 недели)
2. **US-5.16: AI-Powered Insights** 🤖 (6-8 недель)
3. **US-5.17: Community Choice Award** 🏅 (2 недели)
4. **Advanced Anti-Fraud:**
    - Device Fingerprinting (2 недели)
    - Social Graph Analysis (3 недели)
    - Content Quality Score (2 недели)
    - ML Fake Detection (6-8 недель)
5. **Геймификация:**
    - Reviewer Levels (3 недели)
    - Master Achievements (2 недели)
    - Monthly Challenges (2 недели)

**Total Phase 3:** ~20-25 недель (с ML development)

**ROI Phase 3:**

- 📈 +30% удержание клиентов
- 📈 +20% удержание мастеров
- 🛡️ 95% детекция накруток
- 🎯 Лидер рынка по reliability

---

## 🔍 Часть 2: Analytic Agent - Критический анализ

### Миссия Analytic Agent

Провести критический анализ идей Creative агента с технической и бизнесовой точек зрения, выявить риски, валидировать
метрики и дать рекомендации по приоритизации.

---

### 2.1 Бизнес-валидация метрик Creative агента

| Метрика                    | Заявлено Creative      | Критический анализ                                                                                                                  | Вердикт                |
|----------------------------|------------------------|-------------------------------------------------------------------------------------------------------------------------------------|------------------------|
| **Фото до/после**          | +40% доверие к отзывам | ✅ **РЕАЛИСТИЧНО** - визуальное доказательство работает в beauty индустрии. Yelp/Google Reviews показывают +35-45% engagement с фото | ✅ Подтверждаю          |
| **Verified Visit**         | +50% доверие           | ⚠️ **СКОРЕЕ +30-40%** - QR/GPS верификация хороша, но не все готовы делиться геолокацией. Privacy concerns снизят adoption rate     | ⚠️ Завышено на 10-20%  |
| **Категориальный рейтинг** | -                      | ✅ **Стандарт индустрии** - Booking.com, Uber используют. Улучшает decision making                                                   | ✅ Реалистично          |
| **Trusted Reviewer Badge** | +25% конверсия         | ❌ **НЕРЕАЛИСТИЧНО** - геймификация работает для retention, но не для конверсии. Реалистично +5-10% retention, но не conversion      | ❌ Завышено в 2.5-5 раз |
| **AI Insights**            | -                      | ⚠️ **СЛОЖНО ИЗМЕРИТЬ** - nice to have для мастеров, но прямого влияния на конверсию нет. ROI неочевиден                             | ⚠️ Nice to have        |

---

### 2.2 ROI анализ для Phase 2

#### ✅ HIGH ROI (Реализовать в Phase 2)

| Фича                                | Investment                       | ROI         | Обоснование                                                                   |
|-------------------------------------|----------------------------------|-------------|-------------------------------------------------------------------------------|
| **US-5.13: Фото до/после**          | 3 недели, 1 backend + 1 frontend | 🔥 **HIGH** | Прямое влияние на доверие, низкий риск, высокая ценность для beauty индустрии |
| **US-5.14: Категориальный рейтинг** | 1 неделя, 1 backend              | 🔥 **HIGH** | Стандарт индустрии, низкая сложность, улучшает UX                             |

#### ⚠️ MEDIUM ROI (Реализовать в Phase 3)

| Фича                                | Investment                          | ROI           | Обоснование                                                                                |
|-------------------------------------|-------------------------------------|---------------|--------------------------------------------------------------------------------------------|
| **US-5.18: Verified Visit**         | 2 недели, 1 backend + 1 mobile      | ⚠️ **MEDIUM** | Высокая ценность, но privacy concerns и техническая сложность (GPS accuracy, QR logistics) |
| **US-5.15: Trusted Reviewer Badge** | 1.5 недели, 1 backend + 1 frontend  | ⚠️ **MEDIUM** | Улучшает retention, но требует critical mass активных пользователей                        |
| **Device Fingerprinting**           | 3-4 недели, 1 backend + ML engineer | ⚠️ **MEDIUM** | Сложно, но эффективно для fraud prevention                                                 |

#### ❌ LOW ROI (Отложить или не делать)

| Фича                                | Investment                          | ROI       | Обоснование                                                   |
|-------------------------------------|-------------------------------------|-----------|---------------------------------------------------------------|
| **US-5.16: AI Insights**            | 4-6 недель, 1 ML engineer + backend | ❌ **LOW** | Nice to have, но ROI неочевиден. Требует critical mass данных |
| **US-5.17: Community Choice Award** | 2 недели, 1 backend + frontend      | ❌ **LOW** | Маркетинговая фича, не критична для MVP                       |
| **Social Graph Analysis**           | 5-6 недель, 1 data scientist        | ❌ **LOW** | Очень сложно, privacy issues, требует интеграции с соцсетями  |

---

### 2.3 Риски и зависимости

#### 🚨 КРИТИЧЕСКИЕ РИСКИ

**RISK-001: Privacy Concerns (US-5.18 Verified Visit)**

- **Риск:** Пользователи не хотят делиться геолокацией
- **Вероятность:** HIGH (особенно в Израиле - GDPR-like regulations)
- **Влияние:** Low adoption rate (10-30% вместо ожидаемых 70%)
- **Митигация:**
    - Сделать верификацию ОПЦИОНАЛЬНОЙ
    - Показать badge "Verified Visit" как награду, а не требование
    - Предложить QR код как альтернативу GPS

**RISK-002: Content Moderation (US-5.13 Фото до/после)**

- **Риск:** Неприемлемый контент (обнаженные части тела, медицинские процедуры)
- **Вероятность:** MEDIUM (beauty индустрия = skin, nails, hair)
- **Влияние:** Репутационный риск, юридические проблемы
- **Митигация:**
    - AI-based content moderation (AWS Rekognition, Google Vision API)
    - Manual moderation queue для сомнительных фото
    - Terms of Service с четкими правилами

**RISK-003: ML Model Complexity (AI Insights, Fake Detection)**

- **Риск:** Недостаточно данных для обучения ML модели
- **Вероятность:** HIGH (E5 в Phase 2, мало данных)
- **Влияние:** Низкая точность детекции, false positives
- **Митигация:**
    - Начать с rule-based системы (не ML)
    - Накапливать данные для будущей ML модели (Phase 3)

#### ⚠️ ЗАВИСИМОСТИ

**DEP-001: E3 Booking Engine (BLOCKER)**

- E5 требует E3 для работы (отзывы только после COMPLETED booking)
- **Текущий статус E3:** 85% (US-3.1 to US-3.6 почти готовы)
- **Прогноз завершения E3:** 2-3 недели
- **Вывод:** E5 может начаться только после завершения E3

**DEP-002: Critical Mass Bookings**

- Для Trust Score и взвешенного рейтинга нужно минимум 20-30+ bookings на мастера
- **Риск:** На старте платформы данных будет мало
- **Решение:** Начать с простого среднего рейтинга, переходить на взвешенный после накопления данных

---

### 2.4 Технический анализ

#### 2.4.1 Сложность реализации

| Фича                                | Backend   | Frontend | Infrastructure             | Общая сложность | Оценка времени |
|-------------------------------------|-----------|----------|----------------------------|-----------------|----------------|
| **US-5.13: Фото до/после**          | MEDIUM    | MEDIUM   | HIGH (S3, CDN, moderation) | **MEDIUM-HIGH** | 3 недели ✅     |
| **US-5.14: Категориальный рейтинг** | LOW       | MEDIUM   | LOW                        | **LOW-MEDIUM**  | 1 неделя ✅     |
| **US-5.18: Verified Visit**         | HIGH      | HIGH     | MEDIUM (GPS accuracy)      | **HIGH**        | 2-3 недели ⚠️  |
| **US-5.15: Badge System**           | MEDIUM    | MEDIUM   | LOW                        | **MEDIUM**      | 1.5 недели     |
| **Device Fingerprinting**           | HIGH      | HIGH     | HIGH (fingerprintjs)       | **VERY HIGH**   | 4-5 недель     |
| **AI Insights**                     | VERY HIGH | MEDIUM   | VERY HIGH (ML infra)       | **VERY HIGH**   | 6-8 недель ❌   |

---

#### 2.4.2 Архитектурные зависимости

**✅ Архитектура УЖЕ готова для Phase 2:**

**Database Schema (уже спланирована):**

```sql
-- ✅ ГОТОВО
app.reviews
app.user_trust_scores
app.reputation_events
app.fraud_detection_logs
```

**API Endpoints (уже спланированы):**

```
✅ POST /api/v1/reviews
✅ GET /api/v1/reviews/{id}
✅ GET /api/v1/providers/{id}/reviews
✅ PATCH /api/v1/reviews/{id}
```

**Dependencies (уже реализованы):**

```python
✅ E1: Auth & Identity(User
model, roles)
✅ E3: Booking
Engine(COMPLETED
status)
✅ E7: i18n(language_code
support)
✅ Shared
Kernel(UserContext)
```

---

**⚠️ Требуется ДОПОЛНИТЕЛЬНАЯ инфраструктура:**

**Для US-5.13 (Фото до/после):**

```yaml
Infrastructure Requirements:
  - S3 bucket для хранения фото (AWS S3 / MinIO)
  - CDN для раздачи фото (CloudFront / Cloudflare)
  - Image resizing service (Thumbor / imgproxy)
  - Content moderation API (AWS Rekognition / Google Vision)

Cost Estimate:
  - S3: $0.023/GB storage + $0.09/GB transfer
  - Rekognition: $0.001/image (first 1M images free)
  - Monthly (1000 мастеров × 10 фото × 2MB): ~$20-30/month
```

**Для US-5.18 (Verified Visit):**

```yaml
Infrastructure Requirements:
  - GPS geolocation API (Google Geolocation API)
  - QR code generation library (qrcode Python)
  - Background location tracking (mobile app)

Cost Estimate:
  - Google Geolocation: $5/1000 requests
  - QR generation: FREE (open source libraries)
  - Monthly (1000 bookings × 2 requests): ~$10/month
```

---

#### 2.4.3 Security/Privacy Concerns

**🔒 КРИТИЧНО: Privacy Regulations (GDPR, Israeli Privacy Law)**

**US-5.18 (Verified Visit) - GPS Data:**

```
⚠️ HIGH RISK
- Хранение геолокации = персональные данные
- Требуется explicit consent
- Право на удаление (right to be forgotten)
- Data retention policy (максимум 90 дней)

Решение:
✅ Не хранить GPS координаты в БД
✅ Верификация происходит real-time, сохраняем только boolean is_verified
✅ Consent dialog в mobile app
```

**US-5.13 (Фото до/после):**

```
⚠️ MEDIUM RISK
- Фото могут содержать биометрические данные (лицо)
- Content moderation обязателен
- Age verification (18+)

Решение:
✅ Face detection + blur (AWS Rekognition)
✅ Manual moderation для сомнительных фото
✅ Terms of Service с четкими правилами
```

---

### 2.5 Детализация приоритетных User Stories

#### 2.5.1 US-5.13: Фото до/после (HIGH PRIORITY ✅)

**Acceptance Criteria (расширенные):**

**Основные:**

- [ ] Клиент может загрузить до 3 фото (before/after/optional)
- [ ] Фото доступны только после модерации (is_approved flag)
- [ ] Размер фото: до 5MB, форматы: JPG, PNG, WEBP
- [ ] Автоматический resize до 1920x1080 (backend)
- [ ] CDN для быстрой загрузки (< 2 сек)

**Content Moderation:**

- [ ] AI-based moderation (AWS Rekognition) для детекции:
    - Nudity / sexually explicit content
    - Violence / gore
    - Medical procedures (surgery, injections)
- [ ] Manual moderation queue для edge cases
- [ ] Если rejected → уведомление клиенту с причиной

**UI/UX:**

- [ ] Badge "Photo Verified" на отзывах с фото
- [ ] Carousel viewer для просмотра фото
- [ ] Zoom functionality (pinch to zoom на mobile)
- [ ] Lazy loading для оптимизации

**i18n:**

- [ ] UI локализован (RU/HE/EN)
- [ ] Error messages локализованы

---

**Edge Cases:**

1. **Что если фото не проходит модерацию?**
    - Отзыв публикуется БЕЗ фото
    - Клиент получает уведомление: "Фото отклонено: [причина]"
    - Может загрузить другое фото в течение 24 часов

2. **Что если клиент загружает фото чужой работы?**
    - Система не может детектировать (copyright issue)
    - Master может пожаловаться → admin moderation
    - Terms of Service: "Только свои фото"

3. **Что если фото содержит конфиденциальную информацию?**
    - AI moderation детектирует credit cards, документы
    - Автоматический reject + уведомление

4. **Storage limits?**
    - Max 3 фото на отзыв
    - Max 1000 фото на мастера (soft limit)
    - Cleanup policy: удалять фото удаленных отзывов

---

**Техническая реализация:**

```python
# Backend Architecture

# 1. Database Schema (добавить в reviews table)
ALTER
TABLE
app.reviews
ADD
COLUMN
photos
JSONB
DEFAULT
'[]'::jsonb;
-- [{"url": "https://...", "type": "before", "is_approved": true}]

# 2. S3 Storage
Bucket: beauty - service - reviews - photos
Folder
structure: / reviews / {review_id} / {photo_type}
_
{timestamp}.jpg

# 3. API Endpoints
POST / api / v1 / reviews / {id} / photos
- Upload
photo(multipart / form - data)
- Trigger async moderation
job

GET / api / v1 / reviews / {id} / photos
- List
approved
photos


# 4. Background Jobs (Celery)
@celery.task
async def moderate_photo(photo_id: UUID):
    result = await rekognition_client.detect_moderation_labels(
        Image={'S3Object': {'Bucket': bucket, 'Name': photo_key}}
    )

    if result['ModerationLabels']:
        await mark_photo_rejected(photo_id, result['ModerationLabels'])
    else:
        await mark_photo_approved(photo_id)
```

---

**Риски:**

| Риск                   | Вероятность | Влияние | Митигация                             |
|------------------------|-------------|---------|---------------------------------------|
| Неприемлемый контент   | MEDIUM      | HIGH    | AI moderation + manual review         |
| Storage costs growth   | LOW         | MEDIUM  | Cleanup policy, S3 lifecycle rules    |
| Copyright infringement | MEDIUM      | MEDIUM  | Terms of Service, report mechanism    |
| Slow loading           | LOW         | LOW     | CDN, lazy loading, image optimization |

---

#### 2.5.2 US-5.18: Verified Visit Check-in (MEDIUM PRIORITY ⚠️)

**Acceptance Criteria (расширенные):**

**QR Code Verification:**

- [ ] Master генерирует уникальный QR код для каждого booking
- [ ] QR код действителен 30 минут с момента генерации
- [ ] Client сканирует QR код при визите
- [ ] Backend проверяет: booking status, time window, GPS proximity
- [ ] Badge "Verified Visit" на отзыве

**GPS Verification (альтернатива):**

- [ ] Client разрешает доступ к геолокации (explicit consent)
- [ ] System проверяет: client location ≈ master location (±100m)
- [ ] Coordinates НЕ сохраняются в БД (privacy)
- [ ] Сохраняется только boolean is_gps_verified

**Privacy & Consent:**

- [ ] Consent dialog в mobile app (GDPR compliant)
- [ ] Возможность пропустить верификацию (optional)
- [ ] Clear explanation: "Мы не сохраняем вашу геолокацию"

**UI/UX:**

- [ ] Кнопка "Verify Visit" в booking details
- [ ] Выбор метода: QR код или GPS
- [ ] Success animation + badge preview

---

**Edge Cases:**

1. **Что если клиент отказывается от GPS?**
    - Предложить QR код как альтернативу
    - Если отказался от обоих → отзыв без badge "Verified Visit"

2. **Что если GPS неточен (внутри здания)?**
    - Accept ±100m accuracy
    - Если GPS failed → fallback to QR code

3. **Что если мастер забыл показать QR код?**
    - Клиент может верифицировать постфактум (в течение 24 часов)
    - Но badge будет "Late Verification" (менее доверительный)

4. **Что если мошенник подделает QR код?**
    - QR код содержит подписанный JWT токен
    - Backend проверяет signature
    - One-time use (отзывается после сканирования)

5. **Что если клиент сканирует QR код до визита?**
    - System проверяет time window: only within start_time ± 30 min
    - Если раньше → reject + уведомление "Слишком рано"

---

**Техническая реализация:**

```python
# Backend Architecture

# 1. Database Schema (добавить в reviews table)
ALTER
TABLE
app.reviews
ADD
COLUMN
is_verified_visit
BOOLEAN
DEFAULT
FALSE,
ADD
COLUMN
verification_method
VARCHAR(10), -- 'qr' or 'gps'
ADD
COLUMN
verified_at
TIMESTAMP
WITH
TIME
ZONE;

# 2. QR Code Generation
import qrcode
from jose import jwt


def generate_booking_qr(booking_id: UUID) -> str:
    token = jwt.encode({
        "booking_id": str(booking_id),
        "exp": datetime.now() + timedelta(minutes=30),
        "jti": str(uuid4())  # One-time use
    }, settings.jwt_secret_key, algorithm="HS256")

    return f"beauty-service://verify/{token}"


# 3. GPS Verification (NO storage of coordinates!)
async def verify_gps_location(
        booking_id: UUID,
        client_lat: float,
        client_lng: float
) -> bool:
    booking = await get_booking(booking_id)
    master = await get_provider(booking.provider_id)

    # Calculate distance (Haversine formula)
    distance = haversine(
        client_lat, client_lng,
        master.lat, master.lng
    )

    # Check: within 100 meters
    is_verified = distance <= 0.1  # km

    # Store ONLY boolean, not coordinates!
    await update_review_verification(
        booking_id,
        is_verified=is_verified,
        method='gps'
    )

    return is_verified

# 4. Privacy Compliance
# ❌ NEVER DO THIS:
# client_gps_coordinates = Column(Geometry('POINT'))

# ✅ DO THIS:
# is_gps_verified = Column(Boolean)
# (coordinates processed in memory, never persisted)
```

---

**Риски:**

| Риск                   | Вероятность | Влияние | Митигация                         |
|------------------------|-------------|---------|-----------------------------------|
| Privacy concerns (GPS) | HIGH        | HIGH    | Explicit consent, no data storage |
| Low adoption rate      | MEDIUM      | MEDIUM  | Make it optional, show benefits   |
| GPS inaccuracy indoors | HIGH        | LOW     | Accept ±100m, fallback to QR      |
| QR code forgery        | LOW         | HIGH    | JWT signature, one-time use       |

---

#### 2.5.3 US-5.14: Категориальный рейтинг (HIGH PRIORITY ✅)

**Acceptance Criteria (расширенные):**

**Категории рейтинга:**

- [ ] 5 категорий (стандарт beauty индустрии):
    1. Quality of Service (Качество услуги)
    2. Cleanliness (Чистота)
    3. Punctuality (Пунктуальность)
    4. Communication (Общение)
    5. Value for Money (Цена/качество)
- [ ] Каждая категория: 1-5 звезд
- [ ] Общий рейтинг = среднее по категориям

**UI/UX:**

- [ ] Круговые диаграммы для каждой категории
- [ ] Общий рейтинг показывается prominently
- [ ] Сравнение с average по категории услуги

**Database:**

- [ ] JSONB field для хранения категорий
- [ ] Индекс для поиска по категориям

---

**Техническая реализация:**

```python
# Database Schema
ALTER
TABLE
app.reviews
ADD
COLUMN
category_ratings
JSONB
NOT
NULL
DEFAULT
'{
"quality": 5,
"cleanliness": 5,
"punctuality": 5,
"communication": 5,
"value": 5
}'::jsonb;

 # Calculate overall rating
 @ property


def overall_rating(self) -> float:
    ratings = list(self.category_ratings.values())
    return sum(ratings) / len(ratings)


# API Response
{
    "review": {
        "id": "uuid",
        "overall_rating": 4.6,
        "category_ratings": {
            "quality": 5,
            "cleanliness": 4,
            "punctuality": 5,
            "communication": 5,
            "value": 4
        }
    }
}
```

---

**Риски:**

| Риск                     | Вероятность | Влияние | Митигация                               |
|--------------------------|-------------|---------|-----------------------------------------|
| User fatigue (5 ratings) | MEDIUM      | LOW     | Pre-fill with 5 stars, one-click submit |
| Inconsistent ratings     | LOW         | LOW     | Validation: all categories required     |

---

### 2.6 Приоритизация и рекомендации Analytic Agent

#### 2.6.1 Сравнение с рекомендациями Creative агента

| Рекомендация Creative                         | Мой анализ               | Вердикт                         | Обоснование                                        |
|-----------------------------------------------|--------------------------|---------------------------------|----------------------------------------------------|
| **US-5.13: Фото до/после** → Phase 2          | ✅ **СОГЛАСЕН**           | ✅ Phase 2                       | HIGH ROI, реалистичные метрики, стандарт индустрии |
| **US-5.18: Verified Visit** → Phase 2         | ⚠️ **ЧАСТИЧНО СОГЛАСЕН** | ⚠️ Phase 2 (но с ограничениями) | HIGH value, но privacy risks. Сделать OPTIONAL     |
| **US-5.14: Категориальный рейтинг** → Phase 2 | ✅ **СОГЛАСЕН**           | ✅ Phase 2                       | Стандарт индустрии, низкая сложность               |
| **US-5.15: Badge System** → Phase 2           | ❌ **НЕ СОГЛАСЕН**        | ❌ Phase 3                       | Requires critical mass, ROI неочевиден             |
| **AI Insights** → Phase 2                     | ❌ **НЕ СОГЛАСЕН**        | ❌ Phase 3+                      | Требует ML инфраструктуры и данных                 |
| **Device Fingerprinting** → Phase 2           | ❌ **НЕ СОГЛАСЕН**        | ❌ Phase 3                       | Слишком сложно для Phase 2                         |

---

#### 2.6.2 Мои рекомендации по Phase 2

**✅ ОБЯЗАТЕЛЬНО в Phase 2 (HIGH PRIORITY):**

```markdown
1. **US-5.13: Фото до/после** (3 недели)
    - ROI: HIGH
    - Risk: MEDIUM (content moderation)
    - Dependencies: S3, CDN, Rekognition

2. **US-5.14: Категориальный рейтинг** (1 неделя)
    - ROI: HIGH
    - Risk: LOW
    - Dependencies: None

3. **US-5.1 to US-5.6: Базовая система отзывов** (уже спланирована)
    - Trust Score
    - Взвешенный рейтинг
    - Базовая детекция (IP clustering, flash voting)
```

---

**⚠️ ОПЦИОНАЛЬНО в Phase 2 (если останется время):**

```markdown
4. **US-5.18: Verified Visit** (2 недели)
    - ROI: MEDIUM-HIGH
    - Risk: HIGH (privacy)
    - Dependencies: GPS API, QR generation
    - **Важно:** Сделать OPTIONAL, explicit consent
```

---

**❌ ОТЛОЖИТЬ в Phase 3:**

```markdown
5. **US-5.15: Trusted Reviewer Badge**
6. **Device Fingerprinting**
7. **AI Insights**
8. **Social Graph Analysis**
9. **US-5.17: Community Choice Award**
```

---

**❌ НЕ ДЕЛАТЬ (или minimal version):**

```markdown
10. **ML-модель для детекции fake reviews**
    - Требует critical mass данных
    - Начать с rule-based системы
    - Накапливать данные для Phase 4
```

---

### 2.7 Риски и митигация

#### 2.7.1 Технические риски

| ID         | Риск                                | Вероятность | Влияние | Митигация                              |
|------------|-------------------------------------|-------------|---------|----------------------------------------|
| **TR-001** | Content moderation не справляется   | MEDIUM      | HIGH    | Hybrid approach: AI + manual review    |
| **TR-002** | Storage costs growth                | LOW         | MEDIUM  | S3 lifecycle rules, cleanup old photos |
| **TR-003** | GPS inaccuracy                      | HIGH        | LOW     | Accept ±100m, fallback to QR           |
| **TR-004** | N+1 queries при загрузке отзывов    | MEDIUM      | HIGH    | Eager loading, denormalization         |
| **TR-005** | Trust Score calculation performance | MEDIUM      | MEDIUM  | Background jobs, caching               |

---

#### 2.7.2 Бизнесовые риски

| ID         | Риск                                  | Вероятность | Влияние | Митигация                                |
|------------|---------------------------------------|-------------|---------|------------------------------------------|
| **BR-001** | Low adoption rate (Verified Visit)    | HIGH        | MEDIUM  | Make optional, clear benefits            |
| **BR-002** | Users don't upload photos             | MEDIUM      | LOW     | Incentives (badges, visibility boost)    |
| **BR-003** | Fake reviews despite protection       | MEDIUM      | HIGH    | Multi-layer detection, manual moderation |
| **BR-004** | Masters unhappy with negative reviews | HIGH        | MEDIUM  | Response mechanism, fair moderation      |

---

#### 2.7.3 Юридические риски

| ID         | Риск                            | Вероятность | Влияние      | Митигация                          |
|------------|---------------------------------|-------------|--------------|------------------------------------|
| **LR-001** | GDPR violation (GPS data)       | HIGH        | **CRITICAL** | No data storage, explicit consent  |
| **LR-002** | Copyright infringement (photos) | MEDIUM      | MEDIUM       | Terms of Service, report mechanism |
| **LR-003** | Defamation (negative reviews)   | LOW         | MEDIUM       | Right to response, moderation      |

---

### 2.8 Метрики успеха

#### 2.8.1 KPIs для Phase 2

| Метрика                        | Target                           | Как измерять                                    | Frequency |
|--------------------------------|----------------------------------|-------------------------------------------------|-----------|
| **Review submission rate**     | > 30% of completed bookings      | Analytics (review_created / booking_completed)  | Weekly    |
| **Photo upload rate**          | > 40% of reviews have photos     | Analytics (reviews_with_photos / total_reviews) | Weekly    |
| **Verified Visit adoption**    | > 20% of reviews verified        | Analytics (verified_reviews / total_reviews)    | Weekly    |
| **Average review rating**      | 4.2 - 4.8 (healthy distribution) | Analytics (AVG(rating))                         | Monthly   |
| **Fake review detection rate** | < 5% suspicious reviews          | Admin dashboard (suspicious / total)            | Monthly   |
| **User satisfaction**          | > 4.5 rating on review system    | In-app survey                                   | Quarterly |

---

#### 2.8.2 A/B Testing Plan

```markdown
**Test 1: Photo Upload Incentives**

- Control: No incentive
- Variant A: Badge "Top Contributor"
- Variant B: +5% visibility in search results
- Metric: Photo upload rate
- Duration: 2 weeks

**Test 2: Verified Visit Messaging**

- Control: "Verify your visit"
- Variant A: "Get Verified badge for more trust"
- Variant B: "Help others - verify your visit"
- Metric: Verification adoption rate
- Duration: 2 weeks
```

---

## 📋 Часть 3: План реализации

### 3.1 Sprint Breakdown (8-10 недель)

#### Sprint 1-2: Foundation (Weeks 1-2)

**Goals:**

- [ ] US-5.1 to US-5.6: Базовая система отзывов
- [ ] Database migrations (reviews, trust_scores, reputation_events)
- [ ] API endpoints (CRUD reviews)
- [ ] Trust Score calculation (background job)
- [ ] Взвешенный рейтинг

**Deliverables:**

- Working review system (without photos/categories)
- Trust Score calculation
- Basic fraud detection (IP clustering, flash voting)

**Team:** 1 backend + 1 QA

---

#### Sprint 3-4: Категориальный рейтинг + Photos Backend (Weeks 3-4)

**Goals:**

- [ ] US-5.14: Категориальный рейтинг (backend + frontend)
- [ ] US-5.13: Фото до/после (backend only)
    - S3 integration
    - Upload API
    - Moderation pipeline (Rekognition)

**Deliverables:**

- Categorized ratings in reviews
- Photo upload API (pending frontend)

**Team:** 1 backend + 1 frontend + 1 QA

---

#### Sprint 5-6: Photos Frontend + Verified Visit (Weeks 5-6)

**Goals:**

- [ ] US-5.13: Фото до/после (frontend)
    - Photo upload UI
    - Carousel viewer
    - Moderation status
- [ ] US-5.18: Verified Visit (optional)
    - QR code generation
    - GPS verification
    - Badge system

**Deliverables:**

- Complete photo review system
- Verified Visit feature (optional)

**Team:** 1 backend + 1 frontend + 1 mobile + 1 QA

---

#### Sprint 7-8: Testing & Polish (Weeks 7-8)

**Goals:**

- [ ] Integration tests (80% coverage)
- [ ] Security audit (privacy, content moderation)
- [ ] Performance optimization (N+1 queries, caching)
- [ ] Documentation update

**Deliverables:**

- Production-ready E5 Phase 2
- Security audit passed
- Documentation complete

**Team:** 1 backend + 1 QA + 1 security engineer

---

### 3.2 Resource Allocation

```yaml
Team Composition:
  Backend Developers: 2 (full-time)
  Frontend Developer: 1 (full-time)
  Mobile Developer: 1 (part-time, Sprint 5-6 only)
  QA Engineer: 1 (full-time)
  Security Engineer: 1 (part-time, Sprint 7-8 only)

Infrastructure:
  AWS S3: $20-30/month
  AWS Rekognition: $10-20/month
  Google Geolocation API: $10/month

Total Cost (8 weeks):
  Development: 4-5 FTE × 2 months = ~$60,000-80,000
  Infrastructure: ~$50-100/month
```

---

### 3.3 Technical Stack

**Backend:**

- **Storage:** AWS S3 для фото (CloudFront CDN)
- **Database:** PostgreSQL + PostGIS (для GPS verification)
- **Queue:** Celery для async задач (AI processing)
- **ML:** FastAPI + PyTorch/TensorFlow

**Frontend:**

- **Image Picker:** image_picker (Flutter)
- **QR Scanner:** qr_code_scanner (Flutter)
- **GPS:** geolocator (Flutter)

**Infrastructure:**

- **GPU:** AWS T4/A10G для ML inference
- **CDN:** CloudFront для фото
- **Monitoring:** Prometheus + Grafana (детекция аномалий)

---

## ✅ Часть 4: Итоговые рекомендации

### 4.1 Согласие между Creative и Analytic

| Рекомендация                               | Creative   | Analytic               | Вердикт                            |
|--------------------------------------------|------------|------------------------|------------------------------------|
| US-5.13 (Фото до/после) → Phase 2          | ✅ PRIORITY | ✅ HIGH ROI             | ✅ **СОГЛАСЕН**                     |
| US-5.14 (Категориальный рейтинг) → Phase 2 | ✅ PRIORITY | ✅ HIGH ROI             | ✅ **СОГЛАСЕН**                     |
| US-5.18 (Verified Visit) → Phase 2         | ✅ PRIORITY | ⚠️ MEDIUM ROI          | ⚠️ **ЧАСТИЧНО** (сделать OPTIONAL) |
| US-5.15 (Badge System) → Phase 2           | ✅          | ❌ LOW ROI              | ❌ **ОТЛОЖИТЬ в Phase 3**           |
| AI Insights → Phase 2                      | ✅          | ❌ LOW ROI              | ❌ **ОТЛОЖИТЬ в Phase 3**           |
| Device Fingerprinting → Phase 2            | ✅          | ❌ VERY HIGH complexity | ❌ **ОТЛОЖИТЬ в Phase 3**           |

---

### 4.2 Финальные решения

**✅ ОБЯЗАТЕЛЬНО Phase 2:**

1. US-5.13: Фото до/после (3 недели)
2. US-5.14: Категориальный рейтинг (1 неделя)
3. US-5.1 to US-5.6: Базовая система (2 недели)

**⚠️ ОПЦИОНАЛЬНО Phase 2:**

4. US-5.18: Verified Visit (2 недели)
    - **КРИТИЧНО:** Сделать OPTIONAL, privacy-first
    - **Privacy:** NO GPS data storage
    - **Consent:** Explicit consent dialog

**❌ ОТЛОЖИТЬ в Phase 3:**

- US-5.15: Badge System
- Device Fingerprinting
- AI Insights
- Social Graph Analysis
- US-5.17: Community Choice Award

**❌ НЕ ДЕЛАТЬ (или minimal version):**

- ML-модель для fake detection (требует данных)

---

### 4.3 Ключевые принципы

1. **Начать с rule-based fraud detection, не ML**
    - IP clustering
    - Flash voting
    - Trust Score thresholds
    - ML отложить до Phase 3 (нужны данные)

2. **Content moderation = CRITICAL**
    - AWS Rekognition с первого дня
    - Manual moderation queue
    - Clear ToS

3. **Privacy-first approach**
    - GPS: NO data storage
    - Explicit consent dialogs
    - GDPR compliance

4. **A/B testing с первого дня**
    - Test incentives for photo upload
    - Test messaging for Verified Visit

---

### 4.4 Success Metrics

**Business Metrics:**

- Review submission rate: > 30%
- Photo upload rate: > 40%
- Verified Visit adoption: > 20%
- Average review rating: 4.2 - 4.8
- Fake detection rate: < 5%

**Technical Metrics:**

- API response time: < 500ms (P95)
- Photo upload time: < 5 seconds
- Content moderation accuracy: > 95%
- System uptime: > 99.9%

---

### 4.5 Следующие шаги

1. [ ] **PM review этот документ**
2. [ ] Приоритизировать Phase 2 фичи
3. [ ] Дождаться завершения E3 Booking Engine (2-3 недели)
4. [ ] Начать Sprint 1 (Foundation)
5. [ ] Setup infrastructure (S3, Rekognition)
6. [ ] Create detailed technical specs

---

## 📎 Приложения

### A. Связанные документы

- [E5: Reputation Strategy](../06_REPUTATION_STRATEGY.md) @../06_REPUTATION_STRATEGY.md
- [E3: Booking Engine Plan](./E3_BOOKING_ENGINE_PLAN.md) @./E3_BOOKING_ENGINE_PLAN.md
- [Implementation Status](../00_IMPLEMENTATION_STATUS.md) @../00_IMPLEMENTATION_STATUS.md
- [User Stories](../01_USER_STORIES.md) @../01_USER_STORIES.md

---

### B. Glossary

| Термин                 | Определение                                              |
|------------------------|----------------------------------------------------------|
| **Trust Score**        | Скрытый рейтинг пользователя (0-1), влияет на вес отзыва |
| **Verified Visit**     | Подтвержденный визит через QR/GPS                        |
| **Flash Voting**       | Всплеск отзывов за короткий период (признак накрутки)    |
| **IP Clustering**      | Множественные отзывы с одного IP (признак накрутки)      |
| **Content Moderation** | Модерация контента (фото, текст) на приемлемость         |
| **GDPR**               | General Data Protection Regulation (EU)                  |

---

### C. Timeline

```
E3 Booking Engine Complete (2-3 weeks)
         ↓
E5 Phase 2 Sprint 1-2: Foundation (2 weeks)
         ↓
E5 Phase 2 Sprint 3-4: Categories + Photos Backend (2 weeks)
         ↓
E5 Phase 2 Sprint 5-6: Photos Frontend + Verified Visit (2 weeks)
         ↓
E5 Phase 2 Sprint 7-8: Testing & Polish (2 weeks)
         ↓
E5 Phase 2 COMPLETE (8-10 weeks total)
```

---

**Дата создания:** 06.03.2026  
**Авторы:** @creative Agent, @analytic Agent  
**Статус:** ✅ Ready for PM Review  
**Следующий шаг:** Обсуждение с PM и Архитектором
