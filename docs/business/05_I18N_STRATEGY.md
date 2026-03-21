# Стратегия Интернационализации (i18n)

## 1. Целевые Ринки и Языки

### 1.1. География и Языки

| Рынок | Страна | Язык | Код | RTL | Приоритет |
|-------|--------|------|-----|-----|-----------|
| **Россия** | РФ | Русский | `ru` | Нет | 🔴 High (MVP) |
| **Израиль** | IL | Иврит | `he` | **Да** | 🔴 High (MVP) |
| **США/Глобал** | US | Английский | `en` | Нет | 🟡 Medium |

### 1.2. Почему Эти Языки?

1. **Русский:** Основной рынок запуска (Россия, СНГ)
2. **Иврит:** Тестовый рынок для RTL (Израиль)
3. **Английский:** Глобальный масштабирование

---

## 2. Лингвистические Особенности

### 2.1. Иврит (RTL - Right-to-Left)

**Вызовы:**
- 🔄 Зеркальное отображение layout'ов
- 🔄 Направление скролла (horizontal scrolling)
- 🔄 Иконки и изображения с текстом
- 🔄 Нумерация (даты, деньги) - в Иврите также **слева направо**!

**Решения:**
```dart
// Flutter автоматическая поддержка RTL
MaterialApp(
    locale: Locale('he'),
    // Все widgets автоматически разворачиваются
)

// Исключения (иконки, логотипы):
Directionality(
    textDirection: TextDirection.ltr,  // Принудительно LTR
    child: Image.asset('logo.png'),
)
```

**Тестирование RTL:**
- [ ] Текст выровнен по правому краю
- [ ] Кнопки "Назад" (<) mirrored автоматически
- [ ] Поля формы ввода (placeholder'ы) правильно выровнены
- [ ] Списки (ListView) отображаются корректно

---

### 2.2. Форматы Данных

#### Даты и Время

| Язык | Формат (короткий) | Формат (длинный) | Пример |
|------|------------------|------------------|--------|
| Русский | `dd.MM.yyyy` | `d MMMM yyyy` | 14.09.2025 / 14 сентября 2025 |
| Иврит | `dd.MM.yyyy` | `d MMMM yyyy` | 14.09.2025 / 14 בספטמבר 2025 |
| Английский | `MM/dd/yyyy` | `MMM d, yyyy` | 09/14/2025 / Sep 14, 2025 |

**API формат:** Всегда **RFC 3339** (`2025-09-14T14:30:00Z`) - не зависит от языка.

#### Валюты

| Язык | Символ | Позиция | Разделители | Пример |
|------|--------|---------|-------------|--------|
| Русский | `₽` | После | Пробел (1 000) | 1 500 ₽ |
| Иврит | `₪` | После | Запятая (1,000) | 1,000 ₪ |
| Английский | `$` | До | Запятая (1,000) | $1,000.00 |

**Flutter реализация:**
```dart
import 'package:intl/intl.dart';

final currencyFormat = NumberFormat.currency(
    locale: 'he_IL',  // или 'ru_RU', 'en_US'
    symbol: '₪',
    decimalDigits: 0,
);
print(currencyFormat.format(1500));  // "1,500 ₪"
```

#### Числа

| Язык | Разделитель тысяч | Десятичный | Пример |
|------|-------------------|------------|--------|
| Русский | Пробел | Запятая | 1 234,56 |
| Иврит | Запятая | Точка | 1,234.56 |
| Английский | Запятая | Точка | 1,234.56 |

---

## 3. Стратегия Переводов

### 3.1. Кто Переводит?

**Этап 1 (MVP):** Команда проекта + профессиональный переводчик для Иврита
**Этап 2:** Crowdsourcing / Платформа (Crowdin, Gengo)
**Этап 3:** Комьюнити (review от пользователей)

### 3.2. Что Переводится?

**必須 (Must-Have):**
- [x] UI тексты (кнопки, меню, заголовки)
- [x] Категории услуг (Ногти, Волосы)
- [x] Сообщения об ошибках
- [x] Onboarding экраны

**Желательно (Nice-to-Have):**
- [ ] Описания услуг (клиент может создать на любом языке)
- [ ] Отзывы (показывать в оригинале с меткой языка)

**НЕ переводится:**
- Имена мастеров
- Названия бизнеса (бренды)
- Адреса (использовать Google Maps auto-translation)

---

## 4. UX/UI Адаптация

### 4.1. Длина Текста (Text Expansion)

**Проблема:** Перевод может быть длиннее оригинала.

| Пример | EN | HE | RU |
|--------|-----|----|-----|
| "Book" | 4 chars | 6 chars (הזמן) | 14 chars (Записаться) |

**Решение:**
```dart
// ❌ Жестко заданная ширина
Container(width: 100, child: Text("Book"))

// ✅ Адаптивная ширина
IntrinsicWidth(child: Text("Записаться"))
```

### 4.2. Перенос Строк (Line Wrapping)

**Иврит и Арабский:** Слова короче, но нужноRTL.

**Flutter решение:**
```dart
Text(
    AppLocalizations.of(context)!.longText,
    softWrap: true,  // Автоперенос
    overflow: TextOverflow.ellipsis,  // Обрезка если нужно
    maxLines: 2,
)
```

---

## 5. Выбор Языка Пользователем

### 5.1. Автоопределение

**Метод 1: Геолокация (Backend)**
```python
# По IP страны
country = get_country_by_ip(request.ip)
if country == 'IL':
    suggested_language = 'he'
elif country in ['RU', 'UA', 'BY']:
    suggested_language = 'ru'
else:
    suggested_language = 'en'
```

**Метод 2: Системные настройки (Frontend)**
```dart
// Чтение из устройства
final systemLocale = PlatformDispatcher.instance.locale;
// Например: 'he_IL' → Иврит
```

### 5.2. Forced Selection (Onboarding)

**Флоу:**
1. Установка приложения
2. Первый экран: "Выберите язык / בחר שפה / Select Language"
3. Сохранение в `SharedPreferences`
4. В дальнейшем можно изменить в Settings

---

## 6. Управление Переводами

### 6.1. Backend (API Messages)

**Формат файлов:** `.po` (GNU gettext)

```
backend/app/locales/ru/LC_MESSAGES/messages.po
```

**Пример:**
```po
msgid "Booking created successfully"
msgstr "Бронирование создано успешно"

msgid "Invalid date format"
msgstr "Неверный формат даты"
```

**Генерация `.mo` (скомпилированные):**
```bash
msgfmt messages.po -o messages.mo
```

---

### 6.2. Frontend (Flutter UI)

**Формат файлов:** `.arb` (Application Resource Bundle)

```
lib/l10n/app_ru.arb
lib/l10n/app_he.arb
lib/l10n/app_en.arb
```

**Пример (app_he.arb):**
```json
{
    "@@locale": "he",
    "homeScreenTitle": "בית",
    "bookingButton": "הזמן תור",
    "searchPlaceholder": "חיפוש...",
    "reviewsCount": "{count} ביקורות",

    "@reviewsCount": {
        "placeholders": {
            "count": {
                "type": "int"
            }
        },
        "plural": {
            "one": "ביקורת אחת",
            "two": "{count} ביקורות",
            "many": "{count} ביקורות",
            "other": "{count} ביקורות"
        }
    }
}
```

**Генерация кода (l10n.yaml):**
```yaml
arb-dir: lib/l10n
template-arb-file: app_en.arb
output-localization-file: app_localizations.dart
```

```bash
flutter gen-l10n
```

---

## 7. Best Practices

### 7.1. Всегда Использовать Ключи (Кони)!= Хардкод

```dart
// ❌ Плохо
Text("Записаться")

// ✅ Хорошо
Text(AppLocalizations.of(context)!.bookingButton)
```

### 7.2. Параметризация

```json
// app_ru.arb
{
    "bookingConfirmedFor": "Бронирование подтверждено для {name}",
    "@bookingConfirmedFor": {
        "placeholders": {
            "name": {
                "type": "String"
            }
        }
    }
}

// Использование
Text(AppLocalizations.of(context)!.bookingConfirmedFor("Анна"))
```

### 7.3. Pluralization (Множественное Число)

```json
// Русский (сложная морфология)
{
    "reviewsCount": "{count, plural, one{# отзыв} few{# отзыва} many{# отзывов} other{# отзывов}}"
}

// Иврит (двойственное число)
{
    "reviewsCount": "{count, plural, one{ביקורת אחת} two{# ביקורות} many{# ביקורות} other{# ביקורות}}"
}

// Использование
Text(AppLocalizations.of(context)!.reviewsCount(5))  // "5 отзывов"
```

---

## 8. Тестирование Локализации

### 8.1. Unit Тесты (Backend)

```python
def test_error_message_localization():
    response = client.get("/api/v1/bookings/invalid", headers={"Accept-Language": "he"})
    assert response.status_code == 404
    assert "לא נמצא" in response.json()["message"]  # Иврит
```

### 8.2. Widget Тесты (Frontend)

```dart
testWidgets('Home screen shows Hebrew title', (WidgetTester tester) async {
    await tester.pumpWidget(MaterialApp(
        locale: Locale('he'),
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        home: HomeScreen(),
    ));

    expect(find.text('בית'), findsOneWidget);
});
```

### 8.3. Ручное Тестирование

**Чеклист для каждого языка:**
- [ ] Все UI элементы отображаются на нужном языке
- [ ] Нет "falling back" на английский
- [ ] Даты форматируются корректно
- [ ] Валюты показываются правильно
- [ ] **Для Иврита:** Layout зеркальный, иконки не перевёрнуты
- [ ] Текст не обрезается (overflow)
- [ ] Переносы строк работают

---

## 9. Мониторинг и Аналитика

### 9.1. Метрики

| Метрика | Описание |
|---------|----------|
| **CR по языкам** | Conversion Rate по языкам (RU vs HE vs EN) |
| **Ошибка "missing translation"** | Количество fallbacks на дефолтный язык |
| **RTL bounce rate** | Процент отказов на Иврите (indicative of UX issues) |

### 9.2. Логирование

```python
# Логировать случаи, когда перевод не найден
logger.warning(f"Missing translation for key: '{key}' in language: '{lang}'")
```

---

## 10. Rollout План

### Неделя 1-2: Foundation
- [ ] Настроить структуру файлов (.po, .arb)
- [ ] Создать ключи для всех UI элементов
- [ ] Базовый перевод RU/EN

### Неделя 3-4: Hebrew
- [ ] Найти переводчика (native speaker)
- [ ] Перевести все ключи на Иврит
- [ ] RTL тестирование

### Неделя 5: Integration
- [ ] Connect backend i18n
- [ ] Connect frontend i18n
- [ ] End-to-end тесты

### Неделя 6: Launch
- [ ] Onboarding экран выбора языка
- [ ] А/Б тестирование (RU vs HE markets)
- [ ] Мониторинг метрик

---

## 11. Будущие Расширения

**Следующие языки (потенциал):**
- Французский (FR) - Франция, Канада
- Немецкий (DE) - Германия
- Португальский (PT) - Бразилия
- Арабский (AR) - ОАЭ, Саудовская Аравия (еще один RTL)

**Технологии:**
- Google Translate API для авто-перевода пользовательского контента
- Crowdin integration для комьюнити-переводов
