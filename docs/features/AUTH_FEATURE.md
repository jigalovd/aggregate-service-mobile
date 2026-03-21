# 🔐 Auth Feature Documentation

**Feature Name**: Authentication
**Epic**: E1
**Status**: ✅ Complete (100%)
**Last Updated**: 2026-03-21

---

## 📋 Overview

Auth Feature реализует полный цикл аутентификации пользователей с поддержкой **Guest Mode**: незарегистрированные пользователи могут просматривать каталог, а для действий требующих записи (бронирование, отзывы, избранное) им предлагается зарегистрироваться. Построена на принципах Clean Architecture с полным разделением на Domain, Data, Presentation и DI слои.

### Бизнес-ценность

| Функция | Описание | Ценность |
|---------|----------|----------|
| **Guest Mode** | Просмотр каталога без регистрации | Низкий барьер входа для новых пользователей |
| **Login** | Вход по email/паролю | Пользователи получают доступ к персонализированному контенту |
| **Auto Refresh** | Автоматическое обновление токенов | Бесшовный опыт без повторного логина |
| **Logout** | Безопасный выход в Guest состояние | Защита данных пользователя |

### Guest Mode Flow

```
┌─────────────┐    Browse    ┌──────────────┐
│   Guest     │ ──────────►  │   Catalog    │
│  (default)  │              │   Screen     │
└─────────────┘              └──────────────┘
      │
      │  Attempt: Booking/Review/Favorites
      ▼
┌─────────────┐              ┌──────────────┐
│ AuthPrompt  │ ──────────►  │  Login or    │
 │   Dialog   │              │  Register    │
└─────────────┘              └──────────────┘
```

---

## 🏗️ Architecture

### Domain Layer (100% shared)

**Пакет**: `feature/auth/src/commonMain/kotlin/domain/`

#### AuthState Model

```kotlin
// AuthState.kt
sealed class AuthState {

    abstract val userId: String?
    abstract val isAuthenticated: Boolean
    abstract val canWrite: Boolean

    /**
     * Guest state - unregistered user with read-only access.
     * - No token stored
     * - Can browse catalog
     * - Cannot book, review, or save favorites
     */
    data object Guest : AuthState() {
        override val userId: String? = null
        override val isAuthenticated: Boolean = false
        override val canWrite: Boolean = false
    }

    /**
     * Authenticated state - registered user with full access.
     * @property accessToken JWT access token for API calls
     * @property userId User identifier
     * @property userEmail User email (nullable for session restoration)
     */
    data class Authenticated(
        val accessToken: String,
        override val userId: String,
        val userEmail: String? = null,
    ) : AuthState() {
        override val isAuthenticated: Boolean = true
        override val canWrite: Boolean = true
    }

    companion object {
        val Initial: AuthState = Guest
    }
}
```

| Состояние | isAuthenticated | canWrite | Описание |
|-----------|-----------------|----------|----------|
| `Guest` | `false` | `false` | Незарегистрированный пользователь |
| `Authenticated` | `true` | `true` | Зарегистрированный пользователь |

#### Models

| Класс | Описание |
|-------|----------|
| `AuthState` | Sealed class: Guest / Authenticated с `canWrite` property |
| `LoginCredentials` | Value object для учётных данных (email, password) |

#### Repository Interface

```kotlin
// AuthRepository.kt
interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun login(credentials: LoginCredentials): Result<AuthState>
    suspend fun logout(): Result<Unit>
    suspend fun refreshTokens(): Result<String>
}
```

#### UseCases

| UseCase | Описание | Параметры | Возврат |
|---------|----------|-----------|---------|
| `LoginUseCase` | Аутентификация пользователя | LoginCredentials | Result<AuthState> |
| `LogoutUseCase` | Выход в Guest состояние | - | Result<Unit> |
| `ObserveAuthStateUseCase` | Наблюдение за состоянием | - | StateFlow<AuthState> |

---

### Data Layer (100% shared)

**Пакет**: `feature/auth/src/commonMain/kotlin/data/`

#### DTOs

```kotlin
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String = "Bearer"
)

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long
)
```

#### Repository Implementation

```kotlin
// AuthRepositoryImpl.kt
class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Guest)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Restore session from stored token
        ioScope.launch {
            tokenStorage.getAccessToken()?.let { savedToken ->
                _authState.value = AuthState.Authenticated(
                    accessToken = savedToken,
                    userId = "restored",
                    userEmail = null
                )
            }
        }
    }

    override suspend fun login(credentials: LoginCredentials): Result<AuthState> {
        return safeApiCall<AuthResponse> {
            httpClient.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(credentials.email, credentials.password))
            }
        }.fold(
            onSuccess = { response ->
                tokenStorage.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
                val newState = AuthState.Authenticated(
                    accessToken = response.accessToken,
                    userId = credentials.email,
                    userEmail = credentials.email
                )
                _authState.value = newState
                Result.success(newState)
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun logout(): Result<Unit> {
        tokenStorage.clearTokens()
        _authState.value = AuthState.Guest  // Transition to Guest, not Initial
        return Result.success(Unit)
    }
}
```

---

### Presentation Layer (100% shared)

**Пакет**: `feature/auth/src/commonMain/kotlin/presentation/`

#### AuthPromptDialog (Guest Registration Prompt)

```kotlin
// AuthPromptDialog.kt
@Composable
fun AuthPromptDialog(
    trigger: AuthPromptTrigger,
    onDismiss: () -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(trigger.title) },
        text = { Text(trigger.message) },
        confirmButton = {
            TextButton(onClick = onCreateAccount) {
                Text(stringResource("guest_create_account"))
            }
        },
        dismissButton = {
            TextButton(onClick = onSignIn) {
                Text(stringResource("guest_sign_in"))
            }
        }
    )
}
```

#### UI State (UDF Pattern)

```kotlin
// LoginUiState.kt
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val isFormValid: Boolean = false
)
```

---

### Navigation Layer - AuthGuard

**Пакет**: `core/navigation/src/commonMain/kotlin/`

#### AuthGuard Component

```kotlin
// AuthGuard.kt
enum class AuthPromptTrigger {
    Booking,
    Review,
    Favorites;

    val title: StringKey = when (this) {
        Booking -> StringKey.GUEST_PROMPT_BOOKING_TITLE
        Review -> StringKey.GUEST_PROMPT_REVIEW_TITLE
        Favorites -> StringKey.GUEST_PROMPT_FAVORITES_TITLE
    }
}

@Composable
fun AuthGuard(
    isAuthenticated: Boolean,
    trigger: AuthPromptTrigger,
    onShowPrompt: (AuthPromptTrigger) -> Unit,
    content: @Composable () -> Unit
) {
    if (isAuthenticated) {
        content()
    } else {
        // Show prompt instead of protected content
        LaunchedEffect(trigger) {
            onShowPrompt(trigger)
        }
    }
}
```

**Usage Example:**

```kotlin
// In BookingScreen or similar
AuthGuard(
    isAuthenticated = authState.canWrite,
    trigger = AuthPromptTrigger.Booking,
    onShowPrompt = { trigger -> showAuthPrompt(trigger) },
    content = { BookingButton() }
)
```

---

### DI Layer (Koin)

**Файл**: `feature/auth/src/commonMain/kotlin/di/AuthModule.kt`

```kotlin
val authModule = module {
    // Repository
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get(),
            tokenStorage = get(),
            ioDispatcher = Dispatchers.IO
        )
    }

    // UseCases
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ObserveAuthStateUseCase(get()) }

    // ScreenModel
    factory { LoginScreenModel(get(), get(), get()) }
}
```

---

## 🔗 API Endpoints

| Endpoint | Method | Description | Request | Response |
|----------|--------|-------------|---------|----------|
| `/auth/login` | POST | Аутентификация | LoginRequest | AuthResponse |
| `/auth/refresh` | POST | Обновление токена | refresh_token | RefreshTokenResponse |
| `/auth/logout` | POST | Выход из системы | - | 204 No Content |

---

## 🌐 I18n Keys (Guest Mode)

| Key | EN | RU | HE |
|-----|-----|-----|-----|
| `guest_continue_as_guest` | Continue as Guest | Продолжить как гость | המשך כאורח |
| `guest_prompt_booking_title` | Book this service? | Записаться на услугу? | להזמין שירות? |
| `guest_prompt_booking_message` | Create an account to book... | Создайте аккаунт для записи... | צור חשבון להזמנת תורים... |
| `guest_prompt_review_title` | Share your experience? | Поделиться опытом? | לשתף ניסיון? |
| `guest_prompt_favorites_title` | Save for later? | Сохранить? | לשמור? |
| `guest_create_account` | Create Account | Создать аккаунт | צור חשבון |
| `guest_sign_in` | Sign In | Войти | התחברות |
| `guest_maybe_later` | Maybe Later | Позже | אולי מאוחר יותר |

---

## ⚠️ Error Handling

### AppError Mapping

```kotlin
fun AppError.toUserMessage(): String = when (this) {
    is AppError.Unauthorized -> "Неверный email или пароль"
    is AppError.AccountLocked -> "Аккаунт заблокирован до $until"
    is AppError.ValidationError -> when (field) {
        "email" -> "Некорректный email"
        "password" -> "Пароль не соответствует требованиям"
        else -> "Ошибка валидации: $message"
    }
    is AppError.NetworkError -> "Ошибка сети: $message"
    is AppError.RateLimitExceeded -> "Превышен лимит запросов. Повторите через $retryAfter сек"
    is AppError.Forbidden -> "Доступ запрещён"
    else -> message ?: "Произошла неизвестная ошибка"
}
```

---

## 📁 Files Structure

```
feature/auth/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/com/aggregateservice/feature/auth/
    │   ├── domain/
    │   │   ├── model/
    │   │   │   ├── AuthState.kt           # sealed class: Guest, Authenticated
    │   │   │   └── LoginCredentials.kt
    │   │   ├── repository/
    │   │   │   └── AuthRepository.kt
    │   │   └── usecase/
    │   │       ├── LoginUseCase.kt
    │   │       ├── LogoutUseCase.kt
    │   │       └── ObserveAuthStateUseCase.kt
    │   │
    │   ├── data/
    │   │   ├── dto/
    │   │   │   ├── AuthResponse.kt
    │   │   │   ├── LoginRequest.kt
    │   │   │   └── RefreshTokenResponse.kt
    │   │   └── repository/
    │   │       └── AuthRepositoryImpl.kt
    │   │
    │   ├── presentation/
    │   │   ├── component/
    │   │   │   └── AuthPromptDialog.kt    # Guest registration prompt
    │   │   ├── model/
    │   │   │   └── LoginUiState.kt
    │   │   ├── screenmodel/
    │   │   │   └── LoginScreenModel.kt
    │   │   └── screen/
    │   │       └── LoginScreen.kt
    │   │
    │   └── di/
    │       └── AuthModule.kt
    │
    └── commonTest/kotlin/com/aggregateservice/feature/auth/
        ├── domain/
        │   ├── model/
        │   │   ├── AuthStateTest.kt       # Tests for Guest/Authenticated states
        │   │   └── LoginCredentialsTest.kt
        │   └── usecase/
        │       ├── LoginUseCaseTest.kt
        │       ├── LogoutUseCaseTest.kt
        │       └── ObserveAuthStateUseCaseTest.kt
        ├── data/
        │   └── repository/
        │       ├── AuthRepositoryImplTest.kt
        │       └── AuthRepositoryErrorHandlingTest.kt
        └── presentation/
            └── screenmodel/
                └── LoginScreenModelTest.kt

core/navigation/
└── src/commonMain/kotlin/
    └── AuthGuard.kt                       # Write operation protection
```

---

## 🔗 Dependencies

Auth Feature зависит от следующих core модулей:

| Модуль | Назначение |
|--------|------------|
| `:core:network` | Ktor HTTP client, safeApiCall, AppError |
| `:core:storage` | TokenStorage для хранения токенов |
| `:core:di` | Koin интеграция |
| `:core:utils` | EmailValidator, PasswordValidator |
| `:core:navigation` | Voyager Navigator, Screen, AuthGuard |
| `:core:i18n` | StringKey для Guest Mode строк |

---

## 🧪 Testing

### Unit Tests (79 tests)

| Test | Описание | Coverage |
|------|----------|----------|
| `AuthStateTest` | Тестирование Guest/Authenticated states | 12 tests |
| `LoginCredentialsTest` | Value object validation | 14 tests |
| `LoginUseCaseTest` | UseCase с валидацией | 9 tests |
| `LogoutUseCaseTest` | Logout → Guest transition | 5 tests |
| `ObserveAuthStateUseCaseTest` | StateFlow observation | 6 tests |
| `AuthRepositoryImplTest` | Repository implementation | 3 tests |
| `AuthRepositoryErrorHandlingTest` | Error scenarios | 15 tests |
| `LoginScreenModelTest` | ScreenModel state management | 14 tests |

### Test Example: AuthState

```kotlin
@Test
fun `Guest state should not be authenticated`() {
    val state = AuthState.Guest

    assertFalse(state.isAuthenticated)
    assertFalse(state.canWrite)
    assertNull(state.userId)
}

@Test
fun `Authenticated state should have write access`() {
    val state = AuthState.Authenticated(
        accessToken = "token",
        userId = "user_123",
        userEmail = "user@example.com"
    )

    assertTrue(state.isAuthenticated)
    assertTrue(state.canWrite)
    assertEquals("user_123", state.userId)
}
```

---

## 🔗 Related Documentation

- [Network Layer](../architecture/NETWORK_LAYER.md) - Ktor configuration, safeApiCall
- [Implementation Status](../IMPLEMENTATION_STATUS.md) - Общий статус проекта
- [Backend API Reference](../api/BACKEND_API_REFERENCE.md) - API endpoints
- [UX Guidelines](../design/05_UX_GUIDELINES.md) - User experience best practices

---

**Версия документа**: 2.0
**Last Updated**: 2026-03-21
**Maintainer**: Development Team
