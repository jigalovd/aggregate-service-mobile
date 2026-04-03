# ARCH-04: Firebase Auth Parity Audit

**Audit Date:** 2026-04-03
**Requirement:** ARCH-04
**Phase:** 01-architecture
**Plan:** 01-03
**Status:** COMPLETE

## Objective

Audit Android `FirebaseAuthApi` implementation against the KMP expect interface. Document parity gaps between expect and actual. iOS is **out of scope** per D-13.

---

## Method-by-Method Parity Analysis

| Method | Expect Signature | Android Actual | Status | Notes |
|--------|-----------------|---------------|--------|-------|
| `setActivity` | `fun setActivity(activity: Activity): Unit` | Implemented | **MATCH** | Requires `ComponentActivity`. Uses `ActivityResultLauncher` via `ComponentActivity.registerForActivityResult`. |
| `signInWithGoogle` | `suspend fun (): Result<FirebaseToken>` | Implemented | **MATCH** | Full implementation using `GoogleSignInClient` + `ActivityResultLauncher`. Refreshes ID token after Firebase Auth sign-in. |
| `signInWithApple` | `suspend fun (): Result<FirebaseToken>` | Returns `NotImplementedError` | **GAP** | Android returns `NotImplementedError("Apple Sign-In requires native iOS implementation")`. Expected on Android since Apple Sign-In is iOS-only. |
| `signInWithPhoneStart` | `suspend fun (phoneNumber: String): Result<String>` | Returns `NotImplementedError` | **GAP** | Phone auth not implemented on Android. Returns `NotImplementedError("Phone auth requires Activity context. Use FirebaseUI PhoneAuthActivity.")`. |
| `confirmPhoneCode` | `suspend fun (verificationId, code): Result<FirebaseToken>` | Implemented | **MATCH** | Full implementation using `PhoneAuthProvider.getCredential()`. Properly handles token refresh. |
| `signOut` | `suspend fun (): Unit` | Implemented | **MATCH** | Delegates to `FirebaseAuth.signOut()` with `Dispatchers.Main`. |
| `getCurrentUser` | `fun (): FirebaseUser?` | Implemented | **MATCH** | Returns `AndroidFirebaseUserImpl` wrapping `FirebaseUser`. |

### Summary Table

| Category | Count |
|----------|-------|
| MATCH | 5 |
| GAP | 2 |
| **Total** | 7 |

---

## Return Type Analysis

### FirebaseToken Data Class

**Expect (commonMain):**
```kotlin
data class FirebaseToken(
    val idToken: String,
    val authProvider: String,
)
```

**Android actual:** Uses the commonMain definition directly. **MATCH.**

The `authProvider` values are:
- `"google.com"` for Google Sign-In
- `"phone"` for phone authentication
- `"apple.com"` would be for Apple Sign-In (not yet implemented on iOS)

### FirebaseUser Interface

**Expect (commonMain):**
```kotlin
interface FirebaseUser {
    val uid: String
    val email: String?
    val displayName: String?
}
```

**Android actual:** `AndroidFirebaseUserImpl` wraps `com.google.firebase.auth.FirebaseUser`. **MATCH.**

---

## Gap Categorization

### Android-Specific Gaps (Can Be Fixed on Android)

#### Gap 1: `signInWithApple` — NotImplementedError
- **Location:** `FirebaseAuthApiAndroid.kt:139-142`
- **Current behavior:** Returns `Result.failure(NotImplementedError("Apple Sign-In requires native iOS implementation"))`
- **Analysis:** This is correct behavior on Android since Apple Sign-In is iOS-only per Apple's guidelines. The `NotImplementedError` is a reasonable approach to document this limitation.
- **Recommendation:** Keep as-is. The error message clearly indicates this is iOS-only. No Android implementation needed.

#### Gap 2: `signInWithPhoneStart` — NotImplementedError
- **Location:** `FirebaseAuthApiAndroid.kt:144-150`
- **Current behavior:** Returns `Result.failure(NotImplementedError("Phone auth requires Activity context. Use FirebaseUI PhoneAuthActivity."))`
- **Analysis:** Phone auth via Firebase in KMP is problematic because it requires `PhoneAuthActivity` from FirebaseUI, which is an Android-only library. The current implementation cannot work in a KMP context because:
  1. `PhoneAuthProvider` requires `Activity` context
  2. FirebaseUI's `PhoneAuthActivity` is a single-activity pattern that conflicts with KMP navigation
  3. Phone auth verification codes are received via SMS which requires system integration
- **Recommendation:** This gap requires architectural decision (see Rule 4). Options:
  - **Option A:** Use FirebaseUI PhoneAuthActivity (Android-only, abandons KMP purity)
  - **Option B:** Skip phone auth on Android (return `NotImplementedError` permanently)
  - **Option C:** Implement via native KMP flow using `PhoneAuthProvider` with custom `ActivityResultLauncher` integration (complex, not currently implemented)
- **Current state:** Option C is attempted but not completed. The `signInWithPhoneStart` throws instead of returning a verification ID.

### Cross-Platform Gaps (Affect Both Android and iOS)

#### Cross-Platform Gap 1: Phone Auth Architecture
- **Issue:** The expect interface does not reflect the Android limitation that phone auth requires Activity context
- **Impact:** `signInWithPhoneStart` on Android cannot return a `verificationId` — it throws immediately
- **Recommendation:** Consider adding platform-specific notes to the expect interface KDoc, or using `expect fun` with `actual` implementations for platform-specific behavior

---

## Recommendations

### Immediate Actions (This Phase)

1. **No code changes required** — The Android implementation is correct for what it claims to support.
2. **Document the gaps** — This audit report serves as the documentation for ARCH-04.

### Future Actions (Deferred to Relevant Phases)

| Priority | Action | Owner | Phase |
|---------|--------|-------|-------|
| HIGH | Resolve phone auth architecture for Android (Option A/B/C) | Mobile Team | Phase 5 (Stabilization — Mobile) |
| MEDIUM | iOS team implements `signInWithApple` | iOS Team | Separate iOS effort |
| MEDIUM | iOS team implements full FirebaseAuthApi | iOS Team | Separate iOS effort |
| LOW | Add platform limitation notes to expect interface KDoc | Mobile Team | Per-feature |

---

## iOS Status (Per D-13: Out of Scope)

The iOS implementation is **not audited** in this phase per decision D-13 (iOS is separate team/track).

iOS status from codebase inspection:
- iOS actual class exists at `iosMain` but appears to be a stub/KMP-only placeholder
- iOS team needs full implementation of all 7 methods
- This is documented but NOT actioned in this phase

**iOS Gaps (for iOS team reference):**
| Method | iOS Status |
|--------|------------|
| `setActivity` | N/A on iOS (no Activity concept) |
| `signInWithGoogle` | Needs implementation |
| `signInWithApple` | Needs implementation |
| `signInWithPhoneStart` | Needs implementation |
| `confirmPhoneCode` | Needs implementation |
| `signOut` | Needs implementation |
| `getCurrentUser` | Needs implementation |

---

## Conclusion

The Android `FirebaseAuthApi` implementation is **5/7 methods complete** (71% parity).

**Implemented (MATCH):**
- `setActivity` — Correctly uses `ComponentActivity` + `ActivityResultLauncher`
- `signInWithGoogle` — Full implementation with token refresh
- `confirmPhoneCode` — Full implementation with credential verification
- `signOut` — Properly delegates to `FirebaseAuth.signOut()`
- `getCurrentUser` — Returns wrapped `AndroidFirebaseUserImpl`

**Gaps (GAP):**
- `signInWithApple` — Correctly not implemented on Android (Apple-only)
- `signInWithPhoneStart` — Architecture issue requiring future decision

**iOS:** Out of scope per D-13. Full implementation needed by iOS team.

---

*Audit completed as part of ARCH-04 requirement (Phase 01-architecture, Plan 01-03)*
