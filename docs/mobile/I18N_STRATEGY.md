# Стратегия Интернационализации (i18n) - Mobile

## 1. Целевые рынки и языки

### 1.1. Поддерживаемые языки

| Рынок | Страна | Язык | Код | RTL | Приоритет |
|-------|--------|------|-----|-----|-----------|
| **Россия** | РФ | Русский | `ru` | Нет | P0 (MVP) |
| **Израиль** | IL | Иврит | `he` | **Да** | P0 (MVP) |
| **США/Глобал** | US | Английский | `en` | Нет | P1 |

### 1.2. Реализация в KMP

```kotlin
// core:i18n/Locale.kt
enum class AppLocale(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean = false,
) {
    RU(code = "ru", displayName = "Russian", nativeName = "Русский", isRtl = false),
    HE(code = "he", displayName = "Hebrew", nativeName = "עברית", isRtl = true),
    EN(code = "en", displayName = "English", nativeName = "English", isRtl = false);

    companion object {
        val DEFAULT: AppLocale = EN
        val ALL: List<AppLocale> = entries

        fun fromCode(code: String): AppLocale =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
    }
}
```

---

## 2. RTL поддержка (Right-to-Left)

### 2.1. Compose Multiplatform RTL

```kotlin
// core:theme/Theme.kt
@Composable
fun appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    languageCode: String,
    content: @Composable () -> Unit,
) {
    val layoutDirection = when (languageCode) {
        "he", "ar", "fa", "ur" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme else lightColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
        ) {
            content()
        }
    }
}
```

### 2.2. Исключения для RTL

Некоторые элементы остаются LTR даже в RTL контексте:

```kotlin
// Логотипы, иконки с текстом, номера телефонов
CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Ltr
) {
    Image(painterResource("logo.png"), "Logo")
}
```

### 2.3. Чеклист RTL тестирования

- [ ] Текст выровнен по правому краю
- [ ] Кнопки "Назад" автоматически зеркалируются
- [ ] Поля ввода правильно выровнены
- [ ] Списки (LazyColumn) отображаются корректно
- [ ] Иконки с направлением (стрелки, back) зеркалируются

---

## 3. Форматы данных

### 3.1. Даты

| Язык | Формат (короткий) | Формат (длинный) | Пример |
|------|------------------|------------------|--------|
| Русский | `dd.MM.yyyy` | `d MMMM yyyy` | 14.09.2026 / 14 сентября 2026 |
| Иврит | `dd.MM.yyyy` | `d MMMM yyyy` | 14.09.2026 / 14 בספטמבר 2026 |
| Английский | `MM/dd/yyyy` | `MMM d, yyyy` | 09/14/2026 / Sep 14, 2026 |

**API формат:** RFC 3339 (`2026-09-14T14:30:00Z`) - не зависит от языка.

### 3.2. Валюты

| Язык | Символ | Позиция | Разделители | Пример |
|------|--------|---------|-------------|--------|
| Русский | ₽ | После | Пробел (1 000) | 1 500 ₽ |
| Иврит | ₪ | После | Запятая (1,000) | 1,000 ₪ |
| Английский | $ | До | Запятая (1,000) | $1,000.00 |

### 3.3. Числа

| Язык | Разделитель тысяч | Десятичный | Пример |
|------|-------------------|------------|--------|
| Русский | Пробел | Запятая | 1 234,56 |
| Иврит | Запятая | Точка | 1,234.56 |
| Английский | Запятая | Точка | 1,234.56 |

---

## 4. Стратегия переводов

### 4.1. Что переводится

**Обязательно (Must-Have):**
- UI тексты (кнопки, меню, заголовки)
- Категории услуг (Ногти, Волосы)
- Сообщения об ошибках
- Onboarding экраны

**Желательно (Nice-to-Have):**
- Описания услуг (клиент может создать на любом языке)
- Отзывы (показывать в оригинале с меткой языка)

**НЕ переводится:**
- Имена мастеров
- Названия бизнеса (бренды)
- Адреса (использовать Google Maps auto-translation)

### 4.2. Text Expansion

Перевод может быть длиннее оригинала:

| Пример | EN | HE | RU |
|--------|-----|----|-----|
| "Book" | 4 chars | 6 chars (הזמן) | 14 chars (Записаться) |

**Решение в Compose:**

```kotlin
// Использовать Modifier.weight() или intrinsicSize
Text(
    text = i18nProvider[StringKey.Booking.CONFIRM],
    modifier = Modifier.width(IntrinsicSize.Min)
)
```

---

## 5. Выбор языка пользователем

### 5.1. Автоопределение

```kotlin
// При первом запуске
fun detectLocale(): AppLocale {
    val systemLocale = Locale.current.toLanguageTag()
    return AppLocale.fromTag(systemLocale)
}
```

### 5.2. Onboarding флоу

1. Установка приложения
2. Первый экран: "Выберите язык / בחר שפה / Select Language"
3. Сохранение в DataStore
4. Возможность изменить в Settings

---

## 6. Реализация StringKey

### 6.1. Структура ключей

```kotlin
// core:i18n/I18nProvider.kt
object StringKey {
    // Common
    const val APP_NAME = "app_name"
    const val OK = "ok"
    const val CANCEL = "cancel"
    const val LOADING = "loading"
    const val ERROR = "error"

    // Auth
    object Auth {
        const val LOGIN = "auth_login"
        const val LOGOUT = "auth_logout"
        const val EMAIL = "auth_email"
        const val PASSWORD = "auth_password"
        // ...
    }

    // Navigation
    object Navigation {
        const val HOME = "nav_home"
        const val CATALOG = "nav_catalog"
        const val BOOKING = "nav_booking"
        const val PROFILE = "nav_profile"
    }

    // Catalog
    object Catalog {
        const val TITLE = "catalog_title"
        const val SEARCH_HINT = "catalog_search_hint"
        const val NO_RESULTS = "catalog_no_results"
    }

    // Booking
    object Booking {
        const val TITLE = "booking_title"
        const val SELECT_DATE = "booking_select_date"
        const val SELECT_TIME = "booking_select_time"
        const val CONFIRM = "booking_confirm"
    }

    // Error messages
    object Error {
        const val NETWORK = "error_network"
        const val SERVER = "error_server"
        const val UNKNOWN = "error_unknown"
    }
}
```

### 6.2. DefaultStrings

```kotlin
// core:i18n/Strings.kt
public object DefaultStrings {
    public val RU: Map<String, String> = mapOf(
        "app_name" to "Aggregate Service",
        "ok" to "ОК",
        "cancel" to "Отмена",
        "loading" to "Загрузка...",
        "error" to "Ошибка",
        // Auth
        "auth_login" to "Войти",
        "auth_logout" to "Выйти",
        // ...
    )

    public val HE: Map<String, String> = mapOf(
        "app_name" to "Aggregate Service",
        "ok" to "אישור",
        "cancel" to "ביטול",
        "loading" to "טוען...",
        "error" to "שגיאה",
        // Auth
        "auth_login" to "התחברות",
        "auth_logout" to "התנתקות",
        // ...
    )

    public val EN: Map<String, String> = mapOf(
        "app_name" to "Aggregate Service",
        "ok" to "OK",
        "cancel" to "Cancel",
        "loading" to "Loading...",
        "error" to "Error",
        // ...
    )
}
```

---

## 7. Тестирование локализации

### 7.1. Unit тесты

```kotlin
class I18nProviderTest {
    @Test
    fun `should return Hebrew string for HE locale`() {
        val provider = createDefaultI18nProvider(AppLocale.HE)
        assertEquals("התחברות", provider[StringKey.Auth.LOGIN])
    }

    @Test
    fun `should return Russian string for RU locale`() {
        val provider = createDefaultI18nProvider(AppLocale.RU)
        assertEquals("Войти", provider[StringKey.Auth.LOGIN])
    }
}
```

### 7.2. UI тесты

```kotlin
@Test
fun `login screen displays Hebrew text`() = runComposeUiTest {
    setContent {
        val provider = createDefaultI18nProvider(AppLocale.HE)
        LoginScreen(i18nProvider = provider)
    }

    onNodeWithText("התחברות").assertIsDisplayed()
}
```

---

## 8. Rollout план

### Week 1: Foundation
- [x] `AppLocale` enum (RU, HE, EN с isRtl)
- [x] `I18nProvider` интерфейс
- [x] `StringKey` с базовыми категориями
- [x] `DefaultStrings` для 3 языков

### Week 2: Integration
- [ ] Расширить StringKey (Onboarding, Map, Scheduling)
- [ ] Добавить format utils (даты, валюты)
- [ ] RTL тестирование

### Week 3: Polish
- [ ] Носитель иврита review переводов
- [ ] Edge cases тестирование
- [ ] Documentation

---

## 9. Связанные документы

- [Backend i18n Strategy](../business/05_I18N_STRATEGY.md) - Оригинальная стратегия
- [core:i18n Implementation](../implementation/CORE_THEME_AND_I18N_IMPLEMENTATION.md) - Детали реализации
- [UX Guidelines](../05_UX_GUIDELINES.md) - UX best practices

---

**Last Updated:** 2026-03-21
