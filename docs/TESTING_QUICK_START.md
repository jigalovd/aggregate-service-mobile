# 🚀 Testing Quick Start Guide

## 📋 Overview

This guide will help you get started with testing in the KMP project. It covers the basics of running tests, writing tests, and understanding the testing infrastructure.

---

## 🏃 Running Tests

### Run All Tests

```bash
# Run all tests across all modules
./gradlew testAll

# Run all tests and generate coverage report
./gradlew testCoverage
```

### Run Specific Module Tests

```bash
# Run tests for auth feature
./gradlew :feature:auth:test

# Run tests for core:network
./gradlew :core:network:test
```

### Run Platform-Specific Tests

```bash
# Run Android unit tests
./gradlew :feature:auth:androidUnitTest

# Run iOS tests
./gradlew :feature:auth:iosTest

# Run Android instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Generate Coverage Reports

```bash
# Generate HTML coverage report
./gradlew koverReportAll

# Open coverage report in browser
open feature/auth/build/reports/kover/html/index.html  # macOS
xdg-open feature/auth/build/reports/kover/html/index.html  # Linux
start feature/auth/build/reports/kover/html/index.html  # Windows
```

---

## ✍️ Writing Tests

### Test Structure

Tests are organized in the same package structure as the production code:

```
feature/auth/src/
├── commonMain/kotlin/
│   └── com/aggregateservice/feature/auth/
│       ├── domain/
│       │   └── usecase/
│       │       └── LoginUseCase.kt
│       └── presentation/
│           └── screenmodel/
│               └── LoginScreenModel.kt
│
└── commonTest/kotlin/
    └── com/aggregateservice/feature/auth/
        ├── domain/
        │   └── usecase/
        │       └── LoginUseCaseTest.kt
        └── presentation/
            └── screenmodel/
                └── LoginScreenModelTest.kt
```

### Example: Domain Layer Test

```kotlin
package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.test.TestDataGenerator
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {

    @Test
    fun `should return AuthState on successful login`() = runTest {
        // Arrange
        val credentials = TestDataGenerator.createLoginCredentials()
        val expectedState = TestDataGenerator.createAuthState()

        val mockRepository = mockk<AuthRepository> {
            coEvery { login(credentials) } returns Result.success(expectedState)
        }
        val useCase = LoginUseCase(mockRepository)

        // Act
        val result = useCase(credentials)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedState, result.getOrNull())
        coVerify(exactly = 1) { mockRepository.login(credentials) }
    }
}
```

### Example: Presentation Layer Test

```kotlin
package com.aggregateservice.feature.auth.presentation.screenmodel

import app.cash.turbine.test
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.test.TestDataGenerator
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
    fun `should show loading state during login`() = runTest {
        // Arrange
        val credentials = LoginCredentials("test@example.com", "password123")

        val mockUseCase = mockk<LoginUseCase> {
            coEvery { this@mockk(credentials) } coAnswers {
                kotlinx.coroutines.delay(100)
                Result.success(TestDataGenerator.createAuthState())
            }
        }

        val screenModel = LoginScreenModel(mockUseCase)
        screenModel.onEmailChanged("test@example.com")
        screenModel.onPasswordChanged("password123")

        // Act
        screenModel.onLoginClick()

        // Assert
        screenModel.uiState.test {
            skipItems(1) // Skip initial state

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isLoginSuccess)
        }
    }
}
```

---

## 🛠️ Testing Tools

### Test Data Generator

Use `TestDataGenerator` to create test data:

```kotlin
import com.aggregateservice.core.test.TestDataGenerator

// Create login credentials
val credentials = TestDataGenerator.createLoginCredentials(
    email = "test@example.com",
    password = "password123"
)

// Create auth state
val authState = TestDataGenerator.createAuthState(
    accessToken = "token123",
    userId = "user-456"
)

// Generate random email
val randomEmail = TestDataGenerator.generateRandomEmail()

// Generate random string
val randomString = TestDataGenerator.generateRandomString(length = 20)
```

### MockK (Mocking)

```kotlin
import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.coVerify

// Create mock
val mockRepository = mockk<AuthRepository>()

// Setup behavior
coEvery { mockRepository.login(any()) } returns Result.success(authState)

// Verify calls
coVerify(exactly = 1) { mockRepository.login(credentials) }
```

### Turbine (Flow Testing)

```kotlin
import app.cash.turbine.test

// Test Flow emissions
screenModel.uiState.test {
    val initialState = awaitItem()
    assertEquals("", initialState.email)

    val loadingState = awaitItem()
    assertTrue(loadingState.isLoading)

    val successState = awaitItem()
    assertTrue(successState.isLoginSuccess)

    awaitComplete() // Verify flow completed
}
```

### Coroutines Test

```kotlin
import kotlinx.coroutines.test.runTest

@Test
fun test() = runTest {
    // Test code with coroutines
    val result = repository.login(credentials)
    assertTrue(result.isSuccess)
}
```

---

## 📚 Best Practices

### 1. Use Descriptive Test Names

```kotlin
// ✅ GOOD
@Test
fun `should return validation error when email is too long`() { }

// ❌ BAD
@Test
fun testValidation() { }
```

### 2. Follow AAA Pattern

```kotlin
@Test
fun test() = runTest {
    // Arrange - Set up test data
    val credentials = TestDataGenerator.createLoginCredentials()

    // Act - Execute code under test
    val result = useCase(credentials)

    // Assert - Verify results
    assertTrue(result.isSuccess)
}
```

### 3. Test One Thing at a Time

```kotlin
// ✅ GOOD: Separate tests
@Test
fun `should validate email format`() { }
@Test
fun `should validate password length`() { }

// ❌ BAD: Testing multiple things
@Test
fun `should test all validations`() { }
```

### 4. Use Test Utilities

```kotlin
// ✅ GOOD: Use TestDataGenerator
val credentials = TestDataGenerator.createLoginCredentials()

// ❌ BAD: Manual creation
val credentials = LoginCredentials(
    email = "test@example.com",
    password = "password123"
)
```

### 5. Test Error Cases

```kotlin
@Test
fun `should return Unauthorized on invalid credentials`() = runTest {
    coEvery { repository.login(any()) } returns Result.failure(AppError.Unauthorized)

    val result = useCase(credentials)

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is AppError.Unauthorized)
}
```

---

## 🎯 Coverage Goals

| Layer | Target | Current |
|-------|--------|---------|
| Domain | 90%+ | - |
| Data | 80%+ | - |
| Presentation | 70%+ | - |

---

## 📖 Additional Resources

- [Full Testing Documentation](/docs/TESTING_INFRASTRUCTURE.md)
- [Testing Best Practices](/docs/TESTING_INFRASTRUCTURE.md#best-practices)
- [Kotlin Test Documentation](https://kotlinlang.org/api/latest/kotlin.test/)
- [MockK Documentation](https://mockk.io/)
- [Turbine Documentation](https://github.com/cashapp/turbine)

---

**Need Help?** Check the [Testing Infrastructure](/docs/TESTING_INFRASTRUCTURE.md) documentation for more details.
