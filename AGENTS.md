### AGENTS.md - Kotlin Multiplatform Project Guidelines

**Tech Stack:** Kotlin 2.2.20 / Compose Multiplatform 1.10.2 / AGP 8.12.3 / JVM 21
**Architecture:** Feature-First Modularization + Clean Architecture

---

## Build / Lint / Test Commands

```bash
# Build entire project
./gradlew build

# Run all tests across all modules
./gradlew testAll

# Run tests for a specific module (all platforms)
./gradlew :feature:auth:allTests

# Run a single test class
./gradlew :feature:auth:iosSimulatorArm64Test --tests "com.aggregateservice.feature.auth.presentation.screenmodel.LoginScreenModelTest"

# Run Android unit tests for a module
./gradlew :core:network:testDebugUnitTest --tests "com.aggregateservice.core.network.SafeApiCallTest"

# Linting
./gradlew ktlintCheckAll          # Check code style
./gradlew ktlintFormatAll         # Auto-fix code style
./gradlew detektAll               # Static analysis

# Coverage
./gradlew testCoverage            # Run tests + generate coverage
./gradlew koverReportAll          # Generate coverage reports

# Clean build
./gradlew clean build
```

---

## Project Structure

```
core/           # Shared infrastructure modules
  network/      # Ktor client, safeApiCall, AppError
  storage/      # DataStore preferences
  theme/        # Compose theme, colors
  i18n/         # Localization strings
  utils/        # Validators, extensions
  navigation/   # Voyager navigation
  config/       # AppConfig, environment
  di/           # Koin core setup
  test-utils/   # Test helpers

feature/        # Feature modules (Clean Architecture)
  auth/
    domain/     # Pure Kotlin: models, interfaces, UseCases
    data/       # DTOs, Repository implementations
    presentation/  # Compose UI, ScreenModels, UiState
    di/         # Koin module

app/            # Feature aggregator
androidApp/     # Android application entry point
```

---

## Dependency Rules

```
feature:*  -> core:*   (via Koin DI)
core:di    -> core:*   (Koin modules)
core:navigation -> X   (NO feature dependencies)
```

---

## Code Style

### Imports
- NO wildcard imports
- Order: kotlin.*, kotlinx.*, io.*, androidx.*, cafe.adriel.*, com.aggregateservice.*

### Naming Conventions
- Classes: `PascalCase` - `LoginScreenModel`, `AuthRepository`
- Functions: `camelCase` - `onLoginClick()`, `validate()`
- Properties: `camelCase` - `uiState`, `isLoading`
- Constants: `SCREAMING_SNAKE_CASE` - `MAX_RETRIES`
- Packages: `com.aggregateservice.feature.auth.domain.usecase`
- DTOs: suffix `Dto` optional, use `@Serializable`
- Tests: `<ClassUnderTest>Test` - `LoginScreenModelTest`

### Formatting
- Indent: 4 spaces
- Max line length: 120 characters
- Trailing commas: enabled
- Final newline: required

### Types
- Prefer `val` over `var`
- Use `StateFlow` for UI state, not `LiveData`
- Use `Result<T>` for operations that can fail
- Prefer sealed interfaces for state/error modeling

---

## Architecture Patterns

### UseCase (Domain Layer)
```kotlin
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(credentials: LoginCredentials): Result<AuthState>
}
```

### ScreenModel (Presentation Layer)
```kotlin
class LoginScreenModel(
    private val loginUseCase: LoginUseCase
) : ScreenModel {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
}
```

### Koin Module
```kotlin
val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factoryOf(::LoginUseCase)
    factoryOf(::LoginScreenModel)
}
```

---

## Error Handling

- Use `AppError` sealed interface from `:core:network`
- NEVER leak Ktor exceptions to Domain/Presentation
- Use `safeApiCall { }` wrapper for all network calls
- Map errors to user messages in ScreenModel

```kotlin
// In ScreenModel
loginUseCase(credentials).fold(
    onSuccess = { /* handle success */ },
    onFailure = { error ->
        val message = when (error) {
            is AppError.Unauthorized -> "Invalid credentials"
            is AppError.NetworkError -> "Network error"
            else -> "Unknown error"
        }
    }
)
```

---

## Testing

- Use `kotlin-test` (NOT JUnit 4)
- Test dispatcher: `StandardTestDispatcher()`
- Use `runTest { }` for coroutine tests
- Mock with interfaces, not implementations

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is empty`() = runTest { /* ... */ }
}
```

---

## Build-Logic Rules (for Precompiled Scripts)

In `build-logic/src/main/kotlin/*.gradle.kts`:

```kotlin
// Always initialize libs on first line
val libs = the<VersionCatalogsExtension>().named("libs")

// Use maybeCreate for source sets
kotlin.sourceSets.maybeCreate("iosMain").dependencies { }

// For Compose dependencies
val compose = extensions.getByType<ComposeExtension>().dependencies
implementation(compose.runtime)
```

---

## Before Committing

1. `./gradlew ktlintFormatAll`
2. `./gradlew detektAll`
3. `./gradlew testAll`
4. Verify JVM 21 target in all modules

# Языки общения: Русский / English !
