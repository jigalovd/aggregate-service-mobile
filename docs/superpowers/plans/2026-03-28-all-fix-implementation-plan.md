# ALL FIX IMPLEMENTATION PLAN
## Aggregate Mobile KMP/CMP - 2026-03-28

**Based on:** Business Logic Review (review-2026-03-28-business-logic.md)
**Verification:** `./gradlew testAll` (ktlint + detekt + kover)

---

## OVERVIEW

| Category | Count | Priority |
|----------|-------|----------|
| CRITICAL bugs | 2 | Must fix |
| INCOMPLETE workflows | 3 | High |
| Code quality issues | 8+ | Medium/Low |
| Architecture violations | 2 | Medium |

**Execution:** Independent PRs, one task per commit, subagent-driven workflow.

---

## PHASE 1: CRITICAL Business Logic Fixes

### 1.1 [CRITICAL-1] RescheduleBookingUseCase - Add provider.allowReschedule check
**File:** `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/domain/usecase/RescheduleBookingUseCase.kt`

**Problem:** Provider cannot disable rescheduling (US-3.43).

**Fix:**
```kotlin
class RescheduleBookingUseCase(
    private val repository: BookingRepository,
    private val profileRepository: ProfileRepository, // ADD
) {
    suspend operator fun invoke(...): Result<Booking> {
        val booking = repository.getBookingById(bookingId).getOrElse { return it }
        val providerProfile = profileRepository.getProfile(booking.providerId).getOrElse {
            return Result.failure(it)
        }
        if (!providerProfile.allowReschedule) {
            return Result.failure(
                AppError.ValidationError(
                    field = "reschedule",
                    message = "Provider has disabled rescheduling for their bookings"
                )
            )
        }
        // ... existing validations ...
    }
}
```

**Verification:** `./gradlew :feature:booking:testDebugUnitTest`

---

### 1.2 [CRITICAL-2] CreateReviewUseCase - Add booking status validation
**File:** `feature/reviews/src/commonMain/kotlin/com/aggregateservice/feature/reviews/domain/usecase/CreateReviewUseCase.kt`

**Problem:** Review can be created for non-COMPLETED bookings (US-5.2).

**Fix:**
```kotlin
class CreateReviewUseCase(
    private val repository: ReviewsRepository,
    private val bookingRepository: BookingRepository, // ADD
) {
    suspend operator fun invoke(bookingId: String, rating: Int, comment: String?): Result<Review> {
        // ... existing validations ...

        // ADD: Validate booking status is COMPLETED (defense-in-depth)
        val booking = bookingRepository.getBookingById(bookingId).getOrElse {
            return Result.failure(it)
        }
        if (booking.status != BookingStatus.COMPLETED) {
            return Result.failure(
                AppError.ValidationError(
                    field = "bookingId",
                    message = "Review can only be created for completed bookings"
                )
            )
        }

        return repository.createReview(bookingId, rating, trimmedComment)
    }
}
```

**Verification:** `./gradlew :feature:reviews:testDebugUnitTest`

---

## PHASE 2: Booking Engine Fixes

### 2.1 [CONSTRAINT-1] CancelBookingUseCase - Fix time calculation
**File:** `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/domain/usecase/CancelBookingUseCase.kt`

**Problem:** Millisecond precision causes edge case failures. US-3.5.

**Fix:**
```kotlin
// Before (line 54-63):
val minCancelTime = Instant.fromEpochMilliseconds(
    booking.startTime.toEpochMilliseconds() - CANCEL_WINDOW_HOURS * 60 * 60 * 1000,
)

// After:
val minCancelTime = booking.startTime.minus(CANCEL_WINDOW_HOURS, DateTimeUnit.HOUR)
```

**Verification:** `./gradlew :feature:booking:testDebugUnitTest`

---

### 2.2 [INCOMPLETE-1] SelectDateTimeScreen - Add booking_horizon UI
**Files:**
- `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/presentation/screen/SelectDateTimeScreen.kt`
- `feature/booking/src/commonMain/.../SelectDateTimeUiState.kt`

**Problem:** User doesn't see booking limitation (US-3.34).

**Fix:**
```kotlin
// In UiState:
data class SelectDateTimeUiState(
    // ...
    val maxAdvanceDays: Int = MAX_ADVANCE_DAYS, // ADD
    val bookingHorizonVisible: Boolean = false, // ADD
)

// In Screen:
Text(
    text = "Бронирование возможно не более чем за $maxAdvanceDays дней",
    modifier = Modifier.visibility(bookingHorizonVisible)
)
```

**Verification:** `./gradlew :feature:booking:compileKotlinAndroid`

---

### 2.3 [INCOMPLETE-3] ProviderDetailScreen - Add AuthGuard to AddFavorite
**File:** `feature/catalog/src/commonMain/.../presentation/screen/ProviderDetailScreen.kt`

**Problem:** Guest user gets 401 instead of auth prompt (US-2.4).

**Fix:** Wrap AddFavorite button with AuthGuard or check auth state before call.

**Verification:** Code review + manual test

---

## PHASE 3: Network Layer Fixes

### 3.1 SafeApiCall - Fix CancellationException handling
**File:** `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/SafeApiCall.kt`

**Problem:** CancellationException swallowed, breaks structured concurrency.

**Fix (line 158):**
```kotlin
} catch (e: Exception) {
    if (e is kotlinx.coroutines.CancellationException) throw e // ADD as first line
    lastException = e
    // ...
}
```

---

### 3.2 SafeApiCall - Remove unused errorBody variables
**File:** `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/SafeApiCall.kt`

**Problem:** Lines 78, 96 parse errorBody but never use detail.

**Fix:**
```kotlin
// Line 78: Before
val errorBody = response.body<ErrorResponse>()
Result.failure(AppError.Unauthorized)

// After
Result.failure(AppError.Unauthorized)

// Line 96: Before
val errorBody = response.body<ErrorResponse>()
Result.failure(AppError.NotFound)

// After
Result.failure(AppError.NotFound)

// Line 89: Keep - uses errorBody.detail
```

---

### 3.3 SafeApiCall - Fix SerializationException before retry
**File:** `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/SafeApiCall.kt`

**Problem:** SerializationException triggers retry loop unnecessarily.

**Fix (lines 160-168):**
```kotlin
} catch (e: Exception) {
    if (e is kotlinx.serialization.SerializationException) {
        // Don't retry parse errors
        return Result.failure(AppError.UnknownError(...))
    }
    // ... retry logic below ...
}
```

---

## PHASE 4: Code Quality Fixes

### 4.1 ReviewStats.getPercentageForRating - Fix integer division
**File:** `feature/reviews/src/commonMain/kotlin/com/aggregateservice/feature/reviews/domain/model/ReviewStats.kt`

**Problem:** Returns Int instead of Double (CALCULATION-1).

**Fix (line 28):**
```kotlin
// Before:
fun getPercentageForRating(rating: Int): Int {
    if (totalReviews == 0) return 0
    val count = ratingDistribution[rating] ?: 0
    return (count * 100) / totalReviews
}

// After:
fun getPercentageForRating(rating: Int): Double {
    if (totalReviews == 0) return 0.0
    val count = ratingDistribution[rating] ?: 0
    return (count * 100.0) / totalReviews
}
```

**Note:** Update callers to handle Double return type.

---

### 4.2 ProviderService - Add duration validation
**File:** `feature/services/src/commonMain/kotlin/com/aggregateservice/feature/services/domain/model/ProviderService.kt`

**Problem:** Duration validation missing in domain entity (CONSTRAINT-2).

**Fix:**
```kotlin
data class ProviderService(
    val id: String,
    val providerId: String,
    val name: String,
    val description: String?,
    val basePrice: Double,
    val durationMinutes: Int,
    val categoryId: String,
) {
    init {
        require(durationMinutes in 5..480) {
            "Duration must be between 5 and 480 minutes"
        }
    }
}
```

---

### 4.3 build-logic/build.gradle.kts - Remove redundant compileOnly
**File:** `build-logic/build.gradle.kts`

**Problem:** Both compileOnly and implementation for same dependencies.

**Fix:**
```kotlin
dependencies {
    // Remove compileOnly, keep only implementation
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.ktlint.gradlePlugin)
    implementation(libs.kover.gradlePlugin)
}
```

---

## PHASE 5: UI/Architecture Fixes

### 5.1 CatalogScreen - Fix LazyRow for CategoryChipsRow
**File:** `feature/catalog/src/commonMain/.../presentation/screen/CatalogScreen.kt`

**Problem:** Row used instead of LazyRow for scrollable chips.

**Fix:**
```kotlin
@Composable
fun CategoryChipsRow(categories: List<Category>) {
    LazyRow {
        items(categories, key = { it.id }) { category ->
            FilterChip(category = category)
        }
    }
}
```

---

### 5.2 LinkAccountDialog - Remove unused isLoading state
**File:** `androidApp/src/commonMain/.../LinkAccountDialog.kt`

**Problem:** Dead code - isLoading never set to true.

**Fix:** Remove `var isLoading by remember { mutableStateOf(false) }`

---

### 5.3 ProviderCard - Consolidate duplicate implementations
**Files:**
- `feature/catalog/src/commonMain/.../presentation/screen/CatalogScreen.kt`
- `feature/reviews/src/commonMain/.../presentation/screen/ReviewCard.kt`

**Problem:** Duplicate ProviderCard with different i18n.

**Fix:** Use single ProviderCard component with i18n support.

---

## PHASE 6: Tests

### 6.1 Write SafeApiCallTest
**File:** `core/network/src/commonTest/kotlin/.../SafeApiCallRealTest.kt`

**Tests to add:**
```kotlin
@Test
fun cancellationExceptionIsRethrown() = runTest {
    // Verify CancellationException propagates
}

@Test
fun noContentReturnsNull() {
    // Verify 204 handling
}

@Test
fun errorBodyParsingNotRetried() {
    // Verify SerializationException fails fast
}
```

---

### 6.2 Write ReviewStatsTest
**File:** `feature/reviews/src/commonTest/kotlin/.../ReviewStatsTest.kt`

**Tests to add:**
```kotlin
@Test
fun percentageForRating_returnsDouble() {
    val stats = ReviewStats(..., ratingDistribution = mapOf(5 to 1), totalReviews = 3)
    assertEquals(33.33, stats.getPercentageForRating(5), 0.01)
}
```

---

## PHASE 7: Final Verification

### 7.1 Run full test suite
```bash
./gradlew testAll
./gradlew ktlintFormatAll
./gradlew detektAll
./gradlew build
```

### 7.2 Module-specific tests
```bash
./gradlew :feature:auth:allTests
./gradlew :feature:catalog:allTests
./gradlew :feature:booking:allTests
./gradlew :feature:reviews:allTests
./gradlew :feature:favorites:allTests
./gradlew :feature:services:allTests
./gradlew :feature:profile:allTests
./gradlew :core:network:testDebugUnitTest
./gradlew testCoverage
```

---

## DEPENDENCY ORDER

```
Phase 1 (CRITICAL)
  ├── 1.1 RescheduleBookingUseCase
  └── 1.2 CreateReviewUseCase
        ↓
Phase 2 (Booking Engine)
  ├── 2.1 CancelBookingUseCase time fix
  ├── 2.2 SelectDateTimeScreen UI
  └── 2.3 ProviderDetailScreen AuthGuard
        ↓
Phase 3 (Network)
  ├── 3.1 CancellationException
  ├── 3.2 unused errorBody
  └── 3.3 SerializationException retry
        ↓
Phase 4 (Code Quality)
  ├── 4.1 ReviewStats division
  ├── 4.2 ProviderService validation
  └── 4.3 build-logic compileOnly
        ↓
Phase 5 (UI/Architecture)
  ├── 5.1 LazyRow
  ├── 5.2 LinkAccountDialog
  └── 5.3 ProviderCard consolidation
        ↓
Phase 6 (Tests)
  └── 6.1-6.2 New tests
        ↓
Phase 7 (Verification)
  └── 7.1-7.2 Full test suite
```

---

## FILE MANIFEST

| Phase | File | Change |
|-------|------|--------|
| 1.1 | `feature/booking/.../RescheduleBookingUseCase.kt` | Add ProfileRepository + allowReschedule check |
| 1.2 | `feature/reviews/.../CreateReviewUseCase.kt` | Add BookingRepository + status check |
| 2.1 | `feature/booking/.../CancelBookingUseCase.kt` | Use kotlinx.datetime API |
| 2.2 | `feature/booking/.../SelectDateTimeUiState.kt` | Add booking horizon fields |
| 2.2 | `feature/booking/.../SelectDateTimeScreen.kt` | Show booking horizon hint |
| 2.3 | `feature/catalog/.../ProviderDetailScreen.kt` | Add AuthGuard to AddFavorite |
| 3.1 | `core/network/.../SafeApiCall.kt` | Rethrow CancellationException |
| 3.2 | `core/network/.../SafeApiCall.kt` | Remove unused errorBody |
| 3.3 | `core/network/.../SafeApiCall.kt` | Fail fast on SerializationException |
| 4.1 | `feature/reviews/.../ReviewStats.kt` | Return Double from percentage |
| 4.2 | `feature/services/.../ProviderService.kt` | Add init block validation |
| 4.3 | `build-logic/build.gradle.kts` | Remove redundant compileOnly |
| 5.1 | `feature/catalog/.../CatalogScreen.kt` | LazyRow for chips |
| 5.2 | `androidApp/.../LinkAccountDialog.kt` | Remove isLoading |
| 5.3 | `feature/reviews/.../ReviewCard.kt` | Use consolidated ProviderCard |
| 6.1 | `core/network/.../SafeApiCallRealTest.kt` | Add cancellation tests |
| 6.2 | `feature/reviews/.../ReviewStatsTest.kt` | Add percentage tests |

---

**Status:** Ready for execution
**Estimated Tasks:** 16
**Phases:** 7
