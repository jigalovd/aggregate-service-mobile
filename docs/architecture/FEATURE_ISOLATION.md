# Feature Isolation Pattern

## Принцип

Каждая feature-модуль должен быть **полностью независимым** и не иметь прямых зависимостей от других feature-модулей.

```
❌ НЕПРАВИЛЬНО:
feature:booking → feature:catalog (прямая зависимость)

✅ ПРАВИЛЬНО:
feature:booking → core:navigation → (интерфейсы)
feature:catalog → core:navigation → (интерфейсы)
```

## Почему это важно

1. **Переиспользование** - фичу можно использовать в другом проекте
2. **Независимая разработка** - команды могут работать параллельно
3. **Тестирование** - фичи тестируются изолированно
4. **Сборка** - можно исключить фичу из сборки без breaking changes

## Паттерны межфункционального взаимодействия

### 1. ID-based Navigation (Рекомендуемый)

**Ситуация:** Booking нужен список услуг мастера из Catalog

**Решение:** Booking получает только ID, загружает данные самостоятельно

```kotlin
// core:navigation/BookingNavigator.kt
interface BookingNavigator {
    fun createSelectServiceScreen(
        providerId: String,      // Только ID
        providerName: String     // Для заголовка экрана
    ): Screen
}

// feature:booking/domain/model/BookingService.kt
data class BookingService(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val durationMinutes: Int,
)

// feature:booking/domain/repository/BookingRepository.kt
interface BookingRepository {
    suspend fun getProviderServices(providerId: String): Result<List<BookingService>>
}
```

**Обоснование дублирования API запроса:**

Хотя `/providers/{id}/services` вызывается и в Catalog, и в Booking - это **нормально**:
- Каждая фича владеет своими данными
- Можно кэшировать независимо
- Разные форматы ответа могут потребоваться
- Изоляция важнее микро-оптимизации HTTP запросов

### 2. Interface Segregation

**Ситуация:** Несколько фич должны отображать одинаковые данные

**Решение:** Контракт в core, реализация в feature

```kotlin
// core:navigation/contract/ProviderContract.kt
interface ProviderContract {
    val id: String
    val displayName: String
}

// core:navigation/ProviderDataProvider.kt
interface ProviderDataProvider {
    suspend fun getProvider(id: String): Result<ProviderContract>
}
```

### 3. Event Bus (для сложных случаев)

**Ситуация:** Фича должна реагировать на события из другой фичи

**Решение:** Shared Flow в core

```kotlin
// core:event/AppEvent.kt
sealed interface AppEvent {
    data class BookingCreated(val bookingId: String) : AppEvent
    data class UserLoggedIn(val userId: String) : AppEvent
}
```

### 4. State Provider Pattern (Рекомендуемый для Auth)

**Ситуация:** Несколько фич должны знать состояние авторизации

**Решение:** Interface в core:navigation, реализация в feature:auth

```kotlin
// core:navigation/AuthStateProvider.kt
interface AuthStateProvider {
    val isAuthenticatedFlow: StateFlow<Boolean>
    val isAuthenticated: Boolean
        get() = isAuthenticatedFlow.value
}

// feature:auth/AuthStateProviderImpl.kt
class AuthStateProviderImpl(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
) : AuthStateProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val isAuthenticatedFlow: StateFlow<Boolean> =
        observeAuthStateUseCase()
            .map { it.isAuthenticated }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )
}

// feature:catalog/ProviderDetailScreen.kt
@Composable
fun ProviderDetailScreen.Content() {
    // Используем koinInject для Compose-friendly DI
    val authProvider: AuthStateProvider = koinInject()
    val isAuthenticated by authProvider.isAuthenticatedFlow.collectAsState()

    // Теперь можем проверять auth без прямой зависимости от feature:auth
    if (isAuthenticated) { /* ... */ }
}
```

**Обоснование:**
- `feature:catalog` НЕ зависит от `feature:auth`
- Зависит только от `core:navigation` (interface)
- Реализация injected через Koin
- Compose-friendly с `koinInject()` вместо `KoinComponent`

## Чек-лист для Code Review

При добавлении зависимости между фичами проверить:

- [ ] Зависимость только от `core:*` модулей
- [ ] Нет `implementation(project(":feature:xxx"))` в build.gradle.kts
- [ ] Нет импортов `com.aggregateservice.feature.xxx` в коде
- [ ] Навигация через интерфейсы в `core:navigation`
- [ ] Данные передаются как ID, не как объекты

## Примеры нарушений и исправлений

### Нарушение: Прямой импорт модели

```kotlin
// ❌ НЕПРАВИЛЬНО - в feature:booking
import com.aggregateservice.feature.catalog.domain.model.Service

class SelectServiceScreenModel(
    private val getServicesUseCase: GetProviderServicesUseCase // из catalog!
)
```

### Исправление: Своя модель + свой UseCase

```kotlin
// ✅ ПРАВИЛЬНО - в feature:booking
import com.aggregateservice.feature.booking.domain.model.BookingService

class SelectServiceScreenModel(
    private val getBookingServicesUseCase: GetBookingServicesUseCase // свой!
)
```

## Когда можно нарушить правило

Единственное исключение - **monolith-first development**:
- На ранних этапах допустима временная зависимость
- Должна быть issue на рефакторинг
- Документирована в TECH_DEBT.md

---

См. также:
- [Feature-First Architecture](./FEATURE_FIRST.md)
- [Navigation Pattern](../mobile/NAVIGATION.md)
