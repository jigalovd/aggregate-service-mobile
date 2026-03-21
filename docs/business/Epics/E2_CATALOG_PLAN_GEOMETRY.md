# E2: Catalog & Geo-Search - Детальный план (PostGIS GEOMETRY)

**Версия:** 1.0 | **Дата:** 02.03.2026 | **Статус:** Active
**Подход:** PostGIS GEOMETRY (Plane) - оптимизирован для Израиля

---

## 📚 Связанная техническая документация

| Документ                                                                              | Релевантность | Для чего использовать                  |
|---------------------------------------------------------------------------------------|---------------|----------------------------------------|
| [CODING_STANDARDS.md](../../architecture/CODING_STANDARDS.md) @../../architecture/CODING_STANDARDS.md                         | ⭐⭐⭐           | Правила Feature-First, DDD, Type Hints |
| [02_DATABASE_SCHEMA.md](../../architecture/backend/02_DATABASE_SCHEMA.md) @../../architecture/backend/02_DATABASE_SCHEMA.md             | ⭐⭐⭐           | PostGIS, индексы, N+1 prevention       |
| [TESTING_STRATEGY.md](../../architecture/testing/TESTING_STRATEGY.md) @../../architecture/testing/TESTING_STRATEGY.md                 | ⭐⭐⭐           | Unit/Integration тесты, coverage       |
| [07_ERROR_HANDLING.md](../../architecture/backend/07_ERROR_HANDLING.md) @../../architecture/backend/07_ERROR_HANDLING.md               | ⭐⭐⭐           | Exception hierarchy, HTTP mapping      |
| [04_I18N_IMPLEMENTATION.md](../../architecture/backend/04_I18N_IMPLEMENTATION.md) @../../architecture/backend/04_I18N_IMPLEMENTATION.md     | ⭐⭐            | JSONB i18n паттерны                    |
| [01_API_DESIGN.md](../../architecture/backend/01_API_DESIGN.md) @../../architecture/backend/01_API_DESIGN.md                       | ⭐⭐            | REST API спецификация                  |
| [00_ARCHITECTURE_OVERVIEW.md](../../architecture/backend/00_ARCHITECTURE_OVERVIEW.md) @../../architecture/backend/00_ARCHITECTURE_OVERVIEW.md | ⭐⭐            | Feature-First структура                |

---

## Содержание

1. [Почему PostGIS GEOMETRY для Израиля](#1-почему-postgis-geometry-для-израиля) @#1-почему-postgis-geometry-для-израиля
2. [Ключевые изменения от GEOGRAPHY → GEOMETRY](#2-ключевые-изменения-от-geography--geometry) @#2-ключевые-изменения-от-geography--geometry
3. [Helper Functions для GEOMETRY](#3-helper-functions-для-geometry) @#3-helper-functions-для-geometry
4. [Обзор эпика](#4-обзор-эпика) @#4-обзор-эпика
5. [Geo-Search: Теория и архитектура](#5-geo-search-теория-и-архитектура) @#5-geo-search-теория-и-архитектура
6. [Phase 1: Database & Infrastructure](#6-phase-1-database--infrastructure) @#6-phase-1-database--infrastructure
7. [Phase 2: Domain Layer](#7-phase-2-domain-layer) @#7-phase-2-domain-layer
8. [Phase 3: Application Layer](#8-phase-3-application-layer) @#8-phase-3-application-layer
9. [Phase 4: Presentation Layer](#9-phase-4-presentation-layer) @#9-phase-4-presentation-layer
10. [Phase 5: Testing & Documentation](#10-phase-5-testing--documentation) @#10-phase-5-testing--documentation
11. [Риски и митигация](#11-риски-и-митигация) @#11-риски-и-митигация
12. [Definition of Done](#12-definition-of-done) @#12-definition-of-done
13. [Миграция на GEOGRAPHY](#13-миграция-на-geography) @#13-миграция-на-geography

---

## 1. Почему PostGIS GEOMETRY для Израиля

### Географические факты

```
Израиль:
├── Размеры: 420 км (север-юг) × 115 км (восток-запад)
├── Площадь: ~22,000 км²
├── Координаты: 29.5°-33.3° N, 34.3°-35.9° E
└── Типичный радиус поиска: 5-50 км
```

### Сравнение точности

| Расстояние | GEOMETRY ошибка | GEOGRAPHY ошибка | Разница   |
|------------|-----------------|------------------|-----------|
| 10 км      | ±0.01% (< 1 м)  | ±0.01% (< 1 м)   | Незаметна |
| 25 км      | ±0.02% (< 5 м)  | ±0.01% (< 2 м)   | Незаметна |
| 50 км      | ±0.03% (< 15 м) | ±0.01% (< 5 м)   | Незаметна |
| 100 км     | ±0.05% (< 50 м) | ±0.01% (< 10 м)  | Приемлемо |

### Преимущества GEOMETRY

- ✅ **Проще разработка** - плоские расчёты вместо сферических
- ✅ **Быстрее выполнение** - меньше вычислений в БД
- ✅ **Проще TDD** - легче тестировать без сложной сферической математики
- ✅ **Точность достаточна** - ±0.05% незаметно для пользователей
- ✅ **Легко мигрировать** на GEOGRAPHY если понадобится в будущем

### Ограничения GEOMETRY

- ⚠️ Не подходит для глобальных приложений (полярные регионы, date line)
- ⚠️ Расстояние в градусах, нужна конвертация в km

### Почему SRID 4326 для Израиля (а не EPSG:3857)

**Технически правильно** использовать метрическую проекцию (EPSG:3857 Web Mercator), где ST_Distance возвращает метры.
Однако для локального приложения в Израиле:

| Параметр            | SRID 4326                   | EPSG:3857               |
|---------------------|-----------------------------|-------------------------|
| Единицы             | Градусы                     | Метры                   |
| ST_Distance         | Градусы → нужна конвертация | Метры → нативно         |
| Координаты          | lat/lon (как в GPS)         | X/Y в метрах            |
| Совместимость с API | ✅ Прямая                    | ⚠️ Требует ST_Transform |

**Почему выбрали SRID 4326 для MVP:**

1. **Простота API** — координаты от клиента (GPS) записываются напрямую
2. **Точность достаточна** — ошибка конвертации < 0.05% для Израиля
3. **Меньше кода** — не нужен ST_Transform при каждом INSERT/SELECT
4. **Легко мигрировать** — одна ALTER TABLE + UPDATE если понадобится 3857

**Если понадобится глобальное расширение:**

```sql
ALTER TABLE app.providers
    ALTER COLUMN location TYPE GEOMETRY(POINT, 3857)
        USING ST_Transform(location, 3857);
```

---

## 2. Ключевые изменения от GEOGRAPHY → GEOMETRY

### 2.1. Database Column Type

```diff
- location: Mapped[str | None] = mapped_column(
-     Geography(geometry_type="POINT", srid=4326)
- )
+ location: Mapped[str | None] = mapped_column(
+     Geometry(geometry_type="POINT", srid=4326)
+ )
```

### 2.2. Distance Units

```diff
- ST_DWithin(geom1, geom2, radius_km * 1000)  -- meters
+ ST_DWithin(geom1, geom2, radius_degrees)  -- degrees
```

### 2.3. Distance Calculation

```diff
- distance_km = ST_Distance(geom1, geom2) / 1000  -- meters to km
+ distance_deg = ST_Distance(geom1, geom2)
+ distance_km = degrees_to_km(distance_deg, lat)  -- need conversion
```

---

## 3. Helper Functions для GEOMETRY

### Создать файл: `backend/app/core/db/geo.py`

```python
from sqlalchemy import func
from sqlalchemy.ext.asyncio import AsyncSession
from math import cos, radians

ISRAEL_AVG_LAT = 31.5
ISRAEL_MIN_LAT = 29.5
ISRAEL_MAX_LAT = 33.3


def km_to_degrees(km: float, lat: float = ISRAEL_AVG_LAT) -> float:
    lat_rad = radians(lat)
    degrees_per_km = 1 / (111.0 * cos(lat_rad))
    return km * degrees_per_km


def degrees_to_km(degrees: float, lat: float = ISRAEL_AVG_LAT) -> float:
    lat_rad = radians(lat)
    km_per_degree = 111.0 * cos(lat_rad)
    return degrees * km_per_degree


async def check_postgis_available(db: AsyncSession) -> bool:
    result = await db.execute(func.postgis_version())
    return result.scalar() is not None
```

---

## 4. Обзор эпика

### 4.1. Цель

Реализовать базовый каталог мастеров с гео-поиском для MVP.

### 4.2. User Stories

| ID     | Описание                                                | Приоритет |
|--------|---------------------------------------------------------|-----------|
| US-2.1 | Карта с точками мастеров вокруг (радиус поиска)         | ✅ MVP     |
| US-2.2 | Фильтрация мастеров по типу услуги и цене (базовая)     | ✅ MVP     |
| US-2.3 | Список мастеров, отсортированный по расстоянию/рейтингу | ✅ MVP     |
| US-2.4 | Сохранение мастера в "Избранное"                        | ✅ MVP     |

### 4.3. Timeline

| Метрика      | Значение   |
|--------------|------------|
| Общая оценка | 12-17 дней |
| Sprint       | Sprint 1-2 |
| Недели       | Week 3-5   |
| Риск-буфер   | +2 дня     |

### 4.4. Coverage Requirements

**Minimum: 99%** для всех новых модулей catalog feature.

---

## 5. Geo-Search: Теория и архитектура

### 5.1. Проблема

Нужно найти мастеров в радиусе N километров от текущей геопозиции пользователя с сортировкой по расстоянию.

### 5.2. Подходы к решению

#### Вариант A: Python (Haversine) - ✅ Уже реализовано

**Где:** [`backend/app/shared/geo.py`](../../../backend/app/shared/geo.py) @../../../backend/app/shared/geo.py

**Как работает:**

1. Загружаем ВСЕХ мастеров из БД в память
2. Вычисляем расстояние для каждого по формуле Haversine
3. Фильтруем по радиусу
4. Сортируем

**Плюсы:**

- ✅ Не требует PostGIS
- ✅ Работает с любой БД
- ✅ Уже реализовано в проекте

**Минусы:**

- ❌ N+1 problem при загрузке связанных данных
- ❌ Плохая масштабируемость (>1000 мастеров = медленно)
- ❌ Вся фильтрация в Python, не в БД

**Когда использовать:**

- Seed data (< 50 мастеров)
- Offline расчёты
- Fallback если PostGIS недоступен

---

#### Вариант B: PostGIS GEOMETRY (ST_DWithin) - ✅ Выбрано для MVP

**Ключевые функции:**

```sql
SELECT ST_MakePoint(34.7818, 32.0853);

SELECT ST_SetSRID(ST_MakePoint(34.7818, 32.0853), 4326);

SELECT ST_DWithin(
               location,
               ST_SetSRID(ST_MakePoint(34.7818, 32.0853), 4326),
               0.448 -- ~50 км в градусах на широте Израиля
       );

SELECT ST_Distance(
               location,
               ST_SetSRID(ST_MakePoint(34.7818, 32.0853), 4326)
       );
```

**SRID 4326 (WGS84):**

Стандартная система координат для GPS. Используется в Google Maps, OpenStreetMap, мобильных устройствах.

Запрос вида:

```sql 

SELECT *
FROM catalog_stores
WHERE ST_Distance(geom, user_location) < 10000;
```

**❌ Классифицируется как фатальный антипаттерн производительности.**

---

### 5.3. Архитектура Geo-Search в проекте

#### Уровни абстракции

```
┌─────────────────────────────────────────────────────────┐
│  Presentation Layer (Controllers)                       │
│  - GET /api/v1/providers?lat=32.0&lon=34.7&radius=50   │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│  Application Layer (Use Cases)                          │
│  - SearchProvidersUseCase                               │
│  - Вызывает repository.find_within_radius()            │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│  Infrastructure Layer (Repositories)                    │
│  - SqlAlchemyProviderRepository                         │
│  - PostGIS запросы: ST_DWithin, ST_Distance            │
│  - km_to_degrees() конвертация                         │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│  Database (PostgreSQL + PostGIS)                        │
│  - providers.location GEOMETRY(POINT, 4326)            │
│  - GIST индекс для быстрого поиска                     │
└─────────────────────────────────────────────────────────┘
```

#### Hybrid Approach (Рекомендуется)

**Два уровня geo-расчётов:**

1. **PostGIS (Database level):**
    - Предварительная фильтрация по радиусу
    - Сортировка по расстоянию
    - Пагинация

2. **Python Euclidean (Application level):**
    - Точное вычисление расстояния для топ-N результатов
    - Отображение в UI ("5.2 км от вас")

---

### 5.4. Оптимизация производительности

#### GiST Index (стандарт)

```sql
CREATE INDEX ix_providers_location ON app.providers USING GIST (location);
```

**Как работает:**

- R-tree структура (Minimum Bounding Rectangles)
- O(log N) вместо O(N) для поиска в радиусе
- Автоматически используется для ST_DWithin

#### SP-GiST Index (для точек)

```sql
CREATE INDEX ix_providers_location ON app.providers USING SPGIST (location);
```

**Преимущества для точек:**

- Quad-tree / k-d tree структура
- **Быстрее на 10-30%** для миллионов непересекающихся точек
- Меньше размер индекса

**Ограничения:**

- Только для POINT геометрий
- Деградирует при перекрывающихся данных

**Рекомендация:** GiST для MVP, SP-GiST после профилирования при >100K провайдеров.

#### Covering Index (Оптимизация)

```sql
CREATE INDEX ix_providers_location_rating
    ON app.providers USING GIST (location)
    INCLUDE (rating_cached, reviews_count);
```

**Плюсы:**

- Index-only scan (не читаем таблицу)
- Сортировка по рейтингу без extra sort

---

### 5.5. Edge Cases

| Case                | Решение                                            |
|---------------------|----------------------------------------------------|
| Null location       | `WHERE location IS NOT NULL`                       |
| Invalid coordinates | Validation в schema (lat: -90..90, lon: -180..180) |
| Very large radius   | Ограничить max radius = 100 км                     |

---

### 5.6. Конфигурация PostgreSQL для PostGIS

> ⚠️ **Критически важно:** PostGIS требует настройки параметров памяти PostgreSQL.

#### Рекомендуемые параметры (postgresql.conf)

| Параметр               | Значение     | Для чего                   |
|------------------------|--------------|----------------------------|
| `shared_buffers`       | 256MB - 4GB  | Кэш индексов GiST в RAM    |
| `work_mem`             | 64MB - 256MB | Сортировка и spatial joins |
| `maintenance_work_mem` | 1GB - 4GB    | Построение индексов GiST   |
| `effective_cache_size` | 4GB - 16GB   | Оценка планировщика        |
| `random_page_cost`     | 1.1 - 2.0    | Для SSD (по умолчанию 4.0) |

#### Для Docker Compose

```yaml
services:
  postgres:
    image: postgis/postgis:16-3.4
    command:
      - "postgres"
      - "-c"
      - "shared_buffers=256MB"
      - "-c"
      - "work_mem=64MB"
      - "-c"
      - "maintenance_work_mem=1GB"
      - "-c"
      - "effective_cache_size=4GB"
      - "-c"
      - "random_page_cost=1.1"
```

#### Для продакшена (k8s/Helm)

```yaml
# values.yaml для PostgreSQL Helm chart
postgresql:
  sharedPreloadLibraries: "pg_stat_statements"
  parameters:
    shared_buffers: "2GB"
    work_mem: "128MB"
    maintenance_work_mem: "2GB"
    effective_cache_size: "8GB"
    random_page_cost: "1.1"
    max_connections: "200"
```

#### Почему это важно

- **GiST индексы** потребляют больше памяти при построении
- **Spatial joins** (ST_Intersects, ST_DWithin) требуют work_mem для intermediate results
- **Без настройки** планировщик может выбрать Seq Scan вместо Index Scan

---

### 5.7. SP-GiST vs GiST для точек

> **SP-GiST (Space-Partitioned GiST)** — альтернатива GiST для точечных данных.

#### Сравнение

| Характеристика         | GiST                    | SP-GiST              |
|------------------------|-------------------------|----------------------|
| Структура              | R-Tree (bounding boxes) | Quad-tree / k-d tree |
| Для точек              | ✅ Работает              | ✅✅ Оптимизирован     |
| Для полигонов          | ✅✅ Лучше                | ⚠️ Хуже              |
| Перекрывающиеся данные | ✅ Хорошо                | ❌ Деградация         |
| Скорость поиска        | Базовая                 | +10-30% для точек    |

#### Когда использовать SP-GiST

```
✅ Провайдеры = ТОЧКИ (POINT)
✅ Нет перекрытий (каждая точка уникальна)
✅ Миллионы записей
✅ Равномерное распределение по карте
```

#### Миграция на SP-GiST (если профилирование покажет улучшение)

```sql
-- Удалить GiST
DROP INDEX ix_providers_location;

-- Создать SP-GiST
CREATE INDEX ix_providers_location
    ON app.providers USING SP-GIST (location);
```

#### Рекомендация для MVP

- **Начать с GiST** — универсальное решение
- **Профилировать** после 10,000+ провайдеров
- **Перейти на SP-GiST** если бенчмарки покажут улучшение >15%

---

### 5.8. KNN Оператор <-> вместо ST_Distance

> ⚠️ **Критический антипаттерн:** `ORDER BY ST_Distance(a, b)` не использует индекс!

#### Почему ST_Distance в ORDER BY — плохо

```sql
-- ❌ НЕПРАВИЛЬНО — Full Table Scan
SELECT *
FROM providers
ORDER BY ST_Distance(location, search_point)
LIMIT 10;
```

Планировщик:

1. Вычисляет ST_Distance для **каждой строки** в таблице
2. Сортирует все результаты
3. Берёт LIMIT 10

#### Правильно: KNN оператор <->

```sql
-- ✅ ПРАВИЛЬНО — Index Scan
SELECT *
FROM providers
ORDER BY location <-> search_point
LIMIT 10;
```

Планировщик:

1. Использует GiST/SP-GiST индекс напрямую
2. Возвращает результаты по мере обхода дерева
3. **Субмиллисекундный ответ** даже для миллионов строк

#### В SQLAlchemy

```python
# ❌ Неправильно
stmt = stmt.order_by(func.ST_Distance(Provider.location, search_point))

# ✅ Правильно
stmt = stmt.order_by(Provider.location.op('<->')(search_point))
```

---

## 6. Phase 1: Database & Infrastructure

### Task 1.1: PostGIS Setup

**Оценка:** 0.5 дня | **Приоритет:** P0 | **Зависимости:** none

**Подзадачи:**

| ID    | Файл                                  | Описание                                           |
|-------|---------------------------------------|----------------------------------------------------|
| 1.1.1 | `alembic/versions/xxx_add_postgis.py` | Миграция `CREATE EXTENSION IF NOT EXISTS postgis;` |
| 1.1.2 | `app/core/db/geo.py`                  | PostGIS helpers (km_to_degrees, degrees_to_km)     |
| 1.1.3 | `tests/unit/core/db/test_geo.py`      | Unit tests для geo helpers                         |
| 1.1.4 | `tests/integration/test_postgis.py`   | Integration test `SELECT PostGIS_Version()`        |

**Acceptance Criteria:**

- [ ] PostGIS extension активирован в БД
- [ ] `SELECT PostGIS_Version()` возвращает версию
- [ ] Test с `ST_DWithin` запросом проходит
- [ ] Coverage >= 99% для `app/core/db/geo.py`

**Код:**

```python
def upgrade() -> None:
    op.execute("CREATE EXTENSION IF NOT EXISTS postgis;")


def downgrade() -> None:
    op.execute("DROP EXTENSION IF EXISTS postgis;")
```

---

### Task 1.2: Category Model & Migration

**Оценка:** 0.5 дня | **Приоритет:** P0 | **Зависимости:** 1.1

**Подзадачи:**

| ID    | Файл                                                                | Описание                                          |
|-------|---------------------------------------------------------------------|---------------------------------------------------|
| 1.2.1 | `app/features/catalog/infrastructure/models.py`                     | `Category` ORM model (parent_id, name_i18n JSONB) |
| 1.2.2 | `alembic/versions/xxx_add_categories.py`                            | Миграция categories table                         |
| 1.2.3 | `tests/unit/features/catalog/infrastructure/test_category_model.py` | Unit tests                                        |

**Acceptance Criteria:**

- [ ] Таблица `categories` создана в schema `app`
- [ ] Иерархия parent-child работает
- [ ] JSONB поле `name_i18n` корректно хранит `{"ru": "...", "he": "...", "en": "..."}`
- [ ] Index на `parent_id` создан
- [ ] Coverage >= 99%

**Код:**

```python
from sqlalchemy import String, Integer, Boolean, ForeignKey
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import Mapped, mapped_column, relationship


class Category(Base):
    __tablename__ = "categories"
    __table_args__ = {"schema": _SCHEMA}

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    parent_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey(f"{_SCHEMA}.categories.id", ondelete="SET NULL"),
        index=True
    )
    name_i18n: Mapped[dict] = mapped_column(JSONB, nullable=False)
    icon_url: Mapped[str | None] = mapped_column(String(500))
    sort_order: Mapped[int] = mapped_column(Integer, default=0)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, index=True)

    parent: Mapped["Category | None"] = relationship("Category", remote_side=[id])
    children: Mapped[list["Category"]] = relationship("Category", back_populates="parent")
    services: Mapped[list["Service"]] = relationship("Service", back_populates="category")
```

---

### Task 1.3: Service Model & Migration

**Оценка:** 0.5 дня | **Приоритет:** P0 | **Зависимости:** 1.2

**Подзадачи:**

| ID    | Файл                                                               | Описание                                           |
|-------|--------------------------------------------------------------------|----------------------------------------------------|
| 1.3.1 | `app/features/catalog/infrastructure/models.py`                    | `Service` ORM model (category_id FK, price fields) |
| 1.3.2 | `alembic/versions/xxx_add_services.py`                             | Миграция services table                            |
| 1.3.3 | `tests/unit/features/catalog/infrastructure/test_service_model.py` | Unit tests                                         |

**Acceptance Criteria:**

- [ ] Таблица `services` создана
- [ ] FK на `categories.id` работает (RESTRICT на delete)
- [ ] Цена хранится в копейках (INTEGER)
- [ ] JSONB поля `title_i18n`, `description_i18n` работают
- [ ] Coverage >= 99%

**Код:**

```python
class Service(Base):
    __tablename__ = "services"
    __table_args__ = {"schema": _SCHEMA}

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    category_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey(f"{_SCHEMA}.categories.id", ondelete="RESTRICT"),
        nullable=False,
        index=True
    )
    title_i18n: Mapped[dict] = mapped_column(JSONB, nullable=False)
    description_i18n: Mapped[dict | None] = mapped_column(JSONB)
    base_price: Mapped[int | None] = mapped_column(Integer)
    price_min: Mapped[int | None] = mapped_column(Integer)
    price_max: Mapped[int | None] = mapped_column(Integer)
    currency: Mapped[str] = mapped_column(String(3), default="ILS")
    duration_minutes: Mapped[int] = mapped_column(Integer, nullable=False)
    is_combinable: Mapped[bool] = mapped_column(Boolean, default=True)
    is_price_variable: Mapped[bool] = mapped_column(Boolean, default=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, index=True)

    category: Mapped["Category"] = relationship("Category", back_populates="services")
    provider_services: Mapped[list["ProviderService"]] = relationship(
        "ProviderService", back_populates="service"
    )
```

---

### Task 1.4: Provider Model & Migration (GEOMETRY)

**Оценка:** 0.75 дня | **Приоритет:** P0 | **Зависимости:** 1.1

**Подзадачи:**

| ID    | Файл                                                                | Описание                                             |
|-------|---------------------------------------------------------------------|------------------------------------------------------|
| 1.4.1 | `app/features/catalog/infrastructure/models.py`                     | `Provider` ORM model (user_id FK, location GEOMETRY) |
| 1.4.2 | `app/features/catalog/infrastructure/models.py`                     | `ProviderService` junction table                     |
| 1.4.3 | `alembic/versions/xxx_add_providers.py`                             | Миграция providers + provider_services               |
| 1.4.4 | `tests/unit/features/catalog/infrastructure/test_provider_model.py` | Unit tests для geo-поля                              |

**Acceptance Criteria:**

- [ ] Таблицы `providers` и `provider_services` созданы
- [ ] GIST индекс на `location` создан
- [ ] Связь provider-service M2M работает
- [ ] PostGIS GEOMETRY тип корректно работает
- [ ] Coverage >= 99%

**Код:**

```python
from geoalchemy2 import Geometry


class Provider(Base):
    __tablename__ = "providers"
    __table_args__ = {"schema": _SCHEMA}

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey(f"{_SCHEMA}.users.id", ondelete="CASCADE"),
        unique=True,
        nullable=False,
        index=True
    )
    display_name: Mapped[str] = mapped_column(String(255), nullable=False)
    bio: Mapped[str | None] = mapped_column(Text)
    avatar_url: Mapped[str | None] = mapped_column(Text)
    phone: Mapped[str | None] = mapped_column(String(20))
    location: Mapped[str | None] = mapped_column(
        Geometry(geometry_type="POINT", srid=4326)
    )
    address: Mapped[str | None] = mapped_column(Text)
    rating_cached: Mapped[float] = mapped_column(Float, default=0.0)
    reviews_count: Mapped[int] = mapped_column(Integer, default=0)
    is_verified: Mapped[bool] = mapped_column(Boolean, default=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, index=True)

    user: Mapped["User"] = relationship("User")
    provider_services: Mapped[list["ProviderService"]] = relationship(
        "ProviderService", back_populates="provider"
    )
    favorites: Mapped[list["Favorite"]] = relationship(
        "Favorite", back_populates="provider"
    )


class ProviderService(Base):
    __tablename__ = "provider_services"
    __table_args__ = (
        UniqueConstraint("provider_id", "service_id", name="uq_provider_service"),
        {"schema": _SCHEMA}
    )

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    provider_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey(f"{_SCHEMA}.providers.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    service_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey(f"{_SCHEMA}.services.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    custom_price: Mapped[int | None] = mapped_column(Integer)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    provider: Mapped["Provider"] = relationship("Provider", back_populates="provider_services")
    service: Mapped["Service"] = relationship("Service", back_populates="provider_services")
```

**SQL миграция (индексы):**

```sql
-- Основной индекс для geo-поиска (GiST или SP-GiST)

-- Вариант 1: GiST (универсальный, подходит для всего)
CREATE INDEX ix_providers_location ON app.providers USING GIST (location);

-- Вариант 2: SP-GiST (оптимизирован для точек, быстрее на 10-30%)
-- Рекомендуется если: только POINT геометрии, миллионы строк, нет перекрытий
-- CREATE INDEX ix_providers_location ON app.providers USING SPGIST (location);

-- Стандартные индексы
CREATE INDEX ix_providers_user_id ON app.providers (user_id);
CREATE INDEX ix_providers_is_active ON app.providers (is_active);
```

**Рекомендация:** Для MVP использовать GiST. После роста данных >100K провайдеров — профилировать SP-GiST.

---

### Task 1.5: Favorites Model & Migration

**Оценка:** 0.5 дня | **Приоритет:** P0 | **Зависимости:** 1.4

**Подзадачи:**

| ID    | Файл                                                                | Описание             |
|-------|---------------------------------------------------------------------|----------------------|
| 1.5.1 | `app/features/catalog/infrastructure/models.py`                     | `Favorite` ORM model |
| 1.5.2 | `alembic/versions/xxx_add_favorites.py`                             | Миграция favorites   |
| 1.5.3 | `tests/unit/features/catalog/infrastructure/test_favorite_model.py` | Unit tests           |

**Acceptance Criteria:**

- [ ] Таблица `favorites` создана
- [ ] Unique constraint на `(user_id, provider_id)` - нельзя добавить дважды
- [ ] CASCADE delete при удалении user или provider
- [ ] Coverage >= 99%

**Код:**

```python
class Favorite(Base):
    __tablename__ = "favorites"
    __table_args__ = (
        UniqueConstraint("user_id", "provider_id", name="uq_user_provider"),
        {"schema": _SCHEMA}
    )

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey(f"{_SCHEMA}.users.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    provider_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey(f"{_SCHEMA}.providers.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )

    user: Mapped["User"] = relationship("User")
    provider: Mapped["Provider"] = relationship("Provider", back_populates="favorites")
```

---

### Task 1.6: Seed Data

**Оценка:** 0.5 дня | **Приоритет:** P1 | **Зависимости:** 1.5

**Подзадачи:**

| ID    | Файл                                   | Описание                            |
|-------|----------------------------------------|-------------------------------------|
| 1.6.1 | `app/core/db/seeds/seed_categories.py` | 10-15 категорий с i18n              |
| 1.6.2 | `app/core/db/seeds/seed_services.py`   | 30-50 услуг                         |
| 1.6.3 | `app/core/db/seeds/seed_providers.py`  | 50 тестовых мастеров с координатами |
| 1.6.4 | `app/core/db/seeds/__init__.py`        | `seed_all()` функция                |

**Acceptance Criteria:**

- [ ] 50 тестовых мастеров с реальными координатами (Тель-Авив, Иерусалим, Хайфа)
- [ ] Все категории и услуги локализованы (RU/HE/EN)
- [ ] Seed script идемпотентен (можно запускать многократно)
- [ ] Команда `poetry run python -m app.core.db.seeds` работает

**Код:**

```python
PROVIDERS_DATA = [
    {
        "display_name": "Анна Иванова",
        "bio": "Мастер маникюра с 5-летним опытом",
        "location": "SRID=4326;POINT(34.7818 32.0853)",
        "address": "Тель-Авив, ул. Дизенгоф, 50",
    },
    {
        "display_name": "Мария Коэн",
        "bio": "Парикмахер-колорист",
        "location": "SRID=4326;POINT(35.2137 31.7683)",
        "address": "Иерусалим, ул. Яффо, 10",
    },
]
```

---

### Task 1.7: PostgreSQL Configuration for PostGIS

**Оценка:** 0.25 дня | **Приоритет:** P0 | **Зависимости:** 1.1

**Зачем нужен:**

PostGIS требует настройки параметров памяти PostgreSQL для оптимальной производительности пространственных запросов.

**Подзадачи:**

| ID    | Файл                                      | Описание                                |
|-------|-------------------------------------------|-----------------------------------------|
| 1.7.1 | `docker/postgresql.conf` или документация | Рекомендуемые параметры postgresql.conf |
| 1.7.2 | `docs/deployment/postgis_tuning.md`       | Документация по настройке               |

**Acceptance Criteria:**

- [ ] Документация по настройке PostgreSQL создана
- [ ] Параметры work_mem, shared_buffers, maintenance_work_mem определены
- [ ] Рекомендации по VACUUM и ANALYZE добавлены

**Рекомендуемые параметры (postgresql.conf):**

```ini
# Memory Settings for PostGIS
shared_buffers = 256MB          # Минимум для dev, 4GB+ для production
work_mem = 64MB                # Для spatial joins и сортировки
maintenance_work_mem = 1GB     # Для построения GiST индексов
effective_cache_size = 4GB     # Подсказка планировщику

# Autovacuum (критично для PostGIS)
autovacuum = on
autovacuum_analyze_scale_factor = 0.05
autovacuum_vacuum_scale_factor = 0.1

# Random page cost (для SSD)
random_page_cost = 1.1          # По умолчанию 4.0 (для HDD)
```

**Для Docker Compose:**

```yaml
services:
  postgres:
    command: >
      postgres
      -c shared_buffers=256MB
      -c work_mem=64MB
      -c maintenance_work_mem=1GB
      -c random_page_cost=1.1
```

---

## 7. Phase 2: Domain Layer

### Task 2.1: Domain Entities

**Оценка:** 1 день | **Приоритет:** P0 | **Зависимости:** 1.4

**Подзадачи:**

| ID    | Файл                                                       | Описание                               |
|-------|------------------------------------------------------------|----------------------------------------|
| 2.1.1 | `app/features/catalog/domain/value_objects.py`             | `Location`, `PriceRange`, `I18nString` |
| 2.1.2 | `app/features/catalog/domain/entities.py`                  | `CategoryEntity`                       |
| 2.1.3 | ...                                                        | `ServiceEntity`                        |
| 2.1.4 | ...                                                        | `ProviderEntity` с `Location`          |
| 2.1.5 | ...                                                        | `FavoriteEntity`                       |
| 2.1.6 | `tests/unit/features/catalog/domain/test_value_objects.py` | Tests для value objects                |
| 2.1.7 | `tests/unit/features/catalog/domain/test_entities.py`      | Tests для entities (100% coverage)     |

**Acceptance Criteria:**

- [ ] Entities не имеют ORM зависимостей (pure Python)
- [ ] Business logic в entities (например, `distance_to_euclidean()`, `is_price_in_range()`)
- [ ] Value objects immutable (frozen dataclasses)
- [ ] 100% test coverage для domain layer

**Код (Location Value Object для GEOMETRY):**

```python
from dataclasses import dataclass
from decimal import Decimal
from math import sqrt, cos, radians


@dataclass(frozen=True)
class Location:
    lat: float
    lon: float

    def __post_init__(self) -> None:
        if not (-90 <= self.lat <= 90):
            raise ValueError(f"Latitude must be between -90 and 90, got {self.lat}")
        if not (-180 <= self.lon <= 180):
            raise ValueError(f"Longitude must be between -180 and 180, got {self.lon}")

    def to_wkt(self) -> str:
        return f"SRID=4326;POINT({self.lon} {self.lat})"

    def distance_to_euclidean(self, other: "Location") -> float:
        lat_avg = radians((self.lat + other.lat) / 2)

        lat_diff = self.lat - other.lat
        lon_diff = self.lon - other.lon

        lat_km = lat_diff * 111.0
        lon_km = lon_diff * 111.0 * cos(lat_avg)

        return sqrt(lat_km ** 2 + lon_km ** 2)


@dataclass(frozen=True)
class PriceRange:
    min_price: int
    max_price: int

    def __post_init__(self) -> None:
        if self.min_price < 0 or self.max_price < 0:
            raise ValueError("Price cannot be negative")
        if self.min_price > self.max_price:
            raise ValueError(f"min_price ({self.min_price}) > max_price ({self.max_price})")

    def contains(self, price: int) -> bool:
        return self.min_price <= price <= self.max_price

    def to_rub(self) -> tuple[Decimal, Decimal]:
        return (
            Decimal(self.min_price) / 100,
            Decimal(self.max_price) / 100
        )


@dataclass(frozen=True)
class I18nString:
    translations: dict[str, str]

    def get(self, lang: str, default: str = "") -> str:
        return self.translations.get(lang, default)

    def __post_init__(self) -> None:
        required_langs = {"ru", "he", "en"}
        if not required_langs.issubset(self.translations.keys()):
            missing = required_langs - set(self.translations.keys())
            raise ValueError(f"Missing required translations: {missing}")
```

**Код (ProviderEntity):**

```python
from dataclasses import dataclass
from datetime import datetime
from uuid import UUID


@dataclass
class ProviderEntity:
    id: UUID | None
    user_id: UUID
    display_name: str
    bio: str | None = None
    avatar_url: str | None = None
    phone: str | None = None
    location: Location | None = None
    address: str | None = None
    rating_cached: float = 0.0
    reviews_count: int = 0
    is_verified: bool = False
    is_active: bool = True
    created_at: datetime | None = None
    updated_at: datetime | None = None

    def calculate_distance_to(self, lat: float, lon: float) -> float | None:
        if self.location is None:
            return None
        other = Location(lat=lat, lon=lon)
        return self.location.distance_to_euclidean(other)

    def is_within_radius(self, lat: float, lon: float, radius_km: float) -> bool:
        distance = self.calculate_distance_to(lat, lon)
        return distance is not None and distance <= radius_km

    def update_location(self, lat: float, lon: float, address: str) -> None:
        self.location = Location(lat=lat, lon=lon)
        self.address = address
```

---

### Task 2.2: Domain Exceptions

**Оценка:** 0.5 дня | **Приоритет:** P0 | **Зависимости:** none

**Подзадачи:**

| ID    | Файл                                                    | Описание                    |
|-------|---------------------------------------------------------|-----------------------------|
| 2.2.1 | `app/features/catalog/domain/exceptions.py`             | `CategoryNotFoundException` |
| 2.2.2 | ...                                                     | `ServiceNotFoundException`  |
| 2.2.3 | ...                                                     | `ProviderNotFoundException` |
| 2.2.4 | ...                                                     | `InvalidLocationException`  |
| 2.2.5 | `tests/unit/features/catalog/domain/test_exceptions.py` | Tests                       |

**Acceptance Criteria:**

- [ ] Все exceptions наследуются от `AppException`
- [ ] Локализованные сообщения через `gettext as _`
- [ ] Правильные HTTP status codes (404, 400)
- [ ] Coverage >= 99%

**Код:**

```python
from app.core.exceptions import NotFoundException, ValidationException
from app.core.i18n import gettext as _


class CategoryNotFoundException(NotFoundException):
    error_code = "CATEGORY_NOT_FOUND"
    message = _("Категория не найдена")


class ServiceNotFoundException(NotFoundException):
    error_code = "SERVICE_NOT_FOUND"
    message = _("Услуга не найдена")


class ProviderNotFoundException(NotFoundException):
    error_code = "PROVIDER_NOT_FOUND"
    message = _("Мастер не найден")


class InvalidLocationException(ValidationException):
    error_code = "INVALID_LOCATION"
    message = _("Неверные координаты")


class FavoriteAlreadyExistsException(ValidationException):
    error_code = "FAVORITE_ALREADY_EXISTS"
    message = _("Мастер уже добавлен в избранное")
```

---

### Task 2.3: Repository Protocols

**Оценка:** 0.5 дня | **Приоритет:** P0 | **Зависимости:** 2.1

**Подзадачи:**

| ID    | Файл                                                         | Описание                                              |
|-------|--------------------------------------------------------------|-------------------------------------------------------|
| 2.3.1 | `app/features/catalog/application/protocols/repositories.py` | `CategoryRepositoryProtocol`                          |
| 2.3.2 | ...                                                          | `ServiceRepositoryProtocol`                           |
| 2.3.3 | ...                                                          | `ProviderRepositoryProtocol` с `find_within_radius()` |
| 2.3.4 | ...                                                          | `FavoriteRepositoryProtocol`                          |
| 2.3.5 | `app/features/catalog/application/protocols/unit_of_work.py` | `CatalogUnitOfWorkProtocol`                           |

**Acceptance Criteria:**

- [ ] Все protocols используют `typing.Protocol`
- [ ] Type hints для всех методов
- [ ] Async методы
- [ ] Документированы docstrings

**Код:**

```python
from typing import Protocol, runtime_checkable
from uuid import UUID

from app.features.catalog.domain.entities import (
    CategoryEntity,
    ServiceEntity,
    ProviderEntity,
    FavoriteEntity,
)


@runtime_checkable
class CategoryRepositoryProtocol(Protocol):
    async def get_by_id(self, category_id: UUID) -> CategoryEntity | None: ...

    async def get_all_active(self) -> list[CategoryEntity]: ...

    async def get_children(self, parent_id: UUID) -> list[CategoryEntity]: ...


@runtime_checkable
class ProviderRepositoryProtocol(Protocol):
    async def get_by_id(self, provider_id: UUID) -> ProviderEntity | None: ...

    async def find_within_radius(
            self,
            lat: float,
            lon: float,
            radius_km: float,
            limit: int = 50,
            offset: int = 0,
    ) -> list[ProviderEntity]: ...

    async def search(
            self,
            lat: float | None = None,
            lon: float | None = None,
            radius_km: float | None = None,
            category_id: UUID | None = None,
            service_id: UUID | None = None,
            price_min: int | None = None,
            price_max: int | None = None,
            sort_by: str = "distance",
            limit: int = 20,
            offset: int = 0,
    ) -> list[ProviderEntity]: ...

    async def count_search_results(
            self,
            lat: float | None = None,
            lon: float | None = None,
            radius_km: float | None = None,
            category_id: UUID | None = None,
            service_id: UUID | None = None,
    ) -> int: ...
```

---

## 8. Phase 3: Application Layer

### Task 3.1: Repository Implementations (GEOMETRY)

**Оценка:** 2 дня | **Приоритет:** P0 | **Зависимости:** 2.3

**Подзадачи:**

| ID    | Файл                                                              | Описание                                 |
|-------|-------------------------------------------------------------------|------------------------------------------|
| 3.1.1 | `app/features/catalog/infrastructure/repositories.py`             | `SqlAlchemyCategoryRepository`           |
| 3.1.2 | ...                                                               | `SqlAlchemyServiceRepository`            |
| 3.1.3 | ...                                                               | `SqlAlchemyProviderRepository` с PostGIS |
| 3.1.4 | ...                                                               | `SqlAlchemyFavoriteRepository`           |
| 3.1.5 | `app/features/catalog/infrastructure/unit_of_work.py`             | `CatalogUnitOfWork`                      |
| 3.1.6 | `tests/unit/features/catalog/infrastructure/test_repositories.py` | Mocked tests                             |
| 3.1.7 | `tests/integration/test_catalog_repositories.py`                  | Integration tests с test DB              |

**Acceptance Criteria:**

- [ ] Geo-поиск через PostGIS `ST_DWithin` с GEOMETRY
- [ ] km_to_degrees конвертация
- [ ] Eager loading для связанных данных (`selectinload`)
- [ ] Пагинация корректна
- [ ] Coverage >= 99%

**Код (PostGIS GEOMETRY):**

```python
from geoalchemy2 import Geometry
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload
from app.core.db.geo import km_to_degrees, degrees_to_km


class SqlAlchemyProviderRepository:
    def __init__(self, session: AsyncSession) -> None:
        self._session = session

    async def find_within_radius(
            self,
            lat: float,
            lon: float,
            radius_km: float,
            limit: int = 50,
            offset: int = 0,
    ) -> list[ProviderEntity]:
        radius_degrees = km_to_degrees(radius_km, lat)

        search_point = func.ST_SetSRID(
            func.ST_MakePoint(lon, lat),
            4326
        )

        stmt = (
            select(Provider)
            .where(
                func.ST_DWithin(
                    Provider.location,
                    search_point,
                    radius_degrees
                )
            )
            .where(Provider.is_active == True)
            .where(Provider.location.isnot(None))
            .options(
                selectinload(Provider.provider_services).selectinload(
                    ProviderService.service
                )
            )
            .order_by(
                Provider.location.op('<->')(search_point)
            )
            .limit(limit)
            .offset(offset)
        )

        result = await self._session.execute(stmt)
        providers = result.scalars().all()

        return [ProviderMapper.to_entity(p) for p in providers]

    async def search(
            self,
            lat: float | None = None,
            lon: float | None = None,
            radius_km: float | None = None,
            category_id: UUID | None = None,
            service_id: UUID | None = None,
            price_min: int | None = None,
            price_max: int | None = None,
            sort_by: str = "distance",
            limit: int = 20,
            offset: int = 0,
    ) -> list[ProviderEntity]:
        stmt = select(Provider).where(Provider.is_active == True)

        if lat is not None and lon is not None and radius_km is not None:
            radius_degrees = km_to_degrees(radius_km, lat)
            search_point = func.ST_SetSRID(
                func.ST_MakePoint(lon, lat),
                4326
            )

            stmt = stmt.where(
                func.ST_DWithin(
                    Provider.location,
                    search_point,
                    radius_degrees
                )
            )

        if service_id is not None:
            stmt = stmt.join(ProviderService).where(
                ProviderService.service_id == service_id,
                ProviderService.is_active == True
            )

        if category_id is not None:
            stmt = stmt.join(ProviderService).join(Service).where(
                Service.category_id == category_id
            )

        if price_min is not None:
            stmt = stmt.where(
                ProviderService.custom_price >= price_min
            )
        if price_max is not None:
            stmt = stmt.where(
                ProviderService.custom_price <= price_max
            )

        if sort_by == "distance" and lat is not None and lon is not None:
            search_point = func.ST_SetSRID(
                func.ST_MakePoint(lon, lat),
                4326
            )
            stmt = stmt.order_by(
                Provider.location.op('<->')(search_point)
            )
        elif sort_by == "rating":
            stmt = stmt.order_by(Provider.rating_cached.desc())

        stmt = stmt.limit(limit).offset(offset)

        result = await self._session.execute(stmt)
        providers = result.scalars().all()

        return [ProviderMapper.to_entity(p) for p in providers]
```

---

### Task 3.2: Mappers

**Оценка:** 0.5 дня | **Приоритет:** P1 | **Зависимости:** 3.1

**Подзадачи:**

| ID    | Файл                                                         | Описание                            |
|-------|--------------------------------------------------------------|-------------------------------------|
| 3.2.1 | `app/features/catalog/infrastructure/mappers.py`             | `CategoryMapper` ORM ↔ Entity       |
| 3.2.2 | ...                                                          | `ServiceMapper`                     |
| 3.2.3 | ...                                                          | `ProviderMapper` с geo-конвертацией |
| 3.2.4 | ...                                                          | `FavoriteMapper`                    |
| 3.2.5 | `tests/unit/features/catalog/infrastructure/test_mappers.py` | Tests                               |

**Acceptance Criteria:**

- [ ] Двусторонняя конвертация ORM ↔ Entity
- [ ] GeoJSON/WKT ↔ Location конвертация корректна
- [ ] Coverage >= 99%

**Код:**

```python
from app.features.catalog.domain.entities import (
    CategoryEntity,
    ServiceEntity,
    ProviderEntity,
    FavoriteEntity,
    Location,
    PriceRange,
    I18nString,
)


class ProviderMapper:
    @staticmethod
    def to_entity(model: Provider) -> ProviderEntity:
        location: Location | None = None

        if model.location is not None:
            wkt = str(model.location)
            coords = wkt.replace("SRID=4326;POINT(", "").replace(")", "")
            lon_str, lat_str = coords.split(" ")
            location = Location(lat=float(lat_str), lon=float(lon_str))

        return ProviderEntity(
            id=model.id,
            user_id=model.user_id,
            display_name=model.display_name,
            bio=model.bio,
            avatar_url=model.avatar_url,
            phone=model.phone,
            location=location,
            address=model.address,
            rating_cached=model.rating_cached,
            reviews_count=model.reviews_count,
            is_verified=model.is_verified,
            is_active=model.is_active,
            created_at=model.created_at,
            updated_at=model.updated_at,
        )

    @staticmethod
    def to_model(entity: ProviderEntity) -> Provider:
        location_wkt = None
        if entity.location is not None:
            location_wkt = entity.location.to_wkt()

        return Provider(
            id=entity.id,
            user_id=entity.user_id,
            display_name=entity.display_name,
            bio=entity.bio,
            avatar_url=entity.avatar_url,
            phone=entity.phone,
            location=location_wkt,
            address=entity.address,
            rating_cached=entity.rating_cached,
            reviews_count=entity.reviews_count,
            is_verified=entity.is_verified,
            is_active=entity.is_active,
        )
```

---

### Task 3.3: Use Cases

**Оценка:** 2 дня | **Приоритет:** P0 | **Зависимости:** 3.1

**Подзадачи:**

| ID    | Файл                                                             | Описание                                                             |
|-------|------------------------------------------------------------------|----------------------------------------------------------------------|
| 3.3.1 | `app/features/catalog/application/use_cases/get_categories.py`   | `GetCategoriesUseCase`                                               |
| 3.3.2 | `app/features/catalog/application/use_cases/get_services.py`     | `GetServicesByCategoryUseCase`                                       |
| 3.3.3 | `app/features/catalog/application/use_cases/search_providers.py` | `SearchProvidersUseCase` (geo + filters)                             |
| 3.3.4 | `app/features/catalog/application/use_cases/get_provider.py`     | `GetProviderDetailUseCase`                                           |
| 3.3.5 | `app/features/catalog/application/use_cases/favorites.py`        | `AddFavoriteUseCase`, `RemoveFavoriteUseCase`, `GetFavoritesUseCase` |
| 3.3.6 | `tests/unit/features/catalog/application/use_cases/test_*.py`    | Tests (80%+ coverage)                                                |

**Acceptance Criteria:**

- [ ] Use cases используют только repository protocols
- [ ] Business logic в use cases, не в repositories
- [ ] Coverage >= 99%

**Код:**

```python
from dataclasses import dataclass
from uuid import UUID


@dataclass
class SearchProvidersRequest:
    lat: float
    lon: float
    radius_km: float = 10.0
    category_id: UUID | None = None
    service_id: UUID | None = None
    price_min: int | None = None
    price_max: int | None = None
    sort_by: str = "distance"
    limit: int = 20
    offset: int = 0


@dataclass
class ProviderWithDistance:
    provider: ProviderEntity
    distance_km: float | None


@dataclass
class SearchProvidersResponse:
    providers: list[ProviderWithDistance]
    total_count: int


class SearchProvidersUseCase:
    def __init__(self, uow: CatalogUnitOfWorkProtocol) -> None:
        self._uow = uow

    async def execute(self, request: SearchProvidersRequest) -> SearchProvidersResponse:
        if request.radius_km > 100:
            raise InvalidLocationException(_("Максимальный радиус поиска - 100 км"))

        providers = await self._uow.providers.search(
            lat=request.lat,
            lon=request.lon,
            radius_km=request.radius_km,
            category_id=request.category_id,
            service_id=request.service_id,
            price_min=request.price_min,
            price_max=request.price_max,
            sort_by=request.sort_by,
            limit=request.limit,
            offset=request.offset,
        )

        providers_with_distance = []
        for provider in providers:
            distance = provider.calculate_distance_to(request.lat, request.lon)
            providers_with_distance.append(
                ProviderWithDistance(provider=provider, distance_km=distance)
            )

        total_count = await self._uow.providers.count_search_results(
            lat=request.lat,
            lon=request.lon,
            radius_km=request.radius_km,
            category_id=request.category_id,
            service_id=request.service_id,
        )

        return SearchProvidersResponse(
            providers=providers_with_distance,
            total_count=total_count,
        )
```

---

### Task 3.4: Geo Service (Optional)

**Оценка:** 0.5 дня | **Приоритет:** P1 | **Зависимости:** 3.1

**Подзадачи:**

| ID    | Файл                                                                   | Описание             |
|-------|------------------------------------------------------------------------|----------------------|
| 3.4.1 | `app/features/catalog/application/services/geo_service.py`             | `GeoService` wrapper |
| 3.4.2 | `tests/unit/features/catalog/application/services/test_geo_service.py` | Tests                |

**Acceptance Criteria:**

- [ ] Обёртка над `app/shared/geo.py`
- [ ] Coverage >= 99%

---

## 9. Phase 4: Presentation Layer

### Task 4.1: Pydantic Schemas

**Оценка:** 0.5 дня | **Приоритет:** P0 | **Зависимости:** 2.1

**Подзадачи:**

| ID    | Файл                                             | Описание                                                             |
|-------|--------------------------------------------------|----------------------------------------------------------------------|
| 4.1.1 | `app/features/catalog/infrastructure/schemas.py` | `CategoryResponse`, `CategoryListResponse`                           |
| 4.1.2 | ...                                              | `ServiceResponse`, `ServiceListResponse`                             |
| 4.1.3 | ...                                              | `ProviderResponse`, `ProviderListResponse`, `ProviderDetailResponse` |
| 4.1.4 | ...                                              | `FavoriteResponse`, `FavoriteListResponse`                           |
| 4.1.5 | ...                                              | `ProviderSearchRequest` (query params)                               |
| 4.1.6 | ...                                              | `LocationSchema` (lat/lon validation)                                |
| 4.1.7 | `tests/unit/features/catalog/test_schemas.py`    | Tests                                                                |

**Acceptance Criteria:**

- [ ] Все schemas имеют examples для OpenAPI
- [ ] Validation для geo-координат (lat: -90..90, lon: -180..180)
- [ ] Coverage >= 99%

**Код:**

```python
from pydantic import BaseModel, Field, field_validator
from uuid import UUID


class LocationSchema(BaseModel):
    lat: float = Field(..., ge=-90, le=90, description="Широта")
    lon: float = Field(..., ge=-180, le=180, description="Долгота")

    @field_validator("lat", "lon")
    @classmethod
    def validate_coordinates(cls, v: float) -> float:
        return round(v, 6)


class ProviderSearchRequest(BaseModel):
    lat: float = Field(..., ge=-90, le=90, description="Широта центра поиска")
    lon: float = Field(..., ge=-180, le=180, description="Долгота центра поиска")
    radius_km: float = Field(10.0, ge=0.1, le=100, description="Радиус поиска в км")
    category_id: UUID | None = Field(None, description="ID категории")
    service_id: UUID | None = Field(None, description="ID услуги")
    price_min: int | None = Field(None, ge=0, description="Минимальная цена в копейках")
    price_max: int | None = Field(None, ge=0, description="Максимальная цена в копейках")
    sort_by: str = Field("distance", pattern="^(distance|rating)$")
    limit: int = Field(20, ge=1, le=100)
    offset: int = Field(0, ge=0)

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "lat": 32.0853,
                    "lon": 34.7818,
                    "radius_km": 10.0,
                    "category_id": "uuid-here",
                    "sort_by": "distance",
                    "limit": 20,
                    "offset": 0,
                }
            ]
        }
    }


class ProviderResponse(BaseModel):
    id: UUID
    display_name: str
    bio: str | None
    avatar_url: str | None
    location: LocationSchema | None
    address: str | None
    rating_cached: float
    reviews_count: int
    is_verified: bool
    distance_km: float | None = None

    model_config = {"from_attributes": True}


class ProviderListResponse(BaseModel):
    providers: list[ProviderResponse]
    total_count: int
    limit: int
    offset: int
```

---

### Task 4.2: Controllers (API Endpoints)

**Оценка:** 1.5 дня | **Приоритет:** P0 | **Зависимости:** 4.1, 3.3

**Подзадачи:**

| ID    | Файл                                               | Endpoint                                        |
|-------|----------------------------------------------------|-------------------------------------------------|
| 4.2.1 | `app/features/catalog/presentation/controllers.py` | `GET /api/v1/categories`                        |
| 4.2.2 | ...                                                | `GET /api/v1/categories/{id}/services`          |
| 4.2.3 | ...                                                | `GET /api/v1/providers` (geo + filters)         |
| 4.2.4 | ...                                                | `GET /api/v1/providers/{id}`                    |
| 4.2.5 | ...                                                | `POST /api/v1/providers/{id}/favorite` (auth)   |
| 4.2.6 | ...                                                | `DELETE /api/v1/providers/{id}/favorite` (auth) |
| 4.2.7 | ...                                                | `GET /api/v1/me/favorites` (auth)               |
| 4.2.8 | `tests/integration/test_catalog_api.py`            | Integration tests                               |
| 4.2.9 | `app/main.py`                                      | Register catalog router                         |

**Acceptance Criteria:**

- [ ] Публичные endpoints работают без авторизации
- [ ] Favorites endpoints требуют авторизацию (`get_current_user`)
- [ ] Rate limiting применён
- [ ] OpenAPI docs корректны
- [ ] Coverage >= 99%

**Код:**

```python
from fastapi import APIRouter, Depends, Query, status
from typing import Annotated

from app.features.auth.presentation.dependencies import get_current_user
from app.features.catalog.application.use_cases import (
    GetCategoriesUseCase,
    SearchProvidersUseCase,
    AddFavoriteUseCase,
)
from app.features.catalog.infrastructure.schemas import (
    CategoryListResponse,
    ProviderListResponse,
    ProviderSearchRequest,
)

router = APIRouter(prefix="/api/v1", tags=["catalog"])


@router.get("/categories", response_model=CategoryListResponse)
async def get_categories(
        uow: Annotated[CatalogUnitOfWork, Depends(get_catalog_uow)]
) -> CategoryListResponse:
    use_case = GetCategoriesUseCase(uow)
    categories = await use_case.execute()
    return CategoryListResponse(categories=categories)


@router.get("/providers", response_model=ProviderListResponse)
async def search_providers(
        lat: float = Query(..., ge=-90, le=90, description="Широта"),
        lon: float = Query(..., ge=-180, le=180, description="Долгота"),
        radius_km: float = Query(10.0, ge=0.1, le=100, description="Радиус в км"),
        category_id: UUID | None = Query(None),
        service_id: UUID | None = Query(None),
        price_min: int | None = Query(None, ge=0),
        price_max: int | None = Query(None, ge=0),
        sort_by: str = Query("distance", pattern="^(distance|rating)$"),
        limit: int = Query(20, ge=1, le=100),
        offset: int = Query(0, ge=0),
        uow: Annotated[CatalogUnitOfWork, Depends(get_catalog_uow)] = None,
) -> ProviderListResponse:
    use_case = SearchProvidersUseCase(uow)
    request = SearchProvidersRequest(
        lat=lat,
        lon=lon,
        radius_km=radius_km,
        category_id=category_id,
        service_id=service_id,
        price_min=price_min,
        price_max=price_max,
        sort_by=sort_by,
        limit=limit,
        offset=offset,
    )
    response = await use_case.execute(request)
    return response


@router.post(
    "/providers/{provider_id}/favorite",
    status_code=status.HTTP_201_CREATED,
)
async def add_favorite(
        provider_id: UUID,
        current_user: Annotated[User, Depends(get_current_user)],
        uow: Annotated[CatalogUnitOfWork, Depends(get_catalog_uow)]
) -> None:
    use_case = AddFavoriteUseCase(uow)
    await use_case.execute(user_id=current_user.id, provider_id=provider_id)
```

---

### Task 4.3: Dependencies

**Оценка:** 0.5 дня | **Приоритет:** P1 | **Зависимости:** 3.1

**Подзадачи:**

| ID    | Файл                                                | Описание                    |
|-------|-----------------------------------------------------|-----------------------------|
| 4.3.1 | `app/features/catalog/presentation/dependencies.py` | `get_catalog_uow()`         |
| 4.3.2 | ...                                                 | `get_category_repository()` |
| 4.3.3 | ...                                                 | `get_provider_repository()` |

**Acceptance Criteria:**

- [ ] Dependency injection работает
- [ ] Тестирование с overrides через `app.dependency_overrides`

**Код:**

```python
from typing import Annotated

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.db.db import get_db_session
from app.features.catalog.infrastructure.unit_of_work import CatalogUnitOfWork


async def get_catalog_uow(
        db: Annotated[AsyncSession, Depends(get_db_session)]
) -> CatalogUnitOfWork:
    return CatalogUnitOfWork(session=db)
```

---

## 10. Phase 5: Testing & Documentation

### Task 5.1: Unit Tests

**Оценка:** 1 день | **Приоритет:** P0 | **Зависимости:** Phase 2-4

**Coverage target: 99%**

**Подзадачи:**

| ID    | Файл                                                 | Coverage target |
|-------|------------------------------------------------------|-----------------|
| 5.1.1 | `tests/unit/features/catalog/domain/`                | 100%            |
| 5.1.2 | `tests/unit/features/catalog/application/use_cases/` | 99%+            |
| 5.1.3 | `tests/unit/features/catalog/infrastructure/`        | 99%+            |

**Acceptance Criteria:**

- [ ] Coverage >= 99% для catalog feature
- [ ] Все tests проходят
- [ ] `pytest tests/unit/features/catalog/ -v --cov=app/features/catalog --cov-report=term-missing`

**Примеры тестов (GEOMETRY):**

```python
def test_location_distance_euclidean():
    tel_aviv = Location(lat=32.0853, lon=34.7818)
    jerusalem = Location(lat=31.7683, lon=35.2137)

    distance = tel_aviv.distance_to_euclidean(jerusalem)

    assert 50 < distance < 60


def test_km_to_degrees_conversion():
    from app.core.db.geo import km_to_degrees, degrees_to_km

    degrees = km_to_degrees(50, 31.5)
    km_back = degrees_to_km(degrees, 31.5)

    assert abs(km_back - 50) < 1
```

---

### Task 5.2: Integration Tests

**Оценка:** 1.5 дня | **Приоритет:** P0 | **Зависимости:** 4.2

**Подзадачи:**

| ID    | Файл                                       | Описание                         |
|-------|--------------------------------------------|----------------------------------|
| 5.2.1 | `tests/integration/test_categories_api.py` | GET /categories                  |
| 5.2.2 | `tests/integration/test_providers_api.py`  | GET /providers с geo params      |
| 5.2.3 | `tests/integration/test_favorites_api.py`  | POST/DELETE/GET favorites (auth) |
| 5.2.4 | `tests/integration/test_pagination.py`     | Page/limit tests                 |
| 5.2.5 | `tests/integration/test_error_cases.py`    | 404, 400, 401                    |

**Acceptance Criteria:**

- [ ] Integration tests используют test DB (test_app schema)
- [ ] PostGIS работает в test environment
- [ ] Auth integration для favorites
- [ ] Coverage >= 95%

**Пример (GEOMETRY):**

```python
@pytest.mark.integration
async def test_find_providers_within_radius_geometry(db_session: AsyncSession):
    from app.features.catalog.infrastructure.models import Provider
    from app.features.catalog.infrastructure.repositories import SqlAlchemyProviderRepository

    provider = Provider(
        display_name="Тель-Авив Мастер",
        location="SRID=4326;POINT(34.7818 32.0853)"
    )
    db_session.add(provider)
    await db_session.commit()

    repo = SqlAlchemyProviderRepository(db_session)
    results = await repo.find_within_radius(
        lat=32.0853,
        lon=34.7818,
        radius_km=10
    )

    assert len(results) == 1
    assert results[0].display_name == "Тель-Авив Мастер"
```

---

### Task 5.3: Documentation Update

**Оценка:** 0.5 дня | **Приоритет:** P1 | **Зависимости:** 4.2

**Подзадачи:**

| ID    | Файл                                              |
|-------|---------------------------------------------------|
| 5.3.1 | `docs/business/00_IMPLEMENTATION_STATUS.md`       |
| 5.3.2 | `docs/business/BACKLOG.md`                        |
| 5.3.3 | `docs/architecture/backend/02_DATABASE_SCHEMA.md` |
| 5.3.4 | `docs/architecture/backend/01_API_DESIGN.md`      |

**Acceptance Criteria:**

- [ ] Все документы актуальны
- [ ] Changelog обновлён

---

## 11. Риски и митигация

| Риск                        | Вероятность | Влияние | Митигация                                                     |
|-----------------------------|-------------|---------|---------------------------------------------------------------|
| PostGIS сложность           | Medium      | High    | Spike на 0.5 дня перед стартом (уже есть `app/shared/geo.py`) |
| Performance geo-запросов    | Medium      | High    | GIST индексы + тестирование на 1000+ мастеров                 |
| i18n в JSONB полях          | Low         | Medium  | Использовать существующий pattern из profiles                 |
| Несоответствие терминологии | Low         | Low     | Документировать Provider vs Employee в ADR                    |
| Test coverage 99%           | Medium      | Medium  | TDD подход, писать tests перед кодом                          |
| GEOMETRY точность           | Low         | Low     | Для Израиля ошибка < 0.05%, незаметно для пользователей       |

---

## 12. Definition of Done

### Feature Level

- [ ] Все unit tests проходят (coverage >= 99%)
- [ ] Все integration tests проходят
- [ ] Mypy = 0 ошибок
- [ ] `.\make.ps1 check` проходит
- [ ] API endpoints документированы в OpenAPI
- [ ] Код соответствует CODING_STANDARDS.md
- [ ] PR reviewed и approved

### Sprint Level

- [ ] Demo для stakeholders
- [ ] Documentation updated
- [ ] Seed data готов для тестирования
- [ ] Performance tests на geo-запросах (1000 мастеров, 50 км радиус < 100ms)

---

## 13. Миграция на другие проекции (если понадобится)

### Вариант 1: Переход на GEOGRAPHY (глобальное покрытие)

```sql
ALTER TABLE app.providers
    ALTER COLUMN location TYPE GEOGRAPHY(POINT, 4326);
```

**Обновить код:**

```python
from geoalchemy2 import Geography

location: Mapped[str | None] = mapped_column(
    Geography(geometry_type="POINT", srid=4326)
)

stmt = stmt.where(
    func.ST_DWithin(
        Provider.location,
        search_point,
        radius_km * 1000  # метры, не градусы
    )
)
```

### Вариант 2: Переход на EPSG:3857 (Web Mercator, метры)

```sql
ALTER TABLE app.providers
    ALTER COLUMN location TYPE GEOMETRY(POINT, 3857)
        USING ST_Transform(location, 3857);
```

**Обновить код:**

```python
def create_search_point_3857(lat: float, lon: float):
    return func.ST_Transform(
        func.ST_SetSRID(func.ST_MakePoint(lon, lat), 4326),
        3857
    )


stmt = stmt.where(
    func.ST_DWithin(
        Provider.location,
        create_search_point_3857(lat, lon),
        50000  # 50 км в метрах
    )
)
```

**Миграция займёт ~1-2 часа.**

---

## Итого: 32 задачи, 13-18 дней, Coverage >= 99%

| Phase                 | Дней      | Задач | Coverage |
|-----------------------|-----------|-------|----------|
| Phase 1: Database     | 2.75-3.75 | 7     | 99%      |
| Phase 2: Domain       | 2-3       | 3     | 100%     |
| Phase 3: Application  | 3-4       | 4     | 99%      |
| Phase 4: Presentation | 2-3       | 3     | 99%      |
| Phase 5: Testing      | 2-3       | 3     | 99%      |

**Ключевые оптимизации:**

- KNN оператор `<->` вместо `ST_Distance` в ORDER BY
- GiST/SP-GiST индексы для geo-поиска
- Конфигурация PostgreSQL (work_mem, shared_buffers)
- Hybrid подход: PostGIS для фильтрации, Python для отображения

---

**Выбран: PostGIS GEOMETRY(SRID 4326) для Израиля** ✅
