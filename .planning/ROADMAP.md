# Roadmap: Aggregate Service Mobile

**Created:** 2026-03-28
**Phases:** 2
**Requirements:** 22 pending (20 complete)

---

## Phase 1: UI Integration & MVP Completion

**Goal:** Connect all presentation layers to real data and complete the booking user flow

**Requirements:** AUTH-05, CAT-04, CAT-05, CAT-06, CAT-07, CAT-08, BOOK-05, BOOK-06, BOOK-07, BOOK-08, BOOK-09, PROF-04, FAV-03, FAV-04, REV-04, REV-05, NAV-04

**Success Criteria:**
1. User can browse catalog and see real providers
2. User can search providers by name/category
3. User can view provider details with services
4. User can complete full booking flow (select service -> date/time -> confirm)
5. User can view and manage their bookings
6. User can add/remove favorites
7. User can view and write reviews
8. User can edit their profile
9. All screens properly navigate via Voyager
10. User can register with email/password

**Plans:**
5/6 plans executed
- [x] 01-02-PLAN.md -- Core Bug Fixes (hardcoded values, navigation comments)
- [x] 01-03-PLAN.md -- Catalog UI Wiring (CatalogScreen, SearchScreen, ProviderDetailScreen, CategorySelectionScreen)
- [x] 01-04-PLAN.md -- Booking Flow Completion (SelectService, SelectDateTime, BookingConfirmation, BookingHistory)
- [x] 01-05-PLAN.md -- Favorites, Reviews, Profile Wiring (COMPLETED 2026-03-28)
- [ ] 01-06-PLAN.md -- Auth Integration (currentUserId, registration flow)

---

## Phase 2: Quality & Infrastructure

**Goal:** CI/CD pipeline, test coverage improvement, Registration flow

**Requirements:** AUTH-05, INF-02, INF-03

**Success Criteria:**
1. GitHub Actions CI pipeline runs lint + tests on PR
2. Code coverage reports generated via Kover
3. Test coverage reaches 80%
4. Registration with email/password works end-to-end
5. All detekt/ktlint checks pass in CI

---

## Summary

| Phase | Name | Requirements | Status |
|-------|------|--------------|--------|
| 1 | 1/6 | In Progress|  |
| 2 | Quality & Infrastructure | 3 | Pending |

**Total:** 2 phases, 19 requirements pending
