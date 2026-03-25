# UI Components Plan - Aggregate Service Mobile

**Дата создания**: 2026-03-24
**Версия**: 1.0
**Приоритет**: HIGH
**Зависимости**: core:theme (AppColors, Spacing, Dimensions, Typography)

---

## 📊 Executive Summary

### Текущее состояние

| Категория | Файлы | Компоненты | Статус |
|-----------|-------|------------|--------|
| **Theme** | 5 | AppColors, Spacing, Dimensions, Typography, Theme | ✅ Complete |
| **Foundation Components** | 0 | - | ❌ Missing |
| **Navigation Components** | 0 | - | ❌ Missing |
| **Feedback Components** | 0 | - | ❌ Missing |
| **Feature Components** | 0 | - | ❌ Missing |

### Цель

Создать переиспользуемую библиотеку UI компонентов, следующих:
- **Material 3 Design System**
- **Project UX Guidelines** (Short Flows, Progressive Disclosure)
- **Industry Best Practices** (Atomic Design, Stateless Components)

---

## 🏗️ Architecture

### Atomic Design Hierarchy

```
├── Atoms (Базовые)
│   ├── Buttons (AppButton, IconButton)
│   ├── Inputs (AppTextField, AppCheckbox)
│   ├── Typography (Headline, Body, Label)
│   └── Icons (AppIcon)
│
├── Molecules (Комбинированные)
│   ├── Cards (ProviderCard, ServiceCard)
│   ├── List Items (BookingItem, ReviewItem)
│   ├── Chips (FilterChip, CategoryChip)
│   └── Inputs with labels (FormField, SearchField)
│
├── Organisms (Сложные)
│   ├── Navigation (TopAppBar, BottomNav)
│   ├── Forms (LoginForm, RegistrationForm)
│   ├── Lists (ProviderList, BookingList)
│   └── Modals (BottomSheet, Dialog)
│
└── Templates (Экраны)
    ├── AuthScreen
    ├── CatalogScreen
    ├── BookingScreen
    └── ProfileScreen
```

### File Structure

```
core/ui/
├── src/commonMain/kotlin/com/aggregateservice/core/ui/
│   ├── foundation/                    # Atoms
│   │   ├── button/
│   │   │   ├── AppButton.kt           # Primary, Secondary, Text buttons
│   │   │   ├── AppIconButton.kt       # Icon button variants
│   │   │   └── ButtonPreview.kt       # @Preview functions
│   │   ├── input/
│   │   │   ├── AppTextField.kt        # Text input with validation
│   │   │   ├── AppPasswordField.kt    # Password with visibility toggle
│   │   │   ├── AppSearchField.kt      # Search input with icon
│   │   │   └── InputPreview.kt
│   │   ├── selection/
│   │   │   ├── AppCheckbox.kt         # Checkbox with label
│   │   │   ├── AppRadioButton.kt      # Radio button group
│   │   │   ├── AppSwitch.kt           # Toggle switch
│   │   │   └── SelectionPreview.kt
│   │   └── text/
│   │       ├── AppText.kt             # Styled text components
│   │       └── TextPreview.kt
│   │
│   ├── molecules/                     # Molecules
│   │   ├── card/
│   │   │   ├── AppCard.kt             # Base card component
│   │   │   ├── ProviderCard.kt        # Provider summary card
│   │   │   ├── ServiceCard.kt         # Service summary card
│   │   │   └── CardPreview.kt
│   │   ├── chip/
│   │   │   ├── AppFilterChip.kt       # Filter chip
│   │   │   ├── CategoryChip.kt        # Category selection chip
│   │   │   ├── TagChip.kt             # Info tag chip
│   │   │   └── ChipPreview.kt
│   │   ├── form/
│   │   │   ├── FormField.kt           # Label + Input + Error
│   │   │   ├── PhoneField.kt          # Phone input with formatting
│   │   │   └── FormPreview.kt
│   │   ├── list/
│   │   │   ├── ListItem.kt            # Base list item
│   │   │   ├── BookingListItem.kt     # Booking list item
│   │   │   ├── ReviewListItem.kt      # Review list item
│   │   │   └── ListPreview.kt
│   │   └── rating/
│   │       ├── RatingStars.kt         # Interactive rating
│   │       ├── RatingDisplay.kt       # Read-only rating display
│   │       └── RatingPreview.kt
│   │
│   ├── organisms/                     # Organisms
│   │   ├── navigation/
│   │   │   ├── AppTopAppBar.kt        # Top app bar with actions
│   │   │   ├── AppBottomNavBar.kt     # Bottom navigation bar
│   │   │   ├── AppTabBar.kt           # Tab bar for sub-navigation
│   │   │   └── NavigationPreview.kt
│   │   ├── feedback/
│   │   │   ├── AppDialog.kt           # Alert/confirmation dialog
│   │   │   ├── AppBottomSheet.kt      # Bottom sheet container
│   │   │   ├── AppSnackbar.kt         # Toast/snackbar
│   │   │   ├── LoadingOverlay.kt      # Full-screen loading
│   │   │   └── FeedbackPreview.kt
│   │   ├── empty/
│   │   │   ├── EmptyState.kt          # Generic empty state
│   │   │   ├── ErrorState.kt          # Error with retry
│   │   │   └── EmptyPreview.kt
│   │   └── calendar/
│   │       ├── CalendarPicker.kt      # Date picker calendar
│   │       ├── TimeSlotPicker.kt      # Time slot selection
│   │       └── CalendarPreview.kt
│   │
│   └── theme/                         # Extensions
│       ├── ThemeExtensions.kt         # MaterialTheme extensions
│       └── PreviewAnnotations.kt      # Preview annotations
│
└── build.gradle.kts
```

---

## 📋 Phase 1: Foundation Components (Atoms)

### 1.1 AppButton

**Файл:** `foundation/button/AppButton.kt`

**API Design:**

```kotlin
/**
 * Primary button component following Material 3 design.
 *
 * **Variants:**
 * - Primary: Filled button for main actions
 * - Secondary: Outlined button for secondary actions
 * - Text: Text-only button for tertiary actions
 * - Danger: Destructive actions (delete, cancel)
 *
 * **States:**
 * - Enabled, Disabled, Loading
 *
 * **Sizes:**
 * - Small (32dp), Medium (40dp), Large (48dp)
 *
 * **Usage:**
 * ```kotlin
 * AppButton(
 *     text = "Войти",
 *     onClick = { /* ... */ },
 *     variant = ButtonVariant.Primary,
 *     size = ButtonSize.Large,
 *     isLoading = false,
 *     enabled = true,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
)

enum class ButtonVariant {
    Primary,    // Filled with primary color
    Secondary,  // Outlined
    Tertiary,   // Text only
    Danger,     // Filled with error color
}

enum class ButtonSize {
    Small,   // 32dp height
    Medium,  // 40dp height
    Large,   // 48dp height
}
```

**Test Cases:**

| # | Test | Expected |
|---|------|----------|
| 1 | Renders with text | Text visible |
| 2 | onClick called when enabled | Callback invoked |
| 3 | onClick NOT called when disabled | No callback |
| 4 | Loading state shows spinner | Spinner visible |
| 5 | Leading icon rendered | Icon visible |
| 6 | All variants render correctly | Visual match |

---

### 1.2 AppTextField

**Файл:** `foundation/input/AppTextField.kt`

**API Design:**

```kotlin
/**
 * Text input component with validation support.
 *
 * **Features:**
 * - Label and placeholder
 * - Error message display
 * - Leading/trailing icons
 * - Single/multi-line support
 * - Character counter
 * - Input masking support
 *
 * **Usage:**
 * ```kotlin
 * AppTextField(
 *     value = email,
 *     onValueChange = { email = it },
 *     label = "Email",
 *     placeholder = "example@email.com",
 *     leadingIcon = { AppIcon(Icons.Default.Email) },
 *     isError = emailError != null,
 *     errorMessage = emailError,
 *     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
)
```

---

### 1.3 AppPasswordField

**Файл:** `foundation/input/AppPasswordField.kt`

**API Design:**

```kotlin
/**
 * Password input with visibility toggle.
 *
 * **Features:**
 * - All AppTextField features
 * - Automatic visibility toggle icon
 * - Password strength indicator (optional)
 *
 * **Usage:**
 * ```kotlin
 * AppPasswordField(
 *     value = password,
 *     onValueChange = { password = it },
 *     label = "Пароль",
 *     isError = passwordError != null,
 *     errorMessage = passwordError,
 *     showStrengthIndicator = true,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun AppPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    showStrengthIndicator: Boolean = false,
    showVisibilityToggle: Boolean = true,
)
```

---

### 1.4 AppSearchField

**Файл:** `foundation/input/AppSearchField.kt`

**API Design:**

```kotlin
/**
 * Search input with debounced input support.
 *
 * **Features:**
 * - Search icon (leading)
 * - Clear button (trailing, when text exists)
 * - Debounced onValueChange for API calls
 * - Loading state
 *
 * **Usage:**
 * ```kotlin
 * var searchQuery by remember { mutableStateOf("") }
 *
 * AppSearchField(
 *     value = searchQuery,
 *     onValueChange = { searchQuery = it },
 *     placeholder = "Поиск мастеров...",
 *     onSearch = { query -> viewModel.search(query) },
 *     debounceMs = 300,
 *     isLoading = isSearching,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Поиск...",
    onSearch: (String) -> Unit = {},
    onClear: () -> Unit = {},
    debounceMs: Long = 300L,
    isLoading: Boolean = false,
    enabled: Boolean = true,
)
```

---

### 1.5 AppCheckbox

**Файл:** `foundation/selection/AppCheckbox.kt`

**API Design:**

```kotlin
/**
 * Checkbox with label support.
 *
 * **Usage:**
 * ```kotlin
 * AppCheckbox(
 *     checked = isChecked,
 *     onCheckedChange = { isChecked = it },
 *     label = "Запомнить меня",
 *     modifier = Modifier.padding(vertical = Spacing.SM)
 * )
 * ```
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
)
```

---

## 📋 Phase 2: Molecule Components

### 2.1 FormField

**Файл:** `molecules/form/FormField.kt`

**API Design:**

```kotlin
/**
 * Form field wrapper with label and error display.
 *
 * **Usage:**
 * ```kotlin
 * FormField(
 *     label = "Email",
 *     error = emailError,
 *     required = true,
 *     modifier = Modifier.fillMaxWidth()
 * ) {
 *     AppTextField(
 *         value = email,
 *         onValueChange = { email = it },
 *         isError = emailError != null
 *     )
 * }
 * ```
 */
@Composable
fun FormField(
    modifier: Modifier = Modifier,
    label: String? = null,
    error: String? = null,
    required: Boolean = false,
    helperText: String? = null,
    content: @Composable () -> Unit,
)
```

---

### 2.2 ProviderCard

**Файл:** `molecules/card/ProviderCard.kt`

**API Design:**

```kotlin
/**
 * Provider summary card for list/grid display.
 *
 * **Information displayed:**
 * - Business name
 * - Rating with review count
 * - Category tags
 * - Distance (if location available)
 * - Price range
 * - Next available slot (optional)
 *
 * **Actions:**
 * - Tap → Navigate to detail
 * - Long press → Quick actions (favorite, share)
 *
 * **Usage:**
 * ```kotlin
 * ProviderCard(
 *     provider = provider,
 *     onClick = { navController.navigate("provider/${provider.id}") },
 *     showDistance = true,
 *     showNextSlot = true,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun ProviderCard(
    provider: ProviderSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDistance: Boolean = true,
    showNextSlot: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
)

data class ProviderSummary(
    val id: String,
    val businessName: String,
    val rating: Double,
    val reviewCount: Int,
    val categories: List<String>,
    val distance: Double?, // in meters
    val priceRange: PriceRange,
    val nextAvailableSlot: String?,
    val thumbnailUrl: String?,
)
```

---

### 2.3 AppFilterChip

**Файл:** `molecules/chip/AppFilterChip.kt`

**API Design:**

```kotlin
/**
 * Filter chip for category/option selection.
 *
 * **Usage:**
 * ```kotlin
 * AppFilterChip(
 *     text = "Маникюр",
 *     selected = isSelected,
 *     onClick = { isSelected = !isSelected },
 *     leadingIcon = if (isSelected) Icons.Default.Check else null,
 *     modifier = Modifier.padding(end = Spacing.SM)
 * )
 * ```
 */
@Composable
fun AppFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
)
```

---

### 2.4 RatingStars

**Файл:** `molecules/rating/RatingStars.kt`

**API Design:**

```kotlin
/**
 * Interactive rating stars component.
 *
 * **Usage:**
 * ```kotlin
 * // Interactive (for review form)
 * RatingStars(
 *     rating = currentRating,
 *     onRatingChange = { currentRating = it },
 *     maxRating = 5,
 *     starSize = 32.dp,
 *     modifier = Modifier.padding(vertical = Spacing.MD)
 * )
 *
 * // Display only (for reviews)
 * RatingStars(
 *     rating = review.rating,
 *     maxRating = 5,
 *     interactive = false,
 *     showRatingValue = true,
 *     modifier = Modifier
 * )
 * ```
 */
@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier,
    onRatingChange: ((Int) -> Unit)? = null,
    maxRating: Int = 5,
    starSize: Dp = 24.dp,
    interactive: Boolean = onRatingChange != null,
    showRatingValue: Boolean = false,
)
```

---

## 📋 Phase 3: Organism Components

### 3.1 AppTopAppBar

**Файл:** `organisms/navigation/AppTopAppBar.kt`

**API Design:**

```kotlin
/**
 * Top app bar with back navigation and actions.
 *
 * **Variants:**
 * - Simple: Title only
 * - With back: Title + back button
 * - With actions: Title + action buttons
 * - With search: Search field mode
 *
 * **Usage:**
 * ```kotlin
 * AppTopAppBar(
 *     title = "Каталог",
 *     onNavigateBack = { navController.popBackStack() },
 *     actions = {
 *         IconButton(onClick = { /* filter */ }) {
 *             Icon(Icons.Default.FilterList, "Фильтр")
 *         }
 *     },
 *     scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
)
```

---

### 3.2 AppBottomNavBar

**Файл:** `organisms/navigation/AppBottomNavBar.kt`

**API Design:**

```kotlin
/**
 * Bottom navigation bar with 4-5 destinations.
 *
 * **Features:**
 * - Icons with labels
 * - Badge support for notifications
 * - Selected state highlighting
 *
 * **Usage:**
 * ```kotlin
 * val navItems = listOf(
 *     NavItem(Icons.Default.Search, "Поиск", "search"),
 *     NavItem(Icons.Default.CalendarMonth, "Записи", "bookings", badge = 3),
 *     NavItem(Icons.Default.Favorite, "Избранное", "favorites"),
 *     NavItem(Icons.Default.Person, "Профиль", "profile"),
 * )
 *
 * AppBottomNavBar(
 *     items = navItems,
 *     currentRoute = currentRoute,
 *     onNavigate = { route -> navController.navigate(route) },
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun AppBottomNavBar(
    items: List<NavItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
)

data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
    val badge: Int? = null,
)
```

---

### 3.3 AppBottomSheet

**Файл:** `organisms/feedback/AppBottomSheet.kt`

**API Design:**

```kotlin
/**
 * Bottom sheet container for modal content.
 *
 * **Features:**
 * - Drag handle for dismissal
 * - Three states: Collapsed, Partial, Expanded
 * - Swipe to dismiss
 * - Content preserved during collapse
 *
 * **Usage:**
 * ```kotlin
 * val sheetState = rememberAppBottomSheetState()
 *
 * AppBottomSheet(
 *     sheetState = sheetState,
 *     onDismiss = { sheetState.hide() },
 *     title = "Выберите дату",
 *     showDragHandle = true
 * ) {
 *     // Sheet content
 *     CalendarPicker(...)
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    showDragHandle: Boolean = true,
    skipPartiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
)
```

---

### 3.4 AppDialog

**Файл:** `organisms/feedback/AppDialog.kt`

**API Design:**

```kotlin
/**
 * Modal dialog for alerts and confirmations.
 *
 * **Variants:**
 * - Alert: Simple message with single action
 * - Confirmation: Message with confirm/cancel
 * - Form: Dialog with input fields
 *
 * **Usage:**
 * ```kotlin
 * // Alert dialog
 * AppDialog(
 *     showDialog = showError,
 *     onDismiss = { showError = false },
 *     title = "Ошибка",
 *     message = "Не удалось загрузить данные",
 *     confirmText = "OK"
 * )
 *
 * // Confirmation dialog
 * AppDialog(
 *     showDialog = showConfirm,
 *     onDismiss = { showConfirm = false },
 *     title = "Отменить запись?",
 *     message = "Это действие нельзя отменить",
 *     confirmText = "Отменить",
 *     cancelText = "Оставить",
 *     onConfirm = { viewModel.cancelBooking() },
 *     isDestructive = true
 * )
 * ```
 */
@Composable
fun AppDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    confirmText: String = "OK",
    cancelText: String? = null,
    onConfirm: () -> Unit = onDismiss,
    isDestructive: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
)
```

---

### 3.5 EmptyState

**Файл:** `organisms/empty/EmptyState.kt`

**API Design:**

```kotlin
/**
 * Empty state component for lists and screens.
 *
 * **Variants:**
 * - No data: Generic empty list
 * - No results: Search returned nothing
 * - Error: Error with retry
 * - Offline: No internet connection
 *
 * **Usage:**
 * ```kotlin
 * when {
 *     uiState.isLoading -> LoadingState()
 *     uiState.error != null -> EmptyState(
 *         type = EmptyStateType.Error,
 *         message = uiState.error.message,
 *         actionText = "Повторить",
 *         onAction = { viewModel.retry() }
 *     )
 *     uiState.providers.isEmpty() -> EmptyState(
 *         type = EmptyStateType.NoResults,
 *         message = "Мастера не найдены",
 *         actionText = "Сбросить фильтры",
 *         onAction = { viewModel.clearFilters() }
 *     )
 * }
 * ```
 */
@Composable
fun EmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
)

enum class EmptyStateType {
    NoData,      // Empty list
    NoResults,   // Search with no results
    Error,       // Error occurred
    Offline,     // No internet
}
```

---

### 3.6 CalendarPicker & TimeSlotPicker

**Файл:** `organisms/calendar/CalendarPicker.kt`

**API Design:**

```kotlin
/**
 * Calendar date picker with availability highlighting.
 *
 * **Features:**
 * - Month navigation
 * - Available/unavailable dates
 * - Selected date highlighting
 * - RTL support
 *
 * **Usage:**
 * ```kotlin
 * CalendarPicker(
 *     selectedDate = selectedDate,
 *     onDateSelect = { selectedDate = it },
 *     availableDates = availableDates,
 *     minDate = LocalDate.now(),
 *     maxDate = LocalDate.now().plusMonths(2),
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun CalendarPicker(
    selectedDate: LocalDate?,
    onDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    availableDates: Set<LocalDate>? = null,
    minDate: LocalDate = LocalDate.now(),
    maxDate: LocalDate = LocalDate.now().plusMonths(2),
    locale: Locale = Locale.getDefault(),
)

/**
 * Time slot selection grid.
 *
 * **Features:**
 * - Morning/Afternoon/Evening sections
 * - Available/unavailable slots
 * - Selected slot highlighting
 *
 * **Usage:**
 * ```kotlin
 * TimeSlotPicker(
 *     slots = timeSlots,
 *     selectedSlot = selectedSlot,
 *     onSlotSelect = { selectedSlot = it },
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun TimeSlotPicker(
    slots: List<TimeSlot>,
    selectedSlot: TimeSlot?,
    onSlotSelect: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier,
)

data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAvailable: Boolean,
)
```

---

## 📋 Phase 4: Feature-Specific Components

### 4.1 BookingListItem

**Файл:** `molecules/list/BookingListItem.kt`

**API Design:**

```kotlin
/**
 * Booking list item with swipe actions.
 *
 * **Swipe Actions:**
 * - Left: Cancel/Reject
 * - Right: Confirm/Reschedule
 *
 * **Display:**
 * - Client/Provider name
 * - Service name
 * - Date and time
 * - Status chip
 * - Price
 *
 * **Usage:**
 * ```kotlin
 * BookingListItem(
 *     booking = booking,
 *     onClick = { navController.navigate("booking/${booking.id}") },
 *     onConfirm = { viewModel.confirmBooking(booking.id) },
 *     onCancel = { viewModel.cancelBooking(booking.id) },
 *     onReschedule = { showRescheduleDialog = true },
 *     showActions = true, // For providers to confirm
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun BookingListItem(
    booking: BookingSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onReschedule: (() -> Unit)? = null,
    showSwipeActions: Boolean = true,
)

data class BookingSummary(
    val id: String,
    val clientName: String,
    val providerName: String,
    val serviceName: String,
    val dateTime: LocalDateTime,
    val status: BookingStatus,
    val price: Double,
    val currency: String,
)
```

---

### 4.2 ReviewListItem

**Файл:** `molecules/list/ReviewListItem.kt`

**API Design:**

```kotlin
/**
 * Review list item with rating and reply.
 *
 * **Display:**
 * - Client avatar and name
 * - Rating stars
 * - Review text
 * - Date
 * - Provider reply (if exists)
 *
 * **Usage:**
 * ```kotlin
 * ReviewListItem(
 *     review = review,
 *     showReply = true,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun ReviewListItem(
    review: ReviewSummary,
    modifier: Modifier = Modifier,
    showReply: Boolean = true,
)

data class ReviewSummary(
    val id: String,
    val clientId: String,
    val clientName: String,
    val clientAvatarUrl: String?,
    val rating: Int,
    val comment: String,
    val createdAt: LocalDateTime,
    val reply: ReplySummary?,
)

data class ReplySummary(
    val text: String,
    val createdAt: LocalDateTime,
)
```

---

## ⏱️ Estimation

| Phase | Components | Est. Time |
|-------|------------|-----------|
| **Phase 1: Foundation** | 5 components | 8h |
| **Phase 2: Molecules** | 4 components | 8h |
| **Phase 3: Organisms** | 6 components | 12h |
| **Phase 4: Feature** | 2 components | 4h |
| **Tests** | All components | 8h |
| **Total** | **17 components** | **~40h** |

---

## ✅ Acceptance Criteria

- [ ] All components use `core:theme` (AppColors, Spacing, Dimensions)
- [ ] All components are stateless (state hoisting pattern)
- [ ] All components have `modifier: Modifier` parameter
- [ ] All components have `enabled` state support
- [ ] All interactive components support accessibility (content descriptions)
- [ ] All components have @Preview functions for design review
- [ ] Unit tests for all components

---

## 🔗 Related Documentation

- [Design System](04_DESIGN_SYSTEM.md)
- [UX Guidelines](05_UX_GUIDELINES.md)
- [Implementation Status](../IMPLEMENTATION_STATUS.md)

---

**Document Owner**: Development Team
**Last Updated**: 2026-03-24
