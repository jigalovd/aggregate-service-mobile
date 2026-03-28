# Codebase Concerns

**Analysis Date:** 2026-03-28

## Tech Debt

**Hardcoded User ID in Booking Module:**
- Issue: `BookingHistoryScreen.kt` uses hardcoded `val clientId = "current-user-id"` instead of fetching from AuthState
- Files: `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/presentation/screen/BookingHistoryScreen.kt` (line 50)
- Impact: Authentication state is ignored, bookings will not load correctly for logged-in users
- Fix approach: Inject AuthState and extract actual userId

**Empty Services List in Booking Confirmation:**
- Issue: `BookingConfirmationScreen.kt` passes `emptyList()` for services with comment noting booking will fail if services are required
- Files: `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/presentation/screen/BookingConfirmationScreen.kt` (line 70)
- Impact: Booking creation will fail when services are needed
- Fix approach: Pass services from previous screen or load via repository

**Incomplete Favorites Integration:**
- Issue: `ProviderDetailScreenModel` hardcodes `isFavorite = false` instead of checking actual favorite status
- Files: `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/presentation/screenmodel/ProviderDetailScreenModel.kt` (line 74)
- Issue: Toggle favorite only updates local state, does not persist
- Files: `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/presentation/screenmodel/ProviderDetailScreenModel.kt` (line 135)
- Impact: Favorite status is not accurately displayed or persisted
- Fix approach: Implement GetFavoriteStatusUseCase and wire up AddToFavoritesUseCase/RemoveFromFavoritesUseCase

**Missing Navigation for Favorites:**
- Issue: Favorites screen has commented-out navigation with TODO
- Files: `feature/favorites/src/commonMain/kotlin/com/aggregateservice/feature/favorites/presentation/screen/FavoritesScreen.kt` (line 72)
- Impact: Clicking a favorite does not navigate to provider details
- Fix approach: Uncomment and implement navigation

**API Response userId Not Mapped:**
- Issue: Auth repository sets `userId = ""` when API response does not include userId
- Files: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt` (lines 277, 339)
- Impact: Authenticated users have empty userId, breaking user-specific operations
- Fix approach: Update API contract or extract userId from token

**Catalog Filter UI Not Implemented:**
- Issue: Filter button click is a no-op
- Files: `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/presentation/screen/CatalogScreen.kt` (line 157)
- Impact: Users cannot filter catalog results
- Fix approach: Implement filter bottom sheet

## Known Bugs

**Duplicate Text in Empty Booking State:**
- Symptom: `i18nProvider[StringKey.Booking.NO_BOOKINGS]` is used twice in succession
- Files: `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/presentation/screen/BookingHistoryScreen.kt` (lines 117, 121)
- Trigger: Viewing booking history when no bookings exist
- Workaround: None - cosmetic issue only

## Security Considerations

**API Key Exposure Risk:**
- Risk: API_KEY is included in BuildConfig and defaults to empty string
- Files: `androidApp/src/androidMain/kotlin/com/aggregateservice/androidApp/MainApplication.kt` (line 59)
- Current mitigation: Falls back to empty string if not configured
- Recommendations: Ensure API_KEY is properly set in secrets.properties and never committed

**Token Storage:**
- Risk: Access tokens stored via TokenStorage - ensure platform implementations are secure
- Files: `core/storage/src/androidMain/kotlin/com/aggregateservice/core/storage/TokenStorage.android.kt`
- Files: `core/storage/src/iosMain/kotlin/com/aggregateservice/core/storage/TokenStorage.ios.kt`
- Recommendations: Verify encrypted storage on Android (EncryptedSharedPreferences) and Keychain on iOS

## Performance Bottlenecks

**Auth Interceptor Complexity:**
- Problem: `executeWithRefresh` function handles token refresh with manual response parsing
- Files: `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/AuthInterceptor.kt` (lines 151-282)
- Cause: Custom implementation instead of using Ktor's built-in Auth plugin properly
- Improvement path: Leverage Ktor's Auth plugin with proper bearer provider for cleaner token refresh

## Fragile Areas

**Complex Auth State Management:**
- Files: `feature/auth/src/commonMain/kotlin/com/aggregateservice/feature/auth/data/repository/AuthRepositoryImpl.kt`
- Why fragile: Multiple state transitions (Firebase login, token refresh, account linking) with error handling spread across 350+ lines
- Safe modification: Add comprehensive tests before modifying auth flow
- Test coverage: Has error handling tests but relies heavily on mockk

**Token Refresh Race Condition Prevention:**
- Uses Mutex in `AuthInterceptor.configureAuth` for thread-safety
- Files: `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/AuthInterceptor.kt` (line 54)
- Potential issue: If refresh fails silently, multiple concurrent requests could all attempt refresh

## Scaling Limits

**In-Memory Auth State:**
- Current capacity: Single AuthState instance in repository
- Limit: No support for multi-user switching or session management
- Scaling path: Implement session storage and user switching capability

**Booking History Pagination:**
- No pagination detected in `GetClientBookingsUseCase`
- Current capacity: Loads all bookings
- Limit: Will degrade with many historical bookings
- Scaling path: Add cursor-based pagination

## Dependencies at Risk

**Ktor 3.4.1:**
- Risk: Older version of Ktor; check if any known CVEs apply
- Impact: Network layer vulnerability could expose auth tokens
- Migration plan: Consider upgrading to Ktor 3.x latest if security issues exist

**Kotlin 2.2.20:**
- Risk: Using recent Kotlin version which may have undiscovered issues
- Impact: Build failures if breaking changes are discovered
- Migration plan: Pin to stable version, delay rapid updates

**Firebase Auth 23.2.0:**
- Risk: Firebase SDK versions can have security vulnerabilities
- Impact: Authentication bypass possible
- Migration plan: Keep firebase-bom updated regularly

## Missing Critical Features

**No iOS Implementation Detected:**
- Problem: Only `androidApp` module has actual platform code; no iOS source files found
- Blocks: Cannot build iOS app from this codebase
- Priority: HIGH if iOS support is required

**No Error Recovery for Network Failures:**
- Problem: No retry with backoff for failed API calls
- Blocks: Poor UX on flaky connections
- Priority: MEDIUM

**No Offline Mode:**
- Problem: All data fetched from network; no local caching strategy beyond token storage
- Blocks: App unusable without network
- Priority: LOW for MVP, MEDIUM for production

## Test Coverage Gaps

**Feature Modules Without Tests:**

| Module | Test Status | Priority |
|--------|-------------|----------|
| `feature/booking` | No tests | HIGH |
| `feature/services` | No tests | HIGH |
| `feature/favorites` | No tests | HIGH |
| `feature/schedule` | No tests | HIGH |
| `feature/reviews` | 1 test (ReviewStatsTest) | MEDIUM |

**What is tested:**

- `feature/auth`: Domain models, repository with mocks, error handling
- `feature/catalog`: Mappers, use cases, screen models
- `feature/profile`: Use cases, repository, screen model
- `core/utils`: Email and password validation

**Recommended test additions (in priority order):**
1. `feature/booking` - BookingRepositoryImpl, ScreenModels, UseCases
2. `feature/services` - Full test suite for CRUD operations
3. `feature/favorites` - Use cases and repository
4. `feature/schedule` - Scheduling logic
5. `feature/reviews` - CreateReviewUseCase, repository

**ScreenModel Tests Needed:**
- `BookingHistoryScreenModel` - loadBookings, cancelBooking flows
- `BookingConfirmationScreenModel` - booking creation flow
- `ServicesListScreenModel` - service management
- `ReviewsScreenModel` - review submission

---

*Concerns audit: 2026-03-28*
