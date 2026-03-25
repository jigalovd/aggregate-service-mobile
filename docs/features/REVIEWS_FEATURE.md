# Reviews Feature Documentation

## Обзор

**Feature Module**: `:feature:reviews`
**Business Value**: Позволяет клиентам оставлять отзывы о мастерах после оказания услуг, просматривать отзывы других клиентов и статистику рейтингов

## Архитектура

### Clean Architecture Layers

```
feature/reviews/
├── domain/           # Domain Layer (чистый Kotlin)
│   ├── model/        # Entities & Value Objects
│   ├── repository/   # Repository Interfaces
│   └── usecase/      # Use Cases (business logic)
├── data/             # Data Layer
│   ├── api/          # Ktor API Service
│   ├── dto/          # Network DTOs (@Serializable)
│   ├── mapper/       # DTO <-> Domain Mappers
│   └── repository/   # Repository Implementation
├── presentation/     # Presentation Layer
│   ├── model/        # UI State (@Stable)
│   ├── screen/       # Compose Screens
│   ├── screenmodel/  # Voyager ScreenModels
│   └── component/    # Reusable UI Components
└── di/               # Dependency Injection
```

---

## Domain Layer

### Entities

#### Review

```kotlin
data class Review(
    val id: String,
    val providerId: String,
    val clientId: String,
    val clientName: String?,
    val rating: Int,           // 1-5
    val comment: String?,
    val reply: ReviewReply?,   // Provider's response
    val createdAt: Instant,
    val updatedAt: Instant?,
)

data class ReviewReply(
    val text: String,
    val repliedAt: Instant,
)
```

#### ReviewStats

```kotlin
data class ReviewStats(
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>,  // rating -> count
)
```

### Repository Interface

```kotlin
interface ReviewsRepository {
    suspend fun getProviderReviews(
        providerId: String,
        page: Int,
        pageSize: Int,
    ): Result<SearchResult<Review>, AppError>

    suspend fun getReviewStats(providerId: String): Result<ReviewStats, AppError>

    suspend fun createReview(
        providerId: String,
        rating: Int,
        comment: String?,
    ): Result<Review, AppError>

    suspend fun updateReview(
        providerId: String,
        reviewId: String,
        rating: Int,
        comment: String?,
    ): Result<Review, AppError>
}
```

### Use Cases

| Use Case | Описание |
|----------|----------|
| `GetProviderReviewsUseCase` | Получение списка отзывов с пагинацией |
| `GetReviewStatsUseCase` | Получение статистики отзывов (средний рейтинг, распределение) |
| `CreateReviewUseCase` | Создание нового отзыва (требует авторизации) |
| `UpdateReviewUseCase` | Обновление существующего отзыва автора |

---

## Data Layer

### DTOs

```kotlin
@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("client_name") val clientName: String?,
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String?,
    @SerialName("reply") val reply: ReviewReplyDto?,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant?,
)

@Serializable
data class ReviewStatsDto(
    @SerialName("average_rating") val averageRating: Double,
    @SerialName("total_reviews") val totalReviews: Int,
    @SerialName("rating_distribution") val ratingDistribution: Map<String, Int>,
)
```

### API Service

```kotlin
class ReviewsApiService(private val httpClient: HttpClient) {

    suspend fun getProviderReviews(
        providerId: String,
        page: Int,
        pageSize: Int,
    ): Result<ReviewsResponse, AppError>

    suspend fun getReviewStats(providerId: String): Result<ReviewStatsDto, AppError>

    suspend fun createReview(
        providerId: String,
        request: CreateReviewRequest,
    ): Result<ReviewDto, AppError>

    suspend fun updateReview(
        providerId: String,
        reviewId: String,
        request: UpdateReviewRequest,
    ): Result<ReviewDto, AppError>
}
```

### API Endpoints

| Method | Endpoint | Описание |
|--------|----------|----------|
| GET | `/api/v1/providers/{providerId}/reviews` | Список отзывов |
| GET | `/api/v1/providers/{providerId}/reviews/stats` | Статистика отзывов |
| POST | `/api/v1/providers/{providerId}/reviews` | Создать отзыв |
| PATCH | `/api/v1/providers/{providerId}/reviews/{reviewId}` | Обновить отзыв |

---

## Presentation Layer

### UI State

```kotlin
@Stable
data class ReviewsUiState(
    val providerId: String = "",
    val reviews: List<Review> = emptyList(),
    val stats: ReviewStats? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: AppError? = null,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val isWriteDialogOpen: Boolean = false,
    val userReview: Review? = null,
    val writeRating: Int = 0,
    val writeComment: String = "",
    val isSubmitting: Boolean = false,
) {
    companion object {
        val Initial = ReviewsUiState()
    }

    fun canLoadMore(): Boolean = !isLoading && !isLoadingMore && hasMore
}
```

### ScreenModel

```kotlin
class ReviewsScreenModel(
    private val getProviderReviewsUseCase: GetProviderReviewsUseCase,
    private val getReviewStatsUseCase: GetReviewStatsUseCase,
    private val createReviewUseCase: CreateReviewUseCase,
    private val updateReviewUseCase: UpdateReviewUseCase,
) : ScreenModel {

    val uiState: StateFlow<ReviewsUiState>

    fun initialize(providerId: String)
    fun loadMore()
    fun onWriteReviewClick()
    fun onDismissWriteDialog()
    fun onRatingChanged(rating: Int)
    fun onCommentChanged(comment: String)
    fun onSubmitReview()
    fun onRefresh()
    fun clearError()
}
```

### Intents (User Actions)

| Intent | Описание |
|--------|----------|
| `initialize(providerId)` | Загрузка отзывов при открытии экрана |
| `loadMore()` | Подгрузка следующей страницы |
| `onWriteReviewClick()` | Открытие диалога написания отзыва |
| `onDismissWriteDialog()` | Закрытие диалога |
| `onRatingChanged(rating)` | Изменение рейтинга (1-5) |
| `onCommentChanged(comment)` | Изменение комментария |
| `onSubmitReview()` | Отправка отзыва |
| `onRefresh()` | Pull-to-refresh |
| `clearError()` | Закрытие ошибки |

---

## UI Components

### ReviewsScreen

Главный экран со списком отзывов:

- Header со статистикой (средний рейтинг, количество)
- LazyColumn с отзывами
- FAB для написания отзыва
- Pull-to-refresh
- Pagination (load more)

### ReviewCard

Карточка отзыва:

- Аватар клиента (или placeholder)
- Имя клиента
- Рейтинг (звезды)
- Дата отзыва
- Текст комментария
- Ответ мастера (если есть)

### WriteReviewDialog

Диалог написания отзыва:

- Выбор рейтинга (интерактивные звезды)
- Поле для комментария (опционально)
- Кнопки "Отмена" и "Отправить"
- Валидация (минимум 1 звезда)

---

## DI Module

```kotlin
val reviewsModule = module {
    // Repository
    single<ReviewsRepository> {
        ReviewsRepositoryImpl(
            apiService = get(),
        )
    }

    // API Service
    single {
        ReviewsApiService(
            httpClient = get(),
        )
    }

    // Use Cases
    factory { GetProviderReviewsUseCase(get()) }
    factory { GetReviewStatsUseCase(get()) }
    factory { CreateReviewUseCase(get()) }
    factory { UpdateReviewUseCase(get()) }

    // ScreenModel
    factory { (providerId: String) ->
        ReviewsScreenModel(
            getProviderReviewsUseCase = get(),
            getReviewStatsUseCase = get(),
            createReviewUseCase = get(),
            updateReviewUseCase = get(),
        )
    }
}
```

---

## Навигация

### Интеграция с ProviderDetailScreen

```kotlin
// В ProviderDetailScreen
TextButton(
    onClick = { navigator.push(ReviewsScreen(providerId)) }
) {
    Text("Все отзывы (${stats.totalReviews})")
}
```

### Auth Guard

Для написания отзыва требуется авторизация:

```kotlin
fun onWriteReviewClick() {
    if (authState is AuthState.Guest) {
        showAuthPrompt(AuthPromptTrigger.Review)
    } else {
        openWriteDialog()
    }
}
```

---

## Error Handling

Все ошибки обрабатываются через `safeApiCall` и преобразуются в `AppError`:

```kotlin
// В ScreenModel
searchProvidersUseCase(filters)
    .fold(
        onSuccess = { result -> /* update state */ },
        onFailure = { error ->
            _uiState.update { it.copy(error = error.toAppError()) }
        },
    )
```

### Типы ошибок

| Ошибка | Обработка |
|--------|-----------|
| `NetworkError` | "Проверьте интернет-соединение" |
| `Unauthorized` | Редирект на логин |
| `BadRequest` | Валидация на клиенте |
| `NotFound` | "Отзыв не найден" |
| `Conflict` | "Вы уже оставили отзыв" |

---

## Тестирование

### Unit Tests (планируются)

- `GetProviderReviewsUseCaseTest` - тесты получения отзывов
- `GetReviewStatsUseCaseTest` - тесты статистики
- `CreateReviewUseCaseTest` - тесты создания отзыва
- `UpdateReviewUseCaseTest` - тесты обновления отзыва
- `ReviewsScreenModelTest` - тесты UI логики

---

## Зависимости

```
:feature:reviews
├── :core:network (Ktor, safeApiCall, AppError)
├── :core:storage (TokenStorage)
├── :core:navigation (Voyager, Navigator)
├── :core:theme (Material 3)
└── :core:i18n (Localization)
```

---

## Метрики

| Метрика | Значение |
|---------|----------|
| Domain Models | 2 (Review, ReviewStats) |
| Use Cases | 4 |
| DTOs | 2 |
| Screens | 1 |
| Components | 2 (ReviewCard, WriteReviewDialog) |
| API Endpoints | 4 |

---

**Documentation Version**: 1.0
**Last Updated**: 2026-03-22
**Author**: Claude Code
