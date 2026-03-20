# 🔐 Auth Feature Documentation

**Feature Name**: Authentication
**Epic**: E1
**Status**: ✅ Complete (100%)
**Last Updated**: 2026-03-20

---

## 📋 Overview

Auth Feature реализует полный цикл аутентификации пользователей: вход в систему, автоматическое обновление токенов и выход из системы. Построена на принципах Clean Architecture с полным разделением на Domain, Data, Presentation и DI слои.

### Бизнес-ценность

| Функция | Описание | Ценность |
|---------|----------|----------|
| **Login** | Вход по email/паролю | Пользователи получают доступ к персонализированному контенту |
| **Auto Refresh** | Автоматическое обновление токенов | Бесшовный опыт без повторного логина |
| **Logout** | Безопасный выход | Защита данных пользователя |

---

## 🏗️ Architecture

### Domain Layer (100% shared)

**Пакет**: `feature/auth/src/commonMain/kotlin/domain/`

#### Models

| Класс | Описание |
|-------|----------|
| `AuthState` | Sealed class для состояния аутентификации (Initial, Authenticated) |
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
| `LogoutUseCase` | Выход из системы | - | Result<Unit> |
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

    override val authState: StateFlow<AuthState> = flow {
        emit(AuthState.Initial)
        // Наблюдение за токенами
    }.stateIn(
        scope = CoroutineScope(ioDispatcher),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Initial
    )

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
                Result.success(AuthState.Authenticated(
                    email = credentials.email,
                    token = response.accessToken
                ))
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCall<Unit> {
            httpClient.post("auth/logout")
        }.fold(
            onSuccess = {
                tokenStorage.clearTokens()
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
```

#### Helper Functions

```kotlin
// authenticatedApiCall - обёртка для API вызовов с автоматическим refresh
suspend inline fun <reified T : Any> authenticatedApiCall(
    httpClient: HttpClient,
    tokenStorage: TokenStorage,
    crossinline apiCall: suspend () -> HttpResponse
): Result<T>
```

---

### Presentation Layer (100% shared)

**Пакет**: `feature/auth/src/commonMain/kotlin/presentation/`

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

#### ScreenModel (Voyager)

```kotlin
// LoginScreenModel.kt
class LoginScreenModel(
    private val loginUseCase: LoginUseCase,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator
) : ScreenModel {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = emailValidator.validate(email).getErrorOrNull(),
                isFormValid = validateForm(email, state.password)
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = passwordValidator.validate(password).getErrorOrNull(),
                isFormValid = validateForm(state.email, password)
            )
        }
    }

    suspend fun login(): Boolean {
        _uiState.update { it.copy(isLoading = true, generalError = null) }

        val result = loginUseCase(
            LoginCredentials(
                email = _uiState.value.email,
                password = _uiState.value.password
            )
        )

        return when {
            result.isSuccess -> true
            else -> {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        generalError = (result.exceptionOrNull() as? AppError)
                            ?.toUserMessage() ?: "Unknown error"
                    )
                }
                false
            }
        }
    }
}
```

#### Screen (Compose)

```kotlin
// LoginScreen.kt
class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { LoginScreenModel(get(), get(), get()) }
        val uiState by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LoginScreenContent(
            uiState = uiState,
            onEmailChange = screenModel::updateEmail,
            onPasswordChange = screenModel::updatePassword,
            onLoginClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    if (screenModel.login()) {
                        navigator.push(MainScreen)
                    }
                }
            },
            onRegisterClick = { /* Navigate to register */ }
        )
    }
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Email Field
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Field
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Error Message
        uiState.generalError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = onLoginClick,
            enabled = uiState.isFormValid && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
    }
}
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
    │   │   │   ├── AuthState.kt
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
        └── data/repository/
            └── AuthRepositoryImplTest.kt
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
| `:core:navigation` | Voyager Navigator, Screen sealed class |

---

## 🧪 Testing

### Unit Tests

| Test | Описание | Coverage |
|------|----------|----------|
| `AuthRepositoryImplTest` | Тестирование репозитория | Login, Logout, Refresh |
| `LoginScreenModelTest` | Тестирование ScreenModel | State updates, validation |
| `EmailValidatorTest` | Валидация email | Valid/invalid cases |
| `PasswordValidatorTest` | Валидация пароля | Strength requirements |

### Integration Tests

- Полный flow login -> authenticated request -> logout
- Token refresh scenarios
- Error handling scenarios

---

## 🔗 Related Documentation

- [Network Layer](../NETWORK_LAYER.md) - Ktor configuration, safeApiCall
- [Implementation Status](../IMPLEMENTATION_STATUS.md) - Общий статус проекта
- [Backend API Reference](../BACKEND_API_REFERENCE.md) - API endpoints

---

**Версия документа**: 1.0
**Last Updated**: 2026-03-20
**Maintainer**: Development Team
