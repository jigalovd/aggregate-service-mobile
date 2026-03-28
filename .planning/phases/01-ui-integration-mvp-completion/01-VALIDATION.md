---
phase: "01"
slug: 01-ui-integration-mvp-completion
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-28
---

# Phase 01 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Kotlin Test (commonTest) |
| **Config file** | None — convention plugin handles |
| **Quick run command** | `./gradlew :feature:booking:test :feature:reviews:test` |
| **Full suite command** | `./gradlew test` |
| **Estimated runtime** | ~120 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew :feature:booking:test :feature:reviews:test`
- **After every plan wave:** Run `./gradlew test`
- **Before `/gsd:verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 01-02-01 | 02 | 1 | BOOK-09 | unit | `./gradlew :feature:booking:test` | ❌ W0 | ⬜ pending |
| 01-02-02 | 02 | 1 | BOOK-09 | unit | `./gradlew :feature:booking:test` | ❌ W0 | ⬜ pending |
| 01-02-03 | 02 | 1 | CAT-06 | unit | `./gradlew :feature:catalog:test` | ✅ | ⬜ pending |
| 01-02-04 | 02 | 1 | FAV-03 | unit | `./gradlew :feature:favorites:test` | ✅ | ⬜ pending |
| 01-04-01 | 04 | 2 | BOOK-05 | unit | `./gradlew :feature:booking:test` | ❌ W0 | ⬜ pending |
| 01-04-02 | 04 | 2 | BOOK-06 | unit | `./gradlew :feature:booking:test` | ❌ W0 | ⬜ pending |
| 01-04-03 | 04 | 2 | BOOK-08 | unit | `./gradlew :feature:booking:test` | ❌ W0 | ⬜ pending |
| 01-04-04 | 04 | 2 | BOOK-09 | unit | `./gradlew :feature:booking:test` | ❌ W0 | ⬜ pending |
| 01-05-02 | 05 | 2 | REV-04 | unit | `./gradlew :feature:reviews:test` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `feature/booking/src/commonTest/.../BookingHistoryScreenModelTest.kt` — stubs for BOOK-09
- [ ] `feature/booking/src/commonTest/.../SelectServiceScreenModelTest.kt` — stubs for BOOK-06
- [ ] `feature/booking/src/commonTest/.../BookingConfirmationScreenModelTest.kt` — stubs for BOOK-08
- [ ] `feature/reviews/src/commonTest/.../ReviewsScreenModelTest.kt` — stubs for REV-04

*If none: "Existing infrastructure covers all phase requirements."*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Bottom navigation tab switching | NAV-04 | Requires device/emulator | Run app, tap each tab |
| Registration flow end-to-end | AUTH-05 | Requires backend | Run app, complete registration |

*If none: "All phase behaviors have automated verification."*

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending