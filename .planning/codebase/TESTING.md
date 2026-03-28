# Testing Patterns

**Analysis Date:** 2026-03-28

## Test Framework

**Framework:** `kotlin-test` (NOT JUnit 4)
**Assertion Library:** `kotlin-test`
**Coroutines Testing:** `kotlinx-coroutines-test`

**Test Dependencies (from `gradle/libs.versions.toml`):**
```toml
kotlin-test = "org.jetbrains.kotlin:kotlin-test"
kotlin-test-junit5 = "org.jetbrains.kotlin:kotlin-test-junit5"
mockk = "io.mockk:mockk" version "1.14.9"
turbine = "app.cash.turbine:turbine" version "1.2.1"
kotlinx-coroutines-test = "org.jetbrains.kotlinx:kotlinx-coroutines-test"
koin-test = "io.insert-koin:koin-test"
koin-test-junit5 = "io.insert-koin:koin-test-junit5"
```

**Run Commands:**
```bash
./gradlew testAll                         # Run all tests across all modules
./gradlew :feature:auth:allTests         # Run tests for specific module
./gradlew :feature:auth:iosSimulatorArm64Test --tests "com.aggregateservice.feature.auth.presentation.screenmodel.LoginScreenModelTest"
./gradlew :core:network:testDebugUnitTest --tests "com.aggregateservice.core.network.SafeApiCallTest"
./gradlew testCoverage                   # Run tests + generate coverage
```

## Test File Organization

**Location Pattern:**
- Tests are co-located with source in `src/commonTest/kotlin/` directories
- Mirror the package structure of the source code

**Naming:**
- Test files: `<ClassUnderTest>Test.kt` - e.g., `LoginScreenModelTest.kt`
- Test classes: `<ClassUnderTest>Test` - e.g., `class LoginScreenModelTest`

**Directory Structure:**
```
feature/auth/src/
├── commonMain/kotlin/...    # Source code
└── commonTest/kotlin/...  # Tests
    └── com/aggregateservice/feature/auth/
        ├── domain/
        │   ├── model/
        │   │   ├── LoginCredentialsTest.kt
        │   │   └── AuthStateTest.kt
        │   ├── usecase/
        │   │   └── LoginUseCaseTest.kt
        │   └── repository/
        │       └── AuthRepositoryImplTest.kt
        └── presentation/
            └── screenmodel/
                └── LoginScreenModelTest.kt
```

## Test Structure

**ScreenModel Test Pattern:**
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Initial)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Initialize Config if needed
        Config.initialize(AppConfig(...))
        // Create mock repository implementations
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty email and password`() = runTest {
        val screenModel = createScreenModel()
        val state = screenModel.uiState.value
        assertEquals("", state.email)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onLoginClick sets isLoginSuccess on successful login`() = runTest {
        // Arrange
        val loginUseCase = LoginUseCase(createMockRepository(Result.success(...)))
        val screenModel = LoginScreenModel(loginUseCase, ...)
        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("ValidPassword123")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        screenModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = screenModel.uiState.value
        assertTrue(state.isLoginSuccess)
    }
}
```

**UseCase Test Pattern:**
```kotlin
class LoginUseCaseTest {
    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val repository = FakeAuthRepository(Result.success(AuthState.Authenticated(...)))
        val useCase = LoginUseCase(repository)

        val result = useCase(LoginCredentials("test@test.com", "password"))

        assertTrue(result.isSuccess)
    }
}
```

## Mocking

**Framework:** Inline mock objects (interfaces) - not using MockK extensively

**Pattern:** Create anonymous implementations of repository interfaces
```kotlin
private fun createMockRepository(result: Result<AuthState>): AuthRepository {
    return object : AuthRepository {
        override fun observeAuthState(): StateFlow<AuthState> = authStateFlow
        override fun getCurrentAuthState(): AuthState = authStateFlow.value
        override suspend fun login(credentials: LoginCredentials): Result<AuthState> = result
        override suspend fun logout() {}
        // ... other methods
    }
}
```

**Behavior Mocking:** Use behavior variables for different test scenarios
```kotlin
private var searchProvidersBehavior: suspend (SearchFilters) -> Result<SearchResult<Provider>> =
    { Result.success(SearchResult.empty()) }
```

## Test Utilities

**Turbine** (`app.cash.turbine:turbine`): Available for flow testing
**Koin Test**: Available via `io.insert-koin:koin-test`

**Config Test Helper:**
```kotlin
Config.initialize(
    AppConfig(
        apiBaseUrl = "https://api.test.com",
        apiKey = "test-api-key",
        environmentCode = Environment.DEV.name,
        languageCode = Language.RU.code,
        isDebug = true,
        enableLogging = false,
        networkTimeoutMs = 30_000L,
        apiVersion = "v1",
        passwordMinLength = 12,
        passwordMaxLength = 128,
    )
)
// After tests
Config.reset()
```

## Test Coverage

**Tool:** Kover (`org.jetbrains.kotlinx.kover`)

**Commands:**
```bash
./gradlew koverReportAll              # Generate coverage reports
./gradlew koverVerifyAll              # Verify coverage thresholds
./gradlew testCoverage                # Run tests + generate coverage
```

**Reports Location:** `build/reports/kover/xml/**/*.xml`

## Test Categories

**Unit Tests:**
- Domain UseCases
- ScreenModels
- Repositories (with mocked dependencies)
- Validators and utilities

**Integration Tests:**
- Repository implementations with test doubles
- Network calls (via `SafeApiCall`)

**Android Instrumented Tests:**
- `connectedAndroidTest` task
- Runs on Android emulator via `reactivecircus/android-emulator-runner@v2`

**iOS Tests:**
- `iosTest` task
- Runs on macOS

## CI/CD Testing

**GitHub Workflow (`.github/workflows/test.yml`):**
- **Unit Tests**: Runs on Ubuntu with JDK 21 via `./gradlew testAll`
- **Coverage**: Generates Kover report and uploads to Codecov
- **Android Tests**: Runs on macOS with Android emulator (API level 34)
- **iOS Tests**: Runs on macOS via `./gradlew iosTest`

**Upload Artifacts:**
- Test results: `**/build/reports/tests/`
- Coverage reports: `**/build/reports/kover/`
- Android test results: `**/build/reports/androidTests/`

## Common Patterns

**Async Testing with runTest:**
```kotlin
@Test
fun `onLoginClick handles NetworkError`() = runTest {
    loginUseCase = LoginUseCase(createMockRepository(Result.failure(AppError.NetworkError(500, "Server error"))))
    screenModel = LoginScreenModel(loginUseCase, observeAuthStateUseCase, mockAuthRepository)
    // ... setup

    screenModel.onLoginClick()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = screenModel.uiState.value
    assertTrue(state.errorMessage!!.contains("Ошибка сети"))
}
```

**Flow Testing with Turbine:**
```kotlin
test("emits values").runTest {
    val flow = MutableStateFlow(1)
    flow.test {
        assertEquals(1, awaitItem())
        flow.value = 2
        assertEquals(2, awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

**StateFlow Testing:**
```kotlin
private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Initial)
// In tests, update flow and advance dispatcher
authStateFlow.value = AuthState.Authenticated(...)
testDispatcher.scheduler.advanceUntilIdle()
```

---

*Testing analysis: 2026-03-28*
