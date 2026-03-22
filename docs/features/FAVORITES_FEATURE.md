# Favorites Feature Documentation

## Overview

**Module**: `feature:favorites`
**Business Value**: Позволяет пользователям сохранять любимых мастеров для быстрого доступа
**Status**: 🟢 NEAR COMPLETE (95%)

---

## Architecture

```
feature/favorites/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/
    │   ├── domain/
    │   │   ├── model/Favorite.kt
    │   │   ├── repository/FavoritesRepository.kt
    │   │   └── usecase/
    │   │       ├── GetFavoritesUseCase.kt
    │   │       ├── AddFavoriteUseCase.kt
    │   │       ├── RemoveFavoriteUseCase.kt
    │   │       └── IsFavoriteUseCase.kt
    │   ├── data/
    │   │   ├── api/FavoritesApiService.kt
    │   │   ├── dto/FavoriteDto.kt
    │   │   ├── mapper/FavoriteMapper.kt
    │   │   └── repository/FavoritesRepositoryImpl.kt
    │   ├── presentation/
    │   │   ├── model/FavoritesUiState.kt
    │   │   ├── screen/FavoritesScreen.kt
    │   │   └── screenmodel/FavoritesScreenModel.kt
    │   └── di/FavoritesModule.kt
    └── commonTest/kotlin/
        ├── domain/usecase/
        │   ├── GetFavoritesUseCaseTest.kt
        │   ├── AddFavoriteUseCaseTest.kt
        │   ├── RemoveFavoriteUseCaseTest.kt
        │   └── IsFavoriteUseCaseTest.kt
        └── presentation/screenmodel/FavoritesScreenModelTest.kt
```

---

## Domain Layer

### Entities

#### Favorite

```kotlin
package com.aggregateservice.feature.favorites.domain.model

import kotlinx.datetime.Instant

/**
 * Domain entity representing a favorite provider.
 *
 * **Note:** Domain models must NOT import Compose/Android dependencies.
 * Stability is ensured by data class immutability.
 */
data class Favorite(
    val providerId: String,
    val businessName: String,
    val logoUrl: String?,
    val rating: Double,
    val reviewCount: Int,
    val address: String,
    val addedAt: Instant,
) {
    val formattedRating: String
        get() = "%.1f".format(rating)

    val reviewCountText: String
        get() = "$reviewCount reviews"
}
```

### Repository Interface

```kotlin
interface FavoritesRepository {
    suspend fun getFavorites(): Result<List<Favorite>>
    suspend fun addFavorite(providerId: String): Result<Unit>
    suspend fun removeFavorite(providerId: String): Result<Unit>
    suspend fun isFavorite(providerId: String): Result<Boolean>
}
```

### UseCases

| UseCase | Description | Input | Output |
|---------|-------------|-------|--------|
| `GetFavoritesUseCase` | Получает список избранного | - | `Result<List<Favorite>>` |
| `AddFavoriteUseCase` | Добавляет мастера в избранное | `providerId: String` | `Result<Unit>` |
| `RemoveFavoriteUseCase` | Удаляет из избранного | `providerId: String` | `Result<Unit>` |
| `IsFavoriteUseCase` | Проверяет статус избранного | `providerId: String` | `Result<Boolean>` |

---

## Data Layer

### DTO

```kotlin
@Serializable
data class FavoriteDto(
    @SerialName("provider_id") val providerId: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("logo_url") val logoUrl: String?,
    @SerialName("rating") val rating: Double,
    @SerialName("review_count") val reviewCount: Int,
    @SerialName("address") val address: String,
    @SerialName("added_at") val addedAt: String,
)
```

### API Service

```kotlin
class FavoritesApiService(private val httpClient: HttpClient) {

    suspend fun getFavorites(): Result<List<FavoriteDto>> =
        safeApiCall {
            httpClient.get("favorites")
        }

    suspend fun addFavorite(providerId: String): Result<Unit> =
        safeApiCall {
            httpClient.post("favorites") {
                setBody(AddFavoriteRequest(providerId))
            }
        }

    suspend fun removeFavorite(providerId: String): Result<Unit> =
        safeApiCall {
            httpClient.delete("favorites/$providerId")
        }

    suspend fun isFavorite(providerId: String): Result<Boolean> =
        safeApiCall {
            httpClient.get("favorites/$providerId/check")
        }
}
```

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/favorites` | List user's favorites |
| POST | `/api/v1/favorites` | Add provider to favorites |
| DELETE | `/api/v1/favorites/{providerId}` | Remove from favorites |
| GET | `/api/v1/favorites/{providerId}/check` | Check if provider is favorite |

---

## Presentation Layer

### UI State (MVI Pattern)

```kotlin
@Stable
data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isRemoving: Boolean = false,
    val error: AppError? = null,
    val favoriteToRemove: Favorite? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && favorites.isEmpty() && error == null

    val isLoaded: Boolean
        get() = favorites.isNotEmpty() && !isLoading

    companion object {
        val Loading = FavoritesUiState(isLoading = true)

        fun error(error: AppError) = FavoritesUiState(
            isLoading = false,
            error = error,
        )
    }
}
```

### ScreenModel (Voyager)

```kotlin
class FavoritesScreenModel(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun loadFavorites() { /* ... */ }
    fun confirmRemove(favorite: Favorite) { /* ... */ }
    fun dismissRemoveDialog() { /* ... */ }
    fun removeFavorite() { /* ... */ }
    fun clearError() { /* ... */ }
}
```

### Screen (Compose)

```kotlin
class FavoritesScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<FavoritesScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        FavoritesScreenContent(
            uiState = uiState,
            onRefresh = screenModel::loadFavorites,
            onRemoveClick = screenModel::confirmRemove,
            onConfirmRemove = screenModel::removeFavorite,
            onDismissRemove = screenModel::dismissRemoveDialog,
            onClearError = screenModel::clearError,
            onProviderClick = { /* Navigate to provider details */ },
        )
    }
}
```

### UI States

| State | Description |
|-------|-------------|
| **Loading** | Initial loading spinner |
| **Content** | List of favorite providers |
| **Empty** | "No favorites yet" message with CTA |
| **Error** | Error message with retry button |
| **Remove Confirmation** | Dialog to confirm removal |

---

## DI Configuration

```kotlin
// FavoritesModule.kt
val favoritesModule = module {
    // API Service
    single { FavoritesApiService(get()) }

    // Repository
    single<FavoritesRepository> {
        FavoritesRepositoryImpl(apiService = get())
    }

    // UseCases (Domain layer)
    factoryOf(::GetFavoritesUseCase)
    factoryOf(::AddFavoriteUseCase)
    factoryOf(::RemoveFavoriteUseCase)
    factoryOf(::IsFavoriteUseCase)

    // ScreenModels (Presentation layer)
    factoryOf(::FavoritesScreenModel)
}
```

**Registration in MainApplication.kt:**
```kotlin
modules(
    // Core modules
    androidCoreModule,
    coreModule,
    i18nModule,
    // Feature modules
    authModule,
    catalogModule,
    bookingModule,
    servicesModule,
    profileModule,
    favoritesModule,  // <- Added
)
```

---

## Feature Isolation

Favorites feature следует принципу Feature Isolation:

- ✅ Нет прямой зависимости от `feature:catalog`
- ✅ Использует собственную модель `Favorite` (не `Provider`)
- ✅ API endpoints независимы
- ✅ DI модуль изолирован

**Integration Points:**
- `ProviderDetailScreen` может использовать `IsFavoriteUseCase` для отображения статуса
- `AddFavoriteUseCase` вызывается из catalog feature через interface в core:navigation

---

## Test Coverage

| Component | Test Class | Tests |
|-----------|------------|-------|
| GetFavoritesUseCase | GetFavoritesUseCaseTest | 7 |
| AddFavoriteUseCase | AddFavoriteUseCaseTest | 4 |
| RemoveFavoriteUseCase | RemoveFavoriteUseCaseTest | 4 |
| IsFavoriteUseCase | IsFavoriteUseCaseTest | 4 |
| FavoritesScreenModel | FavoritesScreenModelTest | 10 |
| **Total** | | **29** |

---

## Dependencies

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core
                implementation(project(":core:network"))
                implementation(project(":core:storage"))
                implementation(project(":core:navigation"))
                implementation(project(":core:di"))
                implementation(project(":core:theme"))
                implementation(project(":core:i18n"))
                implementation(project(":core:utils"))

                // Libraries
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenModel)
                implementation(libs.voyager.koin)

                // Network
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                // Image loading
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}
```

---

## Future Enhancements

1. **Offline Support**: Cache favorites locally with SQLDelight
2. **Sync**: Background sync when coming online
3. **Batch Operations**: Add/remove multiple favorites
4. **Favorites Sharing**: Share favorites list with friends

---

**Last Updated**: 2026-03-22
**Version**: 1.0
