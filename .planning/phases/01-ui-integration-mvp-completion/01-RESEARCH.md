# Phase 1: UI Integration & MVP Completion - Research

**Researched:** 2026-03-28
**Domain:** KMP Jetpack Compose UI Integration with Voyager Navigation + Koin DI
**Confidence:** HIGH

## Summary

Phase 1 requires connecting all presentation layers (screens, ScreenModels) to real data flows and completing the booking user flow. The project uses Feature-first Clean Architecture with Voyager navigation, Koin DI, and unidirectional data flow (UDF). All ScreenModels and UseCases exist; the work is in wiring screens to navigation, fixing data flow bugs, and implementing AUTH-05 (email/password registration).

**Primary recommendation:** Focus on NAV-04 (connect all screens to navigation) as the blocking task. All other requirements (CAT-04 to CAT-08, BOOK-05 to BOOK-09, etc.) have complete ScreenModels but are disconnected from the main navigation graph.

## Standard Stack

### Core Libraries
| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.2.20 | Primary language |
| Compose Multiplatform | 1.10.2 | UI framework |
| Voyager | 1.1.0-beta02 | Navigation |
| Koin | 4.2.0 | Dependency injection |
| Ktor | 3.4.1 | HTTP client |
| Firebase Auth | 23.2.0 | Authentication |
| Coil3 | 3.4.0 | Image loading |
| DataStore | (built-in) | Token storage |

**Verified via:** `build.gradle.kts` dependencies and `Gradle/LibraryVersion` convention plugin

## Architecture Patterns

### 1. Screen → ScreenModel → UseCase → Repository Data Flow

**Pattern established in codebase:**
```kotlin
// Screen (Voyager)
class CatalogScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<CatalogScreenModel>()  // Koin injection
        val uiState by screenModel.uiState.collectAsState()
        // Pass state and callbacks to stateless content composable
    }
}

// ScreenModel (StateHolder)
class CatalogScreenModel(
    private val searchProvidersUseCase: SearchProvidersUseCase,  // UseCase injection
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ScreenModel {
    private val _uiState = MutableStateFlow(CatalogUiState.Initial)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        screenModelScope.launch {
            getCategoriesUseCase(parentId = null).fold(...)
        }
    }
}
```

**Source:** `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/presentation/screenmodel/CatalogScreenModel.kt`

### 2. Voyager Navigation with AppNavHost

**Current setup:**
```kotlin
// MainActivity.kt
setContent {
    AppNavHost(startScreen = CatalogScreen())  // Only CatalogScreen connected
}

// AppNavHost (core/navigation)
@Composable
fun AppNavHost(startScreen: Screen) {
    Navigator(screen = startScreen) { navigator ->
        SlideTransition(navigator) { currentScreen ->
            currentScreen.Content()
        }
    }
}
```

**Source:** `core/navigation/src/commonMain/kotlin/com/aggregateservice/core/navigation/Navigator.kt`

### 3. Koin DI Module Pattern (Feature Modules)

**Pattern for feature DI:**
```kotlin
// feature/catalog/di/CatalogModule.kt
val catalogModule = module {
    single { CatalogApiService(get()) }
    single<CatalogRepository> { CatalogRepositoryImpl(apiService = get()) }
    factoryOf(::SearchProvidersUseCase)
    factoryOf(::CatalogScreenModel)
}
```

**Source:** `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/di/CatalogModule.kt`

**Initialization via `initializeKoin` in Application:**
```kotlin
// AppModule.kt
fun initializeKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()  // Feature modules added HERE
    modules(appModule)
}
```

### 4. AuthGuard for Protected Operations

**Pattern:**
```kotlin
@Composable
fun SomeProtectedButton(
    isAuthenticated: Boolean,
    onShowAuthPrompt: (AuthPromptTrigger) -> Unit
) {
    AuthGuard(
        isAuthenticated = isAuthenticated,
        trigger = AuthPromptTrigger.Booking,
        onShowPrompt = onShowAuthPrompt,
        content = { BookButton() }
    )
}
```

**Source:** `core/navigation/src/commonMain/kotlin/com/aggregateservice/core/navigation/AuthGuard.kt`

### 5. Cross-Feature Navigation via BookingNavigator

**Interface in core (no feature dependency):**
```kotlin
// core/navigation/BookingNavigator.kt
interface BookingNavigator {
    fun createSelectServiceScreen(providerId: String, providerName: String): Screen
}
```

**Implementation in feature:booking:**
```kotlin
// feature/booking/navigation/BookingNavigatorImpl.kt
class BookingNavigatorImpl : BookingNavigator {
    override fun createSelectServiceScreen(providerId: String, providerName: String): Screen =
        SelectServiceScreen(providerId = providerId, providerName = providerName)
}
```

**Source:** `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/navigation/BookingNavigatorImpl.kt`

## Common Pitfalls

### Pitfall 1: Hardcoded User ID in BookingHistoryScreen
**What goes wrong:** `val clientId = "current-user-id"` ignores AuthState, bookings won't load for logged-in users.
**Location:** `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/presentation/screen/BookingHistoryScreen.kt:50`
**Fix:** Inject `AuthStateProvider` and extract actual userId:
```kotlin
val authProvider: AuthStateProvider = koin.get()
val clientId = authProvider.currentUserId  // Need to add to interface
```

### Pitfall 2: Empty Services List in BookingConfirmationScreen
**What goes wrong:** Passes `emptyList()` for services with comment "booking will fail if services are required"
**Location:** `feature/booking/src/commonMain/kotlin/com/aggregateservice/feature/booking/presentation/screen/BookingConfirmationScreen.kt:70`
**Fix:** Services should be passed via SavedStateHandle or loaded via `GetBookingServicesUseCase` based on serviceIds

### Pitfall 3: Incomplete Favorites Integration
**What goes wrong:** `ProviderDetailScreenModel` hardcodes `isFavorite = false` and toggle only updates local state
**Location:** `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/presentation/screenmodel/ProviderDetailScreenModel.kt:74,135`
**Fix:** Inject `IsFavoriteUseCase` to check status, wire `AddFavoriteUseCase`/`RemoveFavoriteUseCase` for persistence

### Pitfall 4: Commented-Out Navigation in FavoritesScreen
**What goes wrong:** TODO comment `navigator.push(ProviderDetailScreen(favorite.providerId))` prevents navigation
**Location:** `feature/favorites/src/commonMain/kotlin/com/aggregateservice/feature/favorites/presentation/screen/FavoritesScreen.kt:72`
**Fix:** Uncomment and import `ProviderDetailScreen`

### Pitfall 5: Firebase Auth Doesn't Support Email/Password
**What goes wrong:** Firebase Auth supports Google, Apple, Phone - but NOT email/password directly
**Context:** AUTH-05 (email/password registration) is listed as pending with "Registration Flow" note
**Fix approach:** Backend-based registration via `RegisterUseCase` + API endpoint, Firebase Auth only for SSO

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Navigation | Custom navigation | Voyager Screen + AppNavHost | Voyager provides type-safe navigation with Screen sealed class |
| DI | Manual dependency creation | Koin module | Koin provides scope management and testability |
| Auth state | Custom state management | AuthStateProvider + AuthGuard | Abstracts auth state for cross-feature access without circular dependencies |
| HTTP client | Custom fetch | Ktor with SafeApiCall | Ktor provides interceptors, content negotiation, auth |
| Image loading | Coil 2.x | Coil3 | Project uses Coil3 for Compose Multiplatform |

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| AUTH-05 | User can register with email/password | `RegisterUseCase` + `RegistrationScreenModel` exist; needs UI wiring + backend API |
| CAT-04 | CatalogScreen UI connected to real data | ScreenModel complete; needs nav integration + ProviderCard component |
| CAT-05 | SearchScreen with debounced input | `SearchScreenModel` has 300ms debounce implemented via `debounce(SEARCH_DEBOUNCE_MS)` |
| CAT-06 | ProviderDetailScreen with services | `ProviderDetailScreenModel` exists; favorites bug needs fixing |
| CAT-07 | CategorySelectionScreen | `CategorySelectionScreen.kt` exists |
| CAT-08 | ProviderCard component | `ProviderCard.kt` exists in `feature/catalog/presentation/component/` |
| BOOK-05 to BOOK-09 | Booking flow UI | All screens exist; BOOK-06/07/08/09 need services data passed through flow |
| PROF-04 | ProfileScreen UI | `ProfileScreen.kt` exists; needs nav integration |
| FAV-03, FAV-04 | Favorites UI | Screen exists; needs nav wiring + favorite status integration |
| REV-04, REV-05 | Reviews UI | `ReviewsScreen` + `WriteReviewDialog` exist; needs integration |
| NAV-04 | Connect all screens to navigation | Main blocker - all screens exist but disconnected from AppNavHost |

## Key Research Findings

### Navigation Integration Pattern

The key insight is that `AppNavHost` only has `CatalogScreen` connected as start screen. All other screens (`FavoritesScreen`, `ReviewsScreen`, `BookingHistoryScreen`, `ProfileScreen`, `RegistrationScreen`) are implemented but not wired into the navigation graph.

**Pattern for adding screens to navigation:**
1. Add screen as destination in navigation state (bottom nav or drawer)
2. Wire screen creation via `koinScreenModel` in the screen's `Content()`
3. Connect via bottom navigation bar or tab bar

### ScreenModel Pattern with Debounced Search

The `SearchScreenModel` shows the correct pattern:
```kotlin
@OptIn(FlowPreview::class)
class SearchScreenModel(...) : ScreenModel {
    private val searchQueryFlow = MutableStateFlow("")

    init {
        searchQueryFlow
            .debounce(SEARCH_DEBOUNCE_MS)  // 300ms
            .onEach { query -> if (query.isNotBlank()) search(query) }
            .launchIn(screenModelScope)
    }
}
```

### AuthGuard Pattern Usage

From `AuthGuard.kt`:
- `AuthGuard` is a composable wrapper that shows/hides content based on auth state
- `executeProtectedAction` is an extension for executing actions only when authenticated
- `AuthPromptTrigger` enum identifies why auth is needed (Booking, Review, Favorites)

### Registration Flow Issue

The `RegisterUseCase` exists and calls `repository.register(request)` which should call the backend API. The issue is Firebase Auth limitations - email/password registration must go through the backend API, not Firebase. The `RegistrationScreenModel` is fully implemented.

## Code Examples

### ProviderCard Component (CAT-08)
**Source:** `feature/catalog/src/commonMain/kotlin/com/aggregateservice/feature/catalog/presentation/component/ProviderCard.kt`

```kotlin
@Composable
fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    i18nProvider: I18nProvider = koinInject(),
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.MD)) {
            Text(text = provider.businessName, style = MaterialTheme.typography.titleMedium)
            provider.shortDescription?.let { desc ->
                Text(text = desc, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Row {
                Text(text = "⭐ ${provider.formattedRating}", style = MaterialTheme.typography.bodyMedium)
                Text(text = " • ${i18nProvider.get(StringKey.Plurals.REVIEWS_COUNT, provider.reviewCount)}")
            }
            Text(text = provider.location.city, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

### ScreenModel Test Pattern
**Source:** `feature/catalog/src/commonTest/kotlin/com/aggregateservice/feature/catalog/presentation/screenmodel/CatalogScreenModelTest.kt`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load providers and categories`() = runTest {
        searchProvidersBehavior = { Result.success(SearchResult(...)) }
        getCategoriesBehavior = { Result.success(categories) }

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertEquals(3, state.providers.size)
    }
}
```

## Open Questions

1. **How to pass services through booking flow?**
   - `SelectServiceScreen` passes `serviceIds` to `SelectDateTimeScreen`
   - `BookingConfirmationScreen` receives `serviceIds` but needs full `BookingService` objects
   - Need to either: pass services via SavedStateHandle, or reload via `GetBookingServicesUseCase`

2. **AuthStateProvider missing `currentUserId`?**
   - Interface only exposes `isAuthenticatedFlow: StateFlow<Boolean>`
   - `BookingHistoryScreen` needs `userId` to load bookings
   - Need to add `userId` property to `AuthStateProvider`

3. **What's the backend API for email/password registration?**
   - `RegisterUseCase` calls `repository.register(request)`
   - Need to verify endpoint exists and `AuthRepositoryImpl.register()` implementation

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Gradle | Build | ✓ | 8.13 | — |
| Android SDK | Android build | ✓ | (system) | — |
| Koin DI | All screens | ✓ | 4.2.0 | — |
| Voyager | Navigation | ✓ | 1.1.0-beta02 | — |
| Ktor | Network | ✓ | 3.4.1 | — |
| Firebase Auth | AUTH-01-04 | ✓ | 23.2.0 | — |

**Missing dependencies with no fallback:**
- None identified for Phase 1

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Kotlin Test (commonTest) |
| Config file | None — convention plugin handles |
| Quick run command | `./gradlew :feature:catalog:test` |
| Full suite command | `./gradlew test` |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | File |
|--------|----------|-----------|------|
| CAT-04 | CatalogScreen loads providers from API | Unit | `CatalogScreenModelTest.kt` (exists) |
| BOOK-05 | Booking flow integration | Unit | `BookingHistoryScreenModelTest.kt` (missing) |
| BOOK-06 | SelectServiceScreen loads services | Unit | `SelectServiceScreenModelTest.kt` (missing) |
| FAV-03 | FavoritesScreen loads favorites | Unit | `FavoritesScreenModelTest.kt` (exists) |

### Wave 0 Gaps
- [ ] `feature/booking/src/commonTest/.../BookingHistoryScreenModelTest.kt` — covers BOOK-09
- [ ] `feature/booking/src/commonTest/.../SelectServiceScreenModelTest.kt` — covers BOOK-06
- [ ] `feature/booking/src/commonTest/.../BookingConfirmationScreenModelTest.kt` — covers BOOK-08
- [ ] `feature/reviews/src/commonTest/.../ReviewsScreenModelTest.kt` — covers REV-04

## Sources

### Primary (HIGH confidence)
- Voyager documentation — navigation patterns
- Koin 4.x documentation — DI module patterns
- Project codebase analysis — verified existing patterns

### Secondary (MEDIUM confidence)
- CONCERNS.md — technical debt identified in codebase
- REQUIREMENTS.md — requirement traceability

### Tertiary (LOW confidence)
- Firebase Auth email/password limitations — Firebase documentation (needs verification for current SDK)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — all versions verified in build.gradle.kts
- Architecture: HIGH — patterns verified in existing code
- Pitfalls: HIGH — issues documented in CONCERNS.md with file locations

**Research date:** 2026-03-28
**Valid until:** 2026-04-27 (30 days for stable architecture)
