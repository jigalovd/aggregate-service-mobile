# Mobile Platform Problems and Limitations

This document tracks known issues, architectural decisions, and workarounds discovered during mobile UI testing development.

## S06 Findings: Mobile UI Tests

### 1. Test-Friendly Composables Pattern

**Problem:** Production composables (e.g., `LoginScreen`) typically use dependency injection frameworks like Koin and navigation libraries like Voyager, making direct testing difficult.

**Solution:** Create test-specific composable variants that accept all dependencies as parameters:

```kotlin
@Composable
private fun TestableLoginContent(
    title: String,
    subtitle: String,
    buttonText: String,
    uiState: LoginUiState,
    onSignInClick: () -> Unit = {},
)
```

This pattern:
- Bypasses Koin/Voyager DI entirely
- Allows direct state injection for testing
- Keeps production code unchanged
- Makes UI state tests deterministic

**Trade-off:** Tests validate the composable logic, not the production integration. Coordinate with E02 (Instrumented Tests) for full integration coverage.

---

### 2. Robolectric Manifest Resolution with createAndroidComposeRule

**Problem:** `createAndroidComposeRule<ComponentActivity>()` requires an activity with `LAUNCHER` intent defined in the test manifest. Robolectric needs explicit manifest configuration to resolve the activity class.

**Symptom:** Tests may fail with `'Unable to resolve activity'` error when running via Robolectric without proper manifest setup.

**Mitigation:** Tests target the generic `ComponentActivity` base class, which should be resolvable. If manifest issues arise:

1. Ensure `AndroidManifest.xml` in the `androidUnitTest` source set includes:
   ```xml
   <activity android:name="androidx.activity.ComponentActivity" />
   ```

2. Or configure Robolectric to use a synthetic manifest via `@Config(shadows = [...])`

**Status:** Configuration validated; runtime behavior may vary by Robolectric version.

---

### 3. Test Data Class Patterns for ViewModel State

**Problem:** Real ViewModels are DI-dependent, making state-driven UI testing complex.

**Solution:** Define test-specific state data classes that mirror ViewModel state:

```kotlin
private data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
)
```

Benefits:
- Isolates UI logic from ViewModel business logic
- Enables deterministic state-driven tests
- Documents expected state shape for future ViewModel integration

---

### 4. Known Runtime Limitation

**Issue:** Tests compile successfully but may fail at runtime with `'Unable to resolve activity'` error when Robolectric cannot resolve the `ComponentActivity` class from the test manifest.

**Impact:** Unit tests pass in compilation but may fail in CI/local Robolectric runs without emulator.

**Workaround:** 
- Use `createAndroidComposeRule<ComponentActivity>()` which works with Robolectric's built-in activity resolution
- For CI, ensure Robolectric is configured with proper SDK versions
- Consider instrumented tests (E02) as fallback for full integration validation

---

## General Guidelines

- **Unit Tests (androidUnitTest):** Test composable UI logic in isolation using test-friendly variants
- **Instrumented Tests (androidInstrumentedTest):** Test real ViewModels and DI integration on device/emulator
- **Robolectric:** Works for basic Compose testing but has manifest/activity resolution edge cases