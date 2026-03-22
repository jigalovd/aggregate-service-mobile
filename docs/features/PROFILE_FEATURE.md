# Profile Feature Documentation

**Feature:** Profile (User Profile Management)
**Status:** ✅ COMPLETE (100%)
**Last Updated:** 2026-03-22
**Sprint:** Sprint 8

---

## 📋 Overview

Profile Feature позволяет пользователям просматривать и редактировать свой профиль. Пользователи могут обновлять имя, номер телефона и аватар, а также видеть статистику своего аккаунта.

### Business Value

- Пользователи могут просматривать и редактировать личную информацию
- Отображение статистики no-show для прозрачности
- Поддержка аватаров пользователей

### User Stories

- Как пользователь, я хочу видеть свой профиль с личной информацией
- Как пользователь, я хочу редактировать своё имя и телефон
- Как пользователь, я хочу видеть статистику своих бронирований (no-show rate)

---

## 🏗️ Architecture

### Feature Module Structure

```
feature/profile/
├── build.gradle.kts                    # Convention plugin: feature-module
└── src/commonMain/kotlin/
    ├── domain/
    │   ├── model/
    │   │   ├── Profile.kt              # Entity with no-show stats
    │   │   └── UpdateProfileRequest.kt # Request model with validation
    │   ├── repository/
    │   │   └── ProfileRepository.kt    # Interface
    │   └── usecase/
    │       ├── GetProfileUseCase.kt
    │       └── UpdateProfileUseCase.kt
    ├── data/
    │   ├── api/
    │   │   └── ProfileApiService.kt    # Ktor + safeApiCall
    │   ├── dto/
    │   │   ├── ProfileDto.kt
    │   │   └── UpdateProfileRequestDto.kt
    │   ├── mapper/
    │   │   └── ProfileMapper.kt        # DTO <-> Domain
    │   └── repository/
    │       └── ProfileRepositoryImpl.kt
    ├── presentation/
    │   ├── model/
    │   │   └── ProfileUiState.kt       # @Stable, MVI pattern with edit mode
    │   ├── screen/
    │   │   └── ProfileScreen.kt        # Compose UI with view/edit modes
    │   └── screenmodel/
    │       └── ProfileScreenModel.kt   # Voyager + StateFlow
    └── di/
        └── ProfileModule.kt            # Koin module
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

#### Profile

```kotlin
@Stable
data class Profile(
    val id: String,
    val userId: String,
    val fullName: String?,
    val phone: String?,
    val avatarUrl: String?,
    val noShowCount: Int,
    val noShowRate: Double,
) {
    val displayName: String
        get() = fullName?.takeIf { it.isNotBlank() } ?: "User"

    val hasAvatar: Boolean
        get() = !avatarUrl.isNullOrBlank()

    val hasGoodAttendance: Boolean
        get() = noShowRate < 0.1
}
```

#### UpdateProfileRequest

```kotlin
data class UpdateProfileRequest(
    val fullName: String?,
    val phone: String?,
)
```

**Validation Rules:**
- `fullName`: max 100 characters
- `phone`: optional, validated format

### Repository Interface

```kotlin
interface ProfileRepository {
    suspend fun getProfile(): Result<Profile>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<Profile>
}
```

### UseCases

| UseCase | Description | Business Logic |
|---------|-------------|----------------|
| `GetProfileUseCase` | Fetch current user profile | No validation, delegates to repository |
| `UpdateProfileUseCase` | Update user profile | Validates fullName length, phone format |

---

## 📡 Data Layer

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/profile` | Get current user profile |
| PATCH | `/api/v1/profile` | Update profile |

### DTOs

```kotlin
@Serializable
data class ProfileDto(
    val id: String,
    val user_id: String,
    val full_name: String?,
    val phone: String?,
    val avatar_url: String?,
    val no_show_count: Int,
    val no_show_rate: Double,
)

@Serializable
data class UpdateProfileRequestDto(
    val full_name: String?,
    val phone: String?,
)
```

### Mapper

```kotlin
object ProfileMapper {
    fun toDomain(dto: ProfileDto): Profile
    fun toDto(request: UpdateProfileRequest): UpdateProfileRequestDto
}
```

### Error Handling

All API calls use `safeApiCall` wrapper with proper error mapping to `AppError`.

---

## 🎨 Presentation Layer

### ProfileUiState

```kotlin
@Stable
data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,

    // Edit mode
    val isEditing: Boolean = false,
    val editFullName: String = "",
    val editPhone: String = "",
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
) {
    val hasProfile: Boolean get() = profile != null
    val isFormValid: Boolean
        get() = fullNameError == null && phoneError == null &&
                editFullName.isNotBlank()
}
```

### ProfileScreenModel

```kotlin
class ProfileScreenModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile()
    fun startEditing()
    fun cancelEditing()
    fun onFullNameChanged(name: String)
    fun onPhoneChanged(phone: String)
    fun saveProfile()
    fun clearError()
    fun clearSaveSuccess()
}
```

### ProfileScreen

- View mode: Display profile info with edit button
- Edit mode: Form with validation
- Stats section: No-show count and rate
- Avatar display with Coil image loading
- Loading, error, and empty states

---

## 💉 DI Layer

### ProfileModule

```kotlin
val profileModule = module {
    // API Service
    single { ProfileApiService(get()) }

    // Repository
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }

    // UseCases
    factoryOf(::GetProfileUseCase)
    factoryOf(::UpdateProfileUseCase)

    // ScreenModels
    factoryOf(::ProfileScreenModel)
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
        servicesModule,
        profileModule,  // <-- Registered here
    )
}
```

---

## 🧪 Testing

### Unit Tests

| Class | Tests | File |
|-------|-------|------|
| `GetProfileUseCaseTest` | Success, error cases | `domain/usecase/GetProfileUseCaseTest.kt` |
| `UpdateProfileUseCaseTest` | Validation, success, error | `domain/usecase/UpdateProfileUseCaseTest.kt` |
| `ProfileRepositoryImplTest` | API calls, mapping | `data/repository/ProfileRepositoryImplTest.kt` |
| `ProfileScreenModelTest` | State management, editing | `presentation/screenmodel/ProfileScreenModelTest.kt` |

---

## 🔗 Dependencies

### Module Dependencies

```
:feature:profile
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
| Coil 3 | Image loading |

---

## 📊 API Reference

### Get Profile

**Request:**
```http
GET /api/v1/profile
Authorization: Bearer <access_token>
```

**Response (200):**
```json
{
  "id": "prof-123",
  "user_id": "user-456",
  "full_name": "John Doe",
  "phone": "+972501234567",
  "avatar_url": "https://storage.example.com/avatars/user-456.jpg",
  "no_show_count": 1,
  "no_show_rate": 0.05
}
```

### Update Profile

**Request:**
```http
PATCH /api/v1/profile
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "full_name": "John Smith",
  "phone": "+972509876543"
}
```

**Response (200):**
```json
{
  "id": "prof-123",
  "user_id": "user-456",
  "full_name": "John Smith",
  "phone": "+972509876543",
  ...
}
```

---

## 🔒 Security

- All endpoints require Bearer token authentication
- Token refresh handled by `AuthInterceptor`
- Unauthorized requests return 401 → automatic token refresh → retry

---

## 📝 Future Enhancements

- [ ] Avatar upload (image picker + upload API)
- [ ] Email change with verification
- [ ] Phone verification
- [ ] Notification preferences
- [ ] Language preference
- [ ] Delete account functionality

---

**Document Version:** 1.0
**Last Updated:** 2026-03-22
**Author:** Development Team
