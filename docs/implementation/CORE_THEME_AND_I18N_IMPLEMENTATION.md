# Детали имплементации core:theme и core:i18n

**Версия документа:** 1.0
**Дата создания:** 2026-03-21
**Автор:** Development Team
**Статус:** ✅ Complete

---

## 📋 Содержание

1. [Обзор модулей](#1-обзор-модулей)
2. [core:theme - Material 3 Design System](#2-coretheme---material-3-design-system)
3. [core:i18n - Internationalization](#3-corei18n---internationalization)
4. [Интеграция с приложением](#4-интеграция-с-приложением)
5. [Архитектурные решения](#5-архитектурные-решения)
6. [Примеры использования](#6-примеры-использования)

---

## 1. Обзор модулей

### 1.1 Назначение

| Модуль | Назначение | Ключевые возможности |
|--------|------------|---------------------|
| **:core:theme** | Visual Design System | Material 3 colors, Typography, Shapes, Dimensions, RTL support |
| **:core:i18n** | Internationalization | 3 languages (ru, en, he), RTL detection, API i18n extraction |

### 1.2 Структура файлов

```
core/
├── theme/src/commonMain/kotlin/com/aggregateservice/core/theme/
│   ├── AppColors.kt      # 50+ Material 3 colors (light/dark)
│   ├── Theme.kt          # appTheme() composable + RTL support
│   ├── Typography.kt     # Material 3 type scale
│   ├── Shape.kt          # Corner radii for components
│   └── Dimensions.kt     # Spacing, dimensions, animation
│
└── i18n/src/commonMain/kotlin/com/aggregateservice/core/i18n/
    ├── Locale.kt         # AppLocale enum (ru, en, he)
    ├── I18nProvider.kt   # Interface + StringKey object
    ├── I18nProviderImpl.kt # Default implementation + builder
    ├── Strings.kt        # 100+ strings per language
    ├── FlattenI18n.kt    # API response i18n extraction
    └── di/I18nModule.kt  # Koin DI module
```

---

## 2. core:theme - Material 3 Design System

### 2.1 AppColors - Цветовая палитра

**Файл:** `AppColors.kt`

#### Структура цветов

```kotlin
object AppColors {
    // Primary - Основные brand colors
    val Primary = Color(0xFF6750A4)           // Light theme
    val PrimaryDark = Color(0xFFD0BCFF)       // Dark theme

    // Secondary - Акцентные цвета
    val Secondary = Color(0xFF625B71)
    val SecondaryDark = Color(0xFFCCC2DC)

    // Tertiary - Дополнительные акценты
    val Tertiary = Color(0xFF7D5260)
    val TertiaryDark = Color(0xFFEFB8C8)

    // Error - Ошибки
    val Error = Color(0xFFB3261E)
    val ErrorDark = Color(0xFFF2B8B5)

    // Semantic - Семантические цвета
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)

    // Neutral - Фоны и поверхности
    val Surface = Color(0xFFFFFBFE)           // Light
    val SurfaceDark = Color(0xFF1C1B1F)       // Dark
    // ... + surfaceContainer variants

    // Outlines
    val Outline = Color(0xFF79747E)
    val OutlineDark = Color(0xFF938F99)
}
```

#### Категории цветов

| Категория | Цветов | Описание |
|-----------|--------|----------|
| **Primary** | 8 | Основной brand color + контейнеры |
| **Secondary** | 8 | Акцентные цвета |
| **Tertiary** | 8 | Дополнительные акценты |
| **Error** | 8 | Ошибки + контейнеры |
| **Semantic** | 12 | Success, Warning, Info |
| **Surface** | 20 | Фоны (light/dark) |
| **Outline** | 4 | Границы и разделители |
| **Text** | 6 | OnSurface, OnBackground |
| **Inverse** | 3 | Инвертированные цвета |

**Всего:** ~77 цветов

### 2.2 Theme.kt - Тема приложения

**Ключевые функции:**

```kotlin
// Базовая тема с ручным управлением RTL
@Composable
fun appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    content: @Composable () -> Unit,
)

// Автоматический RTL на основе языка
@Composable
fun appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    languageCode: String,  // "en", "ru", "he", "ar", "fa", "ur"
    content: @Composable () -> Unit,
)
```

#### RTL Support

```kotlin
val layoutDirection = when (languageCode) {
    "he", "ar", "fa", "ur" -> LayoutDirection.Rtl
    else -> LayoutDirection.Ltr
}
```

**Поддерживаемые RTL языки:**
- `he` - Иврит
- `ar` - Арабский
- `fa` - Персидский (Farsi)
- `ur` - Урду

#### Extension Properties

```kotlin
// Доступ к цветам через MaterialTheme
val MaterialTheme.appColors: AppColors
    @Composable get() = AppColors

// Доступ к spacing
val MaterialTheme.spacing: Spacing
    @Composable get() = Spacing

// Доступ к dimensions
val MaterialTheme.dimensions: Dimensions
    @Composable get() = Dimensions
```

### 2.3 Typography.kt - Типографика

**Material 3 Type Scale:**

| Стиль | Размер | Line Height | Использование |
|-------|--------|-------------|---------------|
| `displayLarge` | 57sp | 64sp | Hero headlines |
| `displayMedium` | 45sp | 52sp | Large headlines |
| `displaySmall` | 36sp | 44sp | Medium headlines |
| `headlineLarge` | 32sp | 40sp | Section headers |
| `headlineMedium` | 28sp | 36sp | Sub-sections |
| `headlineSmall` | 24sp | 32sp | Card headers |
| `titleLarge` | 22sp | 28sp | App bar titles |
| `titleMedium` | 16sp | 24sp | Card titles |
| `titleSmall` | 14sp | 20sp | Small titles |
| `bodyLarge` | 16sp | 24sp | Main content |
| `bodyMedium` | 14sp | 20sp | Secondary content |
| `bodySmall` | 12sp | 16sp | Captions |
| `labelLarge` | 14sp | 20sp | Buttons |
| `labelMedium` | 12sp | 16sp | Tags |
| `labelSmall` | 11sp | 16sp | Small labels |

### 2.4 Shape.kt - Формы компонентов

```kotlin
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Chips
    small = RoundedCornerShape(8.dp),        // Text fields
    medium = RoundedCornerShape(12.dp),      // Cards
    large = RoundedCornerShape(16.dp),       // Bottom sheets
    extraLarge = RoundedCornerShape(28.dp),  // Modal dialogs
)

// Дополнительные формы
object AppShape {
    val Full = RoundedCornerShape(50)        // Avatars, FAB
    val Button = RoundedCornerShape(20.dp)   // Buttons
    val BottomSheet = RoundedCornerShape(    // Bottom sheets
        topStart = 28.dp, topEnd = 28.dp,
        bottomStart = 0.dp, bottomEnd = 0.dp
    )
}
```

### 2.5 Dimensions.kt - Размеры и отступы

#### Spacing (8dp Grid System)

```kotlin
object Spacing {
    val None = 0.dp
    val XXS = 2.dp      // Minimal spacing
    val XS = 4.dp       // Icon padding, badges
    val SM = 8.dp       // Internal component padding
    val MD = 16.dp      // Standard padding
    val LG = 24.dp      // Section spacing
    val XL = 32.dp      // Large block spacing
    val XXL = 48.dp     // Extra large blocks
    val XXXL = 64.dp    // Hero sections
}
```

#### Component Dimensions

```kotlin
object Dimensions {
    // Icons
    val IconXS = 16.dp
    val IconSM = 20.dp
    val IconMD = 24.dp    // Standard icon
    val IconLG = 32.dp
    val IconXL = 48.dp

    // Buttons
    val ButtonHeightSM = 32.dp
    val ButtonHeightMD = 40.dp
    val ButtonHeightLG = 48.dp
    val ButtonHeightXL = 56.dp

    // Text Fields
    val TextFieldHeight = 56.dp
    val TextFieldHeightSM = 48.dp

    // Cards
    val CardElevation = 2.dp
    val CardCornerSize = 12.dp

    // List Items
    val ListItemHeightSM = 48.dp
    val ListItemHeightMD = 56.dp
    val ListItemHeightLG = 72.dp
    val ListItemHeightXL = 88.dp

    // Avatars
    val AvatarSM = 32.dp
    val AvatarMD = 40.dp
    val AvatarLG = 56.dp
    val AvatarXL = 80.dp

    // Navigation
    val BottomNavHeight = 80.dp
    val TopAppBarHeight = 64.dp

    // FAB
    val FabSize = 56.dp
    val FabSizeMini = 40.dp

    // Accessibility
    val MinTouchTarget = 48.dp  // Material minimum
}
```

#### Animation Durations

```kotlin
object Animation {
    const val Fast = 100      // Quick transitions
    const val Normal = 200    // Standard animations
    const val Slow = 400      // Complex animations
    const val VerySlow = 800  // Hero animations
}
```

---

## 3. core:i18n - Internationalization

### 3.1 AppLocale - Поддерживаемые языки

```kotlin
enum class AppLocale(
    val code: String,        // BCP 47 code
    val displayName: String, // English name
    val nativeName: String,  // Native script
    val isRtl: Boolean = false,
) {
    RU("ru", "Russian", "Русский", false),
    HE("he", "Hebrew", "עברית", true),
    EN("en", "English", "English", false);

    companion object {
        val DEFAULT: AppLocale = EN
        val ALL: List<AppLocale> = entries

        fun fromCode(code: String): AppLocale
        fun fromTag(tag: String): AppLocale  // "en-US" -> EN
    }
}
```

### 3.2 I18nProvider Interface

```kotlin
interface I18nProvider {
    val currentLocale: AppLocale

    // Get string by key (returns key if not found)
    operator fun get(key: String): String

    // Get formatted string
    fun get(key: String, vararg args: Any?): String

    // Nullable getter
    fun getOrNull(key: String): String?

    // Change locale at runtime
    fun setLocale(locale: AppLocale)

    // List available locales
    fun availableLocales(): List<AppLocale>
}

// Extension for default values
fun I18nProvider.getOrDefault(key: String, defaultValue: String): String
```

### 3.3 StringKey - Type-safe String Keys

```kotlin
object StringKey {
    // Common (18 keys)
    const val APP_NAME = "app_name"
    const val OK = "ok"
    const val CANCEL = "cancel"
    const val SAVE = "save"
    const val DELETE = "delete"
    // ... 13 more

    // Auth (13 keys)
    object Auth {
        const val LOGIN = "auth_login"
        const val LOGOUT = "auth_logout"
        const val EMAIL = "auth_email"
        const val PASSWORD = "auth_password"
        const val EMAIL_HINT = "auth_email_hint"
        const val PASSWORD_HINT = "auth_password_hint"
        const val FORGOT_PASSWORD = "auth_forgot_password"
        const val NO_ACCOUNT = "auth_no_account"
        const val SIGN_UP = "auth_sign_up"
        const val LOGIN_SUCCESS = "auth_login_success"
        const val LOGIN_ERROR = "auth_login_error"
        const val INVALID_EMAIL = "auth_invalid_email"
        const val INVALID_PASSWORD = "auth_invalid_password"
        const val SESSION_EXPIRED = "auth_session_expired"
    }

    // Navigation (6 keys)
    object Navigation { ... }

    // Catalog (8 keys)
    object Catalog { ... }

    // Booking (11 keys)
    object Booking { ... }

    // Provider (8 keys)
    object Provider { ... }

    // Profile (7 keys)
    object Profile { ... }

    // Error (5 keys)
    object Error { ... }
}
```

**Total keys:** 76+

### 3.4 DefaultStrings - Строковые ресурсы

```kotlin
object DefaultStrings {
    val EN: Map<String, String> = mapOf(
        "app_name" to "Aggregate Service",
        "ok" to "OK",
        "cancel" to "Cancel",
        "auth_login" to "Login",
        "auth_login_error" to "Login failed. Please check your credentials.",
        // ... 100+ more
    )

    val RU: Map<String, String> = mapOf(
        "app_name" to "Aggregate Service",
        "ok" to "ОК",
        "cancel" to "Отмена",
        "auth_login" to "Войти",
        "auth_login_error" to "Ошибка входа. Проверьте данные.",
        // ... 100+ more
    )

    val HE: Map<String, String> = mapOf(
        "app_name" to "Aggregate Service",
        "ok" to "אישור",
        "cancel" to "ביטול",
        "auth_login" to "התחברות",
        "auth_login_error" to "התחברות נכשלה. בדוק את הפרטים.",
        // ... 100+ more
    )
}
```

**Strings per language:** 100+

### 3.5 FlattenI18n - API Response Helper

Бэкенд возвращает локализованные поля в формате:

```json
{
  "name": {
    "_i18n": {
      "en": "Service",
      "ru": "Услуга",
      "he": "שירות"
    }
  }
}
```

**Использование:**

```kotlin
object FlattenI18n {
    // Из JsonObject
    fun extractString(i18nObject: JsonObject, locale: AppLocale): String?
    fun extractString(i18nObject: JsonObject, locale: AppLocale, fallbackLocale: AppLocale): String?

    // Из Map
    fun extractStringFromMap(map: Map<String, Any?>, locale: AppLocale): String?
}

// Extension functions
fun JsonObject.extractI18nString(locale: AppLocale): String?
fun Map<String, Any?>.extractI18nString(locale: AppLocale): String?
```

**LocalizedString data class:**

```kotlin
data class LocalizedString(
    val en: String? = null,
    val ru: String? = null,
    val he: String? = null,
) {
    fun get(locale: AppLocale): String?
    fun get(locale: AppLocale, fallback: AppLocale): String?
    fun getFirstAvailable(): String?

    companion object {
        fun fromI18nMap(map: Map<String, Any?>): LocalizedString
        fun fromString(value: String): LocalizedString
    }
}
```

### 3.6 I18nProviderImpl - Реализация

```kotlin
class I18nProviderImpl(
    initialLocale: AppLocale = AppLocale.DEFAULT,
    private val resources: Map<AppLocale, Map<String, String>> = emptyMap(),
) : I18nProvider {

    private val _localeFlow = MutableStateFlow(initialLocale)
    val localeFlow: StateFlow<AppLocale> = _localeFlow.asStateFlow()

    override var currentLocale: AppLocale by _localeFlow::value

    override fun get(key: String): String = getOrNull(key) ?: key

    override fun get(key: String, vararg args: Any?): String {
        val template = get(key)
        return if (args.isEmpty()) template
        else template.format(*args)
    }

    override fun getOrNull(key: String): String? =
        resources[currentLocale]?.get(key)

    override fun setLocale(locale: AppLocale) {
        _localeFlow.value = locale
    }
}
```

**Builder pattern:**

```kotlin
fun i18nProvider(
    locale: AppLocale = AppLocale.DEFAULT,
    block: I18nBuilder.() -> Unit,
): I18nProvider

class I18nBuilder {
    fun ru(strings: Map<String, String>)
    fun he(strings: Map<String, String>)
    fun en(strings: Map<String, String>)
    fun build(): Map<AppLocale, Map<String, String>>
}
```

### 3.7 Koin DI Module

```kotlin
val i18nModule = module {
    single<I18nProvider> {
        val initialLocale = AppLocale.RU
        createDefaultI18nProvider(initialLocale)
    }
}
```

---

## 4. Интеграция с приложением

### 4.1 MainActivity.kt (Android)

```kotlin
class MainActivity : ComponentActivity() {

    private val i18nProvider: I18nProvider by inject(I18nProvider::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            appTheme(
                languageCode = i18nProvider.currentLocale.code,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(startScreen = LoginScreen())
                }
            }
        }
    }
}
```

### 4.2 MainApplication.kt - Koin Setup

```kotlin
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initializeLogging()
        initializeConfig()
        initializeKoin()
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
            modules(
                // Core modules
                androidCoreModule,
                coreModule,
                i18nModule,  // <-- i18n module
                // Feature modules
                authModule,
            )
        }
    }
}
```

---

## 5. Архитектурные решения

### 5.1 Design Decisions

| Решение | Обоснование |
|---------|-------------|
| **Object singleton** для цветов | compile-time доступ, нет overhead |
| **@Composable функции** для темы | Compose lifecycle integration |
| **Extension properties** | idiomatic Compose access pattern |
| **StateFlow** для locale | reactive locale changes |
| **Map-based strings** | простота и гибкость, нет XML dependency |
| **BCP 47 codes** | стандарт для locale identification |

### 5.2 KMP Compatibility

**Что работает на всех платформах:**
- ✅ `AppColors` - чистые Kotlin значения
- ✅ `Typography` - Compose Multiplatform
- ✅ `Shapes` - Compose Multiplatform
- ✅ `Dimensions` - Dp units (Compose MP)
- ✅ `AppLocale` - чистый enum
- ✅ `I18nProvider` - интерфейс + реализация
- ✅ `FlattenI18n` - kotlinx.serialization

**Платформо-зависимое:**
- `isSystemInDarkTheme()` - работает на Android/iOS
- `LocalLayoutDirection` - работает на Android/iOS

### 5.3 RTL Strategy

1. **Detection:** Автоматическое определение RTL по language code
2. **Layout:** `CompositionLocalProvider` для `LocalLayoutDirection`
3. **Content:** Иврит/арабский текст рендерится корректно
4. **Mirroring:** UI элементы автоматически зеркалируются

---

## 6. Примеры использования

### 6.1 Использование темы

```kotlin
@Composable
fun MyScreen() {
    // Цвета
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    // Кастомные цвета через extension
    val success = MaterialTheme.appColors.Success
    val warning = MaterialTheme.appColors.Warning

    // Spacing
    Column(
        modifier = Modifier.padding(
            horizontal = MaterialTheme.spacing.MD,
            vertical = MaterialTheme.spacing.SM
        )
    ) {
        // Content
    }

    // Dimensions
    Icon(
        painter = painterResource(R.drawable.ic_star),
        modifier = Modifier.size(MaterialTheme.dimensions.IconMD)
    )
}
```

### 6.2 Использование i18n

```kotlin
@Composable
fun LoginScreen(
    i18nProvider: I18nProvider = koinInject(),
) {
    val locale by i18nProvider.localeFlow.collectAsState()

    Column {
        // Type-safe string access
        Text(text = i18nProvider[StringKey.Auth.LOGIN])

        // With formatting
        Text(text = i18nProvider.get("welcome_message", userName))

        // With default
        Text(text = i18nProvider.getOrDefault("optional_key", "Default"))

        // Change language
        Button(onClick = { i18nProvider.setLocale(AppLocale.HE) }) {
            Text("עברית")
        }
    }
}
```

### 6.3 Обработка API i18n

```kotlin
// DTO from API
@Serializable
data class ServiceDto(
    val id: String,
    val name: Map<String, Any?>,  // Contains _i18n
)

// Domain model
data class Service(
    val id: String,
    val name: String,  // Extracted for current locale
)

// Mapping
fun ServiceDto.toDomain(locale: AppLocale): Service = Service(
    id = id,
    name = name.extractI18nString(locale) ?: "Unknown",
)
```

### 6.4 Conditional RTL Layout

```kotlin
@Composable
fun BidirectionalLayout(
    i18nProvider: I18nProvider,
) {
    Row(
        // Layout автоматически зеркалируется для RTL
        horizontalArrangement = if (i18nProvider.currentLocale.isRtl) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        // Для RTL: [Text] [Icon]
        // Для LTR: [Icon] [Text]
        if (i18nProvider.currentLocale.isRtl) {
            Text(i18nProvider[StringKey.Auth.LOGIN])
            Icon(Icons.Default.ArrowForward, null)
        } else {
            Icon(Icons.Default.ArrowForward, null)
            Text(i18nProvider[StringKey.Auth.LOGIN])
        }
    }
}
```

---

## 📊 Итоговая статистика

| Метрика | core:theme | core:i18n |
|---------|------------|-----------|
| **Файлов** | 5 | 6 |
| **Lines of Code** | ~350 | ~450 |
| **Цветов** | 77 | - |
| **Typography styles** | 15 | - |
| **Dimension tokens** | 40+ | - |
| **Языков** | - | 3 |
| **String keys** | - | 76+ |
| **Strings total** | - | 300+ |

---

**Документация актуальна на:** 2026-03-21
**Соответствует версии:** Phase 1 Complete
