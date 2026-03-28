# Requirements: Aggregate Service Mobile

**Defined:** 2026-03-28
**Core Value:** Пользователь может найти и забронировать услугу мастера за 3 клика

## v1 Requirements

Requirements for MVP completion. Based on existing 86% implementation.

### Authentication

- [x] **AUTH-01**: User can sign in with Google (Firebase) — implemented
- [x] **AUTH-02**: User can sign in with Apple (Firebase) — implemented
- [x] **AUTH-03**: User can sign in with Phone/SMS (Firebase) — implemented
- [x] **AUTH-04**: Guest mode - browse without authentication — implemented
- [ ] **AUTH-05**: User can register with email/password — PENDING (Registration Flow)
- [x] **AUTH-06**: Auth state persists across sessions — implemented
- [x] **AUTH-07**: Auth guard blocks write operations for guests — implemented

### Catalog

- [x] **CAT-01**: Domain layer (Provider, Service, Category, Location, WorkingHours) — implemented
- [x] **CAT-02**: Data layer (DTOs, mappers, API, repository) — implemented
- [x] **CAT-03**: Presentation layer (UiState, ScreenModels, screens) — partial, UI pending
- [x] **CAT-04**: CatalogScreen — UI connected to real data — COMPLETE
- [x] **CAT-05**: SearchScreen — search with debounced input — COMPLETE
- [x] **CAT-06**: ProviderDetailScreen — provider profile with services — COMPLETE
- [x] **CAT-07**: CategorySelectionScreen — category picker — COMPLETE
- [x] **CAT-08**: ProviderCard component — display provider in list — COMPLETE

### Booking

- [x] **BOOK-01**: Domain layer (Booking, BookingItem, TimeSlot, BookingStatus) — implemented
- [x] **BOOK-02**: Data layer (DTOs, API, repository) — implemented
- [x] **BOOK-03**: Presentation layer (screens, models) — implemented
- [x] **BOOK-04**: BookingNavigator — cross-feature navigation — implemented
- [ ] **BOOK-05**: Booking flow UI integration with real data — PENDING
- [ ] **BOOK-06**: SelectServiceScreen — select provider service — PENDING
- [ ] **BOOK-07**: SelectDateTimeScreen — calendar + time slots — PENDING
- [ ] **BOOK-08**: BookingConfirmationScreen — confirm booking — PENDING
- [x] **BOOK-09**: BookingHistoryScreen — list user bookings — PENDING

### Services (Provider)

- [x] **SERV-01**: ProviderService entity with CRUD — implemented
- [x] **SERV-02**: ServicesRepository — implemented
- [x] **SERV-03**: Presentation (ServicesListScreen, ServiceFormScreen) — implemented

### Profile

- [x] **PROF-01**: Profile entity with no-show stats — implemented
- [x] **PROF-02**: ProfileRepository — implemented
- [x] **PROF-03**: Presentation (ProfileScreen) — implemented, needs UI integration
- [ ] **PROF-04**: ProfileScreen UI — view/edit profile — PENDING

### Favorites

- [x] **FAV-01**: Favorite entity, repository, use cases — implemented
- [x] **FAV-02**: FavoritesScreenModel — implemented
- [x] **FAV-03**: FavoritesScreen UI — list favorites — PENDING
- [x] **FAV-04**: Add/remove from favorites — PENDING

### Reviews

- [x] **REV-01**: Review entity, ReviewStats — implemented
- [x] **REV-02**: ReviewsRepository — implemented
- [x] **REV-03**: ReviewsScreenModel — implemented
- [ ] **REV-04**: ReviewsScreen UI — list reviews — PENDING
- [ ] **REV-05**: WriteReviewDialog — submit review — PENDING

### Navigation

- [x] **NAV-01**: Voyager navigation setup — implemented
- [x] **NAV-02**: Screen sealed class — implemented
- [x] **NAV-03**: AppNavHost — implemented
- [ ] **NAV-04**: Connect all screens to navigation — PENDING

### Infrastructure

- [x] **INF-01**: Core modules (network, config, storage, utils, di, navigation, theme, i18n) — implemented
- [ ] **INF-02**: CI/CD pipeline (GitHub Actions) — PENDING
- [ ] **INF-03**: Test coverage 80% — PENDING (currently 55%)

## v2 Requirements

Deferred to future release.

### Maps
- **MAP-01**: Google Maps для Android
- **MAP-02**: MapKit для iOS
- **MAP-03**: Provider markers on map
- **MAP-04**: Map-based provider search

### Push Notifications
- **PUSH-01**: Firebase Cloud Messaging setup
- **PUSH-02**: Booking reminders
- **PUSH-03**: Review prompts

### Offline Mode
- **OFFL-01**: Offline data caching
- **OFFL-02**: Sync when online

## Out of Scope

| Feature | Reason |
|---------|--------|
| Desktop/Web | Mobile-only MVP |
| Real-time chat | Not core to booking value, high complexity |
| Video calls | Not required for service booking |
| Social features | Out of scope for v1 |
| Admin panel | Provider portal is different from client app |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| AUTH-01 | — | Complete |
| AUTH-02 | — | Complete |
| AUTH-03 | — | Complete |
| AUTH-04 | — | Complete |
| AUTH-05 | Phase 1 | Pending |
| AUTH-06 | — | Complete |
| AUTH-07 | — | Complete |
| CAT-01 | — | Complete |
| CAT-02 | — | Complete |
| CAT-03 | — | Complete |
| CAT-04 | Phase 1 | Complete |
| CAT-05 | Phase 1 | Complete |
| CAT-06 | Phase 1 | Complete |
| CAT-07 | Phase 1 | Complete |
| CAT-08 | Phase 1 | Complete |
| BOOK-01 | — | Complete |
| BOOK-02 | — | Complete |
| BOOK-03 | — | Complete |
| BOOK-04 | — | Complete |
| BOOK-05 | Phase 1 | Pending |
| BOOK-06 | Phase 1 | Pending |
| BOOK-07 | Phase 1 | Pending |
| BOOK-08 | Phase 1 | Pending |
| BOOK-09 | Phase 1 | Complete |
| SERV-01 | — | Complete |
| SERV-02 | — | Complete |
| SERV-03 | — | Complete |
| PROF-01 | — | Complete |
| PROF-02 | — | Complete |
| PROF-03 | — | Complete |
| PROF-04 | Phase 1 | Pending |
| FAV-01 | — | Complete |
| FAV-02 | — | Complete |
| FAV-03 | Phase 1 | Complete |
| FAV-04 | Phase 1 | Complete |
| REV-01 | — | Complete |
| REV-02 | — | Complete |
| REV-03 | — | Complete |
| REV-04 | Phase 1 | Pending |
| REV-05 | Phase 1 | Pending |
| NAV-01 | — | Complete |
| NAV-02 | — | Complete |
| NAV-03 | — | Complete |
| NAV-04 | Phase 1 | Pending |
| INF-01 | — | Complete |
| INF-02 | Phase 2 | Pending |
| INF-03 | Phase 2 | Pending |

**Coverage:**
- v1 requirements: 42 total
- Mapped to phases: 22 pending
- Complete: 20
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-28*
*Last updated: 2026-03-28 after completing plan 01-03 (catalog UI wiring)*
