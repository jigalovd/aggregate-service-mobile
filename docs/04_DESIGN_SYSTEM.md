# Design System - Aggregate Service

## Обзор

Базовая Design System для мобильного приложения на базе Material 3 и Compose Multiplatform.

---

## 1. Цветовая палитра

### 1.1. Primary Colors

| Название | Hex | Использование |
|----------|-----|---------------|
| **Primary** | `#6750A4` | Основные кнопки, FAB, активные элементы |
| **Primary Container** | `#EADDFF` | Фон карточек, чипсов |
| **On Primary** | `#FFFFFF` | Текст на primary цвете |
| **On Primary Container** | `#21005D` | Текст на primary container |

### 1.2. Secondary Colors

| Название | Hex | Использование |
|----------|-----|---------------|
| **Secondary** | `#625B71` | Иконки,次要 элементы |
| **Secondary Container** | `#E8DEF8` | Фон для вторичных элементов |
| **On Secondary** | `#FFFFFF` | Текст на secondary |
| **On Secondary Container** | `#1D192B` | Текст на secondary container |

### 1.3. Semantic Colors

| Название | Hex | Использование |
|----------|-----|---------------|
| **Success** | `#4CAF50` | Подтверждение, успешные действия |
| **Warning** | `#FF9800` | Предупреждения |
| **Error** | `#B3261E` | Ошибки, валидация |
| **Info** | `#2196F3` | Информационные сообщения |

### 1.4. Neutral Colors

| Название | Hex | Использование |
|----------|-----|---------------|
| **Surface** | `#FFFBFE` | Основной фон |
| **Surface Variant** | `#E7E0EC` | Фон карточек |
| **Background** | `#FFFBFE` | Фон экранов |
| **Outline** | `#79747E` | Borders, dividers |
| **Outline Variant** | `#CAC4D0` | Secondary borders |

### 1.5. Kotlin Implementation

```kotlin
// core/theme/Color.kt

import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary
    val Primary = Color(0xFF6750A4)
    val PrimaryContainer = Color(0xFFEADDFF)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF21005D)

    // Secondary
    val Secondary = Color(0xFF625B71)
    val SecondaryContainer = Color(0xFFE8DEF8)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF1D192B)

    // Semantic
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFB3261E)
    val Info = Color(0xFF2196F3)

    // Neutral
    val Surface = Color(0xFFFFFBFE)
    val SurfaceVariant = Color(0xFFE7E0EC)
    val Background = Color(0xFFFFFBFE)
    val Outline = Color(0xFF79747E)
    val OutlineVariant = Color(0xFFCAC4D0)

    // Text
    val OnSurface = Color(0xFF1C1B1F)
    val OnSurfaceVariant = Color(0xFF49454F)
}
```

---

## 2. Типографика

### 2.1. Font Families

| Стиль | Размер | Вес | Line Height | Использование |
|-------|--------|-----|-------------|---------------|
| **Display Large** | 57sp | Regular | 64dp | Заголовки экранов |
| **Display Medium** | 45sp | Regular | 52dp | Большие заголовки |
| **Headline Large** | 32sp | Regular | 40dp | Заголовки секций |
| **Headline Medium** | 28sp | Regular | 36dp | Заголовки карточек |
| **Title Large** | 22sp | Medium | 28dp | Заголовки списков |
| **Title Medium** | 16sp | Medium | 24dp | Названия элементов |
| **Title Small** | 14sp | Medium | 20dp | Подзаголовки |
| **Body Large** | 16sp | Regular | 24dp | Основной текст |
| **Body Medium** | 14sp | Regular | 20dp | Вторичный текст |
| **Body Small** | 12sp | Regular | 16dp | Captions |
| **Label Large** | 14sp | Medium | 20dp | Кнопки |
| **Label Medium** | 12sp | Medium | 16dp | Чипсы, теги |
| **Label Small** | 11sp | Medium | 16dp | Маленькие лейблы |

### 2.2. Kotlin Implementation

```kotlin
// core/theme/Type.kt

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
```

---

## 3. Spacing

### 3.1. Grid System

| Token | Value | Использование |
|-------|-------|---------------|
| **None** | 0dp | - |
| **XXS** | 2dp | Минимальные отступы |
| **XS** | 4dp | Иконки, badges |
| **SM** | 8dp | Внутренние отступы |
| **MD** | 16dp | Стандартные отступы |
| **LG** | 24dp | Между секциями |
| **XL** | 32dp | Между экранами |
| **XXL** | 48dp | Большие блоки |

### 3.2. Kotlin Implementation

```kotlin
// core/theme/Dimensions.kt

import androidx.compose.ui.unit.dp

object Spacing {
    val None = 0.dp
    val XXS = 2.dp
    val XS = 4.dp
    val SM = 8.dp
    val MD = 16.dp
    val LG = 24.dp
    val XL = 32.dp
    val XXL = 48.dp
}
```

---

## 4. Компоненты

### 4.1. Buttons

#### Primary Button

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = AppColors.OnPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppColors.OnPrimary
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
```

#### Secondary Button

```kotlin
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.Primary
        ),
        border = BorderStroke(1.dp, AppColors.Outline)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
```

### 4.2. Text Fields

```kotlin
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Outline,
                errorBorderColor = AppColors.Error
            )
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = AppColors.Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = Spacing.XS)
            )
        }
    }
}
```

### 4.3. Cards

```kotlin
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            colors = CardDefaults.elevatedCardColors(
                containerColor = AppColors.SurfaceVariant
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.MD),
                content = content
            )
        }
    } else {
        ElevatedCard(
            modifier = modifier,
            colors = CardDefaults.elevatedCardColors(
                containerColor = AppColors.SurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.MD),
                content = content
            )
        }
    }
}
```

### 4.4. Chips

```kotlin
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.PrimaryContainer,
            selectedLabelColor = AppColors.OnPrimaryContainer
        )
    )
}
```

---

## 5. Иконки

### 5.1. Navigation

| Экран | Иконка | Описание |
|-------|--------|----------|
| Home | `home` | Главная |
| Search | `search` | Поиск |
| Bookings | `calendar_month` | Записи |
| Favorites | `favorite` | Избранное |
| Profile | `person` | Профиль |

### 5.2. Actions

| Действие | Иконка | Описание |
|----------|--------|----------|
| Add | `add` | Добавить |
| Edit | `edit` | Редактировать |
| Delete | `delete` | Удалить |
| Share | `share` | Поделиться |
| Call | `phone` | Позвонить |
| Message | `message` | Написать |
| Location | `location_on` | Локация |
| Filter | `filter_list` | Фильтр |
| Sort | `sort` | Сортировка |

### 5.3. Использование

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person

Icon(
    imageVector = Icons.Default.Home,
    contentDescription = "Home",
    tint = AppColors.Primary
)
```

---

## 6. Shadows & Elevation

| Level | Elevation | Использование |
|-------|-----------|---------------|
| **Level 0** | 0dp | Flat surfaces |
| **Level 1** | 1dp | Cards (resting) |
| **Level 2** | 3dp | Cards (hover) |
| **Level 3** | 6dp | Modals, menus |
| **Level 4** | 8dp | Navigation drawer |
| **Level 5** | 12dp | Dialogs |

---

## 7. RTL Support

### 7.1. Layout Direction

```kotlin
@Composable
fun AppTheme(
    languageCode: String = "ru",
    content: @Composable () -> Unit
) {
    val layoutDirection = when (languageCode) {
        "he" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = AppColors.Primary,
                // ...
            ),
            typography = AppTypography,
            content = content
        )
    }
}
```

### 7.2. Text Alignment

```kotlin
Text(
    text = "שלום", // Hebrew
    textAlign = TextAlign.Start, // Auto-rtl for Hebrew
    modifier = Modifier.fillMaxWidth()
)
```

---

## 8. Dark Mode (Future)

### 8.1. Dark Colors

| Название | Light | Dark |
|----------|-------|------|
| **Primary** | `#6750A4` | `#D0BCFF` |
| **Surface** | `#FFFBFE` | `#1C1B1F` |
| **Background** | `#FFFBFE` | `#1C1B1F` |

### 8.2. Implementation

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = AppColors.PrimaryDark,
            // ...
        )
    } else {
        lightColorScheme(
            primary = AppColors.Primary,
            // ...
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

---

## 9. Accessibility

### 9.1. Minimum Touch Target

```kotlin
// Минимум 48dp для clickable элементов
Modifier.minimumInteractiveComponentSize()
```

### 9.2. Content Descriptions

```kotlin
Icon(
    imageVector = Icons.Default.Home,
    contentDescription = "Navigate to home screen" // Обязательно!
)
```

### 9.3. Text Contrast

- Минимальный contrast ratio: 4.5:1
- Для крупного текста: 3:1

---

## 10. Animation

### 10.1. Durations

| Token | Duration | Использование |
|-------|----------|---------------|
| **Fast** | 100ms | Hover, press |
| **Normal** | 200ms | Transitions |
| **Slow** | 400ms | Complex animations |
| **Very Slow** | 800ms | Large transitions |

### 10.2. Easing

```kotlin
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween

animateColorAsState(
    targetValue = if (selected) AppColors.Primary else AppColors.Surface,
    animationSpec = tween(
        durationMillis = 200,
        easing = EaseInOut
    )
)
```

---

## 11. Компоненты экранов

### 11.1. Bottom Navigation

```kotlin
@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Primary,
                    selectedTextColor = AppColors.Primary,
                    indicatorColor = AppColors.PrimaryContainer
                )
            )
        }
    }
}
```

### 11.2. Top App Bar

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.Surface,
            titleContentColor = AppColors.OnSurface
        )
    )
}
```

---

## 12. Figma Tokens (для дизайнеров)

```
# Colors
primary: #6750A4
primary-container: #EADDFF
secondary: #625B71
error: #B3261E
success: #4CAF50
surface: #FFFBFE

# Typography
font-family: Roboto
display-large: 57sp / Regular
headline-large: 32sp / Regular
title-large: 22sp / Medium
body-large: 16sp / Regular
label-large: 14sp / Medium

# Spacing
xs: 4dp
sm: 8dp
md: 16dp
lg: 24dp
xl: 32dp

# Border Radius
sm: 4dp
md: 8dp
lg: 16dp
full: 9999dp
```

---

**Last Updated**: 2026-03-15
