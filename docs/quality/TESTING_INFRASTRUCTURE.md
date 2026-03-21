# 🔬 Testing Infrastructure Documentation

## 📋 Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Testing Strategy](#testing-strategy)
4. [Tools & Dependencies](#tools--dependencies)
5. [Directory Structure](#directory-structure)
6. [Writing Tests](#writing-tests)
7. [Running Tests](#running-tests)
8. [CI/CD Integration](#cicd-integration)
9. [Best Practices](#best-practices)

---

## 🎯 Overview

This document describes the comprehensive testing infrastructure for the Kotlin Multiplatform Mobile (KMP) project. The infrastructure supports:

- ✅ **Shared Unit Tests** (`commonTest`) - Runs on all platforms
- ✅ **Platform-Specific Tests** (`androidTest`, `iosTest`) - For platform implementations
- ✅ **Integration Tests** - End-to-end API and database tests
- ✅ **Code Coverage** - Kotlin Kover for coverage reports
- ✅ **CI/CD Integration** - Automated testing in GitHub Actions

### Testing Philosophy

```
                    ╭─────────╮
                   ╔╝         ╔╝
                  ║ Platform  ║  5-10% (Android/iOS specific)
                 ╔╝   Tests   ╔╝
                ║             ║
               ╔╝ Integration ╔╝ 15-20% (API, Database, DI)
              ║     Tests     ║
             ╔╝               ╔╝
            ║  Shared Unit    ║ 70-80% (Business Logic)
           ╔╝      Tests      ╔╝
          ║___________________║
```

**Key Principles:**
1. **Test in `commonTest` whenever possible** - Maximize shared test code
2. **Fast feedback** - Unit tests should run in seconds
3. **Isolation** - Tests should not depend on each other
4. **Readability** - Tests should be easy to understand and maintain
5. **Reliability** - Tests should be deterministic (no flaky tests)

---

## 🏗️ Architecture

### Test Directory Structure

```
src/
├── commonMain/kotlin/              # Shared implementation
│   ├── domain/
│   │   ├── usecase/
│   │   │   └── LoginUseCase.kt
│   │   └── model/
│   │       └── AuthState.kt
│   ├── data/
│   │   ├── repository/
│   │   │   └── AuthRepositoryImpl.kt
│   │   └── dto/
│   │       └── LoginResponse.kt
│   └── presentation/
│       ├── screenmodel/
│       │   └── LoginScreenModel.kt
│       └── screen/
│           └── LoginScreen.kt
│
├── commonTest/kotlin/              # 🎯 Shared tests (RUNS EVERYWHERE!)
│   ├── domain/
│   │   ├── usecase/
│   │   │   └── LoginUseCaseTest.kt
│   │   └── model/
│   │       └── LoginCredentialsTest.kt
│   ├── data/
│   │   ├── repository/
│   │   │   └── AuthRepositoryImplTest.kt
│   │   └── dto/
│   │       └── SerializationTest.kt
│   └── presentation/
│       └── screenmodel/
│           └── LoginScreenModelTest.kt
│
├── androidMain/kotlin/             # Android-specific implementation
│   └── platform/
│       └── Config.android.kt
│
├── androidTest/kotlin/             # 🤖 Android-specific tests
│   └── platform/
│       └── ConfigTest.android.kt
│
└── iosMain/kotlin/                 # iOS-specific implementation
    └── platform/
        └── Config.ios.kt
```

### Test Layers

```
┌─────────────────────────────────────┐
│         Presentation Tests          │  ← commonTest
│  (ScreenModels, State management)   │
├─────────────────────────────────────┤
│           Domain Tests              │  ← commonTest
│  (UseCases, Business logic)         │
├─────────────────────────────────────┤
│            Data Tests               │  ← commonTest + platform
│  (Repositories, API, Storage)       │
├─────────────────────────────────────┤
│      Platform Tests (5-10%)         │  ← androidTest, iosTest
│  (expect/actual implementations)    │
└─────────────────────────────────────┘
```

---

## 📊 Testing Strategy

### Test Coverage Goals

| Layer | Target Coverage | Rationale |
|-------|----------------|-----------|
| **Domain** | 90%+ | Business logic is critical and expensive to fix in production |
| **Data** | 80%+ | Network and storage operations are error-prone |
| **Presentation** | 70%+ | UI state and user interactions |
| **Platform** | 60%+ | Platform-specific code is harder to test |

### What to Test

#### ✅ DO Test

**Domain Layer:**
- ✅ Business logic (discount calculations, validations)
- ✅ UseCase interactions with repositories
- ✅ Error handling and transformation
- ✅ State transformations

**Data Layer:**
- ✅ API request/response parsing
- ✅ Error mapping (HTTP codes → AppError)
- ✅ Data mapping (DTO ↔ Domain model)
- ✅ Retry logic (if implemented)
- ✅ Cache operations

**Presentation Layer:**
- ✅ State transitions (idle → loading → success/error)
- ✅ User interactions (click, text input)
- ✅ Error message display
- ✅ Navigation triggers

#### ❌ DON'T Test

- ❌ Data classes (automatic)
- ❌ Simple getters/setters
- ❌ Framework internals (Ktor, Compose, Koin)
- ❌ Third-party libraries

---

## 🛠️ Tools & Dependencies

### Core Testing Stack

```toml
# gradle/libs.versions.toml

[versions]
kotlin = "2.2.20"
coroutines = "1.10.2"
mockk = "1.14.9"
turbine = "1.2.1"
ktor = "3.4.1"
koin = "4.2.0"
kover = "0.9.7"

[libraries]
# Kotlin Test
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlin-test-junit5 = { module = "org.jetbrains.kotlin:kotlin-test-junit5", version.ref = "kotlin" }

# Coroutines Testing
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

# Mocking
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }

# Flow Testing
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

# Ktor Mocking
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }

# Koin Testing
koin-test = { module = "io.insert-koin:koin-test", version.ref = "koin" }
koin-test-junit5 = { module = "io.insert-koin:koin-test-junit5", version.ref = "koin" }

# Android Testing
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version = "1.2.1" }
androidx-test-runner = { module = "androidx.test:runner", version = "1.6.2" }
robolectric = { module = "org.robolectric:robolectric", version = "4.14.1" }

# Coverage
kover-gradlePlugin = { module = "org.jetbrains.kotlinx.kover:org.jetbrains.kotlinx.kover.gradle.plugin", version.ref = "kover" }
```

### Tool Usage

| Tool | Purpose | Example |
|------|---------|---------|
| **kotlin-test** | Basic assertions | `assertEquals(expected, actual)` |
| **coroutines-test** | Test coroutines | `runTest { ... }` |
| **mockk** | Mock dependencies | `mockk<MyClass>()` |
| **turbine** | Test Flows | `flow.test { awaitItem() }` |
| **ktor-mock** | Mock API calls | `MockEngine { ... }` |
| **koin-test** | Test DI | `checkModule { ... }` |
| **kover** | Code coverage | `./gradlew koverReport` |

---

## 📁 Directory Structure

### Complete Module Structure

```
feature/auth/
├── build.gradle.kts
├── src/
│   ├── commonMain/kotlin/
│   │   └── com/aggregateservice/feature/auth/
│   │       ├── domain/
│   │       │   ├── model/
│   │       │   │   ├── AuthState.kt
│   │       │   │   └── LoginCredentials.kt
│   │       │   ├── repository/
│   │       │   │   └── AuthRepository.kt
│   │       │   └── usecase/
│   │       │       ├── LoginUseCase.kt
│   │       │       ├── LogoutUseCase.kt
│   │       │       └── ObserveAuthStateUseCase.kt
│   │       ├── data/
│   │       │   ├── repository/
│   │       │   │   └── AuthRepositoryImpl.kt
│   │       │   ├── dto/
│   │       │   │   ├── LoginRequest.kt
│   │       │   │   └── LoginResponse.kt
│   │       │   └── mapper/
│   │       │       └── AuthResponseMapper.kt
│   │       └── presentation/
│   │           ├── model/
│   │           │   └── LoginUiState.kt
│   │           ├── screenmodel/
│   │           │   └── LoginScreenModel.kt
│   │           └── screen/
│   │               └── LoginScreen.kt
│   │
│   ├── commonTest/kotlin/
│   │   └── com/aggregateservice/feature/auth/
│   │       ├── domain/
│   │       │   ├── usecase/
│   │       │   │   ├── LoginUseCaseTest.kt
│   │       │   │   ├── LogoutUseCaseTest.kt
│   │       │   │   └── ObserveAuthStateUseCaseTest.kt
│   │       │   └── model/
│   │       │       └── LoginCredentialsTest.kt
│   │       ├── data/
│   │       │   ├── repository/
│   │       │   │   └── AuthRepositoryImplTest.kt
│   │       │   └── dto/
│   │       │       └── SerializationTest.kt
│   │       └── presentation/
│   │           └── screenmodel/
│   │               └── LoginScreenModelTest.kt
│   │
│   ├── androidMain/kotlin/
│   │   └── com/aggregateservice/feature/auth/
│   │       └── platform/
│   │           └── AuthExtensions.android.kt
│   │
│   └── androidTest/kotlin/
│       └── com/aggregateservice/feature/auth/
│           ├── integration/
│           │   └── AuthIntegrationTest.kt
│           └── platform/
│               └── AuthExtensionsTest.android.kt
│
└── build/reports/
    ├── kover/
    │   ├── html/
    │   └── xml/
    └── tests/
```

---

## ✍️ Writing Tests

### Domain Layer Tests

**Location:** `commonTest/kotlin/.../domain/usecase/`

**Example:** `LoginUseCaseTest.kt`

```kotlin
package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private lateinit var useCase: LoginUseCase
    private lateinit var mockRepository: AuthRepository

    @Test
    fun `should return AuthState on successful login`() = runTest {
        // Arrange
        val credentials = LoginCredentials(
            email = "test@example.com",
            password = "password123"
        )
        val expectedAuthState = AuthState.Authenticated(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            userId = "user-123",
            email = "test@example.com"
        )

        mockRepository = mockk<AuthRepository> {
            coEvery { login(credentials) } returns Result.success(expectedAuthState)
        }
        useCase = LoginUseCase(mockRepository)

        // Act
        val result = useCase(credentials)

        // Assert
        assertTrue(result.isSuccess, "Result should be successful")
        assertEquals(expectedAuthState, result.getOrNull())

        // Verify repository was called
        coVerify(exactly = 1) { mockRepository.login(credentials) }
    }

    @Test
    fun `should return validation error when email is too long`() = runTest {
        // Arrange
        val credentials = LoginCredentials(
            email = "a".repeat(256), // Too long (>255)
            password = "password123"
        )

        mockRepository = mockk<AuthRepository>()
        useCase = LoginUseCase(mockRepository)

        // Act
        val result = useCase(credentials)

        // Assert
        assertTrue(result.isFailure, "Result should be failure")
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.ValidationError, "Error should be ValidationError")

        val validationError = error as AppError.ValidationError
        assertEquals("email", validationError.field)
        assertEquals("Email too long (max 255 characters)", validationError.message)

        // Verify repository was NOT called
        coVerify(exactly = 0) { mockRepository.login(any()) }
    }

    @Test
    fun `should propagate repository error on network failure`() = runTest {
        // Arrange
        val credentials = LoginCredentials(
            email = "test@example.com",
            password = "wrong-password"
        )
        val expectedError = AppError.Unauthorized

        mockRepository = mockk<AuthRepository> {
            coEvery { login(credentials) } returns Result.failure(expectedError)
        }
        useCase = LoginUseCase(mockRepository)

        // Act
        val result = useCase(credentials)

        // Assert
        assertTrue(result.isFailure, "Result should be failure")
        assertEquals(expectedError, result.exceptionOrNull())
    }
}
```

### Data Layer Tests

**Location:** `commonTest/kotlin/.../data/repository/`

**Example:** `AuthRepositoryImplTest.kt`

```kotlin
package com.aggregateservice.feature.auth.data.repository

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    @Test
    fun `should return AuthState on successful login`() = runTest {
        // Arrange
        val jsonResponse = """
        {
            "access_token": "test-access-token",
            "refresh_token": "test-refresh-token",
            "user_id": "user-123",
            "email": "test@example.com"
        }
        """.trimIndent()

        val mockEngine = MockEngine { _ ->
            respond(
                content = jsonResponse,
                status = HttpStatusCode.OK,
                headers = io.ktor.http.headersOf(
                    "Content-Type" to listOf("application/json")
                )
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Note: You'll need to refactor AuthRepositoryImpl to accept HttpClient
        // Or use a factory pattern for testing

        // Act & Assert will be implemented after refactoring
    }

    @Test
    fun `should return Unauthorized on 401 response`() = runTest {
        // Arrange
        val errorResponse = """
        {
            "detail": "Invalid credentials"
        }
        """.trimIndent()

        val mockEngine = MockEngine { _ ->
            respond(
                content = errorResponse,
                status = HttpStatusCode.Unauthorized,
                headers = io.ktor.http.headersOf(
                    "Content-Type" to listOf("application/json")
                )
            )
        }

        // Act & Assert implementation
    }
}
```

### Presentation Layer Tests

**Location:** `commonTest/kotlin/.../presentation/screenmodel/`

**Example:** `LoginScreenModelTest.kt`

```kotlin
package com.aggregateservice.feature.auth.presentation.screenmodel

import app.cash.turbine.test
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginScreenModelTest {

    @Test
    fun `should update email state when onEmailChanged is called`() = runTest {
        // Arrange
        val mockUseCase = mockk<LoginUseCase>()
        val screenModel = LoginScreenModel(mockUseCase)

        // Act
        screenModel.onEmailChanged("test@example.com")

        // Assert
        screenModel.uiState.test {
            val state = awaitItem()
            assertEquals("test@example.com", state.email)
        }
    }

    @Test
    fun `should update password state when onPasswordChanged is called`() = runTest {
        // Arrange
        val mockUseCase = mockk<LoginUseCase>()
        val screenModel = LoginScreenModel(mockUseCase)

        // Act
        screenModel.onPasswordChanged("password123")

        // Assert
        screenModel.uiState.test {
            val state = awaitItem()
            assertEquals("password123", state.password)
        }
    }

    @Test
    fun `should show loading state during login`() = runTest {
        // Arrange
        val credentials = LoginCredentials(
            email = "test@example.com",
            password = "password123"
        )

        val mockUseCase = mockk<LoginUseCase> {
            coEvery { this@mockk(credentials) } coAnswers {
                kotlinx.coroutines.delay(100) // Simulate network delay
                Result.success(AuthState.Authenticated(
                    accessToken = "token",
                    refreshToken = "refresh",
                    userId = "user-123",
                    email = "test@example.com"
                ))
            }
        }

        val screenModel = LoginScreenModel(mockUseCase)
        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("password123")

        // Act
        screenModel.onLoginClick()

        // Assert
        screenModel.uiState.test {
            // Skip initial states
            skipItems(1)

            // Loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading, "Should be loading")

            // Success state
            val successState = awaitItem()
            assertFalse(successState.isLoading, "Should not be loading")
            assertTrue(successState.isLoginSuccess, "Should be successful")
        }
    }

    @Test
    fun `should show error message when login fails`() = runTest {
        // Arrange
        val credentials = LoginCredentials(
            email = "test@example.com",
            password = "wrong-password"
        )
        val expectedError = AppError.Unauthorized

        val mockUseCase = mockk<LoginUseCase> {
            coEvery { this@mockk(credentials) } returns Result.failure(expectedError)
        }

        val screenModel = LoginScreenModel(mockUseCase)
        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("wrong-password")

        // Act
        screenModel.onLoginClick()

        // Assert
        screenModel.uiState.test {
            // Skip initial states
            skipItems(1)

            // Loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading, "Should be loading")

            // Error state
            val errorState = awaitItem()
            assertFalse(errorState.isLoading, "Should not be loading")
            assertFalse(errorState.isLoginSuccess, "Should not be successful")
            assertEquals("Unauthorized access", errorState.errorMessage)
        }
    }
}
```

---

## 🚀 Running Tests

### Gradle Tasks

```bash
# Run all tests across all modules
./gradlew testAll

# Run tests for specific module
./gradlew :feature:auth:test

# Run tests on specific platform
./gradlew :feature:auth:allTests

# Generate coverage report
./gradlew testCoverage

# Run only unit tests (skip integration tests)
./gradlew testAll -x integrationTest

# Run tests with coverage verification
./gradlew koverVerify
```

### IDE Support

**IntelliJ IDEA / Android Studio:**
- Right-click on test class → Run 'ClassNameTest'
- Right-click on test method → Run 'testMethodName()'
- Green play button next to test class/method
- Ctrl+Shift+F10 (Windows/Linux) or Cmd+Shift+R (Mac)

---

## 🔄 CI/CD Integration

### GitHub Actions Workflow

**File:** `.github/workflows/test.yml`

```yaml
name: Tests

on:
  pull_request:
    branches: [ main, develop ]
  push:
    branches: [ main, develop ]

jobs:
  unit-tests:
    name: Unit Tests
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Run Unit Tests
        run: ./gradlew testAll

      - name: Generate Coverage Report
        run: ./gradlew testCoverage

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: |
            **/build/reports/kover/xml/**/*.xml
          fail_ci_if_error: false
          name: coverage-report

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: |
            **/build/reports/tests/
            **/build/reports/kover/

  android-tests:
    name: Android Instrumented Tests
    runs-on: macos-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run Android Instrumented Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          target: google_apis
          arch: x86_64
          script: ./gradlew connectedAndroidTest

      - name: Upload Android Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: android-test-results
          path: |
            **/build/reports/androidTests/

  ios-tests:
    name: iOS Tests
    runs-on: macos-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run iOS Tests
        run: ./gradlew iosTest

      - name: Upload iOS Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ios-test-results
          path: |
            **/build/reports/tests/
```

---

## 📚 Best Practices

### 1. Test Naming

Use descriptive test names with backticks:

```kotlin
// ✅ GOOD
@Test
fun `should return validation error when email is too long`() { }

@Test
fun `should propagate repository error on network failure`() { }

// ❌ BAD
@Test
fun testValidation() { }

@Test
fun testError() { }
```

### 2. AAA Pattern (Arrange-Act-Assert)

```kotlin
@Test
fun `should return AuthState on successful login`() = runTest {
    // Arrange - Set up test data
    val credentials = LoginCredentials("test@example.com", "password")
    val expectedState = AuthState.Authenticated("token", "user-123")
    coEvery { repository.login(credentials) } returns Result.success(expectedState)

    // Act - Execute the code under test
    val result = useCase(credentials)

    // Assert - Verify the result
    assertTrue(result.isSuccess)
    assertEquals(expectedState, result.getOrNull())
}
```

### 3. Test Independence

Each test should be independent:

```kotlin
class LoginUseCaseTest {
    private lateinit var useCase: LoginUseCase
    private lateinit var mockRepository: AuthRepository

    @BeforeTest
    fun setup() {
        // ✅ GOOD: Fresh setup for each test
        mockRepository = mockk()
        useCase = LoginUseCase(mockRepository)
    }

    @Test
    fun test1() = runTest {
        // Independent test
    }

    @Test
    fun test2() = runTest {
        // Independent test (doesn't depend on test1)
    }
}
```

### 4. Use Test Builders

```kotlin
// test-utils/TestDataGenerator.kt
object TestDataGenerator {
    fun createLoginCredentials(
        email: String = "test@example.com",
        password: String = "password123"
    ) = LoginCredentials(email, password)

    fun createAuthState(
        accessToken: String = "token",
        userId: String = "user-123"
    ) = AuthState.Authenticated(accessToken, "refresh", userId, "test@example.com")
}

// Usage in tests
@Test
fun test() = runTest {
    val credentials = TestDataGenerator.createLoginCredentials()
    val state = TestDataGenerator.createAuthState()
}
```

### 5. Avoid Test Code Duplication

```kotlin
// ✅ GOOD: Extract common setup
abstract class AuthRepositoryTest {
    protected abstract fun createRepository(): AuthRepository

    @Test
    fun `should return AuthState on successful login`() = runTest {
        val repo = createRepository()
        // Test implementation
    }
}

class AuthRepositoryImplTest : AuthRepositoryTest() {
    override fun createRepository() = AuthRepositoryImpl(/* ... */)
}
```

### 6. Mock Only What You Need

```kotlin
// ❌ BAD: Mocking everything
@Test
fun test() {
    val mockRepo = mockk<AuthRepository>()
    val mockMapper = mockk<AuthMapper>()
    val mockValidator = mockk<Validator>()
    // ...
}

// ✅ GOOD: Mock only external dependencies
@Test
fun test() {
    val mockRepo = mockk<AuthRepository> {
        coEvery { login(any()) } returns Result.success(expectedState)
    }
    val useCase = LoginUseCase(mockRepo)
}
```

### 7. Test One Thing at a Time

```kotlin
// ❌ BAD: Testing multiple scenarios
@Test
fun `should test login and logout`() = runTest {
    // Tests login
    // Tests logout
    // Tests validation
    // Too many assertions!
}

// ✅ GOOD: Separate tests for each scenario
@Test
fun `should return AuthState on successful login`() = runTest { }
@Test
fun `should clear tokens on logout`() = runTest { }
@Test
fun `should validate email format`() = runTest { }
```

### 8. Use Parameterized Tests

```kotlin
// ✅ GOOD: Test multiple scenarios with one test
@Test
fun `should validate email format`() {
    val validEmails = listOf(
        "test@example.com",
        "user.name@domain.co.uk",
        "first+last@example.com"
    )

    val invalidEmails = listOf(
        "invalid",
        "@example.com",
        "user@",
        "user @example.com"
    )

    validEmails.forEach { email ->
        assertTrue(EmailValidator.isValid(email), "Email $email should be valid")
    }

    invalidEmails.forEach { email ->
        assertFalse(EmailValidator.isValid(email), "Email $email should be invalid")
    }
}
```

---

## 📊 Coverage Reports

### View Coverage Reports

**HTML Report:**
```bash
# Generate HTML report
./gradlew testCoverage

# Open in browser
open feature/auth/build/reports/kover/html/index.html  # macOS
xdg-open feature/auth/build/reports/kover/html/index.html  # Linux
start feature/auth/build/reports/kover/html/index.html  # Windows
```

**XML Report (for CI/CD):**
```bash
# Generate XML report
./gradlew koverXmlReport

# Path: feature/auth/build/reports/kover/xml/report.xml
```

### Coverage Thresholds

**Add to `build.gradle.kts` (root):**

```kotlin
kover {
    reports {
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
        }
    }

    verify {
        rule {
            name = "Minimum coverage verification"
            bound {
                minValue = 70  // 70% minimum coverage
            }
        }
    }
}
```

---

## 🎯 Summary

This testing infrastructure provides:

✅ **Comprehensive coverage** - Domain, Data, Presentation layers
✅ **Fast feedback** - Unit tests run in seconds
✅ **CI/CD integration** - Automated testing in GitHub Actions
✅ **Code coverage** - Kotlin Kover for coverage reports
✅ **Platform testing** - Support for Android and iOS specific tests
✅ **Best practices** - Clean Architecture, TDD, test independence

### Next Steps

1. ✅ Add testing dependencies to `libs.versions.toml`
2. ✅ Create `testing.gradle.kts` convention plugin
3. ✅ Write example tests for auth feature
4. ✅ Setup CI/CD pipeline
5. ✅ Configure coverage thresholds
6. ✅ Document test writing guidelines for team

---

**Last Updated:** 2026-03-20
**Version:** 1.0.0
**Maintainer:** Development Team
