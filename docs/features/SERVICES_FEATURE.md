# Services Feature Documentation

**Feature:** Services (Provider CRUD)
**Status:** ✅ COMPLETE (100%)
**Last Updated:** 2026-03-22
**Sprint:** Sprint 7

---

## 📋 Overview

Services Feature позволяет провайдерам (мастерам) управлять своими услугами через CRUD операции. Провайдеры могут создавать, просматривать, редактировать и удалять услуги, которые они предлагают клиентам.

### Business Value

- Провайдеры могут создавать услуги с детальным описанием, ценой и длительностью
- Клиенты видят актуальный список услуг при бронировании
- Поддержка валидации бизнес-правил на клиенте и сервере

### User Stories

- Как провайдер, я хочу создать услугу с названием, описанием, ценой и длительностью
- Как провайдер, я хочу видеть список всех моих услуг
- Как провайдер, я хочу редактировать существующие услуги
- Как провайдер, я хочу удалять услуги, которые больше не предлагаю

---

## 🏗️ Architecture

### Feature Module Structure

```
feature/services/
├── build.gradle.kts                    # Convention plugin: feature-module
└── src/commonMain/kotlin/
    ├── domain/
    │   ├── model/
    │   │   ├── ProviderService.kt      # Entity
    │   │   ├── CreateServiceRequest.kt # Request model with validation
    │   │   └── UpdateServiceRequest.kt # Request model with partial updates
    │   ├── repository/
    │   │   └── ServicesRepository.kt   # Interface
    │   └── usecase/
    │       ├── GetServicesUseCase.kt
    │       ├── GetServiceByIdUseCase.kt
    │       ├── CreateServiceUseCase.kt
    │       ├── UpdateServiceUseCase.kt
    │       └── DeleteServiceUseCase.kt
    ├── data/
    │   ├── api/
    │   │   └── ServicesApiService.kt   # Ktor + safeApiCall
    │   ├── dto/
    │   │   ├── ServiceDto.kt
    │   │   ├── CreateServiceRequestDto.kt
    │   │   └── UpdateServiceRequestDto.kt
    │   ├── mapper/
    │   │   └── ServiceMapper.kt        # DTO <-> Domain
    │   └── repository/
    │       └── ServicesRepositoryImpl.kt
    ├── presentation/
    │   ├── model/
    │   │   ├── ServicesListUiState.kt  # @Stable, MVI pattern
    │   │   └── ServiceFormUiState.kt   # @Stable, with validation
    │   ├── screen/
    │   │   ├── ServicesListScreen.kt   # Compose UI
    │   │   └── ServiceFormScreen.kt    # Compose UI for create/edit
    │   └── screenmodel/
    │       ├── ServicesListScreenModel.kt  # Voyager + StateFlow
    │       └── ServiceFormScreenModel.kt   # Voyager + StateFlow
    └── di/
        └── ServicesModule.kt           # Koin module
```

### Layer Dependencies

```
Presentation (Compose, Voyager)
    ↓
Domain (UseCases, Repository Interface)
    ↓
Data (Ktor, DTOs, Repository Impl)
    ↓
Core:Network (HttpClient, safeApiCall, AppError)
```

---

## 🎯 Domain Layer

### Entities

#### ProviderService

```kotlin
data class ProviderService(
    val id: String,
    val name: String,
    val description: String?,
    val basePrice: Double,
    val currency: String = "ILS",
    val durationMinutes: Int,
    val categoryId: String,
    val categoryName: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
```

#### CreateServiceRequest

```kotlin
data class CreateServiceRequest(
    val name: String,
    val description: String?,
    val basePrice: Double,
    val durationMinutes: Int,
    val categoryId: String,
)
```

**Validation Rules:**
- `name`: 3-100 characters
- `basePrice`: >= 0
- `durationMinutes`: 5-480 minutes (8 hours max)
- `categoryId`: required

#### UpdateServiceRequest

```kotlin
data class UpdateServiceRequest(
    val name: String?,
    val description: String?,
    val basePrice: Double?,
    val durationMinutes: Int?,
    val categoryId: String?,
    val isActive: Boolean?,
)
```

### Repository Interface

```kotlin
interface ServicesRepository {
    suspend fun getServices(): Result<List<ProviderService>>
    suspend fun getServiceById(id: String): Result<ProviderService>
    suspend fun createService(request: CreateServiceRequest): Result<ProviderService>
    suspend fun updateService(id: String, request: UpdateServiceRequest): Result<ProviderService>
    suspend fun deleteService(id: String): Result<Unit>
}
```

### UseCases

| UseCase | Description | Business Logic |
|---------|-------------|----------------|
| `GetServicesUseCase` | Get all provider services | No validation |
| `GetServiceByIdUseCase` | Get single service | Validates id not blank |
| `CreateServiceUseCase` | Create new service | Full validation (name, price, duration, category) |
| `UpdateServiceUseCase` | Update existing service | Partial validation |
| `DeleteServiceUseCase` | Delete service | Validates id not blank |

---

## 📡 Data Layer

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/provider-services` | List all services |
| POST | `/api/v1/provider-services` | Create service |
| GET | `/api/v1/provider-services/{id}` | Get service details |
| PATCH | `/api/v1/provider-services/{id}` | Update service |
| DELETE | `/api/v1/provider-services/{id}` | Delete service |

### DTOs

```kotlin
@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    val description: String?,
    val base_price: Double,
    val currency: String = "ILS",
    val duration_minutes: Int,
    val category_id: String,
    val category_name: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null,
)

@Serializable
data class CreateServiceRequestDto(
    val name: String,
    val description: String?,
    val base_price: Double,
    val duration_minutes: Int,
    val category_id: String,
)

@Serializable
data class UpdateServiceRequestDto(
    val name: String?,
    val description: String?,
    val base_price: Double?,
    val duration_minutes: Int?,
    val category_id: String?,
    val is_active: Boolean?,
)
```

### Mapper

```kotlin
object ServiceMapper {
    fun toDomain(dto: ServiceDto): ProviderService
    fun toDomain(dtos: List<ServiceDto>): List<ProviderService>
    fun toDto(request: CreateServiceRequest): CreateServiceRequestDto
    fun toDto(request: UpdateServiceRequest): UpdateServiceRequestDto
}
```

### Error Handling

All API calls use `safeApiCall` wrapper:

```kotlin
suspend fun getServices(): Result<List<ProviderService>> {
    return safeApiCall<List<ServiceDto>> {
        client.get("/api/v1/provider-services") { ... }
    }.fold(
        onSuccess = { dtos -> Result.success(ServiceMapper.toDomain(dtos)) },
        onFailure = { error -> Result.failure(error) },
    )
}
```

---

## 🎨 Presentation Layer

### ServicesListUiState

```kotlin
@Stable
data class ServicesListUiState(
    val services: List<ProviderService> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val showDeleteConfirmation: Boolean = false,
    val serviceToDelete: ProviderService? = null,
) {
    val isEmpty: Boolean get() = !isLoading && services.isEmpty() && error == null
    fun canDelete(service: ProviderService): Boolean = true
}
```

### ServiceFormUiState

```kotlin
@Stable
data class ServiceFormUiState(
    val name: String = "",
    val description: String = "",
    val basePrice: String = "",
    val durationMinutes: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val isActive: Boolean = true,

    // Validation errors
    val nameError: String? = null,
    val priceError: String? = null,
    val durationError: String? = null,
    val categoryError: String? = null,

    // State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val isSaved: Boolean = false,

    // Mode
    val isEditMode: Boolean = false,
    val serviceId: String? = null,
) {
    fun isValid(): Boolean =
        name.isNotBlank() && nameError == null &&
        basePrice.isNotBlank() && priceError == null &&
        durationMinutes.isNotBlank() && durationError == null &&
        categoryId.isNotBlank() && categoryError == null
}
```

### ScreenModels

#### ServicesListScreenModel

```kotlin
class ServicesListScreenModel(
    private val getServicesUseCase: GetServicesUseCase,
    private val deleteServiceUseCase: DeleteServiceUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ServicesListUiState())
    val uiState: StateFlow<ServicesListUiState> = _uiState.asStateFlow()

    fun loadServices()
    fun onDeleteClick(service: ProviderService)
    fun confirmDelete()
    fun dismissDeleteConfirmation()
    fun clearError()
}
```

#### ServiceFormScreenModel

```kotlin
class ServiceFormScreenModel(
    private val getServiceByIdUseCase: GetServiceByIdUseCase,
    private val createServiceUseCase: CreateServiceUseCase,
    private val updateServiceUseCase: UpdateServiceUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ServiceFormUiState())
    val uiState: StateFlow<ServiceFormUiState> = _uiState.asStateFlow()

    fun loadService(serviceId: String)
    fun onNameChanged(name: String)
    fun onDescriptionChanged(description: String)
    fun onPriceChanged(price: String)
    fun onDurationChanged(duration: String)
    fun onCategoryChanged(categoryId: String, categoryName: String)
    fun onActiveChanged(isActive: Boolean)
    fun saveService()
    fun clearError()
}
```

### Screens

#### ServicesListScreen

- LazyColumn with swipe-to-delete
- Empty state with "Add Service" button
- Loading indicator
- Delete confirmation dialog
- Error snackbar

#### ServiceFormScreen

- Form fields with validation
- Create/Edit mode
- Save button with loading state
- Success navigation back to list

---

## 💉 DI Layer

### ServicesModule

```kotlin
val servicesModule = module {
    // API Service
    single { ServicesApiService(get()) }

    // Repository
    single<ServicesRepository> { ServicesRepositoryImpl(get()) }

    // UseCases
    factoryOf(::GetServicesUseCase)
    factoryOf(::GetServiceByIdUseCase)
    factoryOf(::CreateServiceUseCase)
    factoryOf(::UpdateServiceUseCase)
    factoryOf(::DeleteServiceUseCase)

    // ScreenModels
    factoryOf(::ServicesListScreenModel)
    factoryOf(::ServiceFormScreenModel)
}
```

### Registration in MainApplication

```kotlin
// androidApp/src/androidMain/kotlin/.../MainApplication.kt
startKoin {
    modules(
        // Core modules
        androidCoreModule,
        coreModule,
        i18nModule,
        // Feature modules
        authModule,
        catalogModule,
        bookingModule,
        servicesModule,  // <-- Registered here
    )
}
```

---

## 🧪 Testing

### Unit Tests (Planned)

| Class | Tests | Coverage |
|-------|-------|----------|
| `CreateServiceUseCaseTest` | Validation tests | 100% |
| `UpdateServiceUseCaseTest` | Partial update tests | 100% |
| `ServiceMapperTest` | DTO <-> Domain mapping | 100% |
| `ServicesListScreenModelTest` | State management | 100% |
| `ServiceFormScreenModelTest` | Validation + Save | 100% |

### Test Cases

**CreateServiceUseCase:**
- ✅ Valid request creates service
- ✅ Empty name returns ValidationError
- ✅ Name < 3 chars returns ValidationError
- ✅ Name > 100 chars returns ValidationError
- ✅ Negative price returns ValidationError
- ✅ Duration < 5 min returns ValidationError
- ✅ Duration > 480 min returns ValidationError
- ✅ Empty categoryId returns ValidationError

---

## 🔗 Dependencies

### Module Dependencies

```
:feature:services
├── :core:config (AppConfig)
├── :core:network (HttpClient, safeApiCall, AppError)
├── :core:storage (TokenStorage - via HttpClient)
├── :core:theme (Material 3 Theme)
├── :core:i18n (Strings)
├── :core:utils (ValidationUtils)
├── :core:navigation (Voyager)
├── :core:di (Koin)
```

### External Dependencies

| Library | Purpose |
|---------|---------|
| Ktor Client | HTTP requests |
| Kotlinx Serialization | JSON serialization |
| Kotlinx Coroutines | Async operations |
| Koin | Dependency Injection |
| Voyager | Navigation & ScreenModel |
| Compose Multiplatform | UI |

---

## 📊 API Reference

### Create Service

**Request:**
```http
POST /api/v1/provider-services
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Женская стрижка",
  "description": "Классическая женская стрижка любой сложности",
  "base_price": 150.0,
  "duration_minutes": 45,
  "category_id": "cat-123"
}
```

**Response (201):**
```json
{
  "id": "svc-456",
  "name": "Женская стрижка",
  "description": "Классическая женская стрижка любой сложности",
  "base_price": 150.0,
  "currency": "ILS",
  "duration_minutes": 45,
  "category_id": "cat-123",
  "category_name": "Парикмахерские услуги",
  "is_active": true,
  "created_at": "2026-03-22T10:00:00Z",
  "updated_at": null
}
```

### List Services

**Request:**
```http
GET /api/v1/provider-services
Authorization: Bearer <access_token>
```

**Response (200):**
```json
[
  {
    "id": "svc-456",
    "name": "Женская стрижка",
    ...
  }
]
```

---

## 🔒 Security

- All endpoints require Bearer token authentication
- Token refresh handled by `AuthInterceptor`
- Unauthorized requests return 401 → automatic token refresh → retry

---

## 📝 Future Enhancements

- [ ] Image upload for services
- [ ] Service variants (e.g., Short/Medium/Long hair)
- [ ] Service packages (bundle multiple services)
- [ ] Service availability scheduling
- [ ] Service pricing tiers (peak/off-peak hours)

---

**Document Version:** 1.0
**Last Updated:** 2026-03-22
**Author:** Development Team
