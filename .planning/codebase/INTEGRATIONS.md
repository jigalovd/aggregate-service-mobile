# External Integrations

**Analysis Date:** 2026-03-28

## Authentication & Identity

**Firebase Authentication (Android only)**
- **Purpose:** User authentication (sign-in, sign-up, session management)
- **Module:** `core/firebase-auth`
- **Android Dependencies:**
  - `firebase-auth-ktx` (23.2.0)
  - `google-play-services-auth` (21.3.0)
- **iOS:** Stub implementation only (no Firebase on iOS side)
- **Auth Methods:** Not fully specified in build files; Google Sign-In is configured
- **Configuration:** `google-services.json` present at `app/google-services.json`

## API & Backend

**Backend API**
- **Base URLs (per build variant):**
  - Debug/Development: `https://api.dev.aggregateservice.com`
  - Release/Staging: `https://api.staging.aggregateservice.com`
- **Version:** `v1` (via `API_VERSION` BuildConfig)
- **Network Timeout:** 30000ms
- **Client:** Ktor HTTP client with:
  - Content negotiation (JSON)
  - Auth token handling
  - Logging (debug builds only)
- **Environment Variables:**
  - `API_KEY` - API authentication key (from `api.key` property or `API_KEY` env var)

## CI/CD & Deployment

**GitHub Actions**
- **Workflow File:** `.github/workflows/test.yml`
- **Jobs:**
  - `unit-tests` (Ubuntu latest) - Runs `./gradlew testAll`, coverage to Codecov
  - `android-tests` (macOS latest) - Instrumented Android tests on emulator (API 34)
  - `ios-tests` (macOS latest) - iOS tests via `./gradlew iosTest`
- **Coverage:** Codecov integration via `codecov/codecov-action@v4`
- **Artifacts:** Test results and coverage reports uploaded

**Build Variants**
- **Debug:** Development configuration, logging enabled
- **Release:** Minification enabled, ProGuard rules applied

## Local Storage

**Android DataStore Preferences**
- **Type:** Key-value preferences storage
- **Module:** `core/storage`
- **Dependencies:** `androidx-datastore-preferences` (1.2.1)
- **Platform:** Android only (iOS uses native alternatives)

## Logging & Monitoring

**Logging Framework (Android)**
- **SLF4J API** (2.0.16) - Logging facade
- **Logback Android** (3.0.0) - Implementation
- **Configuration:** `config/logging/logback.xml`
- **Features:**
  - Console appender (Logcat) for debug builds
  - File appender with rolling (7-day retention, 10MB cap)
  - Ktor HTTP logging (DEBUG level in debug builds)
  - OkHttp logging (WARN level)
  - Koin DI logging (ERROR level only)
- **Log Location:** `/data/data/com.aggregateservice/files/logs/aggregate-service.log`

**Note:** No crash reporting service (e.g., Crashlytics, Sentry) detected in dependencies.

## Image Loading

**Coil (Kotlin)**
- **Purpose:** Async image loading and caching for Compose
- **Version:** 3.4.0
- **Network Engines:**
  - `coil-network-ktor3` - Ktor-based (shared)
  - `coil-network-okhttp` - OkHttp-based (Android)
- **Features:** Memory/disk caching, Compose integration

## Code Quality Services

**Codecov**
- **Purpose:** Code coverage tracking
- **Integration:** Via GitHub Actions `codecov/codecov-action@v4`
- **Coverage Reports:** XML format from Kover (`**/build/reports/kover/xml/**/*.xml`)

## Environment Configuration

**Build Configuration (Android)**
| Variable | Debug | Release |
|----------|-------|---------|
| API_BASE_URL | `https://api.dev.aggregateservice.com` | `https://api.staging.aggregateservice.com` |
| ENVIRONMENT | `DEV` | `STAGING` |
| DEBUG | `true` | `false` |
| ENABLE_LOGGING | `true` | `false` |

**Secrets Management**
- `secrets.properties` - Local development secrets (gitignored)
- `secrets.properties.local` - Local overrides (gitignored)
- `local.secrets.properties` - Alternative local secrets (gitignored)

## Not Detected

The following common integrations are NOT present in this codebase:

- **Push Notifications:** No FCM, APNs, or equivalent
- **Analytics:** No analytics SDK (Amplitude, Mixpanel, Firebase Analytics)
- **Crash Reporting:** No Crashlytics, Sentry, or equivalent
- **Remote Config:** No Firebase Remote Config or LaunchDarkly
- **A/B Testing:** No experimentation framework
- **iOS Backend:** No Firebase Auth on iOS side (stub only)
- **Social Auth:** Only Google Sign-In mentioned (via play-services-auth)
- **Maps:** No map SDK
- **Payments:** No payment processor integration

---

*Integration audit: 2026-03-28*
