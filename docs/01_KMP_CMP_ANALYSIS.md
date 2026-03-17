# Анализ Kotlin Multiplatform и Compose Multiplatform

## Обзор

Документ содержит анализ применимости Kotlin Multiplatform (KMP) и Compose Multiplatform (CMP) для проекта Beauty
Service Aggregator.

---

## 1. Kotlin Multiplatform (KMP)

### 1.1. Что такое KMP

Kotlin Multiplatform - технология от JetBrains, позволяющая писать общий код для разных платформ (Android, iOS, Desktop,
Web, Backend) на языке Kotlin.

### 1.2. Уровни разделения кода

| Уровень         | Что разделяется                                       | Покрытие |
|-----------------|-------------------------------------------------------|----------|
| **commonMain**  | Бизнес-логика, модели данных, репозитории, API клиент | ~70-80%  |
| **androidMain** | Android-specific: WorkManager, Biometric, NFC         | ~10-15%  |
| **iosMain**     | iOS-specific: CoreLocation, UserNotifications         | ~10-15%  |

### 1.3. Преимущества для проекта

| Преимущество                 | Описание                                          | Применимость к проекту                  |
|------------------------------|---------------------------------------------------|-----------------------------------------|
| **Единый код бизнес-логики** | Репозитории, use cases, модели - пишутся один раз | Auth, Booking, Catalog - 100% общий код |
| **Ktor Client**              | Единый HTTP клиент для всех платформ              | API интеграция без дублирования         |
| **Kotlinx Serialization**    | Сериализация JSON в общий код                     | Модели запросов/ответов                 |
| **Coroutines**               | Асинхронность работает одинаково везде            | Async/await в репозиториях              |
| **Тестирование**             | Unit тесты пишутся один раз для общего кода       | Экономия времени на тестах              |

### 1.4. Ограничения

| Ограничение       | Решение                                      |
|-------------------|----------------------------------------------|
| **Platform APIs** | Использовать expect/actual механизм          |
| **iOS debugging** | Требует LLDB, но улучшается с каждым релизом |
| **Binary size**   | Оптимизация через compiler plugins           |

### 1.5. Зрелость технологии (2024-2025)

- **Статус**: Production Ready
- **JetBrains**: Полная поддержка
- **Netflix, VMware, Philips**: Используют в production
- **Compose Multiplatform**: Stable для Android, iOS, Desktop

---

## 2. Compose Multiplatform (CMP)

### 2.1. Что такое CMP

Compose Multiplatform - декларативный UI фреймворк от JetBrains, основанный на Jetpack Compose. Позволяет создавать UI
для Android, iOS, Desktop, Web.

### 2.2. Архитектура рендеринга

```
┌─────────────────────────────────────┐
│        Compose UI Code              │
│   (Kotlin DSL - одинаковый везде)   │
└─────────────┬───────────────────────┘
              │
    ┌─────────┴─────────┐
    │                   │
┌───▼───┐          ┌────▼────┐
│Android│          │   iOS   │
│Canvas │          │  Skia   │
└───────┘          └─────────┘
```

### 2.3. Преимущества для проекта

| Преимущество         | Описание                                       | Ценность                   |
|----------------------|------------------------------------------------|----------------------------|
| **100% UI общий**    | Один UI код для Android и iOS                  | -50% времени разработки UI |
| **Hot Reload**       | Compose Preview, изменяемый UI без перезапуска | Быстрая итерация           |
| **Material 3**       | Готовая дизайн-система                         | UI kit из коробки          |
| **State Management** | State hoisting, remember, derivedStateOf       | Предсказуемое состояние    |
| **Accessibility**    | Семантика встроена                             | Поддержка a11y             |

### 2.4. Компоненты для проекта

| Компонент      | Описание                            | Готовность       |
|----------------|-------------------------------------|------------------|
| **Material 3** | Design system                       | Stable           |
| **Navigation** | Voyager, Decompose                  | Stable           |
| **Coil**       | Загрузка изображений                | Stable           |
| **Maps**       | Google Maps, Mapbox (через interop) | Requires interop |
| **Lottie**     | Анимации                            | Stable           |

### 2.5. iOS специфичность

```kotlin
// Compose UI рендерится через Skia на iOS
// Не использует UIKit нативно, но можно интегрировать

@Composable
fun IOSMapWrapper() {
    // Interop с UIKit через UIKitView
    UIKitView(
        factory = { MKMapView() },
        modifier = Modifier.fillMaxSize()
    )
}
```

---

## 3. Сравнение с Flutter

| Критерий                  | Flutter              | KMP + CMP                             |
|---------------------------|----------------------|---------------------------------------|
| **Язык**                  | Dart                 | Kotlin                                |
| **Backend совместимость** | Нет                  | Можно использовать Ktor общим кодом   |
| **Native interop**        | Platform channels    | Прямой доступ к native APIs           |
| **Размер приложения**     | ~10-15 MB            | Android: ~3-5 MB, iOS: ~5-8 MB        |
| **Производительность**    | Skia                 | Android: Native, iOS: Skia            |
| **Hot Reload**            | Да                   | Compose Preview + Hot Reload          |
| **Трудоустройство**       | Flutter разработчики | Android разработчики (легкий переход) |
| **JetBrains поддержка**   | Нет                  | Полная                                |
| **Kotlin экосистема**     | Ограничена           | Полная интеграция                     |

### Почему KMP + CMP лучше для проекта

1. **Единая экосистема с Backend** - Kotlin на сервере и клиенте
2. **Native производительность на Android** - Jetpack Compose native
3. **Постепенное внедрение** - Можно начать с Android, добавить iOS позже
4. **Прямой доступ к native APIs** - Без overhead channels
5. **Команда** - Проще найти Kotlin разработчиков

---

## 4. Архитектура приложения: Feature-First + Clean Architecture

### 4.1. Структура модулей

```
mobile/
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
│
├── shared/                           # KMP Shared Module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/
│       │   │
│       │   ├── core/                 # 📦 Ядро (shared kernel)
│       │   │   ├── network/          # ApiClient, Interceptors
│       │   │   ├── storage/          # DataStore, CacheManager
│       │   │   ├── theme/            # Colors, Typography, Theme
│       │   │   ├── i18n/             # Localization (ru, he, en)
│       │   │   ├── utils/            # Extensions, Validators
│       │   │   └── navigation/       # AppNavigator, Routes
│       │   │
│       │   └── features/             # 🎯 Feature-First: Бизнес-фичи
│       │       │
│       │       ├── auth/             # Фича "Аутентификация"
│       │       │   ├── domain/
│       │       │   │   ├── model/        # User, AuthTokens, Session
│       │       │   │   ├── repository/   # AuthRepository (interface)
│       │       │   │   └── usecase/      # LoginUseCase, RegisterUseCase
│       │       │   │
│       │       │   ├── data/
│       │       │   │   ├── remote/       # AuthApiService
│       │       │   │   ├── local/        # TokenStorage
│       │       │   │   └── repository/   # AuthRepositoryImpl
│       │       │   │
│       │       │   └── presentation/
│       │       │       ├── model/        # AuthState, LoginUiState
│       │       │       ├── viewmodel/    # AuthViewModel
│       │       │       └── ui/           # LoginScreen, RegisterScreen
│       │       │
│       │       ├── catalog/          # Фича "Каталог мастеров"
│       │       │   ├── domain/
│       │       │   │   ├── model/        # Provider, Service, Category
│       │       │   │   ├── repository/   # CatalogRepository
│       │       │   │   └── usecase/      # SearchProvidersUseCase
│       │       │   │
│       │       │   ├── data/
│       │       │   │   ├── remote/       # CatalogApiService
│       │       │   │   ├── local/        # RecentSearchStorage
│       │       │   │   └── repository/   # CatalogRepositoryImpl
│       │       │   │
│       │       │   └── presentation/
│       │       │       ├── model/        # CatalogState, FilterState
│       │       │       ├── viewmodel/    # CatalogViewModel
│       │       │       └── ui/           # SearchScreen, ProviderCard
│       │       │
│       │       ├── booking/          # Фича "Бронирование"
│       │       │   ├── domain/
│       │       │   │   ├── model/        # Booking, TimeSlot, Service
│       │       │   │   ├── repository/   # BookingRepository
│       │       │   │   └── usecase/      # CreateBookingUseCase
│       │       │   │
│       │       │   ├── data/
│       │       │   │   ├── remote/       # BookingApiService
│       │       │   │   └── repository/   # BookingRepositoryImpl
│       │       │   │
│       │       │   └── presentation/
│       │       │       ├── model/        # BookingState, CalendarState
│       │       │       ├── viewmodel/    # BookingViewModel
│       │       │       └── ui/           # BookingFlow, CalendarPicker
│       │       │
│       │       ├── profile/          # Фича "Профиль пользователя"
│       │       │   ├── domain/
│       │       │   ├── data/
│       │       │   └── presentation/
│       │       │
│       │       ├── favorites/        # Фича "Избранное"
│       │       │   ├── domain/
│       │       │   ├── data/
│       │       │   └── presentation/
│       │       │
│       │       ├── schedule/         # Фича "Расписание мастера" (Provider)
│       │       │   ├── domain/
│       │       │   ├── data/
│       │       │   └── presentation/
│       │       │
│       │       └── reviews/          # Фича "Отзывы"
│       │           ├── domain/
│       │           ├── data/
│       │           └── presentation/
│       │
│       ├── androidMain/kotlin/       # Android-specific
│       │   └── platform/
│       │       ├── LocationProvider.kt
│       │       └── PushNotification.kt
│       │
│       └── iosMain/kotlin/           # iOS-specific
│           └── platform/
│               ├── LocationProvider.kt
│               └── PushNotification.kt
│
├── androidApp/                       # Android Application
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/
│       │   └── BeautyApplication.kt
│       └── AndroidManifest.xml
│
└── iosApp/                           # iOS Application
    └── iosApp/
        ├── AppDelegate.swift
        └── ContentView.swift
```

### 4.2. Feature-First принципы

| Принцип                   | Описание                                      |
|---------------------------|-----------------------------------------------|
| **Feature = папка**       | Каждая бизнес-фича в отдельной папке          |
| **3 слоя**                | domain, data, presentation внутри каждой фичи |
| **Domain независим**      | Не зависит от фреймворков и других фич        |
| **Zero cross-imports**    | Фичи общаются через core/ (Shared Kernel)     |
| **Single Responsibility** | Одна фича = одна бизнес-область               |

### 4.3. Пример структуры фичи (Auth)

```
features/auth/
├── domain/                           # Бизнес-логика (100% shared)
│   ├── model/
│   │   ├── User.kt                   # Domain entity
│   │   ├── AuthTokens.kt             # Value object
│   │   └── Session.kt                # Domain entity
│   ├── repository/
│   │   └── AuthRepository.kt         # Interface (Protocol)
│   └── usecase/
│       ├── LoginUseCase.kt           # Use case
│       ├── RegisterUseCase.kt        # Use case
│       └── LogoutUseCase.kt          # Use case
│
├── data/                             # Data layer (100% shared)
│   ├── remote/
│   │   ├── AuthApiService.kt         # Ktor API client
│   │   └── dto/
│   │       ├── LoginRequest.kt       # DTO
│   │       └── AuthResponse.kt       # DTO
│   ├── local/
│   │   └── TokenStorage.kt           # DataStore implementation
│   └── repository/
│       └── AuthRepositoryImpl.kt     # Repository implementation
│
└── presentation/                     # UI layer (100% shared)
    ├── model/
    │   ├── AuthState.kt              # UI State
    │   └── LoginUiState.kt           # Form state
    ├── viewmodel/
    │   └── AuthViewModel.kt          # ViewModel
    └── ui/
        ├── LoginScreen.kt            # Compose screen
        ├── RegisterScreen.kt         # Compose screen
        └── components/
            └── AuthForm.kt           # Reusable component
```

### 4.2. Зависимости (gradle/libs.versions.toml)

```toml
[versions]
kotlin = "1.9.22"
agp = "8.2.2"
compose = "1.6.0"
compose-compiler = "1.5.8"
compose-multiplatform = "1.6.0"
ktor = "2.3.8"
koin = "3.5.3"
voyager = "1.0.0"
coroutines = "1.8.0"
serialization = "1.6.2"
datastore = "1.1.0-beta01"
coil = "3.0.0-alpha04"

[libraries]
# Kotlin
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", "0.5.0" }

# Compose
compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "compose-multiplatform" }
compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "compose-multiplatform" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose-multiplatform" }
compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "compose-multiplatform" }

# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

# DI
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }

# Navigation
voyager-navigator = { module = "cafe.adriel.voyager:voyager-navigator", version.ref = "voyager" }
voyager-screenModel = { module = "cafe.adriel.voyager:voyager-screenmodel", version.ref = "voyager" }
voyager-koin = { module = "cafe.adriel.voyager:voyager-koin", version.ref = "voyager" }

# Storage
datastore-preferences = { module = "androidx.datastore:datastore-preferences-core", version.ref = "datastore" }

# Image loading
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor", version.ref = "coil" }
```

---

## 5. Покрытие бизнес-требований

### 5.1. Эпики и реализация

| Эпик             | Функциональность             | KMP Coverage                 |
|------------------|------------------------------|------------------------------|
| **E1: Auth**     | Регистрация, логин, JWT      | 100% shared                  |
| **E2: Catalog**  | Категории, услуги, поиск     | 95% shared (карты - interop) |
| **E3: Booking**  | Создание, управление бронями | 100% shared                  |
| **E4: Services** | CRUD услуг мастера           | 100% shared                  |
| **E7: i18n**     | Локализация (ru, he, en)     | 100% shared                  |

### 5.2. Платформо-зависимый код

| Функция        | Android                  | iOS                           |
|----------------|--------------------------|-------------------------------|
| **Карты**      | Google Maps SDK          | Google Maps SDK (via interop) |
| **Геолокация** | FusedLocationProvider    | CoreLocation                  |
| **Push**       | Firebase Cloud Messaging | APNs                          |
| **Biometric**  | BiometricPrompt          | LocalAuthentication           |
| **Deep Links** | Intent Filters           | Universal Links               |

---

## 6. Риски и митигация

| Риск                               | Вероятность | Влияние | Митигация                                            |
|------------------------------------|-------------|---------|------------------------------------------------------|
| **iOS debugging сложнее**          | Medium      | Medium  | Использовать Xcode debugging, улучшается с релизами  |
| **Compose iOS производительность** | Low         | Medium  | Skia рендеринг оптимизируется                        |
| **Размер APK/IPA**                 | Low         | Low     | R8 shrinking, resource optimization                  |
| **Maps interop сложность**         | Medium      | Medium  | Использовать готовые библиотеки (multiplatform-maps) |

---

## 7. Рекомендации

### 7.1. Начальный стек

```
✅ Kotlin Multiplatform 1.9.22+
✅ Compose Multiplatform 1.6.0+
✅ Ktor Client (сетевые запросы)
✅ Koin (DI)
✅ Voyager (навигация)
✅ DataStore (локальное хранение)
✅ Coil 3 (изображения)
✅ Kotlinx Serialization
✅ Kotlinx DateTime
```

### 7.2. Порядок внедрения (Feature-First)

1. **Phase 1 (Week 1-2)**: Setup проекта, core модуль (network, theme, utils)
2. **Phase 2 (Week 3-4)**: Auth feature (domain/data/presentation)
3. **Phase 3 (Week 5-6)**: Catalog feature (domain/data/presentation)
4. **Phase 4 (Week 7-8)**: Booking feature (domain/data/presentation)
5. **Phase 5 (Week 9-10)**: Maps integration (interop в catalog feature)
6. **Phase 6 (Week 11-12)**: Provider features (schedule, analytics)

### 7.3. Feature-First Best Practices

| Практика                 | Описание                                      |
|--------------------------|-----------------------------------------------|
| **Feature = папка**      | Каждая фича в `features/{feature}/`           |
| **3 слоя**               | domain, data, presentation внутри каждой фичи |
| **Domain независим**     | Не зависит от фреймворков, Ktor, Compose      |
| **Zero cross-imports**   | Фичи не импортируют друг друга                |
| **Shared Kernel**        | Общий код в `core/`                           |
| **Repository Interface** | В domain/, реализация в data/                 |
| **Use Case**             | Оркестрация бизнес-логики в domain/usecase/   |

---

## 8. Заключение

Kotlin Multiplatform + Compose Multiplatform - оптимальный выбор для проекта:

- **Production Ready** - стабильные релизы, поддержка JetBrains
- **Единый код** - 80-95% общего кода между Android и iOS
- **Native производительность** - особенно на Android
- **Экосистема Kotlin** - совместимость с Backend
- **Постепенное внедрение** - можно начать с Android

**Рекомендация**: Использовать KMP + CMP для разработки мобильного приложения.

---

**Last Updated**: 2026-03-15
