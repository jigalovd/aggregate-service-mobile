# Анализ Поставщиков Карт

## Обзор

Документ содержит сравнительный анализ поставщиков карт для мобильного приложения Aggregate Service. Приоритет: **стабильность**.

---

## 1. Критерии оценки

| Критерий | Вес | Описание |
|----------|-----|----------|
| **Стабильность** | 30% | Надежность работы, uptime SLA |
| **iOS + Android** | 20% | Поддержка обеих платформ |
| **KMP совместимость** | 15% | Возможность использования в KMP |
| **Покрытие Израиля** | 15% | Качество карт для целевого региона |
| **Цена** | 10% | Стоимость для MVP и scale |
| **Функциональность** | 10% | Маркеры, кластеризация, стилизация |

---

## 2. Google Maps Platform

### 2.1. Обзор

| Параметр | Значение |
|----------|----------|
| **Компания** | Google |
| **SLA** | 99.9% (Monthly Uptime) |
| **Покрытие** | Глобальное |
| **SDK** | Android, iOS, Web, Flutter |
| **KMP** | Через interop |

### 2.2. Преимущества

| + | Описание |
|---|----------|
| **Стабильность** | Лидер рынка, enterprise-grade надежность |
| **Покрытие Израиля** | Отличное покрытие, актуальные данные |
| **POI данные** | Богатая база точек интереса |
| **Documentation** | Превосходная документация, examples |
| **Community** | Огромное сообщество, StackOverflow |
| **Navigation** | Встроенная навигация (опционально) |

### 2.3. Недостатки

| - | Описание |
|---|----------|
| **Цена** | Дорого при масштабировании |
| **API Key security** | Требует защиты ключей |
| **Usage limits** | Лимиты на бесплатном тарифе |
| **KMP interop** | Требует platform-specific код |

### 2.4. Ценообразование

| Tier | Цена | Лимиты |
|------|------|--------|
| **Free** | $0/мес | 200 USD credit/мес (~28,000 loads) |
| **Pay as you go** | $7/1000 loads | Безлимитный |
| **Enterprise** | Custom | Volume discounts |

**Оценка для MVP**: Free tier достаточно на начальном этапе

### 2.5. KMP Integration

```kotlin
// Android
implementation("com.google.maps.android:maps-compose:4.3.0")

// iOS - через UIKitView interop
@Composable
actual fun MapView() = UIKitView(
    factory = { GMSCMapView() }
)
```

---

## 3. Mapbox

### 3.1. Обзор

| Параметр | Значение |
|----------|----------|
| **Компания** | Mapbox Inc. |
| **SLA** | 99.9% (Business tier) |
| **Покрытие** | Глобальное (OpenStreetMap based) |
| **SDK** | Android, iOS, Web, Flutter |
| **KMP** | Через interop |

### 3.2. Преимущества

| + | Описание |
|---|----------|
| **Кастомизация** | Гибкая стилизация через Mapbox Studio |
| **Offline maps** | Встроенная поддержка оффлайн |
| **Vector tiles** | Быстрая загрузка, малый размер |
| **Price** | Дешевле Google при scale |
| **OpenStreetMap** | Свободные данные |

### 3.3. Недостатки

| - | Описание |
|---|----------|
| **Стабильность** | Хуже чем Google (incidents чаще) |
| **Покрытие Израиля** | Хорошее, но уступает Google |
| **POI данные** | Меньше чем у Google |
| **KMP interop** | Требует platform-specific код |
| **Breaking changes** | Частые изменения API |

### 3.4. Ценообразование

| Tier | Цена | Лимиты |
|------|------|--------|
| **Free** | $0/мес | 50,000 loads/мес |
| **Pay as you go** | $5/1000 loads | Безлимитный |
| **Enterprise** | Custom | Volume discounts |

**Оценка для MVP**: Free tier достаточен

### 3.5. KMP Integration

```kotlin
// Android
implementation("com.mapbox.maps:android:11.7.0")

// iOS - через interop
```

---

## 4. MapLibre (Open Source)

### 4.1. Обзор

| Параметр | Значение |
|----------|----------|
| **Компания** | MapLibre (Open Source) |
| **SLA** | N/A (self-hosted) |
| **Покрытие** | Зависит от источника данных |
| **SDK** | Android, iOS, Web |
| **KMP** | Через interop |

### 4.2. Преимущества

| + | Описание |
|---|----------|
| **Бесплатно** | Open source, без лицензионных платежей |
| **Кастомизация** | Полный контроль |
| **Offline** | Встроенная поддержка |
| **No vendor lock-in** | Независимость от провайдера |
| **Privacy** | Нет телеметрии |

### 4.3. Недостатки

| - | Описание |
|---|----------|
| **Стабильность** | Зависит от self-hosted решения |
| **Нет SLA** | Нет гарантий uptime |
| **Infrastructure** | Требует своего сервера |
| **Support** | Community support только |
| **POI данные** | Требует интеграции отдельно |

### 4.4. Ценообразование

| Tier | Цена |
|------|------|
| **SDK** | Free |
| **Tiles server** | Self-hosted (инфраструктура) |

**Оценка для MVP**: Не рекомендуется (сложность infrastructure)

---

## 5. Яндекс.Карты

### 5.1. Обзор

| Параметр | Значение |
|----------|----------|
| **Компания** | Яндекс |
| **SLA** | 99.5% |
| **Покрытие** | Россия, СНГ, Израиль |
| **SDK** | Android, iOS |
| **KMP** | Через interop |

### 5.2. Преимущества

| + | Описание |
|---|----------|
| **Покрытие Израиля** | Отличное для региона |
| **Русский язык** | Native поддержка |
| **POI данные** | Хорошие для региона |
| **Price** | Конкурентная цена |
| **Offline** | Поддержка оффлайн карт |

### 5.3. Недостатки

| - | Описание |
|---|----------|
| **Глобальное покрытие** | Хуже чем Google/Mapbox |
| **International support** | Ограниченная документация на EN |
| **Sanctions risk** | Риск санкций для компании |
| **KMP interop** | Platform-specific код |

### 5.4. Ценообразование

| Tier | Цена |
|------|------|
| **Free** | 25,000 loads/мес |
| **Pro** | От $100/мес |

**Оценка для MVP**: Не рекомендуется из-за санкционных рисков

---

## 6. Сравнительная таблица

| Критерий | Google Maps | Mapbox | MapLibre | Yandex |
|----------|-------------|--------|----------|--------|
| **Стабильность** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| **iOS + Android** | ✅ | ✅ | ✅ | ✅ |
| **KMP совместимость** | Interop | Interop | Interop | Interop |
| **Покрытие Израиля** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Цена (MVP)** | Free | Free | Free | Free |
| **Цена (Scale)** | $$$ | $$ | Free | $$ |
| **POI данные** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| **Documentation** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| **Community** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Offline** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Взвешенный балл** | **4.5** | **3.8** | **2.5** | **3.5** |

---

## 7. Рекомендация

### 7.1. Primary: Google Maps Platform

**Почему:**

1. **Стабильность** - Enterprise-grade, 99.9% SLA
2. **Покрытие Израиля** - Лучшее покрытие для целевого региона
3. **POI данные** - Богатая база точек интереса (мастерские, салоны)
4. **Documentation** - Превосходная документация
5. **Community** - Огромное сообщество, легко найти решения
6. **Free tier** - Достаточно для MVP

### 7.2. Фолбэк: Mapbox

**Когда использовать:**

- Если Google Maps заблокирован в регионе
- Если нужна глубокая кастомизация стилей
- Если нужен offline-first подход
- Если цена становится критичной при scale

### 7.3. Не рекомендуется для MVP

| Провайдер | Причина |
|-----------|---------|
| **MapLibre** | Сложность infrastructure, нет SLA |
| **Yandex** | Санкционные риски |

---

## 8. План интеграции

### 8.1. Фаза 1: MVP (Google Maps)

```kotlin
// shared/presentation/catalog/widgets/MapWidget.kt

@Composable
expect fun MapView(
    providers: List<Provider>,
    onProviderClick: (Provider) -> Unit,
    userLocation: LatLng?
)
```

```kotlin
// androidMain/kotlin/MapWidget.android.kt

@Composable
actual fun MapView(...) {
    val cameraPositionState = rememberCameraPositionState()

    GoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = Modifier.fillMaxSize()
    ) {
        providers.forEach { provider ->
            Marker(
                state = MarkerState(position = provider.location.toLatLng()),
                onClick = { onProviderClick(provider); true }
            )
        }
    }
}
```

```kotlin
// iosMain/kotlin/MapWidget.ios.kt

@Composable
actual fun MapView(...) {
    UIKitView(
        factory = {
            GMSMapView(
                frame = CGRectZero,
                camera: GMSCameraPosition(...)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { mapView ->
        // Configure markers
    }
}
```

### 8.2. Фаза 2: Абстракция для фолбэка

```kotlin
// shared/domain/repository/MapRepository.kt

interface MapRepository {
    suspend fun searchNearby(
        location: LatLng,
        radius: Double,
        category: String?
    ): List<Provider>

    suspend fun getPlaceDetails(placeId: String): PlaceDetails
}
```

---

## 9. API Keys Security

### 9.1. Android

```gradle
// local.properties (не коммитить!)
MAPS_API_KEY=your_api_key_here

// build.gradle.kts
android {
    defaultConfig {
        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY")
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

### 9.2. iOS

```swift
// AppDelegate.swift
GMSServices.provideAPIKey(Bundle.main.infoDictionary?["MAPS_API_KEY"] as? String)
```

### 9.3. Restrictions

| Платформа | Restriction | Рекомендация |
|-----------|-------------|--------------|
| **Android** | Package name + SHA-1 | Обязательно |
| **iOS** | Bundle ID | Обязательно |
| **API** | HTTP referrer | Для web |

---

## 10. Бюджет на масштабирование

| MAU | Loads/мес | Google Maps | Mapbox |
|-----|-----------|-------------|--------|
| **1,000** | ~50,000 | $0 (Free tier) | $0 (Free tier) |
| **10,000** | ~200,000 | $350 | $250 |
| **100,000** | ~1,000,000 | $2,100 | $1,000 |
| **1,000,000** | ~5,000,000 | $10,500 | $5,000 |

**Вывод**: Mapbox выгоднее при scale > 50,000 MAU

---

## 11. Заключение

### Рекомендация для MVP

**Primary**: Google Maps Platform

- Стабильность: 99.9% SLA
- Покрытие Израиля: Лучшее
- Free tier: Достаточно для MVP
- Documentation: Превосходная

### План миграции

1. **MVP**: Google Maps (Free tier)
2. **Scale**: Мониторинг usage, оптимизация
3. **If needed**: Миграция на Mapbox при превышении бюджета

---

**Last Updated**: 2026-03-14
