# 📋 Booking Feature Documentation

**Feature Name**: Booking (Бронирование)
**Epic**: E3
**Status**: 🟢 Complete (100%)
**Last Updated**: 2026-03-22

---

## 📋 Overview

Booking Feature реализует полный цикл бронирования услуг мастеров. Построена на принципах Clean Architecture с Feature Isolation - не имеет прямых зависимостей от feature:catalog.

### Бизнес-ценность

| Функция | Описание | Ценность |
|---------|----------|----------|
| **Create Booking** | Создание бронирования | Пользователи могут записаться на услуги |
| **Select Services** | Выбор нескольких услуг | Гибкость в комбинировании услуг |
| **Select DateTime** | Выбор даты и времени | Удобный выбор свободных слотов |
| **Manage Bookings** | Просмотр и управление бронированиями | Контроль над своими записями |
| **Cancel/Reschedule** | Отмена и перенос | Гибкость при изменении планов |

---

## 🏗️ Architecture

### Feature Isolation Pattern

Booking Feature реализует паттерн изоляции фич:

```
❌ НЕПРАВИЛЬНО:
feature:booking → feature:catalog (прямая зависимость)

✅ ПРАВИЛЬНО:
feature:booking → core:navigation (интерфейсы)
feature:catalog → core:navigation (интерфейсы)
```

**Ключевые принципы:**
- Booking использует собственную модель `BookingService` вместо `Service` из Catalog
- Booking имеет собственный endpoint `/providers/{id}/services` через `BookingApiService`
- Навигация между фичами через `BookingNavigator` интерфейс в `core:navigation`

### Domain Layer (100% shared)

**Пакет**: `feature/booking/src/commonMain/kotlin/domain/`

#### Models

| Класс | Описание |
|-------|----------|
| `Booking` | Entity бронирования (providerId, clientId, startTime, status, items, totalPrice) |
| `BookingItem` | Позиция в бронировании (serviceId, serviceName, price, duration) |
| `BookingService` | Услуга для бронирования (id, name, price, duration, currency) |
| `BookingStatus` | Enum статусов (PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW) |
| `TimeSlot` | Временной слот (startTime, endTime, isAvailable) |

#### Repository Interface

```kotlin
// BookingRepository.kt
interface BookingRepository {
    suspend fun createBooking(
        providerId: String,
        serviceIds: List<String>,
        startTime: Instant,
        notes: String?
    ): Result<Booking>

    suspend fun getBookingById(bookingId: String): Result<Booking>
    suspend fun getClientBookings(
        clientId: String,
        status: String?,
        page: Int,
        pageSize: Int
    ): Result<List<Booking>>

    suspend fun confirmBooking(bookingId: String): Result<Booking>
    suspend fun cancelBooking(bookingId: String, reason: String?): Result<Booking>
    suspend fun rescheduleBooking(
        bookingId: String,
        newStartTime: Instant
    ): Result<Booking>

    suspend fun getAvailableSlots(
        providerId: String,
        date: LocalDate,
        serviceIds: List<String>
    ): Result<List<TimeSlot>>

    suspend fun getProviderServices(providerId: String): Result<List<BookingService>>
}
```

#### UseCases

| UseCase | Описание | Параметры | Возврат |
|---------|----------|-----------|---------|
| `CreateBookingUseCase` | Создание бронирования | providerId, serviceIds, startTime, notes | Result<Booking> |
| `GetBookingByIdUseCase` | Получение бронирования | bookingId | Result<Booking> |
| `GetClientBookingsUseCase` | История бронирований | clientId, status, page, pageSize | Result<List<Booking>> |
| `CancelBookingUseCase` | Отмена бронирования | bookingId, reason | Result<Booking> |
| `RescheduleBookingUseCase` | Перенос бронирования | bookingId, newStartTime | Result<Booking> |
| `GetAvailableSlotsUseCase` | Доступные слоты | providerId, date, serviceIds | Result<List<TimeSlot>> |
| `GetBookingServicesUseCase` | Услуги мастера для брони | providerId | Result<List<BookingService>> |

---

### Data Layer (100% shared)

**Пакет**: `feature/booking/src/commonMain/kotlin/data/`

#### DTOs

```kotlin
@Serializable
data class BookingDto(
    @SerialName("id") val id: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("provider_name") val providerName: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("status") val status: String,
    @SerialName("items") val items: List<BookingItemDto>,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("total_duration_minutes") val totalDurationMinutes: Int,
    @SerialName("currency") val currency: String,
    @SerialName("notes") val notes: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class CreateBookingRequest(
    @SerialName("provider_id") val providerId: String,
    @SerialName("service_ids") val serviceIds: List<String>,
    @SerialName("start_time") val startTime: Instant,
    @SerialName("notes") val notes: String?,
)

@Serializable
data class TimeSlotDto(
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("is_available") val isAvailable: Boolean,
)
```

#### Mappers

```kotlin
// BookingMapper.kt
object BookingMapper {
    fun toDomain(dto: BookingDto): Booking = Booking(
        id = dto.id,
        providerId = dto.providerId,
        providerName = dto.providerName,
        clientId = dto.clientId,
        startTime = Instant.parse(dto.startTime),
        endTime = Instant.parse(dto.endTime),
        status = BookingStatus.fromString(dto.status),
        items = dto.items.map { BookingItemMapper.toDomain(it) },
        totalPrice = dto.totalPrice,
        totalDurationMinutes = dto.totalDurationMinutes,
        currency = dto.currency,
        notes = dto.notes,
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    fun toDomainList(dtos: List<BookingDto>): List<Booking> =
        dtos.map { toDomain(it) }

    fun toDomainSlots(dtos: List<TimeSlotDto>): List<TimeSlot> =
        dtos.map { TimeSlotMapper.toDomain(it) }
}
```

#### API Service

```kotlin
// BookingApiService.kt
class BookingApiService(
    private val client: HttpClient
) {
    suspend fun createBooking(request: CreateBookingRequest): Result<BookingDto> {
        return safeApiCall<BookingDto> {
            client.post("/bookings") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun getBookingById(bookingId: String): Result<BookingDto> {
        return safeApiCall<BookingDto> {
            client.get("/bookings/$bookingId")
        }
    }

    suspend fun getClientBookings(
        clientId: String,
        status: String?,
        page: Int,
        pageSize: Int
    ): Result<List<BookingDto>> {
        return safeApiCall {
            client.get("/clients/$clientId/bookings") {
                parameter("status", status)
                parameter("page", page)
                parameter("page_size", pageSize)
            }
        }
    }

    suspend fun getAvailableSlots(
        providerId: String,
        date: LocalDate,
        serviceIds: List<String>
    ): Result<List<TimeSlotDto>> {
        return safeApiCall {
            client.get("/providers/$providerId/slots") {
                parameter("date", date.toString())
                serviceIds.forEach { parameter("service_ids", it) }
            }
        }
    }

    suspend fun getProviderServices(providerId: String): Result<List<ServiceDto>> {
        return safeApiCall {
            client.get("/providers/$providerId/services")
        }
    }
}
```

---

### Presentation Layer (100% shared)

**Пакет**: `feature/booking/src/commonMain/kotlin/presentation/`

#### UI States (UDF Pattern)

```kotlin
// SelectServiceUiState.kt
@Stable
data class SelectServiceUiState(
    val services: List<BookingService> = emptyList(),
    val selectedServices: List<BookingService> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
) {
    val totalPrice: Double
        get() = selectedServices.sumOf { it.price }

    val totalDurationMinutes: Int
        get() = selectedServices.sumOf { it.durationMinutes }

    val hasSelection: Boolean
        get() = selectedServices.isNotEmpty()

    val formattedTotal: String
        get() = if (hasSelection) {
            val currency = selectedServices.firstOrNull()?.currency ?: "ILS"
            "%.0f %s".format(totalPrice, currency)
        } else {
            "0 ILS"
        }

    fun isSelected(service: BookingService): Boolean =
        selectedServices.any { it.id == service.id }
}

// SelectDateTimeUiState.kt
@Stable
data class SelectDateTimeUiState(
    val selectedDate: LocalDate? = null,
    val availableSlots: List<TimeSlot> = emptyList(),
    val selectedSlot: TimeSlot? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
)

// BookingConfirmationUiState.kt
@Stable
data class BookingConfirmationUiState(
    val providerName: String = "",
    val services: List<BookingService> = emptyList(),
    val selectedSlot: TimeSlot? = null,
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val bookingResult: Result<Booking>? = null,
    val error: AppError? = null,
)

// BookingHistoryUiState.kt
@Stable
data class BookingHistoryUiState(
    val bookings: List<Booking> = emptyList(),
    val selectedStatus: BookingStatus? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
)
```

#### ScreenModels (Voyager)

```kotlin
// SelectServiceScreenModel.kt
class SelectServiceScreenModel(
    private val getBookingServicesUseCase: GetBookingServicesUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(SelectServiceUiState.Loading)
    val uiState: StateFlow<SelectServiceUiState> = _uiState.asStateFlow()

    fun loadServices(providerId: String) { /* ... */ }
    fun toggleServiceSelection(service: BookingService) { /* ... */ }
}

// SelectDateTimeScreenModel.kt
class SelectDateTimeScreenModel(
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(SelectDateTimeUiState())
    val uiState: StateFlow<SelectDateTimeUiState> = _uiState.asStateFlow()

    fun selectDate(date: LocalDate) { /* ... */ }
    fun selectSlot(slot: TimeSlot) { /* ... */ }
}

// BookingConfirmationScreenModel.kt
class BookingConfirmationScreenModel(
    private val createBookingUseCase: CreateBookingUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(BookingConfirmationUiState())
    val uiState: StateFlow<BookingConfirmationUiState> = _uiState.asStateFlow()

    fun updateNotes(notes: String) { /* ... */ }
    fun submitBooking() { /* ... */ }
}

// BookingHistoryScreenModel.kt
class BookingHistoryScreenModel(
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(BookingHistoryUiState.Loading)
    val uiState: StateFlow<BookingHistoryUiState> = _uiState.asStateFlow()

    fun loadBookings(clientId: String) { /* ... */ }
    fun filterByStatus(status: BookingStatus?) { /* ... */ }
    fun cancelBooking(bookingId: String, reason: String?) { /* ... */ }
}
```

#### Screens (Compose)

```kotlin
// SelectServiceScreen.kt
data class SelectServiceScreen(
    val providerId: String,
    val providerName: String,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SelectServiceScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        // Service selection with checkboxes
        // Bottom bar with total price and "Continue" button
    }
}

// SelectDateTimeScreen.kt
data class SelectDateTimeScreen(
    val providerId: String,
    val providerName: String,
    val serviceIds: List<String>,
) : Screen {
    @Composable
    override fun Content() {
        // Calendar date picker
        // Available time slots grid
    }
}

// BookingConfirmationScreen.kt
data class BookingConfirmationScreen(
    val providerId: String,
    val providerName: String,
    val serviceIds: List<String>,
    val selectedSlot: TimeSlot,
) : Screen {
    @Composable
    override fun Content() {
        // Booking summary
        // Notes input
        // "Confirm Booking" button
    }
}

// BookingHistoryScreen.kt
class BookingHistoryScreen : Screen {
    @Composable
    override fun Content() {
        // List of bookings grouped by status
        // Pull-to-refresh
        // Filter chips
    }
}
```

---

### Navigation Layer

**Пакет**: `core:navigation` + `feature/booking/navigation/`

#### Interface (core:navigation)

```kotlin
// BookingNavigator.kt
interface BookingNavigator {
    fun createSelectServiceScreen(
        providerId: String,
        providerName: String
    ): Screen
}
```

#### Implementation (feature:booking)

```kotlin
// BookingNavigatorImpl.kt
class BookingNavigatorImpl : BookingNavigator {
    override fun createSelectServiceScreen(
        providerId: String,
        providerName: String
    ): Screen {
        return SelectServiceScreen(
            providerId = providerId,
            providerName = providerName,
        )
    }
}
```

---

### DI Layer (Koin)

**Файл**: `feature/booking/src/commonMain/kotlin/di/BookingModule.kt`

```kotlin
val bookingModule = module {
    // Navigator for cross-feature navigation
    single<BookingNavigator> { BookingNavigatorImpl() }

    // API Service
    single { BookingApiService(get()) }

    // Repository
    single<BookingRepository> {
        BookingRepositoryImpl(apiService = get())
    }

    // UseCases (Domain layer)
    factoryOf(::CreateBookingUseCase)
    factoryOf(::CancelBookingUseCase)
    factoryOf(::RescheduleBookingUseCase)
    factoryOf(::GetBookingByIdUseCase)
    factoryOf(::GetClientBookingsUseCase)
    factoryOf(::GetAvailableSlotsUseCase)
    factoryOf(::GetBookingServicesUseCase)

    // ScreenModels (Presentation layer)
    factoryOf(::SelectServiceScreenModel)
    factoryOf(::SelectDateTimeScreenModel)
    factoryOf(::BookingConfirmationScreenModel)
    factoryOf(::BookingHistoryScreenModel)
}
```

---

## 🔗 API Endpoints

| Endpoint | Method | Description | Request | Response |
|----------|--------|-------------|---------|----------|
| `/bookings` | POST | Создать бронирование | CreateBookingRequest | BookingDto |
| `/bookings/{id}` | GET | Получить бронирование | Path: id | BookingDto |
| `/bookings/{id}/confirm` | POST | Подтвердить бронирование | Path: id | BookingDto |
| `/bookings/{id}/cancel` | POST | Отменить бронирование | CancelRequest | BookingDto |
| `/bookings/{id}/reschedule` | POST | Перенести бронирование | RescheduleRequest | BookingDto |
| `/clients/{id}/bookings` | GET | Бронирования клиента | Query: status, page | List<BookingDto> |
| `/providers/{id}/slots` | GET | Доступные слоты | Query: date, service_ids | List<TimeSlotDto> |
| `/providers/{id}/services` | GET | Услуги для бронирования | Path: id | List<ServiceDto> |

---

## ⚠️ Error Handling

### AppError Mapping (Booking-specific)

```kotlin
fun AppError.toUserMessage(): String = when (this) {
    is AppError.Conflict -> when {
        message.contains("slot") -> "Это время уже занято"
        message.contains("booking") -> "Конфликт бронирования"
        else -> message
    }
    is AppError.ValidationError -> when (field) {
        "start_time" -> "Выберите время"
        "service_ids" -> "Выберите хотя бы одну услугу"
        else -> "Ошибка валидации: $message"
    }
    else -> message ?: "Произошла неизвестная ошибка"
}
```

---

## 📁 Files Structure

```
feature/booking/
├── build.gradle.kts
└── src/
    └── commonMain/kotlin/com/aggregateservice/feature/booking/
        ├── domain/
        │   ├── model/
        │   │   ├── Booking.kt
        │   │   ├── BookingItem.kt
        │   │   ├── BookingService.kt
        │   │   ├── BookingStatus.kt
        │   │   └── TimeSlot.kt
        │   ├── repository/
        │   │   └── BookingRepository.kt
        │   └── usecase/
        │       ├── CancelBookingUseCase.kt
        │       ├── CreateBookingUseCase.kt
        │       ├── GetAvailableSlotsUseCase.kt
        │       ├── GetBookingByIdUseCase.kt
        │       ├── GetBookingServicesUseCase.kt
        │       ├── GetClientBookingsUseCase.kt
        │       └── RescheduleBookingUseCase.kt
        │
        ├── data/
        │   ├── api/
        │   │   └── BookingApiService.kt
        │   ├── dto/
        │   │   ├── BookingDto.kt
        │   │   ├── BookingItemDto.kt
        │   │   ├── CancelRequest.kt
        │   │   ├── CreateBookingRequest.kt
        │   │   ├── RescheduleRequest.kt
        │   │   ├── ServiceDto.kt
        │   │   └── TimeSlotDto.kt
        │   ├── mapper/
        │   │   ├── BookingMapper.kt
        │   │   └── ServiceMapper.kt
        │   └── repository/
        │       └── BookingRepositoryImpl.kt
        │
        ├── presentation/
        │   ├── model/
        │   │   ├── BookingConfirmationUiState.kt
        │   │   ├── BookingHistoryUiState.kt
        │   │   ├── SelectDateTimeUiState.kt
        │   │   └── SelectServiceUiState.kt
        │   ├── screen/
        │   │   ├── BookingConfirmationScreen.kt
        │   │   ├── BookingHistoryScreen.kt
        │   │   ├── SelectDateTimeScreen.kt
        │   │   └── SelectServiceScreen.kt
        │   └── screenmodel/
        │       ├── BookingConfirmationScreenModel.kt
        │       ├── BookingHistoryScreenModel.kt
        │       ├── SelectDateTimeScreenModel.kt
        │       └── SelectServiceScreenModel.kt
        │
        ├── navigation/
        │   └── BookingNavigatorImpl.kt
        │
        └── di/
            └── BookingModule.kt
```

---

## 📜 Business Rules

### Time Constraints (US-3.x)

| Rule ID | Description | Implementation |
|---------|-------------|----------------|
| **US-3.5** | Клиент может отменить бронирование минимум за 2 часа до начала | `Booking.canCancel` + `CancelBookingUseCase` |
| **US-3.11** | Клиент может перенести бронирование минимум за 2 часа до начала | `Booking.canReschedule` + `RescheduleBookingUseCase` |
| **US-3.34** | Горизонт бронирования ограничен 30 днями вперёд | `CreateBookingUseCase.MAX_ADVANCE_DAYS` |
| **US-3.35** | Минимальное уведомление о бронировании — 2 часа | `CreateBookingUseCase.MIN_BOOKING_NOTICE_HOURS` |

### Validation Constants

```kotlin
// Booking.kt
companion object {
    // US-3.5: Минимальное время до начала для отмены (в часах)
    private const val CANCEL_WINDOW_HOURS = 2L

    // US-3.11: Минимальное время до начала для переноса (в часах)
    private const val RESCHEDULE_WINDOW_HOURS = 2L
}

// CreateBookingUseCase.kt
companion object {
    // US-3.34: Максимальный горизонт бронирования (в днях)
    private const val MAX_ADVANCE_DAYS = 30L

    // US-3.35: Минимальное уведомление о бронировании (в часах)
    private const val MIN_BOOKING_NOTICE_HOURS = 2L
}
```

### Computed Properties

```kotlin
// Booking.kt - Вычисляемые свойства для UI

/**
 * Можно ли отменить бронирование.
 * US-3.5: Клиент может отменить минимум за 2 часа до начала.
 */
val canCancel: Boolean
    get() {
        if (!status.isCancellable || isPast) return false
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val minCancelTime = Instant.fromEpochMilliseconds(
            startTime.toEpochMilliseconds() - CANCEL_WINDOW_HOURS * 60 * 60 * 1000,
        )
        return now <= minCancelTime
    }

/**
 * Можно ли перенести бронирование.
 * US-3.11: Клиент может перенести минимум за 2 часа до начала.
 */
val canReschedule: Boolean
    get() {
        if (!status.isReschedulable || isPast) return false
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val minRescheduleTime = Instant.fromEpochMilliseconds(
            startTime.toEpochMilliseconds() - RESCHEDULE_WINDOW_HOURS * 60 * 60 * 1000,
        )
        return now <= minRescheduleTime
    }
```

---

## 📋 Implementation Progress

### Phase 1: Core Functionality ✅ COMPLETE

| Компонент | Статус | Прогресс |
|-----------|--------|----------|
| Domain Models | ✅ Complete | 100% |
| Domain Repository | ✅ Complete | 100% |
| Domain UseCases (7) | ✅ Complete | 100% |
| Data DTOs | ✅ Complete | 100% |
| Data Mappers | ✅ Complete | 100% |
| Data API Service | ✅ Complete | 100% |
| Data Repository | ✅ Complete | 100% |
| Presentation States | ✅ Complete | 100% |
| Presentation ScreenModels | ✅ Complete | 100% |
| Presentation Screens | ✅ Complete | 100% |
| Navigation (BookingNavigator) | ✅ Complete | 100% |
| DI Module | ✅ Complete | 100% |

---

## 🔗 Integration with Catalog Feature

ProviderDetailScreen интегрирован с booking flow через:

1. **BookingNavigator injection** - получает навигатор через DI
2. **AuthGuard pattern** - проверяет авторизацию перед бронированием
3. **executeProtectedAction** - показывает AuthPromptDialog для гостей

```kotlin
// ProviderDetailScreen.kt
onBookClick = {
    executeProtectedAction(
        isAuthenticated = authState.canWrite,
        trigger = AuthPromptTrigger.Booking,
        onShowPrompt = { showAuthPrompt = true },
    ) {
        uiState.provider?.let { provider ->
            navigator.push(
                bookingNavigator.createSelectServiceScreen(
                    providerId = provider.id,
                    providerName = provider.businessName,
                ),
            )
        }
    }
}
```

---

## 🔗 Dependencies

Booking Feature зависит от следующих core модулей:

| Модуль | Назначение |
|--------|------------|
| `:core:network` | Ktor HTTP client, safeApiCall, AppError |
| `:core:storage` | TokenStorage для авторизованных запросов |
| `:core:di` | Koin интеграция |
| `:core:navigation` | BookingNavigator interface |

**⚠️ Важно:** Booking НЕ зависит от `:feature:catalog`. Используется Feature Isolation pattern.

---

## 🔗 Related Documentation

- [Feature Isolation Pattern](../architecture/FEATURE_ISOLATION.md) - Архитектурный паттерн
- [Network Layer](../architecture/NETWORK_LAYER.md) - Ktor configuration, safeApiCall
- [Implementation Status](../IMPLEMENTATION_STATUS.md) - Общий статус проекта
- [Backend API Reference](../api/BACKEND_API_REFERENCE.md) - API endpoints
- [Auth Feature](AUTH_FEATURE.md) - Аутентификация
- [Catalog Feature](CATALOG_FEATURE.md) - Каталог мастеров

---

**Версия документа**: 1.1
**Last Updated**: 2026-03-24 (Business Rules Documentation US-3.x)
**Maintainer**: Development Team
