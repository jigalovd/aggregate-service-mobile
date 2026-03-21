# 📋 Catalog Feature Documentation

**Feature Name**: Catalog (Каталог мастеров)
**Epic**: E2
**Status**: 🟡 In Progress (70%)
**Last Updated**: 2026-03-21

---

## 📋 Overview

Catalog Feature реализует поиск и просмотр мастеров (providers) и их услуг. Построена на принципах Clean Architecture с полным разделением на Domain, Data, Presentation и DI слои.

### Бизнес-ценность

| Функция | Описание | Ценность |
|---------|----------|----------|
| **Search Providers** | Поиск мастеров по фильтрам | Пользователи находят мастеров по категории, рейтингу, локации |
| **Categories** | Иерархия категорий услуг | Упрощённая навигация по типам услуг |
| **Provider Details** | Детальная информация о мастере | Пользователи оценивают мастера перед бронированием |
| **Services** | Услуги мастера с ценами | Прозрачность ценообразования |

---

## 🏗️ Architecture

### Domain Layer (100% shared)

**Пакет**: `feature/catalog/src/commonMain/kotlin/domain/`

#### Models

| Класс | Описание |
|-------|----------|
| `Provider` | Entity мастера (businessName, rating, location, workingHours) |
| `Service` | Entity услуги (name, description, price, duration) |
| `Category` | Entity категории (id, name, parentId, children) |
| `Location` | Value object для геолокации (latitude, longitude, address) |
| `WorkingHours` | Value object для расписания (monday-sunday с перерывами) |
| `SearchFilters` | Фильтры поиска (categories, rating, location, pagination, sorting) |
| `SearchResult<T>` | Wrapper для пагинированных результатов |

#### Repository Interface

```kotlin
// CatalogRepository.kt
interface CatalogRepository {
    suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>>
    suspend fun getProviderDetails(providerId: String): Result<Provider>
    suspend fun getProviderServices(providerId: String): Result<List<Service>>
    suspend fun getCategories(parentId: String?): Result<List<Category>>
}
```

#### UseCases

| UseCase | Описание | Параметры | Возврат |
|---------|----------|-----------|---------|
| `SearchProvidersUseCase` | Поиск мастеров по фильтрам | SearchFilters | Result<SearchResult<Provider>> |
| `GetProviderDetailsUseCase` | Детали мастера | providerId: String | Result<Provider> |
| `GetProviderServicesUseCase` | Услуги мастера | providerId: String | Result<List<Service>> |
| `GetCategoriesUseCase` | Категории услуг | parentId: String? | Result<List<Category>> |

---

### Data Layer (100% shared)

**Пакет**: `feature/catalog/src/commonMain/kotlin/data/`

#### DTOs

```kotlin
@Serializable
data class ProviderDto(
    @SerialName("id") val id: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("short_description") val shortDescription: String?,
    @SerialName("rating") val rating: Double,
    @SerialName("review_count") val reviewCount: Int,
    @SerialName("location") val location: LocationDto,
    @SerialName("working_hours") val workingHours: WorkingHoursDto?,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("services_count") val servicesCount: Int
)

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("parent_id") val parentId: String?,
    @SerialName("children") val children: List<CategoryDto>? = null
)

@Serializable
data class ServiceDto(
    @SerialName("id") val id: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("base_price") val basePrice: Double,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("category_id") val categoryId: String
)

@Serializable
data class WorkingHoursDto(
    @SerialName("monday") val monday: DayScheduleDto?,
    @SerialName("tuesday") val tuesday: DayScheduleDto?,
    // ... other days
)
```

#### Mappers

```kotlin
// ProviderMapper.kt
object ProviderMapper {
    fun toDomain(dto: ProviderDto): Provider = Provider(
        id = dto.id,
        businessName = dto.businessName,
        shortDescription = dto.shortDescription,
        rating = dto.rating,
        reviewCount = dto.reviewCount,
        location = LocationMapper.toDomain(dto.location),
        workingHours = dto.workingHours?.toDomain() ?: WorkingHours(),
        isVerified = dto.isVerified,
        servicesCount = dto.servicesCount
    )
}

// CategoryMapper.kt
object CategoryMapper {
    fun toDomain(dto: CategoryDto): Category = Category(
        id = dto.id,
        name = dto.name,
        slug = dto.slug,
        parentId = dto.parentId,
        children = dto.children?.map { toDomain(it) }
    )
}
```

#### API Service

```kotlin
// CatalogApiService.kt
class CatalogApiService(
    private val client: HttpClient
) {
    suspend fun searchProviders(filters: SearchFilters): Result<List<ProviderDto>> {
        return safeApiCall<List<ProviderDto>> {
            client.get("/providers") {
                contentType(ContentType.Application.Json)
                filters.categoryIds.forEach { parameter("category_ids", it) }
                filters.minRating?.let { parameter("min_rating", it) }
                filters.latitude?.let { parameter("lat", it) }
                filters.longitude?.let { parameter("lng", it) }
                filters.radius?.let { parameter("radius", it) }
                parameter("page", filters.page)
                parameter("limit", filters.limit)
                parameter("sort_by", filters.sortBy.apiValue)
                parameter("sort_order", filters.sortOrder.apiValue)
            }
        }
    }

    suspend fun getCategories(parentId: String?): Result<List<CategoryDto>> {
        return safeApiCall {
            client.get("/categories") {
                parentId?.let { parameter("parent_id", it) }
            }
        }
    }
}
```

---

### Presentation Layer (70% shared)

**Пакет**: `feature/catalog/src/commonMain/kotlin/presentation/`

#### UI State (UDF Pattern)

```kotlin
// CatalogUiState.kt
@Stable
data class CatalogUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val providers: List<Provider> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val filters: SearchFilters = SearchFilters(),
    val error: AppError? = null,
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
) {
    companion object {
        val Initial = CatalogUiState()
    }

    fun canLoadMore(): Boolean = !isLoading && !isLoadingMore && hasMore
    fun isEmpty(): Boolean = !isLoading && providers.isEmpty() && error == null
    fun hasActiveFilters(): Boolean = selectedCategory != null || filters.minRating != null

    val activeFiltersCount: Int
        get() = listOfNotNull(selectedCategory, filters.minRating, filters.latitude).count()
}
```

#### ScreenModel (Voyager)

```kotlin
// CatalogScreenModel.kt
class CatalogScreenModel(
    private val searchProvidersUseCase: SearchProvidersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(CatalogUiState.Initial)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        searchProviders()
    }

    fun searchProviders() { /* ... */ }
    fun loadMore() { /* ... */ }
    fun onCategorySelected(category: Category?) { /* ... */ }
    fun onSearchQueryChanged(query: String) { /* ... */ }
    fun onClearFilters() { /* ... */ }
    fun clearError() { /* ... */ }
}
```

#### Screen (Compose)

```kotlin
// CatalogScreen.kt
class CatalogScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<CatalogScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        CatalogScreenContent(
            uiState = uiState,
            onSearchQueryChanged = screenModel::onSearchQueryChanged,
            onSearchSubmit = screenModel::onSearchSubmit,
            onCategorySelected = screenModel::onCategorySelected,
            onClearFilters = screenModel::onClearFilters,
            onLoadMore = screenModel::loadMore,
            onProviderClick = { provider -> /* TODO: Navigate */ },
            onClearError = screenModel::clearError,
        )
    }
}
```

---

### DI Layer (Koin)

**Файл**: `feature/catalog/src/commonMain/kotlin/di/CatalogModule.kt`

```kotlin
val catalogModule = module {
    // Repository
    single<CatalogRepository> {
        CatalogRepositoryImpl(apiService = get())
    }

    // UseCases
    factoryOf(::SearchProvidersUseCase)
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::GetProviderDetailsUseCase)
    factoryOf(::GetProviderServicesUseCase)

    // ScreenModel
    factoryOf(::CatalogScreenModel)
}
```

---

## 🔗 API Endpoints

| Endpoint | Method | Description | Request | Response |
|----------|--------|-------------|---------|----------|
| `/providers` | GET | Поиск мастеров | Query params | List<ProviderDto> |
| `/providers/{id}` | GET | Детали мастера | Path: id | ProviderDto |
| `/providers/{id}/services` | GET | Услуги мастера | Path: id | List<ServiceDto> |
| `/categories` | GET | Категории | parent_id (optional) | List<CategoryDto> |

---

## ⚠️ Error Handling

### AppError Mapping (Catalog-specific)

```kotlin
fun AppError.toUserMessage(): String = when (this) {
    is AppError.NotFound -> "Мастер не найден"
    is AppError.ValidationError -> when (field) {
        "category_ids" -> "Некорректная категория"
        "min_rating" -> "Рейтинг должен быть от 0 до 5"
        else -> "Ошибка валидации: $message"
    }
    is AppError.NetworkError -> "Ошибка сети. Проверьте подключение"
    else -> message ?: "Произошла неизвестная ошибка"
}
```

---

## 📁 Files Structure

```
feature/catalog/
├── build.gradle.kts
└── src/
    └── commonMain/kotlin/com/aggregateservice/feature/catalog/
        ├── domain/
        │   ├── model/
        │   │   ├── Category.kt
        │   │   ├── Location.kt
        │   │   ├── Provider.kt
        │   │   ├── SearchFilters.kt
        │   │   ├── SearchResult.kt
        │   │   ├── Service.kt
        │   │   └── WorkingHours.kt
        │   ├── repository/
        │   │   └── CatalogRepository.kt
        │   └── usecase/
        │       ├── GetCategoriesUseCase.kt
        │       ├── GetProviderDetailsUseCase.kt
        │       ├── GetProviderServicesUseCase.kt
        │       └── SearchProvidersUseCase.kt
        │
        ├── data/
        │   ├── api/
        │   │   └── CatalogApiService.kt
        │   ├── dto/
        │   │   ├── CategoryDto.kt
        │   │   ├── ProviderDto.kt
        │   │   ├── ServiceDto.kt
        │   │   └── WorkingHoursDto.kt
        │   ├── mapper/
        │   │   ├── CategoryMapper.kt
        │   │   ├── ProviderMapper.kt
        │   │   └── ServiceMapper.kt
        │   └── repository/
        │       └── CatalogRepositoryImpl.kt
        │
        ├── presentation/
        │   ├── model/
        │   │   └── CatalogUiState.kt
        │   ├── screen/
        │   │   └── CatalogScreen.kt
        │   └── screenmodel/
        │       └── CatalogScreenModel.kt
        │
        └── di/
            └── CatalogModule.kt
```

---

## 📋 Implementation Progress

### Phase 1: Core Functionality ✅ COMPLETE

| Компонент | Статус | Прогресс |
|-----------|--------|----------|
| Domain Models | ✅ Complete | 100% |
| Domain Repository | ✅ Complete | 100% |
| Domain UseCases | ✅ Complete | 100% |
| Data DTOs | ✅ Complete | 100% |
| Data Mappers | ✅ Complete | 100% |
| Data API Service | ✅ Complete | 100% |
| Data Repository | ✅ Complete | 100% |
| Presentation State | ✅ Complete | 100% |
| Presentation ScreenModel | ✅ Complete | 100% |
| Presentation CatalogScreen | ✅ Complete | 100% |
| DI Module | ✅ Complete | 100% |

### Phase 2: Additional Screens ⏳ PENDING

| Компонент | Статус | Прогресс |
|-----------|--------|----------|
| SearchScreen | ⚪ Not Started | 0% |
| SearchScreenModel | ⚪ Not Started | 0% |
| ProviderDetailScreen | ⚪ Not Started | 0% |
| CategorySelectionScreen | ⚪ Not Started | 0% |

### Phase 3: Testing ⏳ PENDING

| Компонент | Статус | Прогресс |
|-----------|--------|----------|
| UseCase Tests | ⚪ Not Started | 0% |
| Mapper Tests | ⚪ Not Started | 0% |
| ScreenModel Tests | ⚪ Not Started | 0% |

---

## 🔗 Dependencies

Catalog Feature зависит от следующих core модулей:

| Модуль | Назначение |
|--------|------------|
| `:core:network` | Ktor HTTP client, safeApiCall, AppError |
| `:core:storage` | TokenStorage для авторизованных запросов |
| `:core:di` | Koin интеграция |
| `:core:navigation` | Voyager Navigator, Screen sealed class |

---

## 🧪 Testing

### Unit Tests (Planned)

| Test | Описание | Coverage |
|------|----------|----------|
| `SearchProvidersUseCaseTest` | Тестирование поиска с фильтрами | Success, Error, Empty |
| `GetCategoriesUseCaseTest` | Тестирование загрузки категорий | Root, Nested |
| `ProviderMapperTest` | Маппинг DTO -> Domain | Full mapping |
| `CatalogScreenModelTest` | Тестирование ScreenModel | State updates, pagination |

### Integration Tests (Planned)

- Полный flow: open catalog -> search -> filter -> pagination
- Error handling scenarios
- Category selection flow

---

## 🔗 Related Documentation

- [Network Layer](../architecture/NETWORK_LAYER.md) - Ktor configuration, safeApiCall
- [Implementation Status](../IMPLEMENTATION_STATUS.md) - Общий статус проекта
- [Backend API Reference](../api/BACKEND_API_REFERENCE.md) - API endpoints
- [Auth Feature](AUTH_FEATURE.md) - Аутентификация

---

**Версия документа**: 1.0
**Last Updated**: 2026-03-21
**Maintainer**: Development Team
