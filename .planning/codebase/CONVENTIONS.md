# Coding Conventions

**Analysis Date:** 2026-03-28

## Naming Conventions

**Files:**
- Kotlin source files: `PascalCase.kt` - e.g., `LoginScreenModel.kt`, `AuthRepository.kt`
- Kotlin script files: `kebab-case.kts`
- Test files: `<ClassUnderTest>Test.kt` - e.g., `LoginScreenModelTest.kt`

**Classes & Types:**
- `PascalCase` - e.g., `LoginScreenModel`, `AuthRepository`, `AppError`

**Functions:**
- `camelCase` - e.g., `onLoginClick()`, `validate()`, `searchProviders()`

**Properties & Variables:**
- `camelCase` - e.g., `uiState`, `isLoading`, `accessToken`

**Constants:**
- `SCREAMING_SNAKE_CASE` - e.g., `MAX_RETRIES`, `NETWORK_TIMEOUT_MS`

**Packages:**
- `com.aggregateservice.{feature|core}.{module}.{layer}` - e.g., `com.aggregateservice.feature.auth.domain.usecase`

**DTOs:**
- Suffix `Dto` optional, use `@Serializable` annotation

## Code Style

**Formatting (from `.editorconfig`):**
- Indent: 4 spaces (not tabs)
- Max line length: 120 characters (enforced by Detekt)
- Line endings: LF (Unix style)
- Trailing commas: enabled (`ij_kotlin_allow_trailing_comma = true`)
- Final newline: required
- Trim trailing whitespace: enabled

**Imports:**
- NO wildcard imports
- Order:
  1. `kotlin.*`
  2. `kotlinx.*`
  3. `io.*` (Ktor)
  4. `androidx.*`
  5. `cafe.adriel.*` (Voyager)
  6. `com.aggregateservice.*`

**Types:**
- Prefer `val` over `var`
- Use `StateFlow<T>` for UI state (NOT `LiveData`)
- Use `Result<T>` for operations that can fail
- Prefer sealed interfaces for state/error modeling
- Use `object` for singletons, `class` for instances

## Linting & Static Analysis

**Tools:**
- **Ktlint** (`org.jlleitschuh.gradle.ktlint`) - Code style enforcement
- **Detekt** (`io.gitlab.arturbosch.detekt`) - Static analysis

**Configuration Files:**
- Ktlint rules: `config/quality/.editorconfig` (supplements root `.editorconfig`)
- Detekt rules: `config/quality/detekt.yml`

**Key Detekt Rules:**
- `MaxLineLength`: 120 characters
- `CyclomaticComplexMethod`: threshold 15
- `LongMethod`: threshold 60 lines
- `LongParameterList`: function 6, constructor 7
- `NestedBlockDepth`: threshold 4
- `TooManyFunctions`: 20 per file/class
- `WildcardImport`: prohibited (except `java.util.*`, `kotlinx.android.synthetic.*`)
- `MagicNumber`: ignore -1, 0, 1, 2

**Run Commands:**
```bash
./gradlew ktlintCheckAll          # Check code style
./gradlew ktlintFormatAll         # Auto-fix code style
./gradlew detektAll               # Static analysis
```

## Architecture Patterns

**UseCase (Domain Layer):**
```kotlin
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(credentials: LoginCredentials): Result<AuthState>
}
```

**ScreenModel (Presentation Layer):**
```kotlin
class LoginScreenModel(
    private val loginUseCase: LoginUseCase
) : ScreenModel {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
}
```

**Koin Module:**
```kotlin
val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factoryOf(::LoginUseCase)
    factoryOf(::LoginScreenModel)
}
```

## Error Handling

- Use `AppError` sealed interface from `:core:network`
- NEVER leak Ktor exceptions to Domain/Presentation layers
- Use `safeApiCall { }` wrapper for all network calls
- Map errors to user messages in ScreenModel

```kotlin
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

## Dependency Rules

```
feature:*  -> core:*   (via Koin DI)
core:di    -> core:*   (Koin modules)
core:navigation -> X   (NO feature dependencies)
```

## Git Workflow

**Before Committing:**
1. `./gradlew ktlintFormatAll`
2. `./gradlew detektAll`
3. `./gradlew testAll`

**Commit Message Format:**
- Uses standard git commit message format (no specific convention enforced)
- Sample commit hooks exist in `.git/hooks/` but are not actively enforced

**Branch Strategy:**
- `main` - production-ready code
- `develop` - integration branch

---

*Convention analysis: 2026-03-28*
